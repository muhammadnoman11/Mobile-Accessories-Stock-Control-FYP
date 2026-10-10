package com.fyp.mobileaccessories.stockcontrol.service

import android.content.Context
import android.util.Log
import com.fyp.mobileaccessories.stockcontrol.MainActivity
import com.fyp.mobileaccessories.stockcontrol.R
import com.fyp.mobileaccessories.stockcontrol.data.repository.UserRepository
import com.fyp.mobileaccessories.stockcontrol.util.NotificationTopics
import com.github.muhammadnoman11.easyfirebasenotifier.receiver.EasyNotificationReceiver
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UserFcmEntryPoint {
    fun userRepository(): UserRepository
}

class UserFCMService : EasyNotificationReceiver() {
    private val tokenScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getActivityClass() = MainActivity::class.java
    override fun getChannelId() = "user_notifications_channel"
    override fun getChannelName() = "User Notifications"
    override fun getNotificationIcon() = R.drawable.user_icon

    override fun getSubscriptionTopics(): List<String> {
        return listOf(NotificationTopics.USER_NOTIFICATIONS)
    }

    override fun onTokenUpdated(token: String) {
        if (token.isBlank()) return
        val userId = getSharedPreferences("app_session", Context.MODE_PRIVATE).getInt("user_id", -1)
        if (userId <= 0) return
        val repository = EntryPointAccessors.fromApplication(
            applicationContext,
            UserFcmEntryPoint::class.java
        ).userRepository()
        tokenScope.launch {
            try {
                repository.updateFcmToken(userId, token)
            } catch (e: Exception) {
                Log.e("User_Fcm_Token", "Failed to persist FCM token: ${e.message}")
            }
        }
    }
}
