package com.example.easycart.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 💡 Importamos colores desde Color.kt (ya no se vuelven a crear aquí)

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,

    secondary = PurpleAccent,
    onSecondary = Color.White,

    background = BackgroundLight,
    onBackground = TextPrimaryDark,

    surface = Color.White,
    onSurface = TextPrimaryDark,

    error = RedPrimary,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,

    secondary = PurpleAccent,
    onSecondary = Color.White,

    background = BackgroundDark,
    onBackground = Color.White,

    surface = Color(0xFF1A1A1A),
    onSurface = Color.White,

    error = RedPrimary,
    onError = Color.White
)

@Composable
fun EasyCartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
