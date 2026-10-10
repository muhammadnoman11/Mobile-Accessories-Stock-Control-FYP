package com.fyp.mobileaccessories.stockcontrol.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.AnnouncementDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.CategoryDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.CourierDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.OrderDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.PaymentCompanyDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.ProductDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.StockMovementDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.TransactionDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.UserDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.AnnouncementEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CategoryEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CourierEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.OrderEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.PaymentCompanyEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.StockMovementEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.TransactionEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        CourierEntity::class,
        PaymentCompanyEntity::class,
        OrderEntity::class,
        TransactionEntity::class,
        AnnouncementEntity::class,
        StockMovementEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // All DAOs exposed as abstract functions
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun courierDao(): CourierDao
    abstract fun paymentCompanyDao(): PaymentCompanyDao
    abstract fun orderDao(): OrderDao
    abstract fun transactionDao(): TransactionDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun stockMovementDao(): StockMovementDao

    companion object {
        const val DATABASE_NAME = "stock_control_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
