package com.vahitkeskin.fencecalculator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.vahitkeskin.fencecalculator.ui.viewmodel.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6), // Professional Blue
    secondary = Color(0xFF8B5CF6), // Violet
    tertiary = Color(0xFFEC4899), // Pink
    background = Color(0xFF0F172A), // Premium Dark Blue-Grey
    surface = Color(0xFF1E293B), // Slightly lighter blue-grey
    onBackground = Color.White,
    onSurface = Color.White,
    onPrimary = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB), // Deeper Blue for light mode
    secondary = Color(0xFF7C3AED), // Deeper Violet
    tertiary = Color(0xFFDB2777), // Deeper Pink
    background = Color(0xFFF8FAFC), // Off-white/Slate background
    surface = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onPrimary = Color.White
)

@Composable
fun FenceCalculatorTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    dynamicColor: Boolean = false, // Disabled by default for premium branding
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}