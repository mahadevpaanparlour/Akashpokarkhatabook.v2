package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = KhataGreenLight,
    secondary = KhataAmberLight,
    tertiary = AdvanceBlue,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = KhataGreenDark,
    onSecondary = Color.Black,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

private val LightColorScheme = lightColorScheme(
    primary = KhataGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = KhataGreenLight,
    onPrimaryContainer = KhataGreenDark,
    secondary = KhataAmber,
    onSecondary = Color.White,
    secondaryContainer = KhataAmberLight,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = AdvanceBlue,
    background = KhataBg,
    surface = KhataCardSurface,
    onBackground = KhataTextPrimary,
    onSurface = KhataTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = KhataTextSecondary,
    outline = KhataCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Keep consistent merchant green theme
    content: @Composable () -> Unit
) {
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
