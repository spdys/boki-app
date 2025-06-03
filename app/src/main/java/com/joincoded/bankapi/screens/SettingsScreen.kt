package com.joincoded.bankapi.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.utils.SharedPreferencesManager
import com.joincoded.bankapi.viewmodel.BankViewModel

data class SettingsItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val hasSwitch: Boolean = false,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun SettingsScreen(
    bankViewModel: BankViewModel,
    navController: NavHostController,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(true) }

    val userName = SharedPreferencesManager.getSavedUserName(context)
    val greeting = bankViewModel.getGreeting()

    val settingsItems = remember {
        listOf(
            SettingsItem(
                id = "profile",
                title = "Profile Settings",
                description = "Manage your account information",
                icon = Icons.Default.Person,
                onClick = { /* Navigate to profile */ }
            ),
            SettingsItem(
                id = "security",
                title = "Security & Privacy",
                description = "Password, biometric, and privacy settings",
                icon = Icons.Default.Security,
                onClick = { /* Navigate to security */ }
            ),
            SettingsItem(
                id = "notifications",
                title = "Notifications",
                description = "Push notifications and alerts",
                icon = Icons.Default.Notifications,
                hasSwitch = true,
                onClick = { notificationsEnabled = !notificationsEnabled }
            ),
            SettingsItem(
                id = "biometric",
                title = "Biometric Login",
                description = "Use fingerprint or face ID",
                icon = Icons.Default.Fingerprint,
                hasSwitch = true,
                onClick = { biometricEnabled = !biometricEnabled }
            ),
            SettingsItem(
                id = "language",
                title = "Language & Region",
                description = "App language and currency",
                icon = Icons.Default.Language,
                onClick = { /* Navigate to language */ }
            ),
            SettingsItem(
                id = "help",
                title = "Help & Support",
                description = "FAQs, contact us, and tutorials",
                icon = Icons.Default.Help,
                onClick = { /* Navigate to help */ }
            ),
            SettingsItem(
                id = "about",
                title = "About Boki",
                description = "App version and legal information",
                icon = Icons.Default.Info,
                onClick = { /* Navigate to about */ }
            ),
            SettingsItem(
                id = "logout",
                title = "Logout",
                description = "Sign out of your account",
                icon = Icons.Default.Logout,
                isDestructive = true,
                onClick = { showLogoutDialog = true }
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BokiTheme.gradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            SettingsHeader(
                userName = userName,
                greeting = greeting,
                onBackClick = { navController.popBackStack() }
            )

            // Settings List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(settingsItems) { item ->
                    SettingsCard(
                        item = item,
                        switchState = when (item.id) {
                            "notifications" -> notificationsEnabled
                            "biometric" -> biometricEnabled
                            else -> false
                        }
                    )
                }
            }
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            LogoutDialog(
                onConfirm = {
                    bankViewModel.logout()
                    onLogout()
                    showLogoutDialog = false
                },
                onDismiss = { showLogoutDialog = false }
            )
        }
    }
}

@Composable
private fun SettingsHeader(
    userName: String,
    greeting: String,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .padding(top = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                if (userName.isNotEmpty()) {
                    Text(
                        text = "$greeting, ${userName.split(" ").firstOrNull() ?: userName}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    item: SettingsItem,
    switchState: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isDestructive) {
                Color.Red.copy(alpha = 0.1f)
            } else {
                Color.White.copy(alpha = 0.15f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (item.isDestructive) {
                            Color.Red.copy(alpha = 0.2f)
                        } else {
                            Color.White.copy(alpha = 0.2f)
                        },
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = if (item.isDestructive) Color.Red else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    color = if (item.isDestructive) Color.Red else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    color = if (item.isDestructive) {
                        Color.Red.copy(alpha = 0.8f)
                    } else {
                        Color.White.copy(alpha = 0.8f)
                    },
                    fontSize = 14.sp
                )
            }

            // Switch or Arrow
            if (item.hasSwitch) {
                Switch(
                    checked = switchState,
                    onCheckedChange = { item.onClick() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.Green.copy(alpha = 0.8f),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.4f)
                    )
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    tint = if (item.isDestructive) {
                        Color.Red.copy(alpha = 0.6f)
                    } else {
                        Color.White.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Logout",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("Are you sure you want to logout?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color.Black
    )
}