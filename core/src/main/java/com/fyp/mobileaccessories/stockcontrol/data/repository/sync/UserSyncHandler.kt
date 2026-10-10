package com.fyp.mobileaccessories.stockcontrol.data.repository.sync

import android.util.Log
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.UserDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.UserEntity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UserSyncHandler(
    private val userDao: UserDao,
    private val scope: CoroutineScope
) {
    private val TAG = "UserSync"

    fun handleSync(snapshot: DataSnapshot) {
        scope.launch {
            for (child in snapshot.children) {
                try {
                    val id = child.child("id").getValue(Int::class.java) ?: continue
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

                    val existingUser = userDao.getUserById(id)
                    val finalPasswordHash = if (passwordHash == "PROTECTED") {
                        existingUser?.passwordHash ?: ""
                    } else {
                        passwordHash
                    }

                    val user = UserEntity(
                        id = id, firstName = firstName, lastName = lastName, gender = gender,
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
            "passwordHash" to "PROTECTED",
            "role" to user.role,
            "domain" to (user.domain as Any?),
            "dateOfRegistration" to user.dateOfRegistration,
            "isActive" to user.isActive,
            "fcmToken" to user.fcmToken
        )
        ref.child("users").child(user.id.toString()).setValue(map)
    }
}
