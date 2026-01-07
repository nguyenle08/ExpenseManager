package com.example.expensemanager.util

import android.content.Context
import com.example.expensemanager.data.preferences.SettingsPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object LocaleManager {
    
    fun getString(context: Context, key: String): String {
        val settings = SettingsPreferences.getInstance(context)
        // Read from Flow synchronously
        val language = runBlocking { settings.language.first() }
        
        return when (key) {
            // Home Screen
            "home_title" -> if (language == "vi") "Trang chủ" else "Home"
            "month" -> if (language == "vi") "Tháng" else "Month"
            "total_expense" -> if (language == "vi") "Tổng chi" else "Total Expense"
            "total_income" -> if (language == "vi") "Tổng thu" else "Total Income"
            "balance" -> if (language == "vi") "Còn lại" else "Balance"
            "recent_transactions" -> if (language == "vi") "Giao dịch gần đây" else "Recent Transactions"
            "expense" -> if (language == "vi") "Chi tiêu" else "Expense"
            "income" -> if (language == "vi") "Thu nhập" else "Income"
            "balance" -> if (language == "vi") "Số dư tháng này" else "Balance This Month"
            
            // Transaction Detail
            "transaction_history" -> if (language == "vi") "Sổ giao dịch" else "Transaction History"
            "all" -> if (language == "vi") "Tất cả" else "All"
            "date" -> if (language == "vi") "Ngày" else "Date"
            "amount" -> if (language == "vi") "Số tiền" else "Amount"
            "category" -> if (language == "vi") "Danh mục" else "Category"
            "note" -> if (language == "vi") "Ghi chú" else "Note"
            
            // Add Transaction
            "add_transaction" -> if (language == "vi") "Thêm" else "Add"
            "add_transaction_title" -> if (language == "vi") "Thêm giao dịch" else "Add Transaction"
            "edit_transaction" -> if (language == "vi") "Sửa giao dịch" else "Edit Transaction"
            "save" -> if (language == "vi") "Lưu" else "Save"
            "cancel" -> if (language == "vi") "Hủy" else "Cancel"
            "select_category" -> if (language == "vi") "Chọn danh mục" else "Select Category"
            "enter_amount" -> if (language == "vi") "Nhập số tiền" else "Enter Amount"
            "enter_note" -> if (language == "vi") "Nhập ghi chú" else "Enter Note"
            
            // Category Management
            "category_management" -> if (language == "vi") "Danh mục" else "Categories"
            "add_category" -> if (language == "vi") "Thêm danh mục" else "Add Category"
            "edit_category" -> if (language == "vi") "Sửa danh mục" else "Edit Category"
            "delete_category" -> if (language == "vi") "Xóa danh mục" else "Delete Category"
            
            // Report
            "report" -> if (language == "vi") "Biểu đồ" else "Report"
            "chart" -> if (language == "vi") "Biểu đồ" else "Chart"
            "statistics" -> if (language == "vi") "Thống kê" else "Statistics"
            
            // Settings
            "settings" -> if (language == "vi") "Cài đặt" else "Settings"
            "theme_color" -> if (language == "vi") "Màu giao diện" else "Theme Color"
            "language" -> if (language == "vi") "Ngôn ngữ" else "Language"
            "currency" -> if (language == "vi") "Đơn vị tiền tệ" else "Currency"
            
            // Bottom Navigation
            "home" -> if (language == "vi") "Trang chủ" else "Home"
            "categories" -> if (language == "vi") "Danh mục" else "Categories"
            "transactions" -> if (language == "vi") "Giao dịch" else "Transactions"
            "profile" -> if (language == "vi") "Cá nhân" else "Profile"
            
            // Common
            "search" -> if (language == "vi") "Tìm kiếm" else "Search"
            "search_placeholder" -> if (language == "vi") "Tìm theo #nhân, nhóm, v.v..." else "Search by #person, group, etc..."
            "search_by_month" -> if (language == "vi") "Theo tháng" else "By Month"
            "search_by_year" -> if (language == "vi") "Theo năm" else "By Year"
            "this_month" -> if (language == "vi") "Tháng này" else "This Month"
            "this_year" -> if (language == "vi") "Năm này" else "This Year"
            "select_month" -> if (language == "vi") "Chọn tháng" else "Select Month"
            "year" -> if (language == "vi") "Năm" else "Year"
            "no_results" -> if (language == "vi") "Không có kết quả" else "No results"
            "try_different_search" -> if (language == "vi") "Không có giao dịch liên quan đến từ khóa này" else "No transactions related to this keyword"
            "delete" -> if (language == "vi") "Xóa" else "Delete"
            "edit" -> if (language == "vi") "Sửa" else "Edit"
            "confirm" -> if (language == "vi") "Xác nhận" else "Confirm"
            "back" -> if (language == "vi") "Quay lại" else "Back"
            "close" -> if (language == "vi") "Đóng" else "Close"
            
            else -> key
        }
    }
}