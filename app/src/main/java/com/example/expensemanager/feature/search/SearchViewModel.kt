package com.example.expensemanager.feature.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.AppDatabase
import com.example.expensemanager.data.entity.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SearchTransactionItem(
  val id: Long,
  val categoryName: String,
  val amount: Long,
  val note: String?,
  val date: LocalDate,
  val isIncome: Boolean
)

data class SearchResultGroup(
  val date: LocalDate,
  val transactions: List<SearchTransactionItem>
)

data class SearchUiState(
  val isLoading: Boolean = false,
  val searchResults: List<SearchResultGroup> = emptyList()
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {
  private val transactionDao = AppDatabase.getDatabase(application).transactionDao()
  private val categoryDao = AppDatabase.getDatabase(application).categoryDao()
  
  private val _uiState = MutableStateFlow(SearchUiState())
  val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

  init {
    loadAllTransactions()
  }

  private fun loadAllTransactions() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true)
      
      try {
        combine(
          transactionDao.getAllTransactions(),
          categoryDao.getAllCategories()
        ) { transactions, categories ->
          val categoryMap = categories.associateBy { it.id }
          
          transactions
            .map { transaction ->
              SearchTransactionItem(
                id = transaction.id,
                categoryName = categoryMap[transaction.categoryId]?.name ?: "Unknown",
                amount = transaction.amount,
                note = transaction.note,
                date = transaction.date,
                isIncome = transaction.type == TransactionType.INCOME
              )
            }
            .groupBy { it.date }
            .map { (date, items) ->
              SearchResultGroup(date = date, transactions = items)
            }
            .sortedByDescending { it.date }
        }.collect { grouped ->
          _uiState.value = _uiState.value.copy(
            isLoading = false,
            searchResults = grouped
          )
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(isLoading = false)
      }
    }
  }

  fun search(query: String, filterType: String) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true)
      
      try {
        val now = LocalDate.now()
        
        combine(
          transactionDao.getAllTransactions(),
          categoryDao.getAllCategories()
        ) { transactions, categories ->
          val categoryMap = categories.associateBy { it.id }
          
          transactions
            .filter { transaction ->
              // Filter by date range
              val matchesDateFilter = when (filterType) {
                "month" -> transaction.date.year == now.year && transaction.date.month == now.month
                "year" -> transaction.date.year == now.year
                else -> true
              }
              
              // Filter by search query
              val matchesQuery = if (query.isBlank()) {
                true
              } else {
                val searchLower = query.lowercase()
                val categoryName = categoryMap[transaction.categoryId]?.name?.lowercase() ?: ""
                categoryName.contains(searchLower) || transaction.note.lowercase().contains(searchLower)
              }
              
              matchesDateFilter && matchesQuery
            }
            .map { transaction ->
              SearchTransactionItem(
                id = transaction.id,
                categoryName = categoryMap[transaction.categoryId]?.name ?: "Unknown",
                amount = transaction.amount,
                note = transaction.note,
                date = transaction.date,
                isIncome = transaction.type == TransactionType.INCOME
              )
            }
            .groupBy { it.date }
            .map { (date, items) ->
              SearchResultGroup(date = date, transactions = items)
            }
            .sortedByDescending { it.date }
        }.collect { filtered ->
          _uiState.value = _uiState.value.copy(
            isLoading = false,
            searchResults = filtered
          )
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(isLoading = false)
      }
    }
  }
}