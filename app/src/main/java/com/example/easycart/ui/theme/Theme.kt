package com.example.easycart.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueDark,
    onPrimaryContainer = Color.White,
    secondary = GreenPrimary,
    onSecondary = Color.White,
    background = BackgroundLight,
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E)
)

private val DarkColors = darkColorScheme(
    primary = BluePrimary,
    secondary = GreenPrimary
)

@Composable
fun EasyCartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
