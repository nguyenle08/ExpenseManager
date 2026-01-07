package com.example.expensemanager.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.expensemanager.MainActivity
import com.example.expensemanager.R

/**
 * Helper class để tạo và hiển thị notification
 */
object NotificationHelper {
    
    private const val CHANNEL_ID = "expense_reminder_channel"
    private const val CHANNEL_NAME = "Nhắc nhở chi tiêu"
    private const val NOTIFICATION_ID = 1001
    
    /**
     * Tạo notification channel (cần cho Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Nhắc nhở ghi chép chi tiêu hàng ngày"
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Hiển thị notification nhắc nhở
     */
    fun showReminderNotification(
        context: Context,
        title: String,
        message: String
    ) {
        createNotificationChannel(context)
        
        // Intent để mở app khi click notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Show notification
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            Log.d("NotificationHelper", "Notification shown successfully")
        } catch (e: SecurityException) {
            // Permission not granted, ignore
            Log.e("NotificationHelper", "Permission denied: ${e.message}")
        }
    }
}
