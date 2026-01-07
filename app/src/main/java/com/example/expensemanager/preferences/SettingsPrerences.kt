package com.example.expensemanager.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property để tạo DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * SettingsPreferences - Quản lý cài đặt ứng dụng với DataStore
 * Lưu vào disk, persist khi restart app
 * Sử dụng Singleton pattern để đảm bảo tất cả màn hình chia sẻ cùng DataStore
 */
//DATASTORE
//🎯 Vai trò
    //Lưu cài đặt vĩnh viễn
    //Không mất khi restart app
class SettingsPreferences private constructor(private val context: Context) {
    
    // Flow để observe changes
    //👉 ViewModel observe trực tiếp
    //👉 Mỗi setting là Flow:
        //DataStore thay đổi → Flow emit giá trị mới
    val themeColor: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_COLOR_KEY] ?: THEME_PURPLE
    }
    
    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: LANGUAGE_VI
    }
    
    val currency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY_KEY] ?: CURRENCY_VND
    }
    
    // Save functions
    suspend fun setThemeColor(color: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_COLOR_KEY] = color
        }
    }
    
    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }
    
    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_KEY] = currency
        }
    }
    
    companion object {
        // Preference keys
        private val THEME_COLOR_KEY = stringPreferencesKey("theme_color")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val CURRENCY_KEY = stringPreferencesKey("currency")
        
        // Singleton instance
        @Volatile
        private var INSTANCE: SettingsPreferences? = null
        
        fun getInstance(context: Context): SettingsPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        // Convenience method để tương thích với code cũ
        operator fun invoke(context: Context): SettingsPreferences {
            return getInstance(context)
        }

        // Theme colors - chỉ có 2 màu: Tím và Xanh
        const val THEME_PURPLE = "#9C27B0"  // Màu tím (mặc định)
        const val THEME_BLUE = "#2196F3"     // Màu xanh
        
        // Languages
        const val LANGUAGE_VI = "vi"  // Tiếng Việt (mặc định)
        const val LANGUAGE_EN = "en"  // English
        
        // Currencies
        const val CURRENCY_VND = "VND"  // Việt Nam Đồng (mặc định)
        const val CURRENCY_USD = "USD"  // US Dollar
    }
}