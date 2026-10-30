package com.fyp.mobileaccessories.stockcontrol.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val uid: String = "",
    val firstName: String,
    val lastName: String,
    val gender: String,
    val address: String,
    val contactNo: String,
    val email: String,
    val passwordHash: String,
    val role: String,
    val domain: String? = null,
    val dateOfRegistration: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val fcmToken: String = ""
)
