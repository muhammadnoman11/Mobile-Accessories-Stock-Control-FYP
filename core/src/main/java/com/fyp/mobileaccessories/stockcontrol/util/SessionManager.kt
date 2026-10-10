package com.fyp.mobileaccessories.stockcontrol.util

// ============================================================
// FILE: util/SessionManager.kt
// DataStore-based session — persists logged-in user across app restarts
// ============================================================


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


// Extension property to create DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context
) {
    // Preference keys for storing session data
    companion object {
        val KEY_USER_ID = intPreferencesKey("user_id")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
    }

    // Save session after successful login
    suspend fun saveSession(userId: Int, role: String, name: String, email: String) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_ROLE] = role
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
        }
        context.getSharedPreferences("app_session", Context.MODE_PRIVATE)
            .edit()
            .putInt("user_id", userId)
            .apply()
    }

    // Clear session on logout
    suspend fun clearSession() {
        dataStore.edit { it.clear() }
        context.getSharedPreferences("app_session", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    // Observe session as Flow — auto-updates UI when session changes
    val sessionFlow: Flow<SessionData?> = dataStore.data.map { prefs ->
        val userId = prefs[KEY_USER_ID]
        val role = prefs[KEY_USER_ROLE]
        if (userId != null && role != null) {
            SessionData(
                userId = userId,
                role = role,
                name = prefs[KEY_USER_NAME] ?: "",
                email = prefs[KEY_USER_EMAIL] ?: ""
            )
        } else null
    }
}

// Session data holder
data class SessionData(
    val userId: Int,
    val role: String,       // "ADMIN", "MODERATOR", "USER"
    val name: String,
    val email: String
)
