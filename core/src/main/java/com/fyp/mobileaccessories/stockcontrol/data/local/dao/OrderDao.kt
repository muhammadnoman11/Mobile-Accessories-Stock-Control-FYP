package com.fyp.mobileaccessories.stockcontrol.data.local.dao

import androidx.room.*
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY orderDate DESC")
    fun getOrdersByUser(userId: Int): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY orderDate DESC")
    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>>

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String)

    @Query("""
        SELECT * FROM orders WHERE
        (:status = '' OR status = :status)
        ORDER BY orderDate DESC
    """)
    fun filterOrders(status: String = ""): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Int): OrderEntity?

    @Query("SELECT COUNT(*) FROM orders WHERE status = :status")
    suspend fun countOrdersByStatus(status: String): Int

    @Query("SELECT * FROM orders")
    suspend fun getAllOrdersList(): List<OrderEntity>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM orders WHERE status = 'DELIVERED'")
    suspend fun getTotalRevenue(): Double
}
