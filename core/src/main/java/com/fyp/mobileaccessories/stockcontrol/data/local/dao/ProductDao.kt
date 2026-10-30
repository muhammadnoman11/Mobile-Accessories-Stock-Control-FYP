package com.fyp.mobileaccessories.stockcontrol.data.local.dao

import androidx.room.*
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT * FROM products WHERE (status = 'APPROVED' OR isApproved = 1) ORDER BY dateAdded DESC")
    fun getApprovedProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY dateAdded DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products")
    suspend fun getAllProductsList(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE (status = 'PENDING' OR status = '' OR isApproved = 0) AND status != 'REJECTED' ORDER BY dateAdded ASC")
    fun getPendingApprovalProducts(): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products WHERE
        (name LIKE '%' || :query || '%' OR
         description LIKE '%' || :query || '%' OR
         subCategory LIKE '%' || :query || '%')
        AND (:categoryId = -1 OR categoryId = :categoryId)
        AND (:onlyApproved = 0 OR isApproved = 1)
        ORDER BY dateAdded DESC
    """)
    fun searchProducts(
        query: String,
        categoryId: Int = -1,
        onlyApproved: Boolean = true
    ): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stockQuantity <= lowStockThreshold AND isApproved = 1")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): ProductEntity?

    @Query("UPDATE products SET isApproved = :approved, status = :status WHERE id = :productId")
    suspend fun setApprovalStatus(productId: Int, approved: Boolean, status: String)

    @Query("UPDATE products SET stockQuantity = :newQuantity WHERE id = :productId")
    suspend fun updateStock(productId: Int, newQuantity: Int)

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isApproved = 1 ORDER BY name ASC")
    fun getProductsByCategory(categoryId: Int): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products WHERE isApproved = 1")
    suspend fun countApprovedProducts(): Int

    @Query("SELECT COUNT(*) FROM products WHERE isApproved = 0 AND status != 'REJECTED'")
    suspend fun countPendingProducts(): Int

    @Query("SELECT COUNT(*) FROM products WHERE stockQuantity <= lowStockThreshold AND isApproved = 1")
    suspend fun countLowStockProducts(): Int

    @Query("SELECT COUNT(*) FROM products WHERE LOWER(name) = LOWER(:name) AND id != :excludeId")
    suspend fun countProductsByName(name: String, excludeId: Int): Int
}
