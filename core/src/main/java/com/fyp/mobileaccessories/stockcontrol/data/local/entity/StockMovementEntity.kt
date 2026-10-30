package com.fyp.mobileaccessories.stockcontrol.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_movements",
    indices = [Index("productId")]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val movementType: String,
    val quantity: Int,
    val reason: String,
    val previousStock: Int,
    val newStock: Int,
    val movedByUserId: Int,
    val date: Long = System.currentTimeMillis()
)
