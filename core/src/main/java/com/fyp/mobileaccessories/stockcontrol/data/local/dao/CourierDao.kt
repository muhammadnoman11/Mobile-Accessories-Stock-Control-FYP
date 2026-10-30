package com.fyp.mobileaccessories.stockcontrol.data.local.dao

import androidx.room.*
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CourierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourierDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourier(courier: CourierEntity): Long

    @Update
    suspend fun updateCourier(courier: CourierEntity)

    @Delete
    suspend fun deleteCourier(courier: CourierEntity)

    @Query("SELECT * FROM couriers WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveCouriers(): Flow<List<CourierEntity>>

    @Query("SELECT * FROM couriers ORDER BY name ASC")
    fun getAllCouriers(): Flow<List<CourierEntity>>

    @Query("SELECT * FROM couriers")
    suspend fun getAllCouriersList(): List<CourierEntity>

    @Query("SELECT * FROM couriers WHERE id = :id LIMIT 1")
    suspend fun getCourierById(id: Int): CourierEntity?
}
