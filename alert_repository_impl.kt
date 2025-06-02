
// Data Layer - Alert Repository Implementation
@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AlertRepository {

    private val alertsCollection = firestore.collection("alerts")

    override suspend fun getAlerts(): Flow<List<StockAlert>> = callbackFlow {
        val listener = alertsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val alerts = snapshot?.toObjects(StockAlert::class.java) ?: emptyList()
                trySend(alerts)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun addAlert(alert: StockAlert): Result<String> = try {
        val docRef = alertsCollection.add(alert).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun markAsRead(alertId: String): Result<Unit> = try {
        alertsCollection.document(alertId)
            .update("isRead", true).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteAlert(alertId: String): Result<Unit> = try {
        alertsCollection.document(alertId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
