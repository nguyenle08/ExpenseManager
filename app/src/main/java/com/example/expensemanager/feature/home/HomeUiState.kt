package com.example.expensemanager.feature.home

import java.time.LocalDate

/**
 * UI State cho Home screen (Trang chủ)
 */
data class HomeUiState(
    val selectedMonth: LocalDate = LocalDate.now(),
    val balance: Long = 0L, // Số dư tháng (VND)
    val totalIncome: Long = 0L, // Tổng thu
    val totalExpense: Long = 0L, // Tổng chi
    val chartData: List<DayData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Dữ liệu biểu đồ theo ngày
 */
data class DayData(
    val date: LocalDate,
    val income: Long,
    val expense: Long
)

/**
 * Quick filter options
 */
enum class QuickFilter(val displayName: String) {
    TODAY("Hôm nay"),
    THIS_WEEK("Tuần này"),
    THIS_MONTH("Tháng này")
}
