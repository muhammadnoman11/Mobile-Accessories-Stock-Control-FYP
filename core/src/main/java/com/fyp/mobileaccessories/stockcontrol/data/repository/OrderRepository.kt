package com.fyp.mobileaccessories.stockcontrol.data.repository

import android.content.Context
import android.util.Log
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.OrderDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.ProductDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.TransactionDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.OrderEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.TransactionEntity
import com.fyp.mobileaccessories.stockcontrol.util.ConnectionDetector
import com.fyp.mobileaccessories.stockcontrol.util.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val orderDao: OrderDao,
    private val transactionDao: TransactionDao,
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

    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllOrders()
    fun getOrdersByUser(userId: Int): Flow<List<OrderEntity>> = orderDao.getOrdersByUser(userId)
    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>> =
        orderDao.getOrdersByStatus(status)

    fun filterOrders(status: String = "") = orderDao.filterOrders(status)

    suspend fun getOrderById(id: Int) = orderDao.getOrderById(id)

    suspend fun updateOrderStatus(orderId: Int, status: String) {
        ensureOnline("update order status")
        orderDao.updateOrderStatus(orderId, status)
        remoteSyncManager.updateOrderStatus(orderId, status)

        val order = orderDao.getOrderById(orderId)
        val product = if (order != null) productDao.getProductById(order.productId) else null

        if (order != null) {
            val title = "Order Status Update"
            val body = if (product != null) {
                "Your Order #$orderId (${product.name}) status is now $status"
            } else {
                "Your Order #$orderId status is now $status"
            }

            val imageUrl = product?.imageUrl ?: ""
            val fcmToken = userRepository.getUserFcmToken(order.userId)
            if (fcmToken.isBlank()) {
                Log.w("FCM_TOKEN", "No FCM token for user ${order.userId}; order status notification skipped")
            } else {
                Log.d("FCM_TOKEN", "Order status notify user ${order.userId}")
                NotificationHelper.sendNotificationToUserDevice(
                    deviceToken = fcmToken,
                    title = title,
                    body = body,
                    imageUrl = imageUrl
                )
            }
        }
    }

    suspend fun placeOrder(order: OrderEntity): Result<Long> {
        if (!ConnectionDetector.isConnected(context)) {
            return Result.failure(Exception("Internet connection required to place an order."))
        }
        return try {
            val product = productDao.getProductById(order.productId)
                ?: return Result.failure(Exception("Product not found"))
            if (product.stockQuantity < order.quantity) {
                return Result.failure(Exception("Insufficient stock. Available: ${product.stockQuantity}"))
            }

            val orderId = orderDao.insertOrder(order)
            val orderWithId = order.copy(id = orderId.toInt())
            remoteSyncManager.uploadOrder(orderWithId)

            val updatedStock = product.stockQuantity - order.quantity
            productDao.updateStock(order.productId, updatedStock)
            remoteSyncManager.uploadProduct(product.copy(stockQuantity = updatedStock))

            val transaction = TransactionEntity(
                orderId = orderId.toInt(),
                userId = order.userId,
                paymentCompanyId = order.paymentCompanyId,
                amount = order.totalAmount,
                paymentMode = order.paymentMode,
                status = "PENDING"
            )
            val transactionId = transactionDao.insertTransaction(transaction)
            remoteSyncManager.uploadTransaction(transaction.copy(id = transactionId.toInt()))

            NotificationHelper.sendNewOrderNotification(
                orderId = orderId.toInt(),
                totalAmount = order.totalAmount,
                productName = product.name,
                imageUrl = product.imageUrl
            )

            Result.success(orderId)
        } catch (e: Exception) {
            android.util.Log.e("OrderRepo", "Place order failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteOrder(order: OrderEntity) {
        ensureOnline("delete order")
        orderDao.deleteOrder(order)
        remoteSyncManager.deleteOrder(order.id)
    }

    suspend fun countByStatus(status: String) = orderDao.countOrdersByStatus(status)
    suspend fun getTotalRevenue() = orderDao.getTotalRevenue()
}
