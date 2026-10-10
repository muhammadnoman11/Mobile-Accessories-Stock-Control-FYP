package com.fyp.mobileaccessories.stockcontrol.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.fyp.mobileaccessories.stockcontrol.data.local.AppDatabase
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.*
import com.fyp.mobileaccessories.stockcontrol.data.repository.RemoteSyncManager
import com.fyp.mobileaccessories.stockcontrol.data.repository.sync.impl.FirebaseSyncManagerImpl
import com.fyp.mobileaccessories.stockcontrol.util.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)          // Lives as long as the app
object AppModule {

    // ── Room Database ─────────────────────────────
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getDatabase(context)

    // ── DAOs ──────────────────────────────────────
    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()
    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()
    @Provides
    fun provideCourierDao(db: AppDatabase): CourierDao = db.courierDao()
    @Provides
    fun provideOrderDao(db: AppDatabase): OrderDao = db.orderDao()
    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    @Provides
    fun providePaymentCompanyDao(db: AppDatabase): PaymentCompanyDao = db.paymentCompanyDao()
    @Provides
    fun provideAnnouncementDao(db: AppDatabase): AnnouncementDao = db.announcementDao()
    @Provides
    fun provideStockMovementDao(db: AppDatabase): StockMovementDao = db.stockMovementDao()

    // ── DataStore ─────────────────────────────────
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    // ── Remote Sync Manager ────────────────────────
    @Provides
    @Singleton
    fun provideRemoteSyncManager(
        impl: FirebaseSyncManagerImpl
    ): RemoteSyncManager = impl
}
