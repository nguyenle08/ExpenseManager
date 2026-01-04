package com.example.expensemanager.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Category entity - Danh mục chi tiêu/thu nhập
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,                    // Tên danh mục (Ăn uống, Mua sắm, Lương...)
    val type: TransactionType,           // Loại (Thu/Chi)
    val icon: String = "💰",             // Icon emoji
    val color: String = "#4CAF50",       // Màu sắc (hex)
    val isDefault: Boolean = false       // Danh mục mặc định không xóa được
)
