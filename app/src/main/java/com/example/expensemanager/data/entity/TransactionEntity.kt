package com.example.expensemanager.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Transaction entity - Giao dịch thu/chi
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Long,                    // Số tiền (VND)
    val type: TransactionType,           // Thu hoặc Chi
    val categoryId: Long,                // ID danh mục
    val note: String = "",               // Ghi chú
    val date: LocalDate,                 // Ngày giao dịch
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Loại giao dịch
 */
enum class TransactionType {
    INCOME,   // Thu nhập
    EXPENSE   // Chi tiêu
}

/*
Phân loại danh mục
Dùng cho:
Filter tab
Query Room
Logic UI
 */
