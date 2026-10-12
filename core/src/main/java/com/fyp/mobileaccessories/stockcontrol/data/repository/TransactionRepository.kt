package com.fyp.mobileaccessories.stockcontrol.data.repository

import android.content.Context
import android.util.Log
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.OrderDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.ProductDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.TransactionDao
import com.fyp.mobileaccessories.stockcontrol.util.ConnectionDetector
import com.fyp.mobileaccessories.stockcontrol.util.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val orderDao: OrderDao,
    private val productDao: ProductDao,
    private val userRepository: UserRepository,
    private val remoteSyncManager: RemoteSyncManager,
    @ApplicationContext private val context: Context
) {
    private fun ensureOnline(actionName: String) {
        if (!ConnectionDetector.isConnected(context)) {
            throw Exception("Internet connection required to $actionName.")
        }
    }

    fun getAllTransactions() = transactionDao.getAllTransactions()
    fun getTransactionsByUser(userId: Int) = transactionDao.getTransactionsByUser(userId)
    fun filterTransactions(status: String = "", paymentMode: String = "") =
        transactionDao.filterTransactions(status, paymentMode)

    suspend fun updateStatus(transactionId: Int, status: String, remarks: String = "") {
        ensureOnline("update transaction status")
        transactionDao.updateTransactionStatus(transactionId, status, remarks)
        remoteSyncManager.updateTransactionStatus(transactionId, status, remarks)

        val transaction = transactionDao.getTransactionById(transactionId)
        val order = if (transaction != null) orderDao.getOrderById(transaction.orderId) else null

        if (order != null) {
            val product = productDao.getProductById(order.productId)
            val fcmToken = userRepository.getUserFcmToken(order.userId)
            if (fcmToken.isBlank()) {
                Log.w("FCM_TOKEN", "No FCM token for user ${order.userId}; transaction notification skipped")
            }

            if (status == "APPROVED") {
                orderDao.updateOrderStatus(order.id, "CONFIRMED")
                remoteSyncManager.updateOrderStatus(order.id, "CONFIRMED")

                val title = "Order Status Update"
                val body = if (product != null) {
                    "Your Order #${order.id} (${product.name}) is now CONFIRMED"
                } else {
                    "Your Order #${order.id} is now CONFIRMED"
                }
                NotificationHelper.sendNotificationToUserDevice(fcmToken, title, body, product?.imageUrl ?: "")
            } else if (status == "REJECTED") {
                orderDao.updateOrderStatus(order.id, "CANCELLED")
                remoteSyncManager.updateOrderStatus(order.id, "CANCELLED")

                if (product != null) {
                    val restoredStock = product.stockQuantity + order.quantity
                    productDao.updateStock(product.id, restoredStock)
                    remoteSyncManager.uploadProduct(product.copy(stockQuantity = restoredStock))
                }

                val title = "Order Status Update"
                val body = if (product != null) {
                    "Your Order #${order.id} (${product.name}) is now CANCELLED (Payment Rejected)"
                } else {
                    "Your Order #${order.id} is now CANCELLED"
                }
                NotificationHelper.sendNotificationToUserDevice(fcmToken, title, body, product?.imageUrl ?: "")
            }
        }
    }

    suspend fun getByOrderId(orderId: Int) = transactionDao.getTransactionByOrderId(orderId)
    suspend fun countPending() = transactionDao.countPendingTransactions()
}
