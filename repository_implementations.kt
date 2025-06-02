
// Data Layer - Repository Implementation
@Singleton
class SalesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SalesRepository {

    private val salesCollection = firestore.collection("sales")

    override suspend fun getSales(): Flow<List<Sale>> = callbackFlow {
        val listener = salesCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val sales = snapshot?.toObjects(Sale::class.java) ?: emptyList()
                trySend(sales)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun addSale(sale: Sale): Result<String> = try {
        val docRef = salesCollection.add(sale).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getSaleById(id: String): Sale? = try {
        salesCollection.document(id).get().await().toObject(Sale::class.java)
    } catch (e: Exception) {
        null
    }

    override suspend fun deleteSale(id: String): Result<Unit> = try {
        salesCollection.document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProductRepository {

    private val productsCollection = firestore.collection("products")

    override suspend fun getProducts(): Flow<List<Product>> = callbackFlow {
        val listener = productsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val products = snapshot?.toObjects(Product::class.java) ?: emptyList()
                trySend(products)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun addProduct(product: Product): Result<String> = try {
        val docRef = productsCollection.add(product).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateProduct(product: Product): Result<Unit> = try {
        productsCollection.document(product.id).set(product).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getProductById(id: String): Product? = try {
        productsCollection.document(id).get().await().toObject(Product::class.java)
    } catch (e: Exception) {
        null
    }

    override suspend fun updateStock(productId: String, newStock: Int): Result<Unit> = try {
        productsCollection.document(productId)
            .update("stock", newStock).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getExpiringProducts(daysAhead: Int): Flow<List<Product>> = callbackFlow {
        val currentTime = System.currentTimeMillis()
        val warningTime = currentTime + (daysAhead * 24 * 60 * 60 * 1000)

        val listener = productsCollection
            .whereLessThanOrEqualTo("expirationDate", warningTime)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val products = snapshot?.toObjects(Product::class.java) ?: emptyList()
                trySend(products.filter { it.expirationDate > currentTime })
            }

        awaitClose { listener.remove() }
    }
}
