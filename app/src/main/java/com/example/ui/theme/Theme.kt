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
    themeColorName: String = "EMERALD",
    content: @Composable () -> Unit
) {
    val (primaryColor, primaryDark, primaryLight) = when (themeColorName.uppercase()) {
        "NAVY" -> Triple(KhataNavyPrimary, KhataNavyDark, KhataNavyLight)
        "MAROON" -> Triple(KhataMaroonPrimary, KhataMaroonDark, KhataMaroonLight)
        "PURPLE" -> Triple(KhataPurplePrimary, KhataPurpleDark, KhataPurpleLight)
        else -> Triple(KhataGreenPrimary, KhataGreenDark, KhataGreenLight)
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryLight,
            secondary = KhataAmberLight,
            tertiary = AdvanceBlue,
            background = Color(0xFF0F172A),
            surface = Color(0xFF1E293B),
            onPrimary = primaryDark,
            onSecondary = Color.Black,
            onBackground = Color(0xFFF8FAFC),
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF334155),
            onSurfaceVariant = Color(0xFFCBD5E1),
            outline = Color(0xFF475569)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryLight,
            onPrimaryContainer = primaryDark,
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
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = primaryColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
