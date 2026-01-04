package com.example.expensemanager.feature.transactiondetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.AppDatabase
import com.example.expensemanager.data.entity.TransactionType
import com.example.expensemanager.data.repository.CategoryRepository
import com.example.expensemanager.data.repository.TransactionRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình chi tiết giao dịch theo tháng.
 */
class TransactionDetailViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  private val transactionRepository = TransactionRepository(database.transactionDao())
  private val categoryRepository = CategoryRepository(database.categoryDao())

  private val _uiState = MutableStateFlow(TransactionDetailUiState())
  val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

  init {
    loadDataForMonth(_uiState.value.selectedMonth)
  }

  fun onMonthChanged(month: LocalDate) {
    _uiState.update { it.copy(selectedMonth = month) }
    loadDataForMonth(month)
  }

  private fun loadDataForMonth(month: LocalDate) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
      try {
        val transactions = transactionRepository.getTransactionsByMonthOnce(month)
        val categories = categoryRepository.getAllCategoriesOnce()

        val categoryMap = categories.associateBy { it.id }

        val grouped = transactions.groupBy { it.date }
          .toSortedMap(compareByDescending { it }) // Ngày mới ở trên
          .map { (date, dailyTxs) ->
            var totalIncome = 0L
            var totalExpense = 0L

            val items = dailyTxs.map { tx ->
              val category = tx.categoryId?.let { categoryMap[it] }
              val isIncome = tx.type == TransactionType.INCOME
              if (isIncome) totalIncome += tx.amount else totalExpense += tx.amount

              TransactionItemUi(
                id = tx.id,
                categoryName = category?.name ?: "Khác",
                note = tx.note,
                amount = tx.amount,
                isIncome = isIncome,
                categoryColor = category?.color,
                categoryIcon = category?.icon
              )
            }

            DayTransactionGroup(
              date = date,
              totalIncome = totalIncome,
              totalExpense = totalExpense,
              transactions = items
            )
          }

        val totalIncome = grouped.sumOf { it.totalIncome }
        val totalExpense = grouped.sumOf { it.totalExpense }
        val balance = totalIncome - totalExpense

        _uiState.update {
          it.copy(
            balance = balance,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            dailyGroups = grouped,
            isLoading = false,
            error = null
          )
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
            isLoading = false,
            error = e.message ?: "Lỗi tải dữ liệu"
          )
        }
      }
    }
  }
}
