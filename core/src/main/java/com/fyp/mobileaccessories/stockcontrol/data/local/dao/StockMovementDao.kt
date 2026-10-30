package com.fyp.mobileaccessories.stockcontrol.data.local.dao

import androidx.room.*
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity): Long

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY date DESC")
    fun getMovementsForProduct(productId: Int): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements ORDER BY date DESC LIMIT :limit")
    fun getRecentMovements(limit: Int = 50): Flow<List<StockMovementEntity>>
}
