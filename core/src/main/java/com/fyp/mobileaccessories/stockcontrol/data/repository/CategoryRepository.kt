package com.fyp.mobileaccessories.stockcontrol.data.repository

import android.content.Context
import com.fyp.mobileaccessories.stockcontrol.data.local.dao.CategoryDao
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.CategoryEntity
import com.fyp.mobileaccessories.stockcontrol.util.ConnectionDetector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val remoteSyncManager: RemoteSyncManager,
    @ApplicationContext private val context: Context
) {
    private fun ensureOnline(actionName: String) {
        if (!ConnectionDetector.isConnected(context)) {
            throw Exception("Internet connection required to $actionName.")
        }
    }

    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    suspend fun getCategoryById(id: Int) = categoryDao.getCategoryById(id)
    suspend fun insertCategory(category: CategoryEntity): Long {
        ensureOnline("add category")
        val id = categoryDao.insertCategory(category)
        val categoryWithId = category.copy(id = id.toInt())
        remoteSyncManager.uploadCategory(categoryWithId)
        return id
    }
    suspend fun updateCategory(category: CategoryEntity) {
        ensureOnline("update category")
        categoryDao.updateCategory(category)
        remoteSyncManager.uploadCategory(category)
    }
    suspend fun deleteCategory(category: CategoryEntity) {
        ensureOnline("delete category")
        categoryDao.deleteCategory(category)
        remoteSyncManager.deleteCategory(category.id)
    }
}
