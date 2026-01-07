package com.example.expensemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.expensemanager.data.preferences.SettingsPreferences
import com.example.expensemanager.ui.theme.ExpenseManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val settingsPreferences = SettingsPreferences.getInstance(this)
        
        setContent {
            val themeColor by settingsPreferences.themeColor.collectAsState(initial = SettingsPreferences.THEME_PURPLE)
            
            ExpenseManagerTheme(themeColor = themeColor) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ExpenseManagerNavHost()
                }
            }
        }
    }
}