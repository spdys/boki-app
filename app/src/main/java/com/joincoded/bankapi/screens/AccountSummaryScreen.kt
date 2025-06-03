package com.joincoded.bankapi.screens


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
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.math.BigDecimal

@Composable
fun AccountSummaryScreen(viewModel: BankViewModel) {
    val account = viewModel.selectedAccount
    if (account == null) {
        Text("No account selected")
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BokiTheme.gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 32.dp)
        ) {
            Text(
                text = "Account Summary",
                color = BokiTheme.colors.onBackground,
                style = BokiTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

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
                        value = "${account.balance.setScale(3)} ${account.currency}",
                        isBalance = true
                    )

                    account.cardNumber?.let { cardNumber ->
                        AccountDetailRow(
                            label = "Card Number",
                            value = "**** **** **** ${cardNumber.takeLast(4)}"
                        )
                    }
                }
            }

            if (!account.pots.isNullOrEmpty()) {
                Text(
                    text = "Pots",
                    color = BokiTheme.colors.onBackground,
                    style = BokiTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = { /* TODO: Implement create pot logic */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BokiTheme.colors.secondary)
                ) {
                    Text("Create New Pot", color = BokiTheme.colors.onPrimary)
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(account.pots.sortedBy { it.name.lowercase() }) { pot ->
                        PotCard(
                            pot = pot,
                            currency = account.currency,
                            index = 0 // or assign index if needed
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            } else if (account.accountType == AccountType.MAIN) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = BokiTheme.shapes.card,
                    colors = CardDefaults.cardColors(
                        containerColor = BokiTheme.colors.cardBackground
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "You have not created any pots yet.",
                            color = BokiTheme.colors.textSecondary,
                            style = BokiTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { /* TODO: Implement create pot logic */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BokiTheme.colors.secondary)
                        ) {
                            Text("Create Your First Pot", color = BokiTheme.colors.onPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AccountDetailRow(
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
                        text = "${pot.balance.setScale(3)} $currency",
                        color = BokiTheme.colors.onBackground,
                        style = BokiTheme.typography.transactionAmount
                    )
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val allocationText = when (pot.allocationType) {
                        AllocationType.PERCENTAGE -> "Percentage: ${(pot.allocationValue * BigDecimal(100)).setScale(0)}%"
                        AllocationType.FIXED -> "Fixed: ${pot.allocationValue.stripTrailingZeros().toPlainString()} $currency"
                    }
                    Text(
                        text = allocationText,
                        color = BokiTheme.colors.textSecondary,
                        style = BokiTheme.typography.bodyMedium
                    )

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