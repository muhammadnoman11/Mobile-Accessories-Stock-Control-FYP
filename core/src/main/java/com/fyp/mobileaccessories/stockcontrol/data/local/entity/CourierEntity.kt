package com.fyp.mobileaccessories.stockcontrol.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "couriers")
data class CourierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val contactNo: String,
    val email: String = "",
    val address: String = "",
    val deliveryCharges: Double = 0.0,
    val estimatedDays: Int = 3,
    val isActive: Boolean = true,
    val dateAdded: Long = System.currentTimeMillis()
)
