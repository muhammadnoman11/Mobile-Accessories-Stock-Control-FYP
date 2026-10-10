package com.fyp.mobileaccessories.stockcontrol.data.repository.sync.impl

import android.util.Log
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.*
import com.fyp.mobileaccessories.stockcontrol.data.repository.RemoteSyncManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockSyncManagerImpl @Inject constructor() : RemoteSyncManager {
    private val TAG = "MockSyncManager"

    override fun startRealtimeSync() {
        Log.d(TAG, "Mock remote sync started. System running in local SQLite-only mode.")
    }

    override suspend fun uploadProduct(product: ProductEntity) {
        Log.d(TAG, "Uploading product to cloud mock database: ${product.name}")
    }

    override suspend fun uploadOrder(order: OrderEntity) {
        Log.d(TAG, "Uploading order to cloud mock database. Product ID: ${order.productId}, Qty: ${order.quantity}")
    }

    override suspend fun uploadTransaction(transaction: TransactionEntity) {
        Log.d(TAG, "Uploading transaction to cloud mock database: Reference: ${transaction.referenceNo}")
    }

    override suspend fun uploadAnnouncement(announcement: AnnouncementEntity) {
        Log.d(TAG, "Uploading announcement to cloud mock database: ${announcement.title}")
    }

    override suspend fun uploadUser(user: UserEntity) {
        Log.d(TAG, "Uploading user profile to cloud mock database: ${user.email} (${user.role})")
    }

    override suspend fun uploadCourier(courier: CourierEntity) {
        Log.d(TAG, "Uploading courier to cloud mock database: ${courier.name}")
    }

    override suspend fun uploadPaymentCompany(company: PaymentCompanyEntity) {
        Log.d(TAG, "Uploading payment company to cloud mock database: ${company.name}")
    }

    override suspend fun uploadCategory(category: CategoryEntity) {
        Log.d(TAG, "Uploading category to cloud mock database: ${category.name}")
    }

    override suspend fun deleteProduct(productId: Int) {
        Log.d(TAG, "Deleting product #$productId in cloud mock database")
    }

    override suspend fun deleteOrder(orderId: Int) {
        Log.d(TAG, "Deleting order #$orderId in cloud mock database")
    }

    override suspend fun deleteAnnouncement(announcementId: Int) {
        Log.d(TAG, "Deleting announcement #$announcementId in cloud mock database")
    }

    override suspend fun deleteCourier(courierId: Int) {
        Log.d(TAG, "Deleting courier #$courierId in cloud mock database")
    }

    override suspend fun deletePaymentCompany(companyId: Int) {
        Log.d(TAG, "Deleting payment company #$companyId in cloud mock database")
    }

    override suspend fun deleteUser(userId: Int) {
        Log.d(TAG, "Deleting user #$userId in cloud mock database")
    }

    override suspend fun deleteCategory(categoryId: Int) {
        Log.d(TAG, "Deleting category #$categoryId in cloud mock database")
    }

    override suspend fun updateOrderStatus(orderId: Int, status: String) {
        Log.d(TAG, "Updating order status in cloud mock database: Order $orderId -> $status")
    }

    override suspend fun updateProductApproval(productId: Int, isApproved: Boolean) {
        Log.d(TAG, "Updating product approval status in cloud mock database: Product $productId -> isApproved=$isApproved")
    }

    override suspend fun updateTransactionStatus(transactionId: Int, status: String, remarks: String) {
        Log.d(TAG, "Updating transaction status in cloud mock database: Transaction $transactionId -> $status, Remarks: $remarks")
    }

    override suspend fun updateUserFcmToken(userId: Int, token: String) {
        Log.d(TAG, "Updating FCM token in cloud mock database for user $userId")
    }

    override suspend fun fetchUserFcmToken(userId: Int): String = ""
}
