
// Domain Layer - Entidades
data class Sale(
    val id: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val colaboradorId: String = "",
    val discount: Double = 0.0,
    val tax: Double = 0.0
) {
    fun calculateTotalWithTax(): Double {
        return totalPrice + (totalPrice * tax / 100)
    }

    fun isValid(): Boolean {
        return productName.isNotEmpty() && 
               quantity > 0 && 
               unitPrice > 0.0 && 
               colaboradorId.isNotEmpty()
    }
}

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val expirationDate: Long = 0L,
    val category: String = "",
    val description: String = ""
) {
    fun isExpiringSoon(daysAhead: Int = 7): Boolean {
        val currentTime = System.currentTimeMillis()
        val warningTime = currentTime + (daysAhead * 24 * 60 * 60 * 1000)
        return expirationDate <= warningTime
    }

    fun isInStock(): Boolean = stock > 0
}

data class StockAlert(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val alertType: AlertType = AlertType.LOW_STOCK,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

enum class AlertType {
    LOW_STOCK,
    EXPIRING_SOON,
    EXPIRED,
    OUT_OF_STOCK
}
