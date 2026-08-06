// ui/theme/Theme.kt
package com.pip.cheeseroul.ui.theme

import android.app.Activity
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

@Composable
fun CheeseRoulTheme(
    darkTheme: Boolean = false, // ВСЕГДА FALSE (Игнорируем систему)
    content: @Composable () -> Unit
) {
    // Жестко задаем светлую палитру
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}