
// Dependency Injection - Hilt Modules
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return Firebase.firestore.apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindSalesRepository(
        salesRepositoryImpl: SalesRepositoryImpl
    ): SalesRepository

    @Binds
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    abstract fun bindAlertRepository(
        alertRepositoryImpl: AlertRepositoryImpl
    ): AlertRepository
}

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideRegisterSaleUseCase(
        salesRepository: SalesRepository,
        productRepository: ProductRepository
    ): RegisterSaleUseCase {
        return RegisterSaleUseCase(salesRepository, productRepository)
    }

    @Provides
    fun provideGetSalesUseCase(
        salesRepository: SalesRepository
    ): GetSalesUseCase {
        return GetSalesUseCase(salesRepository)
    }

    @Provides
    fun provideCalculateChangeUseCase(): CalculateChangeUseCase {
        return CalculateChangeUseCase()
    }

    @Provides
    fun provideCheckExpiringProductsUseCase(
        productRepository: ProductRepository,
        alertRepository: AlertRepository
    ): CheckExpiringProductsUseCase {
        return CheckExpiringProductsUseCase(productRepository, alertRepository)
    }

    @Provides
    fun provideValidateStockUseCase(
        productRepository: ProductRepository
    ): ValidateStockUseCase {
        return ValidateStockUseCase(productRepository)
    }
}
