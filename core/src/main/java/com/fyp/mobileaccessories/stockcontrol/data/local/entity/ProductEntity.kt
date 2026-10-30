package com.fyp.mobileaccessories.stockcontrol.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index("categoryId"), Index("addedByUserId")]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val categoryId: Int?,
    val subCategory: String = "",
    val price: Double,
    val stockQuantity: Int,
    val lowStockThreshold: Int = 5,
    val addedByUserId: Int?,
    val isApproved: Boolean = false,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val dateAdded: Long = System.currentTimeMillis(),
    val imageUrl: String = ""
)
