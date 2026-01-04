package com.example.expensemanager.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.AppDatabase
import com.example.expensemanager.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate

/**
 * ViewModel quản lý UI state cho Trang chủ (Home)
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = TransactionRepository(database.transactionDao())
    
    private val _selectedMonth = MutableStateFlow(LocalDate.now())
    
    val uiState: StateFlow<HomeUiState> = _selectedMonth
        .flatMapLatest { month ->
            combine(
                flowOf(month),
                repository.getBalanceByMonth(month),
                repository.getTotalIncomeByMonth(month),
                repository.getTotalExpenseByMonth(month),
                repository.getChartDataByMonth(month)
            ) { selectedMonth, balance, income, expense, chartData ->
                HomeUiState(
                    selectedMonth = selectedMonth,
                    balance = balance,
                    totalIncome = income,
                    totalExpense = expense,
                    chartData = chartData,
                    isLoading = false,
                    error = null
                )
            }
        }
        .catch { e ->
            emit(HomeUiState(error = e.message))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true)
        )
    
    /**
     * Thay đổi tháng được chọn
     */
    fun onMonthChanged(month: LocalDate) {
        _selectedMonth.value = month
    }
    
    /**
     * Apply quick filter
     */
    fun applyQuickFilter(filter: QuickFilter) {
        when (filter) {
            QuickFilter.TODAY -> {
                // Filter today's transactions
            }
            QuickFilter.THIS_WEEK -> {
                // Filter this week's transactions
            }
            QuickFilter.THIS_MONTH -> {
                _selectedMonth.value = LocalDate.now()
            }
        }
    }
}
