package com.example.expensemanager.feature.report

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.AppDatabase
import com.example.expensemanager.data.entity.TransactionType
import com.example.expensemanager.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
//LOGIC CHI TIẾT
//Các bước:
    //Parse startDate, endDate
    //Lấy giao dịch trong khoảng
    //Lọc theo categoryId
    //Tính: total, count, avgPerTransaction, avgPerDay
    //Nhóm theo ngày (giảm dần)
    //Map sang UI model
class CategoryReportDetailViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val categoryRepository = CategoryRepository(database.categoryDao())
    
    private val _uiState = MutableStateFlow(CategoryReportDetailUiState())
    val uiState: StateFlow<CategoryReportDetailUiState> = _uiState.asStateFlow()
    
    fun loadData(categoryId: Long, startDateStr: String, endDateStr: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                //Parse ngày
                val startDate = LocalDate.parse(startDateStr, dateFormatter)
                val endDate = LocalDate.parse(endDateStr, dateFormatter)
                //Lấy giao dịch trong khoảng
                val transactions = database.transactionDao()
                    .getTransactionsByMonthOnce(startDate, endDate)
                    .filter { it.categoryId == categoryId }//Lọc theo categoryId
                
                val categories = categoryRepository.getAllCategoriesOnce()
                val categoryMap = categories.associateBy { it.id }
                
                // Tính thống kê
                val total = transactions.sumOf { it.amount }
                val count = transactions.size
                val avgPerTransaction = if (count > 0) total / count else 0L
                
                // Tính số ngày có giao dịch (không phải tổng số ngày trong khoảng)
                val daysWithTransactions = transactions.map { it.date }.distinct().size
                val avgPerDay = if (daysWithTransactions > 0) total / daysWithTransactions else 0L
                
                // Nhóm theo ngày(giảm dần)
                val grouped = transactions.groupBy { it.date }
                    .toSortedMap(compareByDescending { it })
                    .map { (date, txs) ->
                        val dateFormatter2 = DateTimeFormatter.ofPattern("'Th'M dd")
                        val totalAmount = txs.sumOf { it.amount }
                        
                        val items = txs.map { tx ->
                            val category = tx.categoryId?.let { categoryMap[it] }
                            val colorInt = try {
                                android.graphics.Color.parseColor(category?.color ?: "#607D8B")
                            } catch (e: Exception) {
                                android.graphics.Color.parseColor("#607D8B")
                            }
                            
                            TransactionItemDetailUi(
                                id = tx.id,
                                categoryName = category?.name ?: "Khác",
                                note = tx.note,
                                amount = tx.amount,
                                color = Color(colorInt),
                                icon = category?.icon
                            )
                        }
                        //Map sang UI model
                        TransactionDateGroupUi(
                            dateText = date.format(dateFormatter2),
                            totalAmount = totalAmount,
                            transactions = items
                        )
                    }
                
                _uiState.update {
                    it.copy(
                        total = total,
                        count = count,
                        avgPerTransaction = avgPerTransaction,
                        avgPerDay = avgPerDay,
                        transactionsByDate = grouped,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
}

data class CategoryReportDetailUiState(
    val total: Long = 0,
    val count: Int = 0,
    val avgPerTransaction: Long = 0,
    val avgPerDay: Long = 0,
    val transactionsByDate: List<TransactionDateGroupUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class TransactionDateGroupUi(
    val dateText: String,
    val totalAmount: Long,
    val transactions: List<TransactionItemDetailUi>
)

data class TransactionItemDetailUi(
    val id: Long,
    val categoryName: String,
    val note: String?,
    val amount: Long,
    val color: Color,
    val icon: String?
)