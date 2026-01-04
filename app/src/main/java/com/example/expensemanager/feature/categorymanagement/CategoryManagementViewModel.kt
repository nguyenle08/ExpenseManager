package com.example.expensemanager.feature.categorymanagement

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.AppDatabase
import com.example.expensemanager.data.entity.CategoryEntity
import com.example.expensemanager.data.entity.TransactionType
import com.example.expensemanager.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình quản lý danh mục
 */
class CategoryManagementViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: CategoryRepository
    
    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = CategoryRepository(database.categoryDao())
        loadCategories()
        observeCategoryCounts()
    }
    
    /**
     * Load danh sách categories theo loại hiện tại
     */
    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategoriesByType(_uiState.value.selectedType).collect { categories ->
                _uiState.update { it.copy(
                    categories = categories,
                    isLoading = false
                ) }
            }
        }
    }

    /**
     * Quan sát tổng số danh mục Thu / Chi để hiển thị trên tab
     */
    private fun observeCategoryCounts() {
        viewModelScope.launch {
            repository.getAllCategories().collect { list ->
                val expenseCount = list.count { it.type == TransactionType.EXPENSE }
                val incomeCount = list.count { it.type == TransactionType.INCOME }
                _uiState.update { state ->
                    state.copy(
                        expenseCount = expenseCount,
                        incomeCount = incomeCount
                    )
                }
            }
        }
    }
    
    /**
     * Chuyển đổi giữa tab Chi tiêu và Thu nhập
     */
    fun onTabChanged(type: TransactionType) {
        _uiState.update { it.copy(
            selectedType = type,
            isLoading = true
        ) }
        loadCategories()
    }
    
    /**
     * Chuẩn bị state cho thêm danh mục mới (màn hình AddEditCategory)
     */
    fun prepareForNewCategory() {
        _uiState.update { state ->
            state.copy(
                editingCategory = null,
                dialogName = "",
                dialogIcon = getRandomIcon(),
                dialogColor = getRandomColor()
            )
        }
    }

    /**
     * Load dữ liệu danh mục để sửa theo id (màn hình AddEditCategory)
     */
    fun loadCategoryForEdit(id: Long) {
        viewModelScope.launch {
            val category = repository.getCategoryById(id) ?: return@launch
            _uiState.update { state ->
                state.copy(
                    selectedType = category.type,
                    editingCategory = category,
                    dialogName = category.name,
                    dialogIcon = category.icon,
                    dialogColor = category.color
                )
            }
        }
    }
    
    /**
     * Đóng dialog
     */
    fun onDialogDismiss() {
        _uiState.update { it.copy(
            showDialog = false,
            editingCategory = null,
            dialogName = "",
            dialogIcon = "💰",
            dialogColor = "#4CAF50",
            errorMessage = null
        ) }
    }
    
    /**
     * Cập nhật tên trong dialog
     */
    fun onDialogNameChanged(name: String) {
        _uiState.update { it.copy(dialogName = name) }
    }
    
    /**
     * Cập nhật icon trong dialog
     */
    fun onDialogIconChanged(icon: String) {
        _uiState.update { it.copy(dialogIcon = icon) }
    }
    
    /**
     * Cập nhật màu sắc trong dialog
     */
    fun onDialogColorChanged(color: String) {
        _uiState.update { it.copy(dialogColor = color) }
    }
    
    /**
     * Lưu category (thêm mới hoặc cập nhật)
     */
    fun onSaveCategory(name: String) {
        viewModelScope.launch {
            val state = _uiState.value
            
            // Validate
            if (name.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Vui lòng nhập tên danh mục") }
                return@launch
            }
            
            try {
                if (state.editingCategory != null) {
                    // Cập nhật category
                    val updatedCategory = state.editingCategory.copy(
                        name = name.trim(),
                        icon = state.dialogIcon,
                        color = state.dialogColor
                    )
                    repository.updateCategory(updatedCategory)
                } else {
                    // Thêm category mới
                    val newCategory = CategoryEntity(
                        name = name.trim(),
                        type = state.selectedType,
                        icon = state.dialogIcon,
                        color = state.dialogColor,
                        isDefault = false
                    )
                    repository.insertCategory(newCategory)
                }
                
                // Đóng dialog
                onDialogDismiss()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Lỗi khi lưu danh mục: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * Xóa category
     */
    fun onDeleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            try {
                if (category.isDefault) {
                    _uiState.update { it.copy(
                        errorMessage = "Không thể xóa danh mục mặc định"
                    ) }
                    return@launch
                }
                
                repository.deleteCategory(category)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Lỗi khi xóa danh mục: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * Xóa thông báo lỗi
     */
    fun onErrorDismiss() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Danh sách icon gợi ý
     */
    private fun getRandomIcon(): String {
        val icons = listOf(
            "💰", "🍜", "🛒", "💡", "🚗", "🎮", "💊", "📚", 
            "👕", "📦", "🎁", "📈", "🏪", "💵", "🏠", "✈️",
            "☕", "🎵", "🎬", "⚽", "🎨", "📱", "💻", "🍕",
            "🍔", "🍰", "🎂", "🍺", "🍷", "🚌", "🚕", "🚲"
        )
        return icons.random()
    }
    
    /**
     * Danh sách màu sắc gợi ý
     */
    private fun getRandomColor(): String {
        val colors = listOf(
            "#FF5722", "#E91E63", "#9C27B0", "#3F51B5", "#2196F3",
            "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
            "#FFC107", "#FF9800", "#795548", "#607D8B"
        )
        return colors.random()
    }
    
    /**
     * Danh sách icon có sẵn để chọn
     */
    val availableIcons = listOf(
        "💰", "🍜", "🛒", "💡", "🚗", "🎮", "💊", "📚",
        "👕", "📦", "🎁", "📈", "🏪", "💵", "🏠", "✈️",
        "☕", "🎵", "🎬", "⚽", "🎨", "📱", "💻", "🍕",
        "🍔", "🍰", "🎂", "🍺", "🍷", "🚌", "🚕", "🚲"
    )
    
    /**
     * Danh sách màu sắc có sẵn để chọn
     */
    val availableColors = listOf(
        "#FF5722", "#E91E63", "#9C27B0", "#3F51B5",
        "#2196F3", "#00BCD4", "#009688", "#4CAF50",
        "#8BC34A", "#CDDC39", "#FFC107", "#FF9800",
        "#795548", "#607D8B"
    )
}

/**
 * UI State cho CategoryManagement
 */
data class CategoryManagementUiState(
    val isLoading: Boolean = true,
    val categories: List<CategoryEntity> = emptyList(),
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val expenseCount: Int = 0,
    val incomeCount: Int = 0,
    val showDialog: Boolean = false,
    val editingCategory: CategoryEntity? = null,
    val dialogName: String = "",
    val dialogIcon: String = "💰",
    val dialogColor: String = "#4CAF50",
    val errorMessage: String? = null
)
