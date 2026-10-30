package com.fyp.mobileaccessories.stockcontrol.data.repository

import com.fyp.mobileaccessories.stockcontrol.data.local.entity.*

interface RemoteSyncManager {
    // Starts real-time sync listeners between SQLite and Cloud DB
    fun startRealtimeSync()

    // Upload operations (called after local DB inserts/updates)
    suspend fun uploadProduct(product: ProductEntity)
    suspend fun uploadOrder(order: OrderEntity)
    suspend fun uploadTransaction(transaction: TransactionEntity)
    suspend fun uploadAnnouncement(announcement: AnnouncementEntity)
    suspend fun uploadUser(user: UserEntity)
    suspend fun uploadCourier(courier: CourierEntity)
    suspend fun uploadPaymentCompany(company: PaymentCompanyEntity)
    suspend fun uploadCategory(category: CategoryEntity)

    // Delete operations (called after local DB deletes)
    suspend fun deleteProduct(productId: Int)
    suspend fun deleteOrder(orderId: Int)
    suspend fun deleteAnnouncement(announcementId: Int)
    suspend fun deleteCourier(courierId: Int)
    suspend fun deletePaymentCompany(companyId: Int)
    suspend fun deleteUser(userId: Int, uid: String = "")
    suspend fun deleteCategory(categoryId: Int)

    // Status update operations
    suspend fun updateOrderStatus(orderId: Int, status: String)
    suspend fun updateProductApproval(productId: Int, isApproved: Boolean)
    suspend fun updateTransactionStatus(transactionId: Int, status: String, remarks: String)

    suspend fun updateUserFcmToken(userId: Int, token: String)
    suspend fun fetchUserFcmToken(userId: Int): String
}
