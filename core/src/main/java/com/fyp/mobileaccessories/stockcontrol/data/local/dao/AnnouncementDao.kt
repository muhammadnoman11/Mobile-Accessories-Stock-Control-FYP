package com.fyp.mobileaccessories.stockcontrol.data.local.dao

import androidx.room.*
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.AnnouncementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Update
    suspend fun updateAnnouncement(announcement: AnnouncementEntity)

    @Delete
    suspend fun deleteAnnouncement(announcement: AnnouncementEntity)

    @Query("SELECT * FROM announcements WHERE isActive = 1 ORDER BY dateCreated DESC")
    fun getActiveAnnouncements(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements ORDER BY dateCreated DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Query("""
        SELECT * FROM announcements WHERE
        (:type = '' OR type = :type) AND
        (:fromDate = 0 OR dateCreated >= :fromDate)
        ORDER BY dateCreated DESC
    """)
    fun filterAnnouncements(type: String = "", fromDate: Long = 0L): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements")
    suspend fun getAllAnnouncementsList(): List<AnnouncementEntity>
}
