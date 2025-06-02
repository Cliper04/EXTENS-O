
// Unit Tests
@RunWith(MockitoJUnitRunner::class)
class RegisterSaleUseCaseTest {

    @Mock
    private lateinit var salesRepository: SalesRepository

    @Mock
    private lateinit var productRepository: ProductRepository

    private lateinit var registerSaleUseCase: RegisterSaleUseCase

    @Before
    fun setup() {
        registerSaleUseCase = RegisterSaleUseCase(salesRepository, productRepository)
    }

    @Test
    fun `register sale with valid data should succeed`() = runTest {
        // Given
        val sale = Sale(
            id = "1",
            productName = "product1",
            quantity = 2,
            unitPrice = 10.0,
            colaboradorId = "user123"
        )
        val product = Product(
            id = "product1",
            name = "Test Product",
            stock = 10,
            price = 10.0
        )

        whenever(productRepository.getProductById("product1")).thenReturn(product)
        whenever(salesRepository.addSale(any())).thenReturn(Result.success("sale123"))
        whenever(productRepository.updateStock("product1", 8)).thenReturn(Result.success(Unit))

        // When
        val result = registerSaleUseCase(sale)

        // Then
        assertTrue(result.isSuccess)
        verify(salesRepository).addSale(any())
        verify(productRepository).updateStock("product1", 8)
    }

    @Test
    fun `register sale with insufficient stock should fail`() = runTest {
        // Given
        val sale = Sale(
            id = "1",
            productName = "product1",
            quantity = 15,
            unitPrice = 10.0,
            colaboradorId = "user123"
        )
        val product = Product(
            id = "product1",
            name = "Test Product",
            stock = 10,
            price = 10.0
        )

        whenever(productRepository.getProductById("product1")).thenReturn(product)

        // When
        val result = registerSaleUseCase(sale)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Estoque insuficiente", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register sale with invalid data should fail`() = runTest {
        // Given
        val sale = Sale(
            id = "1",
            productName = "",
            quantity = 0,
            unitPrice = 0.0,
            colaboradorId = ""
        )

        // When
        val result = registerSaleUseCase(sale)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Dados de venda inválidos", result.exceptionOrNull()?.message)
    }
}

@RunWith(MockitoJUnitRunner::class)
class CalculateChangeUseCaseTest {

    private val calculateChangeUseCase = CalculateChangeUseCase()

    @Test
    fun `calculate change with sufficient payment should return correct change`() {
        // Given
        val total = 50.0
        val received = 70.0
        val expectedChange = 20.0

        // When
        val change = calculateChangeUseCase(total, received)

        // Then
        assertEquals(expectedChange, change, 0.01)
    }

    @Test
    fun `calculate change with exact payment should return zero`() {
        // Given
        val total = 50.0
        val received = 50.0
        val expectedChange = 0.0

        // When
        val change = calculateChangeUseCase(total, received)

        // Then
        assertEquals(expectedChange, change, 0.01)
    }

    @Test
    fun `calculate change with insufficient payment should return zero`() {
        // Given
        val total = 50.0
        val received = 30.0
        val expectedChange = 0.0

        // When
        val change = calculateChangeUseCase(total, received)

        // Then
        assertEquals(expectedChange, change, 0.01)
    }
}

@RunWith(MockitoJUnitRunner::class)
class SalesViewModelTest {

    @Mock
    private lateinit var registerSaleUseCase: RegisterSaleUseCase

    @Mock
    private lateinit var getSalesUseCase: GetSalesUseCase

    @Mock
    private lateinit var calculateChangeUseCase: CalculateChangeUseCase

    @Mock
    private lateinit var validateStockUseCase: ValidateStockUseCase

    private lateinit var viewModel: SalesViewModel

    @Before
    fun setup() {
        viewModel = SalesViewModel(
            registerSaleUseCase,
            getSalesUseCase,
            calculateChangeUseCase,
            validateStockUseCase
        )
    }

    @Test
    fun `update product name should update ui state`() {
        // Given
        val productName = "Test Product"

        // When
        viewModel.updateProductName(productName)

        // Then
        assertEquals(productName, viewModel.uiState.value.currentSale.productName)
    }

    @Test
    fun `update quantity should calculate total price`() {
        // Given
        viewModel.updateUnitPrice("10.0")

        // When
        viewModel.updateQuantity("5")

        // Then
        assertEquals(5, viewModel.uiState.value.currentSale.quantity)
        assertEquals(50.0, viewModel.uiState.value.totalPrice, 0.01)
    }
}
