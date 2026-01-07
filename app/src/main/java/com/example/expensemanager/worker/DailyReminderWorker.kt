package com.example.expensemanager.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.expensemanager.data.AppDatabase
import com.example.expensemanager.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

/**
 * Worker để gửi notification nhắc nhở hàng ngày
 */
class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val transactionDao = database.transactionDao()
            
            // Kiểm tra xem hôm nay đã có giao dịch chưa
            val today = LocalDate.now()
            val todayTransactions = transactionDao.getTransactionsByMonthOnce(
                today,
                today
            )
            
            // Lấy tổng chi tiêu hôm qua
            val yesterday = today.minusDays(1)
            val yesterdayTransactions = transactionDao.getTransactionsByMonthOnce(
                yesterday,
                yesterday
            )
            
            val yesterdayExpense = yesterdayTransactions
                .filter { it.type == com.example.expensemanager.data.entity.TransactionType.EXPENSE }
                .sumOf { it.amount }
            
            // Tạo message
            val message = if (todayTransactions.isEmpty()) {
                if (yesterdayExpense > 0) {
                    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
                    "Hôm qua bạn chi ${formatter.format(yesterdayExpense)} ₫. Hãy ghi chép chi tiêu hôm nay nhé! 💰"
                } else {
                    "Bạn chưa ghi chép chi tiêu hôm nay. Hãy cập nhật ngay! 📝"
                }
            } else {
                "Hôm nay bạn đã có ${todayTransactions.size} giao dịch. Đừng quên cập nhật đầy đủ nhé! ✅"
            }
            
            // Show notification
            NotificationHelper.showReminderNotification(
                context = applicationContext,
                title = "Nhắc nhở chi tiêu 💸",
                message = message
            )
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
