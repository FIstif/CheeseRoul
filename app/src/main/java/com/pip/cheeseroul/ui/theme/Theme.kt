// ui/theme/Theme.kt
package com.pip.cheeseroul.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CheeseYellow,
    onPrimary = CheeseBrown,
    secondary = CheeseOrange,
    onSecondary = CheeseSurface,
    background = CheeseBackground,
    onBackground = CheeseBrown,
    surface = CheeseSurface,
    onSurface = CheeseBrown,
    surfaceVariant = CheeseCardBg,
    onSurfaceVariant = CheeseBrown
)

private val DarkColorScheme = darkColorScheme(
    primary = CheeseYellow,
    onPrimary = CheeseBrown,
    secondary = CheeseOrange,
    onSecondary = CheeseSurface,
    background = CheeseBrown,
    onBackground = CheeseBackground,
    surface = CheeseBrown,
    onSurface = CheeseBackground,
    surfaceVariant = CheeseOrange,
    onSurfaceVariant = CheeseBackground
)

@Composable
fun CheeseRoulTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Используем нашу тему (при желании можно принудительно выставить LightColorScheme)
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}