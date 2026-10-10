package com.fyp.mobileaccessories.stockcontrol.data.repository

import android.content.Context
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.PaymentCompanyDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.PaymentCompanyEntity
import com.fyp.mobileaccessories.stockcontrol.util.ConnectionDetector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentCompanyRepository @Inject constructor(
    private val paymentCompanyDao: PaymentCompanyDao,
    private val remoteSyncManager: RemoteSyncManager,
    @ApplicationContext private val context: Context
) {
    private fun ensureOnline(actionName: String) {
        if (!ConnectionDetector.isConnected(context)) {
            throw Exception("Internet connection required to $actionName.")
        }
    }

    fun getAllPaymentCompanies(): Flow<List<PaymentCompanyEntity>> = paymentCompanyDao.getAllPaymentCompanies()
    fun getActivePaymentCompanies(): Flow<List<PaymentCompanyEntity>> = paymentCompanyDao.getActivePaymentCompanies()
    suspend fun getPaymentCompanyById(id: Int) = paymentCompanyDao.getPaymentCompanyById(id)
    suspend fun insertPaymentCompany(company: PaymentCompanyEntity) {
        ensureOnline("add payment company")
        val id = paymentCompanyDao.insertPaymentCompany(company)
        remoteSyncManager.uploadPaymentCompany(company.copy(id = id.toInt()))
    }
    suspend fun updatePaymentCompany(company: PaymentCompanyEntity) {
        ensureOnline("update payment company")
        paymentCompanyDao.updatePaymentCompany(company)
        remoteSyncManager.uploadPaymentCompany(company)
    }
    suspend fun deletePaymentCompany(company: PaymentCompanyEntity) {
        ensureOnline("delete payment company")
        paymentCompanyDao.deletePaymentCompany(company)
        remoteSyncManager.deletePaymentCompany(company.id)
    }
    fun searchPaymentCompanies(query: String) = paymentCompanyDao.searchPaymentCompanies(query)
}
