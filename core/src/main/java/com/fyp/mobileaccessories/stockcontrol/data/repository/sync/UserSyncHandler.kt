package com.fyp.mobileaccessories.stockcontrol.data.repository.sync

import android.content.Context
import android.util.Log
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.UserDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity
import com.fyp.mobileaccessories.stockcontrol.util.SessionManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UserSyncHandler(
    private val userDao: UserDao,
    private val scope: CoroutineScope,
    private val context: Context,
    private val sessionManager: SessionManager
) {
    private val TAG = "UserSync"

    fun handleSync(snapshot: DataSnapshot) {
        scope.launch {
            val remoteKeys = mutableSetOf<String>()
            val remoteIds = mutableSetOf<Int>()

            for (child in snapshot.children) {
                try {
                    val id = child.child("id").getValue(Int::class.java) ?: child.key?.toIntOrNull() ?: 0
                    val firstName = child.child("firstName").getValue(String::class.java) ?: ""
                    val lastName = child.child("lastName").getValue(String::class.java) ?: ""
                    val gender = child.child("gender").getValue(String::class.java) ?: ""
                    val address = child.child("address").getValue(String::class.java) ?: ""
                    val contactNo = child.child("contactNo").getValue(String::class.java) ?: ""
                    val email = child.child("email").getValue(String::class.java) ?: ""
                    val passwordHash = child.child("passwordHash").getValue(String::class.java) ?: ""
                    val role = child.child("role").getValue(String::class.java) ?: "USER"
                    val domain = child.child("domain").getValue(String::class.java)
                    val isActive = child.child("active").getValue(Boolean::class.java)
                        ?: child.child("isActive").getValue(Boolean::class.java) ?: true
                    val dateOfRegistration = child.child("dateOfRegistration").getValue(Long::class.java)
                        ?: child.child("dateRegistered").getValue(Long::class.java) ?: System.currentTimeMillis()
                    val uid = child.child("uid").getValue(String::class.java) ?: ""
                    val fcmToken = child.child("fcmToken").getValue(String::class.java) ?: ""

                    if (uid.isNotBlank()) remoteKeys.add(uid)
                    if (id > 0) remoteIds.add(id)
                    remoteKeys.add(child.key ?: "")

                    val existingUser = userDao.getUserById(id) ?: userDao.getUserByEmail(email)
                    val finalPasswordHash = if (passwordHash == "PROTECTED" || passwordHash.isBlank()) {
                        existingUser?.passwordHash ?: passwordHash
                    } else {
                        passwordHash
                    }

                    val user = UserEntity(
                        id = if (id > 0) id else (existingUser?.id ?: 0),
                        firstName = firstName, lastName = lastName, gender = gender,
                        address = address, contactNo = contactNo, email = email,
                        passwordHash = finalPasswordHash, role = role, domain = domain,
                        isActive = isActive, dateOfRegistration = dateOfRegistration, uid = uid,
                        fcmToken = fcmToken
                    )
                    userDao.upsertUser(user)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user: ${e.message}")
                }
            }

            // Sync deletions: remove local users no longer present in Realtime Database
            try {
                val localUsers = userDao.getAllUsersList()
                for (localUser in localUsers) {
                    val isPresent = (localUser.uid.isNotBlank() && remoteKeys.contains(localUser.uid)) ||
                                    remoteKeys.contains(localUser.id.toString()) ||
                                    remoteIds.contains(localUser.id)
                    if (!isPresent) {
                        val sharedPrefs = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)
                        val activeUserId = sharedPrefs.getInt("user_id", -1)
                        if (activeUserId == localUser.id) {
                            scope.launch {
                                sessionManager.clearSession()
                            }
                            Log.d(TAG, "Active user ${localUser.email} was deleted remotely. Cleared session via SessionManager.")
                        }
                        userDao.deleteUser(localUser)
                        Log.d(TAG, "Deleted local user ${localUser.email} because it was removed from Realtime Database.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing user deletions: ${e.message}")
            }
        }
    }

    fun upload(ref: DatabaseReference, user: UserEntity) {
        val map = mapOf(
            "id" to user.id,
            "uid" to user.uid,
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "gender" to user.gender,
            "address" to user.address,
            "contactNo" to user.contactNo,
            "email" to user.email,
            "passwordHash" to user.passwordHash,
            "role" to user.role,
            "domain" to (user.domain as Any?),
            "dateOfRegistration" to user.dateOfRegistration,
            "isActive" to user.isActive,
            "fcmToken" to user.fcmToken
        )
        // Standardize RTDB user keys to always use numeric ID (e.g. users/9)
        // so updates always overwrite the exact same node instead of creating new keys.
        val key = if (user.id > 0) user.id.toString() else (user.uid.ifBlank { java.util.UUID.randomUUID().toString() })
        ref.child("users").child(key).setValue(map)
    }
}
