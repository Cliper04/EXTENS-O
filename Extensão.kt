// 1. Camada de Dados (Módulo :data)
// FirestoreRepository.kt
@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val salesCollection = firestore.collection("sales")
    private val inventoryCollection = firestore.collection("inventory")

    suspend fun registerSale(sale: Sale) = try {
        salesCollection.add(sale.toMap()).await()
        updateInventoryOnSale(sale.items)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun updateInventoryOnSale(items: List<SaleItem>) {
        items.forEach { item ->
            inventoryCollection.document(item.productId)
                .update("quantity", FieldValue.increment(-item.quantity))
                .await()
        }
    }

    fun getRealTimeInventory() = inventoryCollection
        .whereLessThanOrEqualTo("quantity", 5)
        .snapshots()
        .map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(InventoryItem::class.java)
            }
        }
}

// 2. Camada de Domínio (Módulo :domain)
// RegisterSaleUseCase.kt
class RegisterSaleUseCase @Inject constructor(
    private val repository: FirestoreRepository
) {
    suspend operator fun invoke(sale: Sale): Result<Unit> {
        validateSale(sale)
        return repository.registerSale(sale)
    }

    private fun validateSale(sale: Sale) {
        require(sale.items.isNotEmpty()) { "Sale must have at least one item" }
        require(sale.total > 0) { "Total must be positive" }
    }
}

// 3. Camada de Apresentação (Módulo :presentation)
// SalesScreen.kt
@Composable
fun SalesScreen(viewModel: SalesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    
    SalesScreenContent(
        state = state,
        onItemAdded = viewModel::addItem,
        onPaymentReceived = viewModel::completeSale
    )
}

@Composable
private fun SalesScreenContent(
    state: SalesState,
    onItemAdded: (Product) -> Unit,
    onPaymentReceived: (Double) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        ProductGrid(products = state.availableProducts, onItemSelected = onItemAdded)
        CurrentSaleItems(items = state.currentItems)
        PaymentSection(
            total = state.total,
            onPaymentReceived = onPaymentReceived
        )
    }
}

// 4. ViewModel com Validações de Negócio
// SalesViewModel.kt
@HiltViewModel
class SalesViewModel @Inject constructor(
    private val registerSale: RegisterSaleUseCase,
    private val inventoryObserver: InventoryObserver
) : ViewModel() {

    private val _state = mutableStateOf(SalesState())
    val state: State<SalesState> = _state

    fun addItem(product: Product) {
        _state.value = _state.value.copy(
            currentItems = _state.value.currentItems + SaleItem(
                productId = product.id,
                name = product.name,
                price = product.price,
                quantity = 1
            ),
            total = _state.value.total + product.price
        )
    }

    fun completeSale(amountReceived: Double) {
        viewModelScope.launch {
            val sale = Sale(
                items = _state.value.currentItems,
                total = _state.value.total,
                paymentReceived = amountReceived,
                change = calculateChange(amountReceived)
            )
            
            when (val result = registerSale(sale)) {
                is Result.Success -> showSuccess()
                is Result.Failure -> showError(result.exception)
            }
        }
    }

    private fun calculateChange(amountReceived: Double) = 
        amountReceived - _state.value.total
}

// 5. Módulo de Autenticação
// PhoneAuthActivity.kt
class PhoneAuthActivity : ComponentActivity() {
    private val auth = Firebase.auth
    
    fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }
                
                override fun onVerificationFailed(e: FirebaseException) {
                    // Handle error
                }
            })
            .build()
        
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Navigate to main flow
                }
            }
    }
}

// 6. Geração de Relatórios PDF
// PdfReportGenerator.kt
class PdfReportGenerator(context: Context) {
    fun generateSalesReport(sales: List<Sale>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        
        val canvas = page.canvas
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        
        var yPosition = 50f
        canvas.drawText("Relatório de Vendas", 50f, yPosition, paint)
        
        sales.forEach { sale ->
            yPosition += 20
            canvas.drawText("Data: ${sale.date}", 50f, yPosition, paint)
            // Add more details
        }
        
        document.finishPage(page)
        
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "sales_report_${System.currentTimeMillis()}.pdf"
        )
        
        document.writeTo(FileOutputStream(file))
        document.close()
        
        return file
    }
}

// 7. Testes Unitários
// SalesViewModelTest.kt
@ExperimentalCoroutinesApi
class SalesViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `when adding item, should update total correctly`() = runTest {
        val viewModel = SalesViewModel(...)
        val testProduct = Product(id = "1", name = "Test", price = 10.0)
        
        viewModel.addItem(testProduct)
        advanceUntilIdle()
        
        assertEquals(10.0, viewModel.state.value.total)
    }
}

// 8. Configuração do Firebase
// FirebaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        Firebase.firestore.apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        }
        return Firebase.firestore
    }
}
