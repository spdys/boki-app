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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.joincoded.bankapi.components.BottomNavBar
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.viewmodel.BankViewModel

data class ServiceItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val icon: ImageVector,
    val route: String,
    val backgroundColor: Color? = null
)

@Composable
fun ServicesScreen(
    bankViewModel: BankViewModel,
    navController: NavHostController
) {
    val services = remember {
        listOf(
            ServiceItem(
                id = "transfer",
                title = "Transfer Money",
                description = "Send money between accounts",
                icon = Icons.Default.SwapHoriz,
                route = "transfer_money"
            ),
            ServiceItem(
                id = "add_account",
                title = "Add Account",
                description = "Create a new bank account",
                icon = Icons.Default.AccountBalance,
                route = "create_account"
            ),
            ServiceItem(
                id = "add_pot",
                title = "Add Pot",
                description = "Create a new savings pot",
                icon = Icons.Default.Savings,
                route = "create_pot"
            ),
            ServiceItem(
                id = "transactions",
                title = "Transactions",
                description = "View transaction history",
                icon = Icons.Default.Receipt,
                route = "transactions"
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
            ServicesHeader()

            // Services List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    top = 20.dp,
                    bottom = 100.dp
                )
            ) {
                items(services) { service ->
                    ServiceCard(
                        service = service,
                        onClick = {
                            navController.navigate(service.route)
                        }
                    )
                }
            }
        }

        // Bottom Navigation - MAIN SCREEN so it shows nav bar
        BottomNavBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ServicesHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .padding(top = 20.dp)
    ) {
        Text(
            text = "Services",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Manage your banking needs",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun ServiceCard(
    service: ServiceItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        service.backgroundColor ?: Color(0xFF6366F1).copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = service.title,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = service.title,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                service.description?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        color = Color.Black.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Arrow indicator
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Color.Black.copy(alpha = 0.05f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Navigate",
                    tint = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}