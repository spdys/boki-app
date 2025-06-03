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
        Text("No account selected")
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

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(account.pots) { pot ->
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
            } else {
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

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        if (showBottomSheet.value) {
            TransactionBottomSheet(
                viewModel = viewModel,
                sheetState = bottomSheetState,
                onDismiss = {
                    showBottomSheet.value = false
                },
                transactionSource = TransactionSource.ACCOUNT
            )
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

//@Preview(showBackground = true)
//@Composable
//fun AccountSummaryScreenPreview() {
//    val samplePots = listOf(
//        PotSummaryDto(
//            potId = 1L,
//            name = "Emergency Fund",
//            balance = BigDecimal("2500.00"),
//            cardToken = "1234567890123456",
//            allocationType = AllocationType.PERCENTAGE,
//            allocationValue = BigDecimal("15.0")
//        ),
//        PotSummaryDto(
//            potId = 2L,
//            name = "Vacation Savings",
//            balance = BigDecimal("1200.50"),
//            cardToken = null,
//            allocationType = AllocationType.FIXED,
//            allocationValue = BigDecimal("200.00")
//        ),
//        PotSummaryDto(
//            potId = 3L,
//            name = "New Car Fund",
//            balance = BigDecimal("850.75"),
//            cardToken = "9876543210987654",
//            allocationType = AllocationType.PERCENTAGE,
//            allocationValue = BigDecimal("10.0")
//        )
//    )
//
//    val sampleAccount = AccountSummaryDto(
//        accountId = 1L,
//        accountNumber = "ACC123456789",
//        accountType = AccountType.SAVINGS,
//        balance = BigDecimal("15750.25"),
//        cardNumber = "1234567890123456",
//        currency = "USD",
//        isActive = true,
//        pots = samplePots
//    )

//    val sampleTransactions = listOf(
//        TransactionHistoryResponse(
//            id = 1L,
//            amount = BigDecimal("250.000"),
//            transactionType = "DEPOSIT",
//            description = "Salary Payment",
//            createdAt = LocalDateTime.now().minusHours(2)
//        ),
//        TransactionHistoryResponse(
//            id = 2L,
//            amount = BigDecimal("45.500"),
//            transactionType = "WITHDRAWAL",
//            description = "Coffee Shop",
//            createdAt = LocalDateTime.now().minusHours(5)
//        ),
//        TransactionHistoryResponse(
//            id = 3L,
//            amount = BigDecimal("12.750"),
//            transactionType = "DEBIT",
//            description = "Grocery Store",
//            createdAt = LocalDateTime.now().minusDays(1)
//        ),
//        TransactionHistoryResponse(
//            id = 4L,
//            amount = BigDecimal("100.000"),
//            transactionType = "TRANSFER",
//            description = "Transfer to Savings",
//            createdAt = LocalDateTime.now().minusDays(2)
//        )
//    )