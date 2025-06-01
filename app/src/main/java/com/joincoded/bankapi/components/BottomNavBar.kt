package com.joincoded.bankapi.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.ui.theme.BankAPITheme
import com.joincoded.bankapi.utils.Routes

@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
                ambientColor = BokiTheme.colors.secondary.copy(alpha = 0.3f),
                spotColor = BokiTheme.colors.secondary.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
        colors = CardDefaults.cardColors(
            containerColor = BokiTheme.colors.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            BokiTheme.colors.surface,
                            BokiTheme.colors.surface.copy(alpha = 0.98f),
                            BokiTheme.colors.surface
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            NavBarItem(
                icon = if (currentRoute == Routes.homeRoute) Icons.Filled.Home else Icons.Outlined.Home,
                label = "Home",
                isSelected = currentRoute == Routes.homeRoute,
                onClick = {
                    navController.navigate(Routes.homeRoute) {
                        popUpTo(Routes.homeRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            // Quick Pay - Prominent round button
            ProminentPayButton(
                isSelected = currentRoute == Routes.quickPayRoute,
                onClick = {
                    navController.navigate(Routes.quickPayRoute) {
                        popUpTo(Routes.homeRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            // Services
            NavBarItem(
                icon = if (currentRoute == Routes.servicesRoute) Icons.Filled.MiscellaneousServices else Icons.Outlined.MiscellaneousServices,
                label = "Services",
                isSelected = currentRoute == Routes.servicesRoute,
                onClick = {
                    navController.navigate(Routes.servicesRoute) {
                        popUpTo(Routes.homeRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // Progress indicator bar at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(BokiTheme.colors.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                BokiTheme.colors.secondary.copy(alpha = 0.3f),
                                BokiTheme.colors.secondary,
                                BokiTheme.colors.secondary.copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(300, easing = EaseOutBack),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) BokiTheme.colors.secondary else BokiTheme.colors.textSecondary,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = BokiTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) BokiTheme.colors.secondary else BokiTheme.colors.textSecondary
        )

        // Selection indicator
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(width = 24.dp, height = 3.dp)
                    .background(
                        BokiTheme.colors.secondary,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
private fun ProminentPayButton(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = tween(300, easing = EaseOutBack),
        label = "scale"
    )

    val animatedElevation by animateDpAsState(
        targetValue = if (isSelected) 12.dp else 8.dp,
        animationSpec = tween(300),
        label = "elevation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        }
    ) {
        Card(
            modifier = Modifier
                .size(64.dp)
                .clickable(onClick = onClick),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) BokiTheme.colors.secondary else BokiTheme.colors.secondary.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = animatedElevation
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (isSelected) {
                            Brush.radialGradient(
                                colors = listOf(
                                    BokiTheme.colors.secondary,
                                    BokiTheme.colors.secondary.copy(alpha = 0.8f)
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    BokiTheme.colors.secondary.copy(alpha = 0.9f),
                                    BokiTheme.colors.secondary.copy(alpha = 0.7f)
                                )
                            )
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CreditCard,
                    contentDescription = "Quick Pay",
                    tint = BokiTheme.colors.onPrimary,
                    modifier = Modifier.size(if (isSelected) 32.dp else 28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Quick Pay",
            style = BokiTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) BokiTheme.colors.secondary else BokiTheme.colors.textSecondary
        )

        // Wave effect for selected state
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            WaveIndicator()
        }
    }
}

@Composable
private fun WaveIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveScale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveAlpha"
    )

    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(
                BokiTheme.colors.secondary.copy(alpha = alpha)
            )
            .graphicsLayer {
                scaleX = scale
            }
    )
}

// Mock Routes object for preview
object Routes {
    const val homeRoute = "home"
    const val quickPayRoute = "quick_pay"
    const val servicesRoute = "services"
}

@Preview(showBackground = true, backgroundColor = 0xFF1B263B)
@Composable
fun BokiBottomNavigationPreview() {
    BankAPITheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(BokiTheme.gradient)
                .padding(16.dp)
        ) {
            BottomNavBar(
                navController = rememberNavController()
            )
        }
    }
}