package com.fyp.mobileaccessories.stockcontrol.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    indices = [Index("userId"), Index("productId"),
        Index("courierId"), Index("paymentCompanyId")]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val quantity: Int,
    val totalAmount: Double,
    val courierId: Int?,
    val paymentCompanyId: Int? = null,
    val paymentMode: String,
    val status: String = "PENDING",
    val deliveryAddress: String,
    val notes: String = "",
    val orderDate: Long = System.currentTimeMillis(),
    val deliveryDate: Long? = null
)
