package com.example.expensemanager.data

import androidx.room.TypeConverter
import com.example.expensemanager.data.entity.TransactionType
import java.time.LocalDate

/**
 * Type Converters cho Room Database
 * Chuyển đổi giữa kiểu dữ liệu Java/Kotlin và SQLite
 */
class Converters {

    /**
     * LocalDate → String (ISO format: 2026-01-04)
     */
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    /**
     * String → LocalDate
     */
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }

    /**
     * TransactionType → String
     */
    @TypeConverter
    fun fromTransactionType(type: TransactionType?): String? {
        return type?.name
    }

    /**
     * String → TransactionType
     */
    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? {
        return value?.let { TransactionType.valueOf(it) }
    }
}

/*
Vì sao cần?
Room không hiểu:
LocalDate
Enum (TransactionType)
👉 Phải convert sang String

Lấy lên: String → Enum
 */
