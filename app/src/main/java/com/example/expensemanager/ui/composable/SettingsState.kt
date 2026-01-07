package com.example.expensemanager.ui.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.expensemanager.data.preferences.SettingsPreferences

@Composable
fun rememberSettings(): SettingsState {
    val context = LocalContext.current
    val settingsPreferences = remember { SettingsPreferences.getInstance(context) }
    
    val themeColor by settingsPreferences.themeColor.collectAsState(initial = SettingsPreferences.THEME_PURPLE)
    val language by settingsPreferences.language.collectAsState(initial = SettingsPreferences.LANGUAGE_VI)
    val currency by settingsPreferences.currency.collectAsState(initial = SettingsPreferences.CURRENCY_VND)
    
    return SettingsState(
        themeColor = themeColor,
        language = language,
        currency = currency,
        context = context
    )
}

data class SettingsState(
    val themeColor: String,
    val language: String,
    val currency: String,
    val context: android.content.Context
)