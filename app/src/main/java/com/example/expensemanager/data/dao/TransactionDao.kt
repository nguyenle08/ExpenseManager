package com.example.expensemanager.data.dao

import androidx.room.*
import com.example.expensemanager.data.entity.TransactionEntity
import com.example.expensemanager.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * DAO cho Transaction
 */
@Dao
interface TransactionDao {
    
    /**
     * Thêm giao dịch mới
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long
    
    /**
     * Cập nhật giao dịch
     */
    @Update
    suspend fun update(transaction: TransactionEntity)
    
    /**
     * Xóa giao dịch
     */
    @Delete
    suspend fun delete(transaction: TransactionEntity)
    
    /**
     * Lấy tất cả giao dịch (reactive với Flow)
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    
    /**
     * Lấy giao dịch theo tháng
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE date >= :startDate AND date <= :endDate 
        ORDER BY date DESC, createdAt DESC
    """)
    fun getTransactionsByMonth(startDate: LocalDate, endDate: LocalDate): Flow<List<TransactionEntity>>
    
    /**
     * Lấy giao dịch theo ID
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?
    
    /**
     * Tổng thu trong tháng
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = 'INCOME' AND date >= :startDate AND date <= :endDate
    """)
    fun getTotalIncomeByMonth(startDate: LocalDate, endDate: LocalDate): Flow<Long>
    
    /**
     * Tổng chi trong tháng
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = 'EXPENSE' AND date >= :startDate AND date <= :endDate
    """)
    fun getTotalExpenseByMonth(startDate: LocalDate, endDate: LocalDate): Flow<Long>
    
    /**
     * Lấy giao dịch theo ngày (cho biểu đồ)
     */
    @Query("""
        SELECT date, 
               SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as totalIncome,
               SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as totalExpense
        FROM transactions 
        WHERE date >= :startDate AND date <= :endDate
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getDailyTransactionsByMonth(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyTransaction>>
    
    /**
     * Xóa tất cả giao dịch (cho testing)
     */
    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

/**
 * Data class cho giao dịch theo ngày (dùng cho biểu đồ)
 */
data class DailyTransaction(
    val date: LocalDate,
    val totalIncome: Long,
    val totalExpense: Long
)
