package com.fyp.mobileaccessories.stockcontrol.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object FcmTokenHelper {
    suspend fun fetchCurrentToken(): String {
        return try {
            FirebaseMessaging.getInstance().token.await().orEmpty()
        } catch (e: Exception) {
            Log.e("FCM_TOKEN", "Error fetching FCM token: ${e.message}")
            ""
        }
    }
}
