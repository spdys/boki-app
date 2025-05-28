package com.joincoded.bankapi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BokiPrimaryDark,
    onPrimary = BokiTextDark,
    secondary = BokiAccentRed,
    background = BokiBackgroundDark,
    surface = BokiBackgroundDark,
    onBackground = BokiTextDark,
    onSurface = BokiTextDark
)

private val LightColorScheme = lightColorScheme(
    primary = BokiPrimaryLight,
    onPrimary = BokiTextLight,
    secondary = BokiAccentRed,
    background = BokiBackgroundLight,
    surface = BokiPrimaryLight,
    onBackground = BokiTextLight,
    onSurface = BokiTextLight
)

@Composable
fun BankAPITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
