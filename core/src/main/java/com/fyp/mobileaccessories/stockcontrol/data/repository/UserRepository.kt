package com.fyp.mobileaccessories.stockcontrol.data.repository

import android.content.Context
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.UserDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity
import com.fyp.mobileaccessories.stockcontrol.util.ConnectionDetector
import com.fyp.mobileaccessories.stockcontrol.util.Constant
import com.fyp.mobileaccessories.stockcontrol.util.PasswordHasher
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val remoteSyncManager: RemoteSyncManager,
    @ApplicationContext private val context: Context
) {
    private val firebaseAuth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private fun ensureOnline(actionName: String = "Action") {
        if (!ConnectionDetector.isConnected(context)) {
            throw Exception("Internet connection required to $actionName.")
        }
    }

    suspend fun login(email: String, plainPassword: String): UserEntity? {
        ensureOnline("log in to account")
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, plainPassword).await()
        val uid = authResult.user?.uid ?: ""

        val hashed = PasswordHasher.hash(plainPassword)
        var user = userDao.login(email, hashed)

        if (user == null) {
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                user = existingUser.copy(passwordHash = hashed, uid = uid)
                userDao.updateUser(user)
            } else {
                // Fetch real profile created by Admin or stored in Firebase Realtime DB
                val cloudUser = fetchUserFromFirebase(email, uid)
                if (cloudUser != null) {
                    val userToInsert = cloudUser.copy(id = 0, passwordHash = hashed, uid = uid)
                    val newId = userDao.insertUser(userToInsert)
                    user = userToInsert.copy(id = newId.toInt())
                } else {
                    user = UserEntity(
                        id = 0,
                        uid = uid,
                        firstName = email.substringBefore("@").replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        },
                        lastName = "User",
                        gender = "Male",
                        address = "Registered Address",
                        contactNo = "03000000000",
                        email = email,
                        passwordHash = hashed,
                        role = when {
                            email.contains("admin", ignoreCase = true) -> "ADMIN"
                            email.contains("mod", ignoreCase = true) -> "MODERATOR"
                            else -> "USER"
                        }
                    )
                    val newId = userDao.insertUser(user)
                    user = user.copy(id = newId.toInt())
                    remoteSyncManager.uploadUser(user)
                }
            }
        }
        return user
    }

    private suspend fun fetchUserFromFirebase(email: String, uid: String): UserEntity? {
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
                val childUid = child.child("uid").getValue(String::class.java) ?: ""
                if (childEmail.equals(
                        email,
                        ignoreCase = true
                    ) || (uid.isNotBlank() && childUid == uid)
                ) {
                    val firstName = child.child("firstName").getValue(String::class.java) ?: ""
                    val lastName = child.child("lastName").getValue(String::class.java) ?: ""
                    val gender = child.child("gender").getValue(String::class.java) ?: "Male"
                    val address = child.child("address").getValue(String::class.java) ?: ""
                    val contactNo = child.child("contactNo").getValue(String::class.java) ?: ""
                    val fcmToken = child.child("fcmToken").getValue(String::class.java) ?: ""
                    val role = child.child("role").getValue(String::class.java) ?: "USER"
                    val domain = child.child("domain").getValue(String::class.java)
                    val dateOfRegistration =
                        child.child("dateOfRegistration").getValue(Long::class.java)
                            ?: System.currentTimeMillis()
                    return UserEntity(
                        id = 0,
                        uid = if (childUid.isNotBlank()) childUid else uid,
                        firstName = firstName,
                        lastName = lastName,
                        gender = gender,
                        address = address,
                        contactNo = contactNo,
                        email = email,
                        passwordHash = "PROTECTED",
                        role = role,
                        domain = domain,
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
        return userDao.login(email, passwordHash)
    }

    suspend fun register(user: UserEntity, plainPassword: String = ""): Result<Long> {
        if (!ConnectionDetector.isConnected(context)) {
            return Result.failure(Exception("Internet connection required to register an account."))
        }
        return try {
            val passToUse = if (plainPassword.isNotBlank()) plainPassword else "Default123"
            var firebaseUid = user.uid

            if (firebaseUid.isBlank()) {
                try {
                    val authResult =
                        firebaseAuth.createUserWithEmailAndPassword(user.email, passToUse).await()
                    firebaseUid = authResult.user?.uid ?: ""
                } catch (e: Exception) {
                    try {
                        val authResult =
                            firebaseAuth.signInWithEmailAndPassword(user.email, passToUse).await()
                        firebaseUid = authResult.user?.uid ?: ""
                    } catch (_: Exception) {
                    }
                }
            }

            val userWithUid = user.copy(uid = firebaseUid)
            val id = userDao.insertUser(userWithUid)
            val registeredUser = userWithUid.copy(id = id.toInt())
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
        userDao.deleteUser(user)
        remoteSyncManager.deleteUser(user.id)
    }

    suspend fun countAdmins() = userDao.countUsersByRole("ADMIN")
    suspend fun countModerators() = userDao.countUsersByRole("MODERATOR")
    suspend fun countUsers() = userDao.countUsersByRole("USER")
}
