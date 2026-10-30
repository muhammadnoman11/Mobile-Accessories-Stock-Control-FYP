package com.fyp.mobileaccessories.stockcontrol.data.local.dao

import androidx.room.*
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsList(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY transactionDate DESC")
    fun getTransactionsByUser(userId: Int): Flow<List<TransactionEntity>>

    @Query("UPDATE transactions SET status = :status, remarks = :remarks WHERE id = :transactionId")
    suspend fun updateTransactionStatus(transactionId: Int, status: String, remarks: String = "")

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Int): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE orderId = :orderId LIMIT 1")
    suspend fun getTransactionByOrderId(orderId: Int): TransactionEntity?

    @Query("""
        SELECT * FROM transactions WHERE
        (:status = '' OR status = :status) AND
        (:paymentMode = '' OR paymentMode = :paymentMode)
        ORDER BY transactionDate DESC
    """)
    fun filterTransactions(status: String = "", paymentMode: String = ""): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE status = 'PENDING'")
    suspend fun countPendingTransactions(): Int
}
