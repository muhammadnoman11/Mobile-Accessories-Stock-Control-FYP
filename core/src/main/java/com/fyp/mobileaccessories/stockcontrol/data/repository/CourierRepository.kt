package com.fyp.mobileaccessories.stockcontrol.data.repository

import android.content.Context
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.CourierDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CourierEntity
import com.fyp.mobileaccessories.stockcontrol.util.ConnectionDetector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourierRepository @Inject constructor(
    private val courierDao: CourierDao,
    private val remoteSyncManager: RemoteSyncManager,
    @ApplicationContext private val context: Context
) {
    private fun ensureOnline(actionName: String) {
        if (!ConnectionDetector.isConnected(context)) {
            throw Exception("Internet connection required to $actionName.")
        }
    }

    fun getAllCouriers(): Flow<List<CourierEntity>> = courierDao.getAllCouriers()
    fun getActiveCouriers(): Flow<List<CourierEntity>> = courierDao.getActiveCouriers()
    suspend fun getCourierById(id: Int) = courierDao.getCourierById(id)
    suspend fun insertCourier(courier: CourierEntity) {
        ensureOnline("add courier")
        val id = courierDao.insertCourier(courier)
        remoteSyncManager.uploadCourier(courier.copy(id = id.toInt()))
    }
    suspend fun updateCourier(courier: CourierEntity) {
        ensureOnline("update courier")
        courierDao.updateCourier(courier)
        remoteSyncManager.uploadCourier(courier)
    }
    suspend fun deleteCourier(courier: CourierEntity) {
        ensureOnline("delete courier")
        courierDao.deleteCourier(courier)
        remoteSyncManager.deleteCourier(courier.id)
    }
}
