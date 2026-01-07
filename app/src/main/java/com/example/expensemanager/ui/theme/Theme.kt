package com.example.expensemanager.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light Theme Colors
private val LightPrimary = Color(0xFF6200EE)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightSecondary = Color(0xFF03DAC6)
private val LightOnSecondary = Color(0xFF000000)
private val LightBackground = Color(0xFFF5F5F5)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnBackground = Color(0xFF1C1B1F)
private val LightOnSurface = Color(0xFF1C1B1F)

// Dark Theme Colors
private val DarkPrimary = Color(0xFFBB86FC)
private val DarkOnPrimary = Color(0xFF000000)
private val DarkSecondary = Color(0xFF03DAC6)
private val DarkOnSecondary = Color(0xFF000000)
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val DarkOnBackground = Color(0xFFE1E1E1)
private val DarkOnSurface = Color(0xFFE1E1E1)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface
)

@Composable
fun ExpenseManagerTheme(
    themeColor: String = "#6200EE",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val primaryColor = try {
        Color(android.graphics.Color.parseColor(themeColor))
    } catch (e: Exception) {
        Color(0xFF6200EE)
    }
    
    val colorScheme = if (darkTheme) {
        DarkColorScheme.copy(primary = primaryColor)
    } else {
        LightColorScheme.copy(primary = primaryColor)
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}