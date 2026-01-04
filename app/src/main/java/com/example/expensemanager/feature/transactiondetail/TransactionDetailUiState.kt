package com.example.expensemanager.feature.transactiondetail

import java.time.LocalDate

/**
 * UI state cho màn hình chi tiết giao dịch theo tháng.
 */
data class TransactionDetailUiState(
  val selectedMonth: LocalDate = LocalDate.now(),
  val balance: Long = 0L,
  val totalIncome: Long = 0L,
  val totalExpense: Long = 0L,
  val dailyGroups: List<DayTransactionGroup> = emptyList(),
  val isLoading: Boolean = false,
  val error: String? = null
)

/**
 * Nhóm giao dịch theo ngày để hiển thị giống card trong thiết kế.
 */
data class DayTransactionGroup(
  val date: LocalDate,
  val totalIncome: Long,
  val totalExpense: Long,
  val transactions: List<TransactionItemUi>
)

/**
 * Thông tin 1 giao dịch trong UI.
 */
data class TransactionItemUi(
  val id: Long,
  val categoryName: String,
  val note: String?,
  val amount: Long,
  val isIncome: Boolean,
  val categoryColor: String?,
  val categoryIcon: String?
)
