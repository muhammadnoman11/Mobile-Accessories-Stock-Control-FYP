package com.fyp.mobileaccessories.stockcontrol.data.repository.sync

import android.util.Log
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.CategoryDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CategoryEntity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CategorySyncHandler(
    private val categoryDao: CategoryDao,
    private val scope: CoroutineScope
) {
    private val TAG = "CategorySync"

    fun handleSync(snapshot: DataSnapshot) {
        scope.launch {
            val cloudIds = mutableSetOf<Int>()
            if (snapshot.exists()) {
                for (child in snapshot.children) {
                    try {
                        val id = child.child("id").getValue(Int::class.java)
                            ?: child.key?.toIntOrNull() ?: continue
                        cloudIds.add(id)
                        val name = child.child("name").getValue(String::class.java) ?: ""

                        val category = CategoryEntity(
                            id = id, name = name
                        )
                        categoryDao.insertCategory(category)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing category: ${e.message}")
                    }
                }
            }
            try {
                val localCategories = categoryDao.getAllCategoriesList()
                for (local in localCategories) {
                    if (local.id !in cloudIds) {
                        categoryDao.deleteCategory(local)
                        Log.d(TAG, "Deleted local category ${local.name} as it was removed from Realtime Database.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing category deletions: ${e.message}")
            }
        }
    }

    fun upload(ref: DatabaseReference, category: CategoryEntity) {
        val map = mapOf(
            "id" to category.id,
            "name" to category.name
        )
        ref.child("categories").child(category.id.toString()).setValue(map)
    }
}
