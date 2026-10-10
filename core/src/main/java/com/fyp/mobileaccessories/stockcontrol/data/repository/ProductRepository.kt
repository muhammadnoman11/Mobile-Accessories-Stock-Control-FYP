package com.fyp.mobileaccessories.stockcontrol.data.repository

import android.content.Context
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.ProductDao
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.StockMovementDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.StockMovementEntity
import com.fyp.mobileaccessories.stockcontrol.util.ConnectionDetector
import com.fyp.mobileaccessories.stockcontrol.util.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao,
    private val remoteSyncManager: RemoteSyncManager,
    @ApplicationContext private val context: Context
) {
    private fun ensureOnline(actionName: String = "perform this product action") {
        if (!ConnectionDetector.isConnected(context)) {
            throw Exception("Internet connection required to $actionName.")
        }
    }

    fun getApprovedProducts(): Flow<List<ProductEntity>> = productDao.getApprovedProducts()
    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()
    fun getPendingProducts(): Flow<List<ProductEntity>> = productDao.getPendingApprovalProducts()
    fun getLowStockProducts(): Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    fun searchProducts(query: String, categoryId: Int = -1, onlyApproved: Boolean = true) =
        productDao.searchProducts(query, categoryId, onlyApproved)

    fun getProductsByCategory(categoryId: Int) = productDao.getProductsByCategory(categoryId)

    suspend fun insertProduct(product: ProductEntity): Long {
        ensureOnline("add a product")
        val id = productDao.insertProduct(product)
        val productWithId = product.copy(id = id.toInt())
        remoteSyncManager.uploadProduct(productWithId)
        if (!product.isApproved) {
            NotificationHelper.sendProductPendingNotification(product.name, product.imageUrl)
        }
        return id
    }

    suspend fun updateProduct(product: ProductEntity) {
        ensureOnline("update a product")
        productDao.updateProduct(product)
        remoteSyncManager.uploadProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        ensureOnline("delete a product")
        productDao.deleteProduct(product)
        remoteSyncManager.deleteProduct(product.id)
    }

    suspend fun getProductById(id: Int) = productDao.getProductById(id)

    suspend fun isProductNameDuplicate(name: String, excludeId: Int = 0): Boolean {
        return productDao.countProductsByName(name.trim(), excludeId) > 0
    }

    suspend fun setApprovalStatus(productId: Int, approved: Boolean) {
        ensureOnline(if (approved) "approve product" else "reject product")
        productDao.setApprovalStatus(productId, approved)
        remoteSyncManager.updateProductApproval(productId, approved)

        val product = productDao.getProductById(productId)
        if (product != null) {
            NotificationHelper.sendProductApprovalNotification(
                productName = product.name,
                isApproved = approved,
                imageUrl = product.imageUrl
            )
        }
    }

    suspend fun adjustStock(
        productId: Int,
        currentStock: Int,
        newStock: Int,
        movementType: String,
        reason: String,
        movedByUserId: Int
    ) {
        ensureOnline("adjust product stock")
        productDao.updateStock(productId, newStock)
        stockMovementDao.insertMovement(
            StockMovementEntity(
                productId = productId,
                movementType = movementType,
                quantity = newStock - currentStock,
                reason = reason,
                previousStock = currentStock,
                newStock = newStock,
                movedByUserId = movedByUserId
            )
        )
        productDao.getProductById(productId)?.let {
            remoteSyncManager.uploadProduct(it)
        }
    }

    suspend fun countApproved() = productDao.countApprovedProducts()
    suspend fun countPending() = productDao.countPendingProducts()
    suspend fun countLowStock() = productDao.countLowStockProducts()
}
