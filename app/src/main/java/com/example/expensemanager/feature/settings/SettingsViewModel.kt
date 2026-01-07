package com.example.expensemanager.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.preferences.SettingsPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settingsPreferences = SettingsPreferences.getInstance(application)
    
    // Observe settings from DataStore và tạo UiState
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            themeColor = SettingsPreferences.THEME_PURPLE,
            language = SettingsPreferences.LANGUAGE_VI,
            currency = SettingsPreferences.CURRENCY_VND
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    //VIEWMODEL ĐƯỢC TẠO → init {} CHẠY, TỰ LOAD DỮ LIỆU BAN ĐẦU
    init {
        // Load settings from DataStore
        viewModelScope.launch {
            //🔹 Kết nối DataStore👉 DataStore thay đổi → UI tự cập nhật
            combine(
                settingsPreferences.themeColor,
                settingsPreferences.language,
                settingsPreferences.currency
            ) { themeColor, language, currency ->
                SettingsUiState(
                    themeColor = themeColor,
                    language = language,
                    currency = currency
                )
                //👉 ViewModel:
                    //Nhận state mới
                    //Gán cho _uiState
                    //UI tự recompose
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
    
    fun setThemeColor(color: String) {
        viewModelScope.launch {
            settingsPreferences.setThemeColor(color)
        }
    }
    //🔹 Set giá trị
    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsPreferences.setLanguage(language)
        }
    }
    
    fun setCurrency(currency: String) {
        viewModelScope.launch {
            settingsPreferences.setCurrency(currency)
        }
    }
}

data class SettingsUiState(
    val themeColor: String = SettingsPreferences.THEME_PURPLE,
    val language: String = SettingsPreferences.LANGUAGE_VI,
    val currency: String = SettingsPreferences.CURRENCY_VND
)