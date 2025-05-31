package com.joincoded.bankapi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

// ---- Light & Dark Color Schemes ----
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

object BokiTypography {
    val titleBold = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold
    )

    val titleRegular = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal
    )

    val titleThin = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Light
    )
}

private val BaseTypography = Typography()

@Composable
fun BankAPITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = BaseTypography,
        shapes = Shapes(),
        content = content
    )
}