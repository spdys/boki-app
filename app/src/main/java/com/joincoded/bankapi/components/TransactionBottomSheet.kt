package com.joincoded.bankapi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joincoded.bankapi.data.TransactionHistoryResponse
import com.joincoded.bankapi.ui.theme.BankAPITheme
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionBottomSheet(
    viewModel: BankViewModel = viewModel(),
//    transactions: List<TransactionHistoryResponse>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var transactions =  viewModel
    val currency: String = "KWD"
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = BokiTheme.shapes.bottomSheet,
        containerColor = BokiTheme.colors.cardBackground,
        contentColor = BokiTheme.colors.onBackground,
        dragHandle = {
            // Custom drag handle with Boki styling
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            BokiTheme.colors.textSecondary.copy(alpha = 0.5f),
                            BokiTheme.shapes.small
                        )
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction History",
                    style = BokiTheme.typography.headlineMedium,
                    color = BokiTheme.colors.onBackground
                )

                Card(
                    shape = BokiTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = BokiTheme.colors.secondary.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "${transactions.size} transactions",
                        style = BokiTheme.typography.labelMedium,
                        color = BokiTheme.colors.secondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Transaction List
            if (transactions.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            currency = currency
                        )
                    }
                }
            } else {
                // Empty state
                EmptyTransactionState()
            }
        }
    }
}

@Composable
fun TransactionFloatingButton(
    transactions: List<TransactionHistoryResponse>,
    modifier: Modifier = Modifier
) {
    val  currency: String = "KWD"

    var showBottomSheet by remember { mutableStateOf(false) }

    // Floating Action Button to trigger bottom sheet
    if (transactions.isNotEmpty()) {
        Card(
            modifier = modifier
                .padding(16.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = BokiTheme.shapes.medium,
                    ambientColor = BokiTheme.colors.secondary.copy(alpha = 0.3f)
                ),
            shape = BokiTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = BokiTheme.colors.secondary
            ),
            onClick = { showBottomSheet = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "View Transactions",
                    tint = BokiTheme.colors.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View Transactions (${transactions.size})",
                    style = BokiTheme.typography.labelLarge,
                    color = BokiTheme.colors.onPrimary
                )
            }
        }

        // Bottom Sheet
        if (showBottomSheet) {
            TransactionBottomSheet(
                transactions = transactions,
                onDismiss = { showBottomSheet = false }
            )
        }
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionHistoryResponse,
    modifier: Modifier = Modifier
) {
    val currency: String = "KWD"
    val transactionIcon = getTransactionIcon(transaction.transactionType)
    val transactionColor = getTransactionColor(transaction.transactionType)
    val amountPrefix = getAmountPrefix(transaction.transactionType)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = BokiTheme.shapes.medium,
                ambientColor = BokiTheme.colors.secondary.copy(alpha = 0.1f)
            ),
        shape = BokiTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = BokiTheme.colors.surface.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction Icon and Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            transactionColor.copy(alpha = 0.1f),
                            BokiTheme.shapes.circle
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = transactionIcon,
                        contentDescription = transaction.transactionType,
                        tint = transactionColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Transaction Details
                Column {
                    Text(
                        text = transaction.transactionType.replace("_", " ").uppercase(),
                        style = BokiTheme.typography.titleRegular,
                        color = BokiTheme.colors.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    transaction.description?.let { desc ->
                        Text(
                            text = desc,
                            style = BokiTheme.typography.bodyMedium,
                            color = BokiTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = transaction.createdAt.format(
                            DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")
                        ),
                        style = BokiTheme.typography.labelSmall,
                        color = BokiTheme.colors.textSecondary
                    )
                }
            }

            // Amount
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$amountPrefix$currency ${String.format("%.3f", transaction.amount)}",
                    style = BokiTheme.typography.transactionAmount,
                    color = transactionColor,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun EmptyTransactionState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = BokiTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = BokiTheme.colors.surface.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = "No Transactions",
                tint = BokiTheme.colors.textSecondary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No transactions yet",
                style = BokiTheme.typography.titleRegular,
                color = BokiTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Your transaction history will appear here",
                style = BokiTheme.typography.bodyMedium,
                color = BokiTheme.colors.textSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun getTransactionIcon(transactionType: String): ImageVector {
    return when (transactionType.lowercase()) {
        "deposit", "credit" -> Icons.Default.ArrowDownward
        "withdrawal", "debit" -> Icons.Default.ArrowUpward
        else -> Icons.Default.SwapHoriz
    }
}

@Composable
private fun getTransactionColor(transactionType: String): Color {
    return when (transactionType.lowercase()) {
        "deposit", "credit" -> BokiTheme.colors.success
        "withdrawal", "debit" -> BokiTheme.colors.error
        else -> BokiTheme.colors.info
    }
}

private fun getAmountPrefix(transactionType: String): String {
    return when (transactionType.lowercase()) {
        "deposit", "credit" -> "+"
        "withdrawal", "debit" -> "-"
        else -> ""
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionFloatingButtonPreview() {
    val sampleTransactions = listOf(
        TransactionHistoryResponse(
            id = 1L,
            amount = BigDecimal("250.000"),
            transactionType = "DEPOSIT",
            description = "Salary Payment",
            createdAt = LocalDateTime.now().minusHours(2)
        ),
        TransactionHistoryResponse(
            id = 2L,
            amount = BigDecimal("45.500"),
            transactionType = "WITHDRAWAL",
            description = "Coffee Shop",
            createdAt = LocalDateTime.now().minusHours(5)
        ),
        TransactionHistoryResponse(
            id = 3L,
            amount = BigDecimal("12.750"),
            transactionType = "DEBIT",
            description = "Grocery Store",
            createdAt = LocalDateTime.now().minusDays(1)
        ),
        TransactionHistoryResponse(
            id = 4L,
            amount = BigDecimal("100.000"),
            transactionType = "TRANSFER",
            description = "Transfer to Savings",
            createdAt = LocalDateTime.now().minusDays(2)
        ),
        TransactionHistoryResponse(
            id = 5L,
            amount = BigDecimal("75.250"),
            transactionType = "CREDIT",
            description = "Refund",
            createdAt = LocalDateTime.now().minusDays(3)
        )
    )

    BankAPITheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BokiTheme.gradient),
            contentAlignment = Alignment.BottomEnd
        ) {
            TransactionFloatingButton(
                transactions = sampleTransactions,
            )
        }
    }
}