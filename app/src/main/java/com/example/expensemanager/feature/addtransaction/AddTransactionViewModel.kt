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
import java.time.LocalDate

/**
 * ViewModel cho Add Transaction Screen
 */
class AddTransactionViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val transactionRepository = TransactionRepository(database.transactionDao())
    private val categoryRepository = CategoryRepository(database.categoryDao())
    
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
        val amountLong = amount.toLongOrNull() ?: 0L
        _uiState.update { it.copy(amount = amountLong, amountText = amount) }
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
                    amount = state.amount,
                    type = state.type,
                    categoryId = state.selectedCategory.id,
                    note = state.note,
                    date = state.date
                )
                
                transactionRepository.insertTransaction(transaction)
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
