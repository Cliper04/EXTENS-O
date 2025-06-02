
// Domain Layer - Use Cases
class RegisterSaleUseCase @Inject constructor(
    private val salesRepository: SalesRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(sale: Sale): Result<String> {
        if (!sale.isValid()) {
            return Result.failure(Exception("Dados de venda inválidos"))
        }

        // Verificar se há estoque suficiente
        val product = productRepository.getProductById(sale.productName)
        if (product == null || product.stock < sale.quantity) {
            return Result.failure(Exception("Estoque insuficiente"))
        }

        // Calcular impostos (ISS 5% + ICMS 18%)
        val taxRate = 23.0 // 5% ISS + 18% ICMS
        val saleWithTax = sale.copy(
            tax = taxRate,
            totalPrice = sale.quantity * sale.unitPrice
        )

        return try {
            // Registrar venda
            val saleResult = salesRepository.addSale(saleWithTax)

            if (saleResult.isSuccess) {
                // Atualizar estoque
                val newStock = product.stock - sale.quantity
                productRepository.updateStock(product.id, newStock)
            }

            saleResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetSalesUseCase @Inject constructor(
    private val salesRepository: SalesRepository
) {
    suspend operator fun invoke(): Flow<List<Sale>> {
        return salesRepository.getSales()
    }
}

class CalculateChangeUseCase {
    operator fun invoke(total: Double, received: Double): Double {
        return if (received >= total) received - total else 0.0
    }
}

class CheckExpiringProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val alertRepository: AlertRepository
) {
    suspend operator fun invoke(daysAhead: Int = 7): Flow<List<Product>> {
        return productRepository.getExpiringProducts(daysAhead)
            .onEach { expiringProducts ->
                // Criar alertas para produtos próximos ao vencimento
                expiringProducts.forEach { product ->
                    val alert = StockAlert(
                        id = "",
                        productId = product.id,
                        productName = product.name,
                        alertType = AlertType.EXPIRING_SOON,
                        message = "Produto ${product.name} vence em ${daysAhead} dias"
                    )
                    alertRepository.addAlert(alert)
                }
            }
    }
}

class ValidateStockUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productId: String, quantity: Int): Boolean {
        val product = productRepository.getProductById(productId)
        return product != null && product.stock >= quantity
    }
}
