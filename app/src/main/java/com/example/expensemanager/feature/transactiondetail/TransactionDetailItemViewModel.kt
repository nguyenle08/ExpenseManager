package com.example.expensemanager.feature.transactiondetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.AppDatabase
import com.example.expensemanager.data.entity.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel cho màn hình chi tiết một giao dịch cụ thể
 */
class TransactionDetailItemViewModel(application: Application) : AndroidViewModel(application) {

  private val transactionDao = AppDatabase.getDatabase(application).transactionDao()

  private val _uiState = MutableStateFlow(TransactionDetailItemUiState())
  val uiState: StateFlow<TransactionDetailItemUiState> = _uiState.asStateFlow()

  /**
   * Load thông tin chi tiết của giao dịch
   */
  fun loadTransaction(transactionId: Long) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)

      try {
        val transaction = transactionDao.getTransactionWithCategoryById(transactionId)

        if (transaction != null) {
          _uiState.value = _uiState.value.copy(
            transaction = TransactionDetailItemUi(
              id = transaction.id,
              categoryName = transaction.categoryName ?: "Không có danh mục",
              note = transaction.note,
              amount = transaction.amount,
              isIncome = transaction.type == TransactionType.INCOME,
              date = transaction.date,
              categoryColor = transaction.categoryColor,
              categoryIcon = transaction.categoryIcon
            ),
            isLoading = false
          )
        } else {
          _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = "Không tìm thấy giao dịch"
          )
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          error = "Có lỗi xảy ra: ${e.message}"
        )
      }
    }
  }

  /**
   * Xóa giao dịch
   */
  fun deleteTransaction(transactionId: Long) {
    viewModelScope.launch {
      try {
        val transaction = transactionDao.getTransactionById(transactionId)
        if (transaction != null) {
          transactionDao.delete(transaction)
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          error = "Không thể xóa giao dịch: ${e.message}"
        )
      }
    }
  }
}

/**
 * UI State cho màn hình chi tiết giao dịch
 */
data class TransactionDetailItemUiState(
  val transaction: TransactionDetailItemUi? = null,
  val isLoading: Boolean = false,
  val error: String? = null
)

/**
 * Model UI cho chi tiết giao dịch
 */
data class TransactionDetailItemUi(
  val id: Long,
  val categoryName: String,
  val note: String?,
  val amount: Long,
  val isIncome: Boolean,
  val date: LocalDate,
  val categoryColor: String?,
  val categoryIcon: String?
)
