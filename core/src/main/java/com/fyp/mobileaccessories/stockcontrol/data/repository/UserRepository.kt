package com.fyp.mobileaccessories.stockcontrol.data.repository

import android.content.Context
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.UserDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity
import com.fyp.mobileaccessories.stockcontrol.util.ConnectionDetector
import com.fyp.mobileaccessories.stockcontrol.util.Constant
import com.fyp.mobileaccessories.stockcontrol.util.PasswordHasher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val remoteSyncManager: RemoteSyncManager,
    @ApplicationContext private val context: Context
) {

    private fun ensureOnline(actionName: String = "Action") {
        if (!ConnectionDetector.isConnected(context)) {
            throw Exception("Internet connection required to $actionName.")
        }
    }

    suspend fun login(email: String, plainPassword: String): UserEntity? {
        val hashed = PasswordHasher.hash(plainPassword)

        // 1. Try logging in locally via Room cache (case-insensitive email, supporting both hashed and plain text passwords)
        var user = userDao.login(email, hashed, plainPassword)

        // 2. If not found locally, check Realtime Database if online
        if (user == null && ConnectionDetector.isConnected(context)) {
            val cloudUser = fetchUserFromFirebase(email)
            if (cloudUser != null) {
                val isPasswordValid = cloudUser.passwordHash == "PROTECTED" ||
                        cloudUser.passwordHash.isBlank() ||
                        cloudUser.passwordHash == hashed ||
                        cloudUser.passwordHash == plainPassword
                if (isPasswordValid) {
                    val userToInsert =
                        if (cloudUser.passwordHash == "PROTECTED" || cloudUser.passwordHash.isBlank()) {
                            cloudUser.copy(passwordHash = hashed)
                        } else {
                            cloudUser
                        }
                    val newId = userDao.insertUser(userToInsert)
                    user = userToInsert.copy(id = newId.toInt())
                }
            }
        }

        if (user == null) {
            throw Exception("Invalid email or password")
        }
        if (!user.isActive) {
            throw Exception("Account is deactivated")
        }
        return user
    }

    private suspend fun fetchUserFromFirebase(email: String): UserEntity? {
        return try {
            val fallbackUrl = Constant.DATABASE_URL
            val db = try {
                com.google.firebase.database.FirebaseDatabase.getInstance()
            } catch (_: Exception) {
                com.google.firebase.database.FirebaseDatabase.getInstance(fallbackUrl)
            }
            val snapshot = db.reference.child("users").get().await()
            for (child in snapshot.children) {
                val childEmail = child.child("email").getValue(String::class.java) ?: ""
                if (childEmail.equals(email, ignoreCase = true)) {
                    val id = child.child("id").getValue(Int::class.java) ?: 0
                    val uid = child.child("uid").getValue(String::class.java) ?: UUID.randomUUID()
                        .toString()
                    val firstName = child.child("firstName").getValue(String::class.java) ?: ""
                    val lastName = child.child("lastName").getValue(String::class.java) ?: ""
                    val gender = child.child("gender").getValue(String::class.java) ?: "Male"
                    val address = child.child("address").getValue(String::class.java) ?: ""
                    val contactNo = child.child("contactNo").getValue(String::class.java) ?: ""
                    val passwordHash =
                        child.child("passwordHash").getValue(String::class.java) ?: ""
                    val fcmToken = child.child("fcmToken").getValue(String::class.java) ?: ""
                    val role = child.child("role").getValue(String::class.java) ?: "USER"
                    val domain = child.child("domain").getValue(String::class.java)
                    val isActive = child.child("active").getValue(Boolean::class.java)
                        ?: child.child("isActive").getValue(Boolean::class.java) ?: true
                    val dateOfRegistration =
                        child.child("dateOfRegistration").getValue(Long::class.java)
                            ?: System.currentTimeMillis()

                    return UserEntity(
                        id = id,
                        uid = uid,
                        firstName = firstName,
                        lastName = lastName,
                        gender = gender,
                        address = address,
                        contactNo = contactNo,
                        email = email,
                        passwordHash = passwordHash,
                        role = role,
                        domain = domain,
                        isActive = isActive,
                        dateOfRegistration = dateOfRegistration,
                        fcmToken = fcmToken
                    )
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loginLocally(email: String, passwordHash: String): UserEntity? {
        return userDao.login(email, passwordHash, passwordHash)
    }

    suspend fun register(user: UserEntity, plainPassword: String = ""): Result<Long> {
        return try {
            val hashed =
                if (plainPassword.isNotBlank()) PasswordHasher.hash(plainPassword) else user.passwordHash
            val uid = if (user.uid.isBlank()) UUID.randomUUID().toString() else user.uid

            val existing = userDao.getUserByEmail(user.email)
            if (existing != null) {
                return Result.failure(Exception("Email already registered"))
            }

            val userToSave = user.copy(uid = uid, passwordHash = hashed)
            val id = userDao.insertUser(userToSave)
            val registeredUser = userToSave.copy(id = id.toInt())
            remoteSyncManager.uploadUser(registeredUser)

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    fun getUsersByRole(role: String): Flow<List<UserEntity>> = userDao.getUsersByRole(role)
    fun searchUsers(
        query: String,
        role: String = "",
        domain: String = "",
        fromDate: Long = 0L,
        toDate: Long = 0L
    ): Flow<List<UserEntity>> = userDao.searchUsers(query, role, domain, fromDate, toDate)

    suspend fun getUserById(id: Int): UserEntity? = userDao.getUserById(id)

    suspend fun updateFcmToken(userId: Int, token: String) {
        if (userId <= 0 || token.isBlank()) return
        val current = userDao.getUserById(userId) ?: return
        if (current.fcmToken == token) return
        userDao.updateFcmToken(userId, token)
        try {
            remoteSyncManager.updateUserFcmToken(userId, token)
        } catch (_: Exception) {
        }
    }

    suspend fun getUserFcmToken(userId: Int): String {
        val local = userDao.getUserById(userId)?.fcmToken.orEmpty()
        if (local.isNotBlank()) return local
        return try {
            remoteSyncManager.fetchUserFcmToken(userId)
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun updateUser(user: UserEntity) {
        ensureOnline("update profile/user")
        val existing = userDao.getUserById(user.id)
        val existingToken = existing?.fcmToken.orEmpty()
        val toSave = if (user.fcmToken.isBlank() && existingToken.isNotBlank()) {
            user.copy(fcmToken = existingToken)
        } else {
            user
        }
        userDao.updateUser(toSave)
        remoteSyncManager.uploadUser(toSave)
    }

    suspend fun deactivateUser(userId: Int) {
        ensureOnline("deactivate user")
        userDao.deactivateUser(userId)
        userDao.getUserById(userId)?.let { remoteSyncManager.uploadUser(it) }
    }

    suspend fun deleteUser(user: UserEntity) {
        ensureOnline("delete user")
        remoteSyncManager.deleteUser(user.id, user.uid)
        userDao.deleteUser(user)
    }

    suspend fun countAdmins() = userDao.countUsersByRole("ADMIN")
    suspend fun countModerators() = userDao.countUsersByRole("MODERATOR")
    suspend fun countUsers() = userDao.countUsersByRole("USER")
}
