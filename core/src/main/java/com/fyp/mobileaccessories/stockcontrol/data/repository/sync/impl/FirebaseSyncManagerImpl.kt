package com.fyp.mobileaccessories.stockcontrol.data.repository.sync.impl

import android.content.Context
import android.util.Log
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.*
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.*
import com.fyp.mobileaccessories.stockcontrol.data.repository.RemoteSyncManager
import com.fyp.mobileaccessories.stockcontrol.data.repository.sync.*
import com.fyp.mobileaccessories.stockcontrol.util.Constant
import com.fyp.mobileaccessories.stockcontrol.util.SessionManager
import com.google.firebase.database.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncManagerImpl @Inject constructor(
    private val userDao: UserDao,
    private val productDao: ProductDao,
    private val orderDao: OrderDao,
    private val transactionDao: TransactionDao,
    private val announcementDao: AnnouncementDao,
    private val courierDao: CourierDao,
    private val paymentCompanyDao: PaymentCompanyDao,
    private val categoryDao: CategoryDao,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : RemoteSyncManager {
    private val TAG = "FirebaseSync"
    private var dbRef: DatabaseReference? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val productSync = ProductSyncHandler(productDao, scope)
    private val orderSync = OrderSyncHandler(orderDao, scope)
    private val userSync = UserSyncHandler(userDao, scope, context, sessionManager)
    private val announcementSync = AnnouncementSyncHandler(announcementDao, scope)
    private val supportSync =
        SupportSyncHandler(transactionDao, courierDao, paymentCompanyDao, scope)
    private val categorySync = CategorySyncHandler(categoryDao, scope)

    init {
        val fallbackUrl = Constant.DATABASE_URL
        try {
            val database = try {
                FirebaseDatabase.getInstance()
            } catch (_: Exception) {
                FirebaseDatabase.getInstance(fallbackUrl)
            }
            dbRef = database.reference
            Log.d(TAG, "Firebase Realtime Database initialized successfully.")
        } catch (e: Exception) {
            try {
                val database = FirebaseDatabase.getInstance(fallbackUrl)
                dbRef = database.reference
                Log.d(TAG, "Firebase initialized via fallback URL.")
            } catch (e2: Exception) {
                Log.w(TAG, "Firebase Database not initialized: ${e2.message}.")
            }
        }
    }

    override fun startRealtimeSync() {
        val ref = dbRef ?: return

        ref.child("products").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productSync.handleSync(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Products sync cancelled: ${error.message}")
            }
        })

        ref.child("orders").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderSync.handleSync(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Orders sync cancelled: ${error.message}")
            }
        })

        ref.child("transactions").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                supportSync.handleTransactionSync(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Transactions sync cancelled: ${error.message}")
            }
        })

        ref.child("announcements").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                announcementSync.handleSync(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Announcements sync cancelled: ${error.message}")
            }
        })

        ref.child("couriers").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                supportSync.handleCourierSync(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Couriers sync cancelled: ${error.message}")
            }
        })

        ref.child("payment_companies").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                supportSync.handlePaymentCompanySync(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Payment companies sync cancelled: ${error.message}")
            }
        })

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userSync.handleSync(snapshot.child("users"))
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Users sync cancelled: ${error.message}")
            }
        })

        ref.child("categories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categorySync.handleSync(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Categories sync cancelled: ${error.message}")
            }
        })
    }

    override suspend fun uploadProduct(product: ProductEntity) {
        dbRef?.let { productSync.upload(it, product) }
    }

    override suspend fun uploadOrder(order: OrderEntity) {
        dbRef?.let { orderSync.upload(it, order) }
    }

    override suspend fun uploadTransaction(transaction: TransactionEntity) {
        dbRef?.let { supportSync.uploadTransaction(it, transaction) }
    }

    override suspend fun uploadAnnouncement(announcement: AnnouncementEntity) {
        dbRef?.let { announcementSync.upload(it, announcement) }
    }

    override suspend fun uploadUser(user: UserEntity) {
        dbRef?.let { userSync.upload(it, user) }
    }

    override suspend fun uploadCourier(courier: CourierEntity) {
        dbRef?.let { supportSync.uploadCourier(it, courier) }
    }

    override suspend fun uploadPaymentCompany(company: PaymentCompanyEntity) {
        dbRef?.let { supportSync.uploadPaymentCompany(it, company) }
    }

    override suspend fun deleteProduct(productId: Int) {
        dbRef?.child("products")?.child(productId.toString())?.removeValue()
    }

    override suspend fun deleteOrder(orderId: Int) {
        dbRef?.child("orders")?.child(orderId.toString())?.removeValue()
    }

    override suspend fun deleteAnnouncement(announcementId: Int) {
        dbRef?.child("announcements")?.child(announcementId.toString())?.removeValue()
    }

    override suspend fun deleteCourier(courierId: Int) {
        dbRef?.child("couriers")?.child(courierId.toString())?.removeValue()
    }

    override suspend fun deletePaymentCompany(companyId: Int) {
        dbRef?.child("payment_companies")?.child(companyId.toString())?.removeValue()
    }

    override suspend fun deleteUser(userId: Int, uid: String) {
        val ref = dbRef ?: return
        if (uid.isNotBlank()) {
            ref.child("users").child(uid).removeValue()
        }
        ref.child("users").child(userId.toString()).removeValue()
    }

    override suspend fun updateOrderStatus(orderId: Int, status: String) {
        val ref = dbRef ?: return
        ref.child("orders").child(orderId.toString()).child("status").setValue(status)
        if (status == "DELIVERED") {
            ref.child("orders").child(orderId.toString()).child("deliveryDate")
                .setValue(System.currentTimeMillis())
        }
    }

    override suspend fun updateProductApproval(productId: Int, isApproved: Boolean) {
        dbRef?.child("products")?.child(productId.toString())?.child("isApproved")
            ?.setValue(isApproved)
    }

    override suspend fun updateTransactionStatus(
        transactionId: Int,
        status: String,
        remarks: String
    ) {
        val ref = dbRef ?: return
        ref.child("transactions").child(transactionId.toString()).child("status").setValue(status)
        ref.child("transactions").child(transactionId.toString()).child("remarks").setValue(remarks)
    }

    override suspend fun uploadCategory(category: CategoryEntity) {
        dbRef?.let { categorySync.upload(it, category) }
    }

    override suspend fun deleteCategory(categoryId: Int) {
        dbRef?.child("categories")?.child(categoryId.toString())?.removeValue()
    }

    override suspend fun updateUserFcmToken(userId: Int, token: String) {
        val user = userDao.getUserById(userId)
        val ref = dbRef ?: return
        val key = if (user != null && user.uid.isNotBlank()) user.uid else userId.toString()
        ref.child("users").child(key).child("fcmToken").setValue(token).await()
    }

    override suspend fun fetchUserFcmToken(userId: Int): String {
        val user = userDao.getUserById(userId)
        val key = if (user != null && user.uid.isNotBlank()) user.uid else userId.toString()
        val snapshot = dbRef?.child("users")?.child(key)?.child("fcmToken")?.get()?.await()
        return snapshot?.getValue(String::class.java).orEmpty()
    }
}
