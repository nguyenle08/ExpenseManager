package com.example.expensemanager.utils

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Currency utilities for formatting money
 */
object CurrencyUtils {
    private val vndFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
    
    /**
     * Format số tiền theo định dạng VND
     */
    fun formatVND(amount: Long): String {
        return vndFormatter.format(amount)
    }
    
    /**
     * Format số tiền ngắn gọn (1.5M, 2.3K)
     */
    fun formatCompactVND(amount: Long): String {
        return when {
            amount >= 1_000_000_000 -> String.format("%.1fB", amount / 1_000_000_000.0)
            amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000.0)
            amount >= 1_000 -> String.format("%.1fK", amount / 1_000.0)
            else -> amount.toString()
        } + "đ"
    }
}

/**
 * Date utilities for formatting dates
 */
object DateUtils {
    private val viLocale = Locale.forLanguageTag("vi-VN")
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", viLocale)
    private val dayMonthFormatter = DateTimeFormatter.ofPattern("dd/MM", viLocale)
    private val fullDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", viLocale)
    
    /**
     * Format LocalDate to "Tháng 1 2026"
     */
    fun formatMonthYear(date: LocalDate): String {
        return date.format(monthYearFormatter)
    }
    
    /**
     * Format LocalDate to "25/01"
     */
    fun formatDayMonth(date: LocalDate): String {
        return date.format(dayMonthFormatter)
    }
    
    /**
     * Format LocalDate to "25/01/2026"
     */
    fun formatFullDate(date: LocalDate): String {
        return date.format(fullDateFormatter)
    }
}
