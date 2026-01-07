package com.example.expensemanager.util

import android.content.Context
import com.example.expensemanager.data.preferences.SettingsPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {
    // Tỷ giá chuyển đổi: 1 USD = 24,000 VND
    private const val USD_TO_VND_RATE = 24000.0
    
    /**
     * Chuyển đổi số tiền từ VND sang đơn vị hiển thị
     * Dữ liệu gốc luôn lưu bằng VND, chỉ chuyển đổi khi hiển thị
     */
    private fun convertAmount(amountVnd: Long, targetCurrency: String): Double {
        return when (targetCurrency) {
            "USD" -> amountVnd / USD_TO_VND_RATE
            "VND" -> amountVnd.toDouble()
            else -> amountVnd.toDouble()
        }
    }
    
    /**
     * Format số tiền với đơn vị tiền tệ
     * amount: số tiền gốc (luôn là VND)
     * context: context để lấy currency setting
     */
    fun format(amount: Long, context: Context): String {
        val settings = SettingsPreferences.getInstance(context)
        // Read from Flow synchronously
        val currency = runBlocking { settings.currency.first() }
        
        val convertedAmount = convertAmount(amount, currency)
        
        return when (currency) {
            "USD" -> {
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                formatter.currency = Currency.getInstance("USD")
                formatter.format(convertedAmount)
            }
            "VND" -> {
                val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
                "${formatter.format(amount)} ₫"
            }
            else -> {
                val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
                "${formatter.format(amount)} ₫"
            }
        }
    }
    
    /**
     * Format số tiền không có symbol
     */
    fun formatWithoutSymbol(amount: Long, context: Context): String {
        val settings = SettingsPreferences.getInstance(context)
        val currency = runBlocking { settings.currency.first() }
        
        val convertedAmount = convertAmount(amount, currency)
        
        return when (currency) {
            "USD" -> {
                val formatter = NumberFormat.getInstance(Locale.US)
                "$${formatter.format(convertedAmount)}"
            }
            "VND" -> {
                val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
                formatter.format(amount)
            }
            else -> {
                val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
                formatter.format(amount)
            }
        }
    }
    
    /**
     * Lấy symbol của đơn vị tiền tệ hiện tại
     */
    fun getCurrencySymbol(context: Context): String {
        val settings = SettingsPreferences.getInstance(context)
        val currency = runBlocking { settings.currency.first() }
        return when (currency) {
            "USD" -> "$"
            "VND" -> "₫"
            else -> "₫"
        }
    }
}