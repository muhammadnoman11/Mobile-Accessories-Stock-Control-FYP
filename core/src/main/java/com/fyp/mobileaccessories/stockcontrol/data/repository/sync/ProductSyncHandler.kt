package com.fyp.mobileaccessories.stockcontrol.data.repository.sync

import android.util.Log
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.ProductDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.ProductEntity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ProductSyncHandler(
    private val productDao: ProductDao,
    private val scope: CoroutineScope
) {
    private val TAG = "ProductSync"

    fun handleSync(snapshot: DataSnapshot) {
        scope.launch {
            if (!snapshot.exists() || !snapshot.hasChildren()) {
                return@launch
            }
            val cloudIds = mutableSetOf<Int>()
            for (child in snapshot.children) {
                try {
                    val id = child.child("id").getValue(Int::class.java) ?: continue
                    cloudIds.add(id)
                    val name = child.child("name").getValue(String::class.java) ?: ""
                    val description = child.child("description").getValue(String::class.java) ?: ""
                    val categoryId = child.child("categoryId").getValue(Int::class.java)
                    val subCategory = child.child("subCategory").getValue(String::class.java) ?: ""
                    val price = child.child("price").getValue(Double::class.java) ?: 0.0
                    val stockQuantity = child.child("stockQuantity").getValue(Int::class.java) ?: 0
                    val lowStockThreshold = child.child("lowStockThreshold").getValue(Int::class.java) ?: 5
                    val addedByUserId = child.child("addedByUserId").getValue(Int::class.java)
                    val isApproved = child.child("isApproved").getValue(Boolean::class.java)
                        ?: child.child("approved").getValue(Boolean::class.java) ?: false
                    val status = child.child("status").getValue(String::class.java)
                        ?: if (isApproved) "APPROVED" else "PENDING"
                    val rejectionReason = child.child("rejectionReason").getValue(String::class.java) ?: ""
                    val dateAdded = child.child("dateAdded").getValue(Long::class.java) ?: System.currentTimeMillis()
                    val imageUrl = child.child("imageUrl").getValue(String::class.java)?.trim() ?: ""

                    val product = ProductEntity(
                        id = id, name = name, description = description, categoryId = categoryId,
                        subCategory = subCategory, price = price, stockQuantity = stockQuantity,
                        lowStockThreshold = lowStockThreshold, addedByUserId = addedByUserId,
                        isApproved = isApproved, status = status, rejectionReason = rejectionReason,
                        dateAdded = dateAdded, imageUrl = imageUrl
                    )
                    productDao.insertProduct(product)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing product: ${e.message}")
                }
            }
            val localProducts = productDao.getAllProductsList()
            for (local in localProducts) {
                if (local.id !in cloudIds) {
                    productDao.deleteProduct(local)
                }
            }
        }
    }

    fun upload(ref: DatabaseReference, product: ProductEntity) {
        val map = mapOf(
            "id" to product.id,
            "name" to product.name,
            "description" to product.description,
            "categoryId" to (product.categoryId as Any?),
            "subCategory" to product.subCategory,
            "price" to product.price,
            "stockQuantity" to product.stockQuantity,
            "lowStockThreshold" to product.lowStockThreshold,
            "addedByUserId" to (product.addedByUserId as Any?),
            "isApproved" to product.isApproved,
            "status" to product.status,
            "rejectionReason" to product.rejectionReason,
            "dateAdded" to product.dateAdded,
            "imageUrl" to product.imageUrl
        )
        ref.child("products").child(product.id.toString()).setValue(map)
    }
}
