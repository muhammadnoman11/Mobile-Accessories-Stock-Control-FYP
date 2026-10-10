package com.fyp.mobileaccessories.stockcontrol.data.repository

import com.fyp.mobileaccessories.stockcontrol.data.local.dao.StockMovementDao
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockMovementRepository @Inject constructor(
    private val stockMovementDao: StockMovementDao
) {
    fun getMovementsForProduct(productId: Int) = stockMovementDao.getMovementsForProduct(productId)
    fun getRecentMovements(limit: Int = 50) = stockMovementDao.getRecentMovements(limit)
}
