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

// CompositionLocal for custom colors
val LocalBokiColors = staticCompositionLocalOf { DarkBokiColors }

// Helper extensions for easy access
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

// Usage Examples and Helper Functions
object BokiColorUtils {
    /**
     * Get pot color by index (cycles through available colors)
     */
    @Composable
    fun getPotColors(index: Int): List<androidx.compose.ui.graphics.Color> {
        return BokiTheme.colors.potColors[index % BokiTheme.colors.potColors.size]
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
}

/* 
=== USAGE EXAMPLES ===

@Composable
fun MyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BokiTheme.gradient) // Use gradient background
    ) {
        Text(
            text = "Account Balance",
            style = BokiTheme.typography.headlineMedium,
            color = BokiTheme.colors.onBackground
        )
        
        Text(
            text = "1000 KWD",
            style = BokiTheme.typography.balanceDisplay,
            color = BokiTheme.colors.onBackground
        )
        
        Card(
            shape = BokiTheme.shapes.card,
            colors = CardDefaults.cardColors(
                containerColor = BokiTheme.colors.cardBackground
            )
        ) {
            // Card content
        }
    }
}

@Composable
fun PotCard(pot: PotSummaryDto, index: Int) {
    Card(
        modifier = Modifier.background(BokiTheme.potGradient(index)),
        shape = BokiTheme.shapes.card
    ) {
        Text(
            text = pot.name,
            style = BokiTheme.typography.cardLabel,
            color = BokiColorUtils.getTextColorForBackground(true)
        )
    }
}
*/