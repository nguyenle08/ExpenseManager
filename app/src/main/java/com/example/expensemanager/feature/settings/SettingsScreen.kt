package com.example.expensemanager.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as? android.app.Application
        ?: throw IllegalStateException("Application context is required")

    val viewModel: SettingsViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.language == "vi") "Cài đặt" else "Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Theme Color
            item {
                SettingItem(
                    title = if (uiState.language == "vi") "Màu giao diện" else "Theme Color",
                    subtitle = getThemeColorName(uiState.themeColor, uiState.language),
                    onClick = { showThemeDialog = true },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Color(android.graphics.Color.parseColor(uiState.themeColor)),
                                    CircleShape
                                )
                        )
                    }
                )
            }
            
            // Language
            item {
                SettingItem(
                    title = if (uiState.language == "vi") "Ngôn ngữ" else "Language",
                    subtitle = getLanguageDisplay(uiState.language),
                    onClick = { showLanguageDialog = true }
                )
            }
            
            // Currency
            item {
                SettingItem(
                    title = if (uiState.language == "vi") "Đơn vị tiền tệ" else "Currency",
                    subtitle = getCurrencyDisplay(uiState.currency, uiState.language),
                    onClick = { showCurrencyDialog = true }
                )
            }
            
            // Dark Mode
            item {
                SettingItemWithSwitch(
                    title = if (uiState.language == "vi") "Chế độ tối" else "Dark Mode",
                    subtitle = if (uiState.isDarkMode) {
                        if (uiState.language == "vi") "Bật" else "On"
                    } else {
                        if (uiState.language == "vi") "Tắt" else "Off"
                    },
                    checked = uiState.isDarkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
            }
            
            // Test Notification Button
            item {
                SettingItem(
                    title = if (uiState.language == "vi") "Test Thông báo" else "Test Notification",
                    subtitle = if (uiState.language == "vi") "Xem thử notification" else "Preview notification",
                    onClick = { 
                        com.example.expensemanager.util.NotificationHelper.showReminderNotification(
                            context = context,
                            title = "Nhắc nhở chi tiêu 💸",
                            message = "Đây là notification test! Click để mở app."
                        )
                    }
                )
            }
            
            // Test Content Provider Button
            item {
                SettingItem(
                    title = if (uiState.language == "vi") "Test Content Provider" else "Test Content Provider",
                    subtitle = if (uiState.language == "vi") "Query dữ liệu từ provider" else "Query data from provider",
                    onClick = { 
                        try {
                            val uri = android.net.Uri.parse("content://com.example.expensemanager.provider/transactions")
                            val cursor = context.contentResolver.query(uri, null, null, null, null)
                            val count = cursor?.count ?: 0
                            cursor?.close()
                            
                            com.example.expensemanager.util.NotificationHelper.showReminderNotification(
                                context = context,
                                title = "Content Provider Test",
                                message = "Tìm thấy $count transactions qua Content Provider!"
                            )
                        } catch (e: Exception) {
                            com.example.expensemanager.util.NotificationHelper.showReminderNotification(
                                context = context,
                                title = "Content Provider Error",
                                message = "Lỗi: ${e.message}"
                            )
                        }
                    }
                )
            }
        }
    }
    
    // Theme Dialog
    if (showThemeDialog) {
        ThemePickerDialog(
            currentTheme = uiState.themeColor,
            language = uiState.language,
            onThemeSelected = { 
                viewModel.setThemeColor(it)
            },
            onDismiss = { showThemeDialog = false }
        )
    }
    
    // Language Dialog
    if (showLanguageDialog) {
        LanguagePickerDialog(
            currentLanguage = uiState.language,
            onLanguageSelected = { 
                viewModel.setLanguage(it)
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
    
    // Currency Dialog
    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            currentCurrency = uiState.currency,
            language = uiState.language,
            onCurrencySelected = { 
                viewModel.setCurrency(it)
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            icon?.invoke()
        }
    }
}

@Composable
private fun SettingItemWithSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun ThemePickerDialog(
    currentTheme: String,
    language: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val themes = listOf(
        com.example.expensemanager.data.preferences.SettingsPreferences.THEME_PURPLE to (if (language == "vi") "Màu tím" else "Purple"),
        com.example.expensemanager.data.preferences.SettingsPreferences.THEME_BLUE to (if (language == "vi") "Màu xanh" else "Blue")
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (language == "vi") "Chọn màu giao diện" else "Select Theme Color") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themes.forEach { (color, name) ->
                    val itemInteractionSource = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    onThemeSelected(color)
                                    onDismiss()
                                },
                                indication = null,
                                interactionSource = itemInteractionSource
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color(android.graphics.Color.parseColor(color)),
                                        CircleShape
                                    )
                            )
                            Text(text = name)
                        }
                        
                        if (color == currentTheme) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (language == "vi") "Đóng" else "Close")
            }
        }
    )
}

@Composable
private fun LanguagePickerDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        "vi" to "Tiếng Việt 🇻🇳",
        "en" to "English 🇺🇸"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (currentLanguage == "vi") "Chọn ngôn ngữ" else "Select Language") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                languages.forEach { (code, name) ->
                    val itemInteractionSource = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    onLanguageSelected(code)
                                    onDismiss()
                                },
                                indication = null,
                                interactionSource = itemInteractionSource
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = name)
                        
                        if (code == currentLanguage) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (currentLanguage == "vi") "Đóng" else "Close")
            }
        }
    )
}

@Composable
private fun CurrencyPickerDialog(
    currentCurrency: String,
    language: String,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val currencies = listOf(
        "VND" to "VND (₫)",
        "USD" to "USD ($)"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (language == "vi") "Chọn đơn vị tiền tệ" else "Select Currency") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currencies.forEach { (code, name) ->
                    val itemInteractionSource = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    onCurrencySelected(code)
                                    onDismiss()
                                },
                                indication = null,
                                interactionSource = itemInteractionSource
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = name)
                        
                        if (code == currentCurrency) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (language == "vi") "Đóng" else "Close")
            }
        }
    )
}

private fun getThemeColorName(color: String, language: String): String {
    return when (color) {
        com.example.expensemanager.data.preferences.SettingsPreferences.THEME_PURPLE -> if (language == "vi") "Màu tím" else "Purple"
        com.example.expensemanager.data.preferences.SettingsPreferences.THEME_BLUE -> if (language == "vi") "Màu xanh" else "Blue"
        else -> if (language == "vi") "Màu tím" else "Purple"
    }
}

private fun getLanguageDisplay(language: String): String {
    return when (language) {
        "vi" -> "Tiếng Việt 🇻🇳"
        "en" -> "English 🇺🇸"
        else -> "Tiếng Việt 🇻🇳"
    }
}

private fun getCurrencyDisplay(currency: String, language: String): String {
    return when (currency) {
        "VND" -> if (language == "vi") "VND (₫)" else "VND (₫)"
        "USD" -> if (language == "vi") "USD ($)" else "USD ($)"
        else -> "VND (₫)"
    }
}