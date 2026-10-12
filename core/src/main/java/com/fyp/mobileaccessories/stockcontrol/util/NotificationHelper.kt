package com.fyp.mobileaccessories.stockcontrol.util

import android.util.Log
import com.github.muhammadnoman11.easyfirebasenotifier.data.NotificationData
import com.github.muhammadnoman11.easyfirebasenotifier.data.NotificationType
import com.github.muhammadnoman11.easyfirebasenotifier.network.EasyNotificationSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationTopics {
    const val USER_NOTIFICATIONS = "user_notifications"
    const val ADMIN_NOTIFICATIONS = "admin_notifications"
    const val MODERATOR_NOTIFICATIONS = "moderator_notifications"
}

object NotificationHelper {
    private const val TAG = "NotificationHelper"
    private val sender by lazy { EasyNotificationSender() }
    private val scope = CoroutineScope(Dispatchers.IO)

    fun sendAnnouncementNotification(
        title: String,
        body: String,
        imageUrl: String? = null
    ) {
        scope.launch {
            try {
                sender.sendToTopic(
                    topic = NotificationTopics.USER_NOTIFICATIONS,
                    notificationData = NotificationData(
                        title = title,
                        body = body,
                        imageUrl = imageUrl ?: "",
                        type = NotificationType.NORMAL
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed sending announcement notification: ${e.message}")
            }
        }
    }

    fun sendNewOrderNotification(
        orderId: Int,
        totalAmount: Double,
        productName: String = "",
        imageUrl: String = ""
    ) {
        scope.launch {
            try {
                val title = "New Order Placed"
                val body = if (productName.isNotBlank()) {
                    "Order #$orderId: $productName — Rs. ${"%,.0f".format(totalAmount)}"
                } else {
                    "Order #$orderId has been placed for Rs. ${"%,.0f".format(totalAmount)}"
                }
                val data = NotificationData(
                    title = title,
                    body = body,
                    imageUrl = imageUrl,
                    type = NotificationType.NORMAL
                )
                sender.sendToTopic(NotificationTopics.ADMIN_NOTIFICATIONS, data)
                sender.sendToTopic(NotificationTopics.MODERATOR_NOTIFICATIONS, data)
            } catch (e: Exception) {
                Log.e(TAG, "Failed sending order notification: ${e.message}")
            }
        }
    }

    fun sendProductPendingNotification(
        productName: String,
        imageUrl: String = ""
    ) {
        scope.launch {
            try {
                val title = "New Product Pending Approval"
                val body = "$productName is awaiting moderator review"
                sender.sendToTopic(
                    topic = NotificationTopics.MODERATOR_NOTIFICATIONS,
                    notificationData = NotificationData(
                        title = title,
                        body = body,
                        imageUrl = imageUrl,
                        type = NotificationType.NORMAL
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed sending product pending notification: ${e.message}")
            }
        }
    }

    fun sendProductApprovalNotification(
        productName: String,
        isApproved: Boolean,
        imageUrl: String = ""
    ) {
        scope.launch {
            try {
                val title = if (isApproved) "Product Approved" else "Product Rejected"
                val body = "$productName has been ${if (isApproved) "approved for store listing" else "rejected by moderator"}."
                val data = NotificationData(
                    title = title,
                    body = body,
                    imageUrl = imageUrl,
                    type = NotificationType.NORMAL
                )
                sender.sendToTopic(NotificationTopics.ADMIN_NOTIFICATIONS, data)
                sender.sendToTopic(NotificationTopics.MODERATOR_NOTIFICATIONS, data)
            } catch (e: Exception) {
                Log.e(TAG, "Failed sending product approval notification: ${e.message}")
            }
        }
    }

    fun sendNotificationToUserDevice(
        deviceToken: String,
        title: String,
        body: String,
        imageUrl: String = ""
    ) {
        if (deviceToken.isBlank()) {
            Log.w(TAG, "Skipped device notification; FCM token is blank")
            return
        }
        scope.launch {
            try {
                sender.sendToDevice(
                    deviceToken = deviceToken,
                    notificationData = NotificationData(
                        title = title,
                        body = body,
                        imageUrl = imageUrl,
                        type = NotificationType.NORMAL
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed sending notification to device: ${e.message}")
            }
        }
    }

    fun sendWelcomeNotification(deviceToken: String) {
        if (deviceToken.isBlank()) {
            Log.w(TAG, "Skipped welcome notification; FCM token is blank")
            return
        }
        scope.launch {
            try {
                sender.sendToDevice(
                    deviceToken = deviceToken,
                    notificationData = NotificationData(
                        title = "Welcome to Mobile Accessories Store! 🎉",
                        body = "Thank you for joining us. Browse our latest collection of chargers, earphones, and cases!",
                        imageUrl = "",
                        type = NotificationType.NORMAL
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed sending welcome notification: ${e.message}")
            }
        }
    }
}
