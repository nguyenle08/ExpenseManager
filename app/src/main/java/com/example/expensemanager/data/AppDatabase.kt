package com.example.expensemanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensemanager.data.dao.CategoryDao
import com.example.expensemanager.data.dao.TransactionDao
import com.example.expensemanager.data.entity.CategoryEntity
import com.example.expensemanager.data.entity.TransactionEntity
import com.example.expensemanager.data.entity.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room Database chính của ứng dụng
 */
@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_manager_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Callback để insert dữ liệu mặc định khi tạo database lần đầu
         */
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.categoryDao())
                    }
                }
            }
        }
        
        /**
         * Insert danh mục mặc định
         */
        private suspend fun populateDatabase(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                // Chi tiêu
                CategoryEntity(name = "Ăn uống", type = TransactionType.EXPENSE, icon = "🍜", color = "#FF5722", isDefault = true),
                CategoryEntity(name = "Mua sắm", type = TransactionType.EXPENSE, icon = "🛒", color = "#E91E63", isDefault = true),
                CategoryEntity(name = "Hóa đơn", type = TransactionType.EXPENSE, icon = "💡", color = "#9C27B0", isDefault = true),
                CategoryEntity(name = "Đi lại", type = TransactionType.EXPENSE, icon = "🚗", color = "#3F51B5", isDefault = true),
                CategoryEntity(name = "Giải trí", type = TransactionType.EXPENSE, icon = "🎮", color = "#2196F3", isDefault = true),
                CategoryEntity(name = "Y tế", type = TransactionType.EXPENSE, icon = "💊", color = "#00BCD4", isDefault = true),
                CategoryEntity(name = "Giáo dục", type = TransactionType.EXPENSE, icon = "📚", color = "#009688", isDefault = true),
                CategoryEntity(name = "Quần áo", type = TransactionType.EXPENSE, icon = "👕", color = "#795548", isDefault = true),
                CategoryEntity(name = "Khác", type = TransactionType.EXPENSE, icon = "📦", color = "#607D8B", isDefault = true),
                
                // Thu nhập
                CategoryEntity(name = "Lương", type = TransactionType.INCOME, icon = "💰", color = "#4CAF50", isDefault = true),
                CategoryEntity(name = "Thưởng", type = TransactionType.INCOME, icon = "🎁", color = "#8BC34A", isDefault = true),
                CategoryEntity(name = "Đầu tư", type = TransactionType.INCOME, icon = "📈", color = "#CDDC39", isDefault = true),
                CategoryEntity(name = "Bán hàng", type = TransactionType.INCOME, icon = "🏪", color = "#FFC107", isDefault = true),
                CategoryEntity(name = "Thu nhập khác", type = TransactionType.INCOME, icon = "💵", color = "#FF9800", isDefault = true)
            )
            
            categoryDao.insertAll(defaultCategories)
        }
    }
}
