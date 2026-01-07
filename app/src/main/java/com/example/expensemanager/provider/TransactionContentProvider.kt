package com.example.expensemanager.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.example.expensemanager.data.AppDatabase
import kotlinx.coroutines.runBlocking

/**
 * Content Provider để chia sẻ dữ liệu transaction với app khác
 */
class TransactionContentProvider : ContentProvider() {
    
    private lateinit var database: AppDatabase
    
    companion object {
        private const val TRANSACTIONS = 1
        private const val TRANSACTION_ID = 2
        
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(TransactionContract.AUTHORITY, TransactionContract.PATH_TRANSACTIONS, TRANSACTIONS)
            addURI(TransactionContract.AUTHORITY, "${TransactionContract.PATH_TRANSACTIONS}/#", TRANSACTION_ID)
        }
    }
    
    override fun onCreate(): Boolean {
        context?.let {
            database = AppDatabase.getDatabase(it)
            return true
        }
        return false
    }
    
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            TRANSACTIONS -> {
                // Query tất cả transactions
                val transactions = runBlocking {
                    database.transactionDao().getTransactionsByMonthOnce(
                        java.time.LocalDate.now().minusYears(10),
                        java.time.LocalDate.now()
                    )
                }
                
                // Tạo cursor
                val cursor = MatrixCursor(arrayOf(
                    TransactionContract.Columns.ID,
                    TransactionContract.Columns.AMOUNT,
                    TransactionContract.Columns.TYPE,
                    TransactionContract.Columns.CATEGORY_ID,
                    TransactionContract.Columns.NOTE,
                    TransactionContract.Columns.DATE,
                    TransactionContract.Columns.CREATED_AT
                ))
                
                transactions.forEach { transaction ->
                    cursor.addRow(arrayOf(
                        transaction.id,
                        transaction.amount,
                        transaction.type.name,
                        transaction.categoryId,
                        transaction.note ?: "",
                        transaction.date.toString(),
                        transaction.createdAt
                    ))
                }
                
                cursor
            }
            TRANSACTION_ID -> {
                // Query 1 transaction theo ID
                val id = uri.lastPathSegment?.toLongOrNull() ?: return null
                val transaction = runBlocking {
                    database.transactionDao().getTransactionById(id)
                }
                
                if (transaction == null) return null
                
                val cursor = MatrixCursor(arrayOf(
                    TransactionContract.Columns.ID,
                    TransactionContract.Columns.AMOUNT,
                    TransactionContract.Columns.TYPE,
                    TransactionContract.Columns.CATEGORY_ID,
                    TransactionContract.Columns.NOTE,
                    TransactionContract.Columns.DATE,
                    TransactionContract.Columns.CREATED_AT
                ))
                
                cursor.addRow(arrayOf(
                    transaction.id,
                    transaction.amount,
                    transaction.type.name,
                    transaction.categoryId,
                    transaction.note ?: "",
                    transaction.date.toString(),
                    transaction.createdAt
                ))
                
                cursor
            }
            else -> null
        }
    }
    
    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            TRANSACTIONS -> "vnd.android.cursor.dir/vnd.${TransactionContract.AUTHORITY}.${TransactionContract.PATH_TRANSACTIONS}"
            TRANSACTION_ID -> "vnd.android.cursor.item/vnd.${TransactionContract.AUTHORITY}.${TransactionContract.PATH_TRANSACTIONS}"
            else -> null
        }
    }
    
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // Read-only provider
        throw UnsupportedOperationException("Insert not supported")
    }
    
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        // Read-only provider
        throw UnsupportedOperationException("Delete not supported")
    }
    
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        // Read-only provider
        throw UnsupportedOperationException("Update not supported")
    }
}
