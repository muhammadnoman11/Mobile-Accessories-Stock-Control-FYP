package com.fyp.mobileaccessories.stockcontrol.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index("orderId"), Index("userId"), Index("paymentCompanyId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orderId: Int,
    val userId: Int,
    val paymentCompanyId: Int? = null,
    val amount: Double,
    val paymentMode: String,
    val status: String = "PENDING",
    val transactionDate: Long = System.currentTimeMillis(),
    val referenceNo: String = "TXN-${System.currentTimeMillis()}",
    val remarks: String = ""
)
