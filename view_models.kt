
// Presentation Layer - ViewModels
@HiltViewModel
class SalesViewModel @Inject constructor(
    private val registerSaleUseCase: RegisterSaleUseCase,
    private val getSalesUseCase: GetSalesUseCase,
    private val calculateChangeUseCase: CalculateChangeUseCase,
    private val validateStockUseCase: ValidateStockUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    private val _showSuccessMessage = MutableSharedFlow<String>()
    val showSuccessMessage: SharedFlow<String> = _showSuccessMessage.asSharedFlow()

    private val _showErrorMessage = MutableSharedFlow<String>()
    val showErrorMessage: SharedFlow<String> = _showErrorMessage.asSharedFlow()

    init {
        loadSales()
    }

    private fun loadSales() {
        viewModelScope.launch {
            getSalesUseCase().collect { sales ->
                _uiState.update { it.copy(salesList = sales, isLoading = false) }
            }
        }
    }

    fun updateProductName(name: String) {
        _uiState.update { it.copy(currentSale = it.currentSale.copy(productName = name)) }
    }

    fun updateQuantity(quantity: String) {
        val parsedQuantity = quantity.toIntOrNull() ?: 0
        _uiState.update { 
            it.copy(
                currentSale = it.currentSale.copy(quantity = parsedQuantity),
                totalPrice = calculateTotal(parsedQuantity, it.currentSale.unitPrice)
            ) 
        }
    }

    fun updateUnitPrice(price: String) {
        val parsedPrice = price.toDoubleOrNull() ?: 0.0
        _uiState.update { 
            it.copy(
                currentSale = it.currentSale.copy(unitPrice = parsedPrice),
                totalPrice = calculateTotal(it.currentSale.quantity, parsedPrice)
            ) 
        }
    }

    fun updateReceivedAmount(amount: String) {
        val parsedAmount = amount.toDoubleOrNull() ?: 0.0
        _uiState.update { 
            it.copy(
                receivedAmount = parsedAmount,
                changeAmount = calculateChangeUseCase(it.totalPrice, parsedAmount)
            ) 
        }
    }

    private fun calculateTotal(quantity: Int, unitPrice: Double): Double {
        return quantity * unitPrice
    }

    fun registerSale() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val sale = _uiState.value.currentSale.copy(
                totalPrice = _uiState.value.totalPrice,
                colaboradorId = "user_123" // Em produção, vem da autenticação
            )

            // Validar estoque antes de registrar
            val hasStock = validateStockUseCase(sale.productName, sale.quantity)
            if (!hasStock) {
                _showErrorMessage.emit("Estoque insuficiente para ${sale.productName}")
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            registerSaleUseCase(sale)
                .onSuccess { saleId ->
                    _showSuccessMessage.emit("Venda registrada com sucesso!")
                    clearCurrentSale()
                }
                .onFailure { error ->
                    _showErrorMessage.emit(error.message ?: "Erro ao registrar venda")
                }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun clearCurrentSale() {
        _uiState.update { 
            it.copy(
                currentSale = Sale(),
                totalPrice = 0.0,
                receivedAmount = 0.0,
                changeAmount = 0.0
            ) 
        }
    }
}

@HiltViewModel
class StockViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val checkExpiringProductsUseCase: CheckExpiringProductsUseCase,
    private val alertRepository: AlertRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        checkExpiringProducts()
        loadAlerts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.getProducts().collect { products ->
                _uiState.update { 
                    it.copy(
                        products = products,
                        lowStockProducts = products.filter { product -> product.stock <= 5 }
                    ) 
                }
            }
        }
    }

    private fun checkExpiringProducts() {
        viewModelScope.launch {
            checkExpiringProductsUseCase().collect { expiringProducts ->
                _uiState.update { it.copy(expiringProducts = expiringProducts) }
            }
        }
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            alertRepository.getAlerts().collect { alerts ->
                _uiState.update { it.copy(alerts = alerts) }
            }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            productRepository.addProduct(product)
        }
    }

    fun updateStock(productId: String, newStock: Int) {
        viewModelScope.launch {
            productRepository.updateStock(productId, newStock)
        }
    }

    fun markAlertAsRead(alertId: String) {
        viewModelScope.launch {
            alertRepository.markAsRead(alertId)
        }
    }
}

// UI State classes
data class SalesUiState(
    val salesList: List<Sale> = emptyList(),
    val currentSale: Sale = Sale(),
    val totalPrice: Double = 0.0,
    val receivedAmount: Double = 0.0,
    val changeAmount: Double = 0.0,
    val isLoading: Boolean = false
)

data class StockUiState(
    val products: List<Product> = emptyList(),
    val lowStockProducts: List<Product> = emptyList(),
    val expiringProducts: List<Product> = emptyList(),
    val alerts: List<StockAlert> = emptyList(),
    val isLoading: Boolean = false
)
