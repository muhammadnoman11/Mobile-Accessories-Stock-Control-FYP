package com.fyp.mobileaccessories.stockcontrol.data.repository.sync

import android.util.Log
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.AnnouncementDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.AnnouncementEntity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AnnouncementSyncHandler(
    private val announcementDao: AnnouncementDao,
    private val scope: CoroutineScope
) {
    private val TAG = "AnnouncementSync"

    fun handleSync(snapshot: DataSnapshot) {
        scope.launch {
            val cloudIds = mutableSetOf<Int>()
            for (child in snapshot.children) {
                try {
                    val id = child.child("id").getValue(Int::class.java) ?: continue
                    cloudIds.add(id)
                    val title = child.child("title").getValue(String::class.java) ?: ""
                    val content = child.child("content").getValue(String::class.java) ?: ""
                    val type = child.child("type").getValue(String::class.java) ?: "GENERAL"
                    val isActive = child.child("active").getValue(Boolean::class.java)
                        ?: child.child("isActive").getValue(Boolean::class.java) ?: true
                    val dateCreated = child.child("dateCreated").getValue(Long::class.java) ?: System.currentTimeMillis()

                    val announcement = AnnouncementEntity(
                        id = id, title = title, content = content, type = type,
                        isActive = isActive, dateCreated = dateCreated
                    )
                    announcementDao.insertAnnouncement(announcement)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing announcement: ${e.message}")
                }
            }
            val localAnnouncements = announcementDao.getAllAnnouncementsList()
            for (local in localAnnouncements) {
                if (local.id !in cloudIds) {
                    announcementDao.deleteAnnouncement(local)
                }
            }
        }
    }

    fun upload(ref: DatabaseReference, announcement: AnnouncementEntity) {
        val map = mapOf(
            "id" to announcement.id,
            "title" to announcement.title,
            "content" to announcement.content,
            "type" to announcement.type,
            "isActive" to announcement.isActive,
            "dateCreated" to announcement.dateCreated
        )
        ref.child("announcements").child(announcement.id.toString()).setValue(map)
    }
}
