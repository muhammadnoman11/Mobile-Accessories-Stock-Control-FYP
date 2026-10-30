package com.fyp.mobileaccessories.stockcontrol.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_companies")
data class PaymentCompanyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: String,
    val accountNo: String = "",
    val contactNo: String = "",
    val email: String = "",
    val isActive: Boolean = true,
    val dateAdded: Long = System.currentTimeMillis()
)
