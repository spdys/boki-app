package com.joincoded.bankapi.components


import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.joincoded.bankapi.data.TransactionHistoryResponse
import com.joincoded.bankapi.ui.theme.BankAPITheme
import com.joincoded.bankapi.ui.theme.BokiTheme
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun SwipeUpTransactionList(
    transactions: List<TransactionHistoryResponse>,
    modifier: Modifier = Modifier,
    currency: String = "KWD"
) {
    val density = LocalDensity.current
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isExpanded by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (isExpanded) -300f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "transactionListOffset"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Floating Transaction Handle
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .offset(y = (animatedOffset + offsetY).dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    ambientColor = BokiTheme.colors.secondary.copy(alpha = 0.3f)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (offsetY < -100) {
                                isExpanded = true
                            } else if (offsetY > 100) {
                                isExpanded = false
                            }
                            offsetY = 0f
                        }
                    ) { _, dragAmount ->
                        offsetY += dragAmount.y / density.density
                    }
                },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = BokiTheme.colors.cardBackground
            )
        ) {
            Column {
                // Handle Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                BokiTheme.colors.textSecondary.copy(alpha = 0.5f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }

                // Transaction Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = BokiTheme.typography.headlineSmall,
                        color = BokiTheme.colors.onBackground
                    )

                    Text(
                        text = "${transactions.size} transactions",
                        style = BokiTheme.typography.bodySmall,
                        color = BokiTheme.colors.textSecondary
                    )
                }

                // Transaction List
                if (isExpanded) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactions) { transaction ->
                            TransactionCard(
                                transaction = transaction,
                                currency = currency
                            )
                        }

                        // Bottom padding
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                } else {
                    // Preview of first few transactions
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        transactions.take(2).forEach { transaction ->
                            TransactionCard(
                                transaction = transaction,
                                currency = currency,
                                isPreview = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (transactions.size > 2) {
                            Text(
                                text = "Swipe up to see ${transactions.size - 2} more transactions",
                                style = BokiTheme.typography.bodySmall,
                                color = BokiTheme.colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionHistoryResponse,
    currency: String,
    modifier: Modifier = Modifier,
    isPreview: Boolean = false
) {
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
                        .size(40.dp)
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
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Transaction Details
                Column {
                    Text(
                        text = transaction.transactionType.replace("_", " ").uppercase(),
                        style = BokiTheme.typography.labelMedium,
                        color = BokiTheme.colors.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    transaction.description?.let { desc ->
                        Text(
                            text = desc,
                            style = BokiTheme.typography.bodySmall,
                            color = BokiTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!isPreview) {
                        Text(
                            text = transaction.createdAt.format(
                                DateTimeFormatter.ofPattern("MMM dd, HH:mm")
                            ),
                            style = BokiTheme.typography.labelSmall,
                            color = BokiTheme.colors.textSecondary
                        )
                    }
                }
            }

            // Amount
            Text(
                text = "$amountPrefix$currency ${String.format("%.3f", transaction.amount)}",
                style = BokiTheme.typography.transactionAmount,
                color = transactionColor,
                textAlign = TextAlign.End
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
fun SwipeUpTransactionListPreview() {
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
                .background(BokiTheme.gradient)
        ) {
            SwipeUpTransactionList(
                transactions = sampleTransactions,
                currency = "KWD"
            )
        }
    }
}