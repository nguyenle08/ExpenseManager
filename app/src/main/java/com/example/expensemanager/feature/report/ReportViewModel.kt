package com.example.expensemanager.feature.report

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.AppDatabase
import com.example.expensemanager.data.entity.TransactionType
import com.example.expensemanager.data.repository.CategoryRepository
import com.example.expensemanager.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val transactionRepository = TransactionRepository(database.transactionDao())
    private val categoryRepository = CategoryRepository(database.categoryDao())
    
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()
    //ReportViewModel ĐƯỢC TẠO → init {} CHẠY
    init {
        loadData()
    }
    
    fun onTypeChanged(isIncome: Boolean) {
        _uiState.update { it.copy(isIncome = isIncome) }
        loadData()
    }
    
    fun onMonthChanged(month: LocalDate) {
        _uiState.update { it.copy(selectedMonth = month, isYearMode = false) }
        loadData()
    }
    
    fun onYearChanged(year: Int) {
        _uiState.update { 
            it.copy(
                selectedMonth = LocalDate.of(year, 1, 1),
                isYearMode = true
            ) 
        }
        loadData()
    }
    //LOGIC CHÍNH
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val state = _uiState.value
                val selectedMonth = state.selectedMonth
                
                // Xác định start date và end date
                val startDate: LocalDate
                val endDate: LocalDate
                //Xác định khoảng thời gian👉 Quyết định dữ liệu lấy theo Tháng hay Năm
                if (state.isYearMode) {
                    // Lọc theo năm: từ 1/1 đến 31/12
                    startDate = LocalDate.of(selectedMonth.year, 1, 1)
                    endDate = LocalDate.of(selectedMonth.year, 12, 31)
                } else {
                    // Lọc theo tháng
                    startDate = selectedMonth.withDayOfMonth(1)
                    endDate = selectedMonth.withDayOfMonth(selectedMonth.lengthOfMonth())
                }
                //Lấy dữ liệu từ Room👉 ViewModel trực tiếp gọi DAO / Repository
                val transactions = database.transactionDao().getTransactionsByMonthOnce(startDate, endDate)
                val categories = categoryRepository.getAllCategoriesOnce()
                
                val isIncome = _uiState.value.isIncome
                //Lọc theo loại(Chi / Thu)👉 Chỉ giữ Chi tiêu hoặc Thu nhập
                val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
                
                // Lọc giao dịch theo loại
                val filteredTxs = transactions.filter { it.type == type }
                
                // Nhóm theo category👉 Mỗi nhóm = 1 danh mục
                val categoryMap = categories.associateBy { it.id }
                val grouped = filteredTxs.groupBy { it.categoryId }
                
                // Tính tổng👉 Tổng chi / thu trong khoảng thời gian
                val total = filteredTxs.sumOf { it.amount }
                
                // Tạo stats
                val stats = grouped.map { (categoryId, txs) ->
                    val category = categoryId?.let { categoryMap[it] }
                    val amount = txs.sumOf { it.amount }//amount = sum(amount)
                    val percentage = if (total > 0) (amount.toFloat() / total.toFloat() * 100) else 0f//percentage = amount / total * 100
                    
                    val colorInt = try {
                        android.graphics.Color.parseColor(category?.color ?: "#607D8B")
                    } catch (e: Exception) {
                        android.graphics.Color.parseColor("#607D8B")
                    }
                    //Tạo CategoryStatUi (UI model)
                    CategoryStatUi(
                        id = category?.id ?: 0,
                        categoryId = categoryId,
                        name = category?.name ?: "Khác",
                        icon = category?.icon,
                        color = Color(colorInt),
                        amount = amount,
                        percentage = String.format("%.1f", percentage).toFloat(),//percentage = amount / total * 100
                        count = txs.size//số giao dịch
                    )
                }.sortedByDescending { it.amount }
                //Đổ vào UiState👉 Compose tự động cập nhật UI
                _uiState.update {
                    it.copy(
                        categoryStats = stats,
                        total = total,
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

//UI chỉ đọc, không sửa trực tiếp
data class ReportUiState(
    val selectedMonth: LocalDate = LocalDate.now(),
    val isIncome: Boolean = false,
    val isYearMode: Boolean = false,
    val categoryStats: List<CategoryStatUi> = emptyList(),
    val total: Long = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CategoryStatUi(
    val id: Long,
    val categoryId: Long,
    val name: String,
    val icon: String?,
    val color: Color,
    val amount: Long,
    val percentage: Float,
    val count: Int
)