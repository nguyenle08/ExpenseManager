package com.example.expensemanager.feature.addtransaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.AppDatabase
import com.example.expensemanager.data.entity.CategoryEntity
import com.example.expensemanager.data.entity.TransactionEntity
import com.example.expensemanager.data.entity.TransactionType
import com.example.expensemanager.data.repository.CategoryRepository
import com.example.expensemanager.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

/**
 * ViewModel cho Add Transaction Screen
 */
class AddTransactionViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  private val transactionRepository = TransactionRepository(database.transactionDao())
  private val categoryRepository = CategoryRepository(database.categoryDao())
  private val transactionDao = database.transactionDao()

  private val _uiState = MutableStateFlow(AddTransactionUiState())
  val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

  // Load categories when ViewModel is created
  init {
    loadCategories(TransactionType.EXPENSE)
  }

  /**
   * Load danh mục theo loại
   */
  private fun loadCategories(type: TransactionType) {
    viewModelScope.launch {
      categoryRepository.getCategoriesByType(type).collect { categories ->
        _uiState.update { it.copy(categories = categories) }
      }
    }
  }

  /**
   * Thay đổi loại giao dịch (Thu/Chi)
   */
  fun onTransactionTypeChanged(type: TransactionType) {
    _uiState.update { it.copy(type = type, selectedCategory = null) }
    loadCategories(type)
  }

  /**
   * Chọn danh mục
   */
  fun onCategorySelected(category: CategoryEntity) {
    _uiState.update { it.copy(selectedCategory = category) }
  }

  /**
   * Thay đổi số tiền
   */
  fun onAmountChanged(amount: String) {
    // Chỉ giữ lại ký tự số
    val digitsOnly = amount.filter { it.isDigit() }

    if (digitsOnly.isEmpty()) {
      _uiState.update { it.copy(amount = 0L, amountText = "") }
      return
    }

    val amountLong = digitsOnly.toLongOrNull() ?: 0L

    // Định dạng có dấu chấm phân cách hàng nghìn (theo vi-VN)
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).apply {
      maximumFractionDigits = 0
    }
    val formatted = formatter.format(amountLong)

    _uiState.update { it.copy(amount = amountLong, amountText = formatted) }
  }

  /**
   * Thay đổi ghi chú
   */
  fun onNoteChanged(note: String) {
    _uiState.update { it.copy(note = note) }
  }

  /**
   * Thay đổi ngày
   */
  fun onDateChanged(date: LocalDate) {
    _uiState.update { it.copy(date = date) }
  }

  /**
   * Load giao dịch để edit
   */
  fun loadTransaction(transactionId: Long) {
    viewModelScope.launch {
      try {
        _uiState.update { it.copy(isLoading = true) }

        val transaction = transactionDao.getTransactionWithCategoryById(transactionId)
        if (transaction != null) {
          // Load categories trước
          loadCategories(transaction.type)

          // Format số tiền
          val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).apply {
            maximumFractionDigits = 0
          }
          val formatted = formatter.format(transaction.amount)

          // Tìm category
          val category = categoryRepository.getCategoryById(transaction.categoryId)

          _uiState.update {
            it.copy(
              transactionId = transactionId,
              type = transaction.type,
              amount = transaction.amount,
              amountText = formatted,
              selectedCategory = category,
              note = transaction.note,
              date = transaction.date,
              isLoading = false
            )
          }
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(isLoading = false, error = "Không thể load giao dịch: ${e.message}")
        }
      }
    }
  }

  /**
   * Lưu giao dịch
   */
  fun saveTransaction(onSuccess: () -> Unit) {
    val state = _uiState.value

    if (state.amount <= 0) {
      _uiState.update { it.copy(error = "Vui lòng nhập số tiền") }
      return
    }

    if (state.selectedCategory == null) {
      _uiState.update { it.copy(error = "Vui lòng chọn danh mục") }
      return
    }

    viewModelScope.launch {
      try {
        _uiState.update { it.copy(isLoading = true, error = null) }

        val transaction = TransactionEntity(
          id = state.transactionId ?: 0,
          amount = state.amount,
          type = state.type,
          categoryId = state.selectedCategory.id,
          note = state.note,
          date = state.date
        )

        if (state.transactionId != null && state.transactionId > 0) {
          // Update existing transaction
          transactionDao.update(transaction)
        } else {
          // Insert new transaction
          transactionRepository.insertTransaction(transaction)
        }

        _uiState.update { it.copy(isLoading = false) }
        onSuccess()
      } catch (e: Exception) {
        _uiState.update {
          it.copy(isLoading = false, error = e.message ?: "Lỗi không xác định")
        }
      }
    }
  }
}

/**
 * UI State cho Add Transaction
 */
data class AddTransactionUiState(
  val transactionId: Long? = null,
  val type: TransactionType = TransactionType.EXPENSE,
  val amount: Long = 0,
  val amountText: String = "",
  val selectedCategory: CategoryEntity? = null,
  val categories: List<CategoryEntity> = emptyList(),
  val note: String = "",
  val date: LocalDate = LocalDate.now(),
  val isLoading: Boolean = false,
  val error: String? = null
)
