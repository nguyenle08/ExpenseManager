package com.example.expensemanager.provider

import android.net.Uri

/**
 * Contract class định nghĩa URI và columns cho TransactionContentProvider
 */
object TransactionContract {
    
    const val AUTHORITY = "com.example.expensemanager.provider"
    
    const val PATH_TRANSACTIONS = "transactions"
    
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_TRANSACTIONS")
    
    // Column names (match với TransactionEntity)
    object Columns {
        const val ID = "id"
        const val AMOUNT = "amount"
        const val TYPE = "type"
        const val CATEGORY_ID = "categoryId"
        const val NOTE = "note"
        const val DATE = "date"
        const val CREATED_AT = "createdAt"
    }
}
