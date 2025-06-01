// Color.kt
package com.joincoded.bankapi.ui.theme

import androidx.compose.ui.graphics.Color

// Base Brand Colors
object BokiColors {
    // Primary Colors
    val PrimaryDark = Color(0xFF0D1B2A)
    val PrimaryLight = Color(0xFFFFFFFF)

    // Background Colors
    val BackgroundDark = Color(0xFF1B263B)
    val BackgroundLight = Color(0xFFF5F5F5)

    // Accent Colors
    val AccentRed = Color(0xFFEF233C)
    val SoftGray = Color(0xFF6D829B)

    // Text Colors
    val TextDark = Color.White
    val TextLight = Color(0xFF0D1B2A)

    // Gradient Colors (from your existing screens)
    val GradientStart = Color(0xFF0A0D34)
    val GradientEnd = Color(0xFF141C58)

    // Additional Banking Theme Colors
    val Success = Color(0xFF38A169)
    val Warning = Color(0xFFD69E2E)
    val Error = Color(0xFFE53E3E)
    val Info = Color(0xFF3182CE)

    // Pot Colors (for visual distinction)
    val PotRed = listOf(Color(0xFFE53E3E), Color(0xFFC53030))
    val PotGreen = listOf(Color(0xFF38A169), Color(0xFF2F855A))
    val PotBlue = listOf(Color(0xFF3182CE), Color(0xFF2C5282))
    val PotOrange = listOf(Color(0xFFD69E2E), Color(0xFFB7791F))
    val PotPurple = listOf(Color(0xFF805AD5), Color(0xFF6B46C1))
    val PotTeal = listOf(Color(0xFF319795), Color(0xFF2C7A7B))

    // Card Colors
    val CardRed = listOf(Color(0xFF8C1515), Color(0xFFB02828))
    val CardTransparentDark = Color.White.copy(alpha = 0.1f)
    val CardTransparentLight = Color.Black.copy(alpha = 0.05f)
}

// Extended Color Scheme
data class BokiColorScheme(
    // Material 3 Colors
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val error: Color,
    val onError: Color,

    // Custom Banking Colors
    val gradient: List<Color>,
    val cardBackground: Color,
    val cardAccent: List<Color>,
    val textSecondary: Color,
    val success: Color,
    val warning: Color,
    val info: Color,
    val divider: Color,
    val potColors: List<List<Color>>
)

// Dark Theme Colors
val DarkBokiColors = BokiColorScheme(
    // Material 3
    primary = BokiColors.PrimaryDark,
    onPrimary = BokiColors.TextDark,
    secondary = BokiColors.AccentRed,
    onSecondary = BokiColors.TextDark,
    background = BokiColors.BackgroundDark,
    onBackground = BokiColors.TextDark,
    surface = BokiColors.BackgroundDark,
    onSurface = BokiColors.TextDark,
    error = BokiColors.Error,
    onError = BokiColors.TextDark,

    // Custom
    gradient = listOf(BokiColors.GradientStart, BokiColors.GradientEnd),
    cardBackground = BokiColors.CardTransparentDark,
    cardAccent = BokiColors.CardRed,
    textSecondary = BokiColors.SoftGray,
    success = BokiColors.Success,
    warning = BokiColors.Warning,
    info = BokiColors.Info,
    divider = Color.White.copy(alpha = 0.1f),
    potColors = listOf(
        BokiColors.PotRed,
        BokiColors.PotGreen,
        BokiColors.PotBlue,
        BokiColors.PotOrange,
        BokiColors.PotPurple,
        BokiColors.PotTeal
    )
)

// Light Theme Colors
val LightBokiColors = BokiColorScheme(
    // Material 3
    primary = BokiColors.PrimaryLight,
    onPrimary = BokiColors.TextLight,
    secondary = BokiColors.AccentRed,
    onSecondary = BokiColors.TextDark,
    background = BokiColors.BackgroundLight,
    onBackground = BokiColors.TextLight,
    surface = BokiColors.PrimaryLight,
    onSurface = BokiColors.TextLight,
    error = BokiColors.Error,
    onError = BokiColors.TextDark,

    // Custom
    gradient = listOf(BokiColors.PrimaryLight, BokiColors.BackgroundLight),
    cardBackground = BokiColors.CardTransparentLight,
    cardAccent = BokiColors.CardRed,
    textSecondary = BokiColors.SoftGray,
    success = BokiColors.Success,
    warning = BokiColors.Warning,
    info = BokiColors.Info,
    divider = Color.Black.copy(alpha = 0.1f),
    potColors = listOf(
        BokiColors.PotRed,
        BokiColors.PotGreen,
        BokiColors.PotBlue,
        BokiColors.PotOrange,
        BokiColors.PotPurple,
        BokiColors.PotTeal
    )
)