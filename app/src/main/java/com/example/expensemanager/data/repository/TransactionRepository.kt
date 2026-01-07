package com.example.expensemanager.data.repository

import com.example.expensemanager.data.dao.TransactionDao
import com.example.expensemanager.data.entity.TransactionEntity
import com.example.expensemanager.feature.home.DayData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth

/**
 * Repository để truy cập dữ liệu Transaction
 */
//🎯 Vai trò
  //Trung gian giữa ViewModel ↔ DAO
  //Gom logic truy vấn
class TransactionRepository(private val transactionDao: TransactionDao) {

  /**
   * Lấy tất cả giao dịch
   */
  fun getAllTransactions(): Flow<List<TransactionEntity>> {
    return transactionDao.getAllTransactions()
  }

  /**
   * Lấy giao dịch theo tháng
   */
  fun getTransactionsByMonth(month: LocalDate): Flow<List<TransactionEntity>> {
    val yearMonth = YearMonth.from(month)
    val startDate = yearMonth.atDay(1)
    val endDate = yearMonth.atEndOfMonth()
    return transactionDao.getTransactionsByMonth(startDate, endDate)
  }

  /**
   * Lấy danh sách giao dịch trong 1 tháng một lần (suspend), dùng cho màn chi tiết.
   */
  suspend fun getTransactionsByMonthOnce(month: LocalDate): List<TransactionEntity> {
    val startDate = month.withDayOfMonth(1)
    val endDate = month.withDayOfMonth(month.lengthOfMonth())
    return transactionDao.getTransactionsByMonthOnce(startDate, endDate)
  }

  /**
   * Lấy tổng thu trong tháng
   */
  fun getTotalIncomeByMonth(month: LocalDate): Flow<Long> {
    val yearMonth = YearMonth.from(month)
    val startDate = yearMonth.atDay(1)
    val endDate = yearMonth.atEndOfMonth()
    return transactionDao.getTotalIncomeByMonth(startDate, endDate)
  }

  /**
   * Lấy tổng chi trong tháng
   */
  fun getTotalExpenseByMonth(month: LocalDate): Flow<Long> {
    val yearMonth = YearMonth.from(month)
    val startDate = yearMonth.atDay(1)
    val endDate = yearMonth.atEndOfMonth()
    return transactionDao.getTotalExpenseByMonth(startDate, endDate)
  }

  /**
   * Lấy số dư (thu - chi) trong tháng
   */
  fun getBalanceByMonth(month: LocalDate): Flow<Long> {
    return combine(
      getTotalIncomeByMonth(month),
      getTotalExpenseByMonth(month)
    ) { income, expense ->
      income - expense
    }
  }

  /**
   * Lấy dữ liệu biểu đồ theo ngày trong tháng
   */
  fun getChartDataByMonth(month: LocalDate): Flow<List<DayData>> {
    val yearMonth = YearMonth.from(month)
    val startDate = yearMonth.atDay(1)
    val endDate = yearMonth.atEndOfMonth()

    return transactionDao.getDailyTransactionsByMonth(startDate, endDate).map { dailyTransactions ->
      // Tạo danh sách đầy đủ các ngày trong tháng
      val daysInMonth = yearMonth.lengthOfMonth()
      val dailyMap = dailyTransactions.associateBy { it.date }

      (1..daysInMonth).map { day ->
        val date = LocalDate.of(month.year, month.month, day)
        val daily = dailyMap[date]
        DayData(
          date = date,
          income = daily?.totalIncome ?: 0L,
          expense = daily?.totalExpense ?: 0L
        )
      }
    }
  }

  /**
   * Thêm giao dịch mới
   */
  suspend fun insertTransaction(transaction: TransactionEntity): Long {
    return transactionDao.insert(transaction)
  }

  /**
   * Cập nhật giao dịch
   */
  suspend fun updateTransaction(transaction: TransactionEntity) {
    transactionDao.update(transaction)
  }

  /**
   * Xóa giao dịch
   */
  suspend fun deleteTransaction(transaction: TransactionEntity) {
    transactionDao.delete(transaction)
  }

  /**
   * Lấy giao dịch theo ID
   */
  suspend fun getTransactionById(id: Long): TransactionEntity? {
    return transactionDao.getTransactionById(id)
  }
}
