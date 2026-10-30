package com.fyp.mobileaccessories.stockcontrol.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val type: String,
    val isActive: Boolean = true,
    val dateCreated: Long = System.currentTimeMillis(),
    val expiryDate: Long? = null
)
