package com.joincoded.bankapi.screens

import androidx.activity.compose.BackHandler
import com.joincoded.bankapi.data.AccountSummaryDto
import com.joincoded.bankapi.data.AccountType
import com.joincoded.bankapi.data.AllocationType
import com.joincoded.bankapi.data.PotSummaryDto
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.joincoded.bankapi.components.TransactionBottomSheet
import com.joincoded.bankapi.components.TransactionSource
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSummaryScreen(viewModel: BankViewModel) {
    val account = viewModel.selectedAccount
    if (account == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BokiTheme.gradient),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(24.dp),
                shape = BokiTheme.shapes.card,
                colors = CardDefaults.cardColors(
                    containerColor = BokiTheme.colors.cardBackground
                )
            ) {
                Text(
                    text = "No account selected",
                    color = BokiTheme.colors.onBackground,
                    style = BokiTheme.typography.bodyLarge,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
        return
    }

    val showBottomSheet = remember { mutableStateOf(true) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { newValue ->
            newValue != SheetValue.Hidden // prevent hiding
        }
    )

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    LaunchedEffect(isLoggedIn) {
        viewModel.getAccountTransactionHistory()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BokiTheme.gradient)
    ) {
        // Main Account Summary Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 32.dp)
        ) {
            // Header
            Text(
                text = "Account Summary",
                color = BokiTheme.colors.onBackground,
                style = BokiTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Account Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 15.dp,
                        shape = BokiTheme.shapes.card,
                        ambientColor = BokiTheme.colors.secondary.copy(alpha = 0.2f)
                    )
                    .padding(bottom = 24.dp),
                shape = BokiTheme.shapes.card,
                colors = CardDefaults.cardColors(
                    containerColor = BokiTheme.colors.cardBackground
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Account Number
                    AccountDetailRow(
                        label = "Account Number",
                        value = account.accountNumber
                    )

                    // Account Type
                    AccountDetailRow(
                        label = "Account Type",
                        value = account.accountType.name.replace("_", " ")
                    )

                    // Balance
                    AccountDetailRow(
                        label = "Balance",
                        value = "${account.currency} ${account.balance.setScale(3)}",
                        isBalance = true
                    )

                    // Card Number (if available)
                    account.cardNumber?.let { cardNumber ->
                        AccountDetailRow(
                            label = "Card Number",
                            value = "**** **** **** ${cardNumber.takeLast(4)}"
                        )
                    }
                }
            }

            // Pots Section
            if (!account.pots.isNullOrEmpty()) {
                Text(
                    text = "Savings Pots",
                    color = BokiTheme.colors.onBackground,
                    style = BokiTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f), // Allow scrolling without interference
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(account.pots.indices.toList()) { index ->
                        PotCard(
                            pot = account.pots[index],
                            currency = account.currency,
                            index = index
                        )
                    }

                    // Add bottom padding to account for transaction overlay
                    item {
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            } else {
                // Empty state for pots
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = BokiTheme.shapes.card,
                    colors = CardDefaults.cardColors(
                        containerColor = BokiTheme.colors.cardBackground
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No savings pots available",
                            color = BokiTheme.colors.textSecondary,
                            style = BokiTheme.typography.bodyMedium
                        )
                    }
                }

                // Add spacer to push content up when no pots
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Transaction Bottom Sheet
        if (showBottomSheet.value) {
            TransactionBottomSheet(
                viewModel = viewModel,
                onDismiss = {
                    showBottomSheet.value = false
                },
                transactionSource = TransactionSource.ACCOUNT
            )
        }
    }
}

@Composable
private fun AccountDetailRow(
    label: String,
    value: String,
    isBalance: Boolean = false
) {
    Column(
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Text(
            text = label,
            color = BokiTheme.colors.textSecondary,
            style = BokiTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = BokiTheme.colors.onBackground,
            style = if (isBalance) BokiTheme.typography.transactionAmount
            else BokiTheme.typography.bodyMedium
        )
    }
}

@Composable
fun PotCard(
    pot: PotSummaryDto,
    currency: String,
    index: Int,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val cardHeight by animateDpAsState(
        targetValue = if (isExpanded) 140.dp else 90.dp,
        animationSpec = tween(300),
        label = "potCardHeight"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .shadow(
                elevation = 12.dp,
                shape = BokiTheme.shapes.card,
                ambientColor = BokiTheme.colors.secondary.copy(alpha = 0.15f)
            ),
        shape = BokiTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = BokiTheme.colors.cardBackground
        ),
        onClick = { isExpanded = !isExpanded }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BokiTheme.potGradient(index))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pot.name,
                        color = BokiTheme.colors.onBackground,
                        style = BokiTheme.typography.titleBold,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "$currency ${pot.balance.setScale(3)}",
                        color = BokiTheme.colors.onBackground,
                        style = BokiTheme.typography.transactionAmount
                    )
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Allocation Info
                    val allocationText = when (pot.allocationType) {
                        AllocationType.PERCENTAGE -> "Percentage: ${(pot.allocationValue * BigDecimal(100)).setScale(0)}%"
                        AllocationType.FIXED -> "Fixed: ${pot.allocationValue.stripTrailingZeros().toPlainString()} $currency"
                    }
                    Text(
                        text = allocationText,
                        color = BokiTheme.colors.textSecondary,
                        style = BokiTheme.typography.bodyMedium
                    )

                    // Card Token Info
                    pot.cardToken?.let { token ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Card: **** **** **** ${token.takeLast(4)}",
                            color = BokiTheme.colors.textSecondary,
                            style = BokiTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}