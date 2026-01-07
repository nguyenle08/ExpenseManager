package com.example.expensemanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.expensemanager.data.preferences.SettingsPreferences
import com.example.expensemanager.ui.theme.ExpenseManagerTheme
import com.example.expensemanager.util.NotificationHelper
import com.example.expensemanager.worker.DailyReminderWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    
    // Request notification permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, notification will work
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Setup notification channel
        NotificationHelper.createNotificationChannel(this)
        
        // Request notification permission for Android 13+
        requestNotificationPermission()
        
        // Schedule daily reminder worker
        scheduleDailyReminder()
        
        val settingsPreferences = SettingsPreferences.getInstance(this)
        
        setContent {
            val themeColor by settingsPreferences.themeColor.collectAsState(initial = SettingsPreferences.THEME_PURPLE)
            val isDarkMode by settingsPreferences.isDarkMode.collectAsState(initial = false)
            
            ExpenseManagerTheme(
                themeColor = themeColor,
                darkTheme = isDarkMode
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ExpenseManagerNavHost()
                }
            }
        }
    }
    
    /**
     * Request notification permission for Android 13+
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    /**
     * Schedule daily reminder notification
     */
    private fun scheduleDailyReminder() {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            15, TimeUnit.MINUTES  // TEST: 15 phút (đổi thành DAYS khi release)
        ).build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }
}