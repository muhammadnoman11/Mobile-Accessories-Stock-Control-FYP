package com.fyp.mobileaccessories.stockcontrol.data.local.dao

import androidx.room.*
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.PaymentCompanyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentCompanyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentCompany(company: PaymentCompanyEntity): Long

    @Update
    suspend fun updatePaymentCompany(company: PaymentCompanyEntity)

    @Delete
    suspend fun deletePaymentCompany(company: PaymentCompanyEntity)

    @Query("SELECT * FROM payment_companies WHERE isActive = 1 ORDER BY name ASC")
    fun getActivePaymentCompanies(): Flow<List<PaymentCompanyEntity>>

    @Query("SELECT * FROM payment_companies ORDER BY name ASC")
    fun getAllPaymentCompanies(): Flow<List<PaymentCompanyEntity>>

    @Query("SELECT * FROM payment_companies")
    suspend fun getAllPaymentCompaniesList(): List<PaymentCompanyEntity>

    @Query("SELECT * FROM payment_companies WHERE id = :id LIMIT 1")
    suspend fun getPaymentCompanyById(id: Int): PaymentCompanyEntity?

    @Query("""
        SELECT * FROM payment_companies WHERE 
        (name LIKE '%' || :query || '%' OR type LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchPaymentCompanies(query: String): Flow<List<PaymentCompanyEntity>>
}
