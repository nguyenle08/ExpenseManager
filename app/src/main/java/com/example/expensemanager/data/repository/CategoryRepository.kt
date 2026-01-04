package com.example.expensemanager.data.repository

import com.example.expensemanager.data.dao.CategoryDao
import com.example.expensemanager.data.entity.CategoryEntity
import com.example.expensemanager.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * Repository để truy cập dữ liệu Category
 */
class CategoryRepository(private val categoryDao: CategoryDao) {
    
    /**
     * Lấy tất cả danh mục
     */
    fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories()
    }
    
    /**
     * Lấy danh mục theo loại (Thu/Chi)
     */
    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesByType(type)
    }
    
    /**
     * Lấy danh mục theo ID
     */
    suspend fun getCategoryById(id: Long): CategoryEntity? {
        return categoryDao.getCategoryById(id)
    }
    
    /**
     * Thêm danh mục mới
     */
    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insert(category)
    }
    
    /**
     * Cập nhật danh mục
     */
    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.update(category)
    }
    
    /**
     * Xóa danh mục
     */
    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.delete(category)
    }
}
