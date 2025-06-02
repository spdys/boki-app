// Theme.kt
package com.joincoded.bankapi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

// Custom Shape System
object BokiShapes {
    val extraSmall = RoundedCornerShape(4.dp)
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large = RoundedCornerShape(16.dp)
    val extraLarge = RoundedCornerShape(20.dp)
    val circle = RoundedCornerShape(50)

    // Banking specific shapes
    val card = RoundedCornerShape(16.dp)
    val button = RoundedCornerShape(12.dp)
    val bottomSheet = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val dialog = RoundedCornerShape(20.dp)
}

// Material 3 Shapes
val BokiMaterial3Shapes = Shapes(
    extraSmall = BokiShapes.extraSmall,
    small = BokiShapes.small,
    medium = BokiShapes.medium,
    large = BokiShapes.large,
    extraLarge = BokiShapes.extraLarge
)


val LocalBokiColors = staticCompositionLocalOf { DarkBokiColors }


object BokiTheme {
    val colors: BokiColorScheme
        @Composable
        get() = LocalBokiColors.current

    val typography = BokiTypography
    val shapes = BokiShapes

    // Convenience properties
    val gradient: Brush
        @Composable
        get() = Brush.verticalGradient(colors.gradient)

    val cardGradient: Brush
        @Composable
        get() = Brush.horizontalGradient(colors.cardAccent)


    @Composable
    fun potGradient(index: Int): Brush {
        val potColors = colors.potColors[index % colors.potColors.size]
        return Brush.verticalGradient(potColors)
    }

    // Quick Pay card gradient function
    @Composable
    fun quickPayCardGradient(index: Int): Brush {
        val cardColors = colors.quickPayCardColors[index % colors.quickPayCardColors.size]
        return Brush.horizontalGradient(cardColors)
    }


    @Composable
    fun getCardColors(index: Int): List<androidx.compose.ui.graphics.Color> {
        return colors.quickPayCardColors[index % colors.quickPayCardColors.size]
    }
}

@Composable
fun BankAPITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val bokiColors = if (darkTheme) DarkBokiColors else LightBokiColors

    val materialColors = if (darkTheme) {
        darkColorScheme(
            primary = bokiColors.primary,
            onPrimary = bokiColors.onPrimary,
            secondary = bokiColors.secondary,
            onSecondary = bokiColors.onSecondary,
            background = bokiColors.background,
            onBackground = bokiColors.onBackground,
            surface = bokiColors.surface,
            onSurface = bokiColors.onSurface,
            error = bokiColors.error,
            onError = bokiColors.onError
        )
    } else {
        lightColorScheme(
            primary = bokiColors.primary,
            onPrimary = bokiColors.onPrimary,
            secondary = bokiColors.secondary,
            onSecondary = bokiColors.onSecondary,
            background = bokiColors.background,
            onBackground = bokiColors.onBackground,
            surface = bokiColors.surface,
            onSurface = bokiColors.onSurface,
            error = bokiColors.error,
            onError = bokiColors.onError
        )
    }

    CompositionLocalProvider(LocalBokiColors provides bokiColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = BokiMaterial3Typography,
            shapes = BokiMaterial3Shapes,
            content = content
        )
    }
}

// Usage Examples and Helper Functions - UPDATED
object BokiColorUtils {
    /**
     * Get pot color by index (cycles through available sophisticated colors)
     */
    @Composable
    fun getPotColors(index: Int): List<androidx.compose.ui.graphics.Color> {
        return BokiTheme.colors.potColors[index % BokiTheme.colors.potColors.size]
    }

    /**
     * Get Quick Pay card colors by index
     */
    @Composable
    fun getQuickPayCardColors(index: Int): List<androidx.compose.ui.graphics.Color> {
        return BokiTheme.colors.quickPayCardColors[index % BokiTheme.colors.quickPayCardColors.size]
    }

    /**
     * Get status color based on transaction status
     */
    @Composable
    fun getStatusColor(isSuccess: Boolean): androidx.compose.ui.graphics.Color {
        return if (isSuccess) BokiTheme.colors.success else BokiTheme.colors.error
    }

    /**
     * Get appropriate text color for background
     */
    @Composable
    fun getTextColorForBackground(isDark: Boolean): androidx.compose.ui.graphics.Color {
        return if (isDark) BokiColors.TextDark else BokiColors.TextLight
    }

    /**
     * Get sophisticated color by name for user selection during pot creation
     */
    @Composable
    fun getSophisticatedColorByName(colorName: String): List<androidx.compose.ui.graphics.Color> {
        return when (colorName.lowercase()) {
            "slate" -> BokiColors.SophisticatedCardColors.Slate
            "stone" -> BokiColors.SophisticatedCardColors.Stone
            "zinc" -> BokiColors.SophisticatedCardColors.Zinc
            "neutral" -> BokiColors.SophisticatedCardColors.Neutral
            "warm" -> BokiColors.SophisticatedCardColors.WarmGray
            "cool" -> BokiColors.SophisticatedCardColors.CoolBlueGray
            else -> BokiColors.SophisticatedCardColors.Slate // Default
        }
    }
}

/*
=== UPDATED USAGE EXAMPLES ===

@Composable
fun QuickPayCardExample() {
    Card(
        modifier = Modifier.background(BokiTheme.quickPayCardGradient(0)),
        shape = BokiTheme.shapes.card
    ) {
        // Card content with sophisticated gradient
    }
}

@Composable
fun PotCardExample(pot: PotSummaryDto, index: Int) {
    val potColors = BokiColorUtils.getPotColors(index)

    Card(
        modifier = Modifier.background(
            brush = Brush.verticalGradient(potColors)
        ),
        shape = BokiTheme.shapes.card
    ) {
        Text(
            text = pot.name,
            style = BokiTheme.typography.cardLabel,
            color = BokiColorUtils.getTextColorForBackground(true)
        )
    }
}

@Composable
fun UserSelectableCardColorExample() {
    // When user creates a pot, they can pick from these sophisticated options
    val colorOptions = listOf("Slate", "Stone", "Zinc", "Neutral", "Warm", "Cool")
    val selectedColor = "Slate" // User's choice
    val cardColors = BokiColorUtils.getSophisticatedColorByName(selectedColor)

    Card(
        modifier = Modifier.background(Brush.horizontalGradient(cardColors))
    ) {
        // Pot with user-selected sophisticated color
    }
}
*/