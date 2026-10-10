package com.fyp.mobileaccessories.stockcontrol.data.repository

import android.content.Context
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.AnnouncementDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.AnnouncementEntity
import com.fyp.mobileaccessories.stockcontrol.util.ConnectionDetector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnouncementRepository @Inject constructor(
    private val announcementDao: AnnouncementDao,
    private val remoteSyncManager: RemoteSyncManager,
    @ApplicationContext private val context: Context
) {
    private fun ensureOnline(actionName: String) {
        if (!ConnectionDetector.isConnected(context)) {
            throw Exception("Internet connection required to $actionName.")
        }
    }

    fun getActiveAnnouncements(): Flow<List<AnnouncementEntity>> =
        announcementDao.getActiveAnnouncements()

    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>> =
        announcementDao.getAllAnnouncements()

    fun filterAnnouncements(type: String = "", fromDate: Long = 0L) =
        announcementDao.filterAnnouncements(type, fromDate)

    suspend fun insert(announcement: AnnouncementEntity) {
        ensureOnline("add announcement")
        val id = announcementDao.insertAnnouncement(announcement)
        val annWithId = announcement.copy(id = id.toInt())
        remoteSyncManager.uploadAnnouncement(annWithId)
        com.fyp.mobileaccessories.stockcontrol.util.NotificationHelper.sendAnnouncementNotification(
            title = announcement.title,
            body = announcement.content
        )
    }

    suspend fun update(announcement: AnnouncementEntity) {
        ensureOnline("update announcement")
        announcementDao.updateAnnouncement(announcement)
        remoteSyncManager.uploadAnnouncement(announcement)
    }

    suspend fun delete(announcement: AnnouncementEntity) {
        ensureOnline("delete announcement")
        announcementDao.deleteAnnouncement(announcement)
        remoteSyncManager.deleteAnnouncement(announcement.id)
    }
}
