package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FinancialDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = TerminalDarkBg,
    primaryContainer = TerminalSurfaceHighlight,
    onPrimaryContainer = ElectricCyan,
    secondary = BrightGold,
    onSecondary = TerminalDarkBg,
    secondaryContainer = TerminalSurfaceVariant,
    onSecondaryContainer = BrightGold,
    tertiary = MarketGreen,
    onTertiary = TerminalDarkBg,
    background = TerminalDarkBg,
    onBackground = TextPrimary,
    surface = TerminalSurface,
    onSurface = TextPrimary,
    surfaceVariant = TerminalSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = TerminalBorder,
    error = MarketRed,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Financial terminals default to professional high-density dark mode
    val colorScheme = FinancialDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = TerminalDarkBg.toArgb()
                window.navigationBarColor = TerminalDarkBg.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
