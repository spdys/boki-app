package com.joincoded.bankapi.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.joincoded.bankapi.data.TransactionHistoryResponse
import com.joincoded.bankapi.data.TransactionType
import com.joincoded.bankapi.ui.theme.BankAPITheme
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class TransactionSource {
    POT,
    ACCOUNT
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionBottomSheet(
    viewModel: BankViewModel,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    transactionSource: TransactionSource,
    modifier: Modifier = Modifier
) {

    val transactions = when (transactionSource) {
        TransactionSource.POT -> viewModel.potTransactions ?: emptyList()
        TransactionSource.ACCOUNT -> viewModel.accountTransactions ?: emptyList()
    }
    val currency: String = "KWD"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
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
            when {
                transactions.isNotEmpty() -> {
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
                }

                else -> {
                    // Empty state
                    EmptyTransactionState()
                }
            }
        }
    }
}


@SuppressLint("DefaultLocale")
@Composable
fun TransactionCard(
    transaction: TransactionHistoryResponse,
    currency: String,
    modifier: Modifier = Modifier
) {
    // Inline transaction icon logic
    val transactionIcon = when (transaction.transactionType) {
        TransactionType.DEPOSIT.toString() -> Icons.Default.ArrowDownward
        TransactionType.WITHDRAW.toString() -> Icons.Default.ArrowUpward
        TransactionType.TRANSFER.toString() -> Icons.Default.SwapHoriz
        TransactionType.PURCHASE.toString() -> Icons.Default.ShoppingCart
        else -> Icons.Default.Help
    }

    // Inline transaction color logic
    val transactionColor = when (transaction.transactionType) {
        TransactionType.DEPOSIT.toString() -> Color(0xFF4CAF50) // Green
        TransactionType.WITHDRAW.toString() -> Color(0xFFF44336) // Red
        TransactionType.TRANSFER.toString() -> Color(0xFF2196F3) // Blue
        TransactionType.PURCHASE.toString() -> Color(0xFFFF9800) // Orange
        else -> Color.Gray
    }

    // Inline amount prefix logic
    val amountPrefix = when (transaction.transactionType) {
        TransactionType.DEPOSIT.toString() -> "+"
        TransactionType.WITHDRAW.toString() -> "-"
        TransactionType.TRANSFER.toString() -> ""
        TransactionType.PURCHASE.toString() -> "-"
        else -> ""
    }

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
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = transactionIcon,
                        contentDescription = transaction.transactionType.toString(),
                        tint = transactionColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Transaction Details
                Column {
                    Text(
                        text = transaction.transactionType.toString().replace("_", " "),
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
                    text = "$currency ${String.format("%.3f", transaction.amount)}",
                    color = transactionColor,
                    textAlign = TextAlign.End,
                    style = BokiTheme.typography.transactionAmount
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
    // Note: This preview shows sample data, but in real usage the button
    // only appears when viewModel.potTransactions has data
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
        )
    )

    BankAPITheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BokiTheme.gradient),
            contentAlignment = Alignment.BottomEnd
        ) {
            // For preview purposes, simulate the button appearance
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = BokiTheme.shapes.medium,
                        ambientColor = BokiTheme.colors.secondary.copy(alpha = 0.3f)
                    ),
                shape = BokiTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = BokiTheme.colors.secondary
                )
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
                        text = "View Transactions (${sampleTransactions.size})",
                        style = BokiTheme.typography.labelLarge,
                        color = BokiTheme.colors.onPrimary
                    )
                }
            }
        }
    }
}