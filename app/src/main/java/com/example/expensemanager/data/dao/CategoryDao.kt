package com.example.expensemanager.data.dao

import androidx.room.*
import com.example.expensemanager.data.entity.CategoryEntity
import com.example.expensemanager.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * DAO cho Category
 */
@Dao
interface CategoryDao {

  /**
   * Thêm danh mục mới
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(category: CategoryEntity): Long

  /**
   * Thêm nhiều danh mục
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(categories: List<CategoryEntity>)

  /**
   * Cập nhật danh mục
   */
  @Update
  suspend fun update(category: CategoryEntity)

  /**
   * Xóa danh mục
   */
  @Delete
  suspend fun delete(category: CategoryEntity)

  /**
   * Lấy tất cả danh mục
   */
  @Query("SELECT * FROM categories ORDER BY type, name")
  fun getAllCategories(): Flow<List<CategoryEntity>>

  /**
   * Lấy tất cả danh mục (suspend) dùng cho các màn hình cần snapshot ngay.
   */
  @Query("SELECT * FROM categories ORDER BY type, name")
  suspend fun getAllCategoriesOnce(): List<CategoryEntity>

  /**
   * Lấy danh mục theo loại (Thu/Chi)
   */
  @Query("SELECT * FROM categories WHERE type = :type ORDER BY name")
  fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>>

  /**
   * Lấy danh mục theo ID
   */
  @Query("SELECT * FROM categories WHERE id = :id")
  suspend fun getCategoryById(id: Long): CategoryEntity?

  /**
   * Lấy danh mục mặc định
   */
  @Query("SELECT * FROM categories WHERE isDefault = 1")
  fun getDefaultCategories(): Flow<List<CategoryEntity>>

  /**
   * Xóa tất cả danh mục (cho testing)
   */
  @Query("DELETE FROM categories WHERE isDefault = 0")
  suspend fun deleteAllNonDefault()
}
