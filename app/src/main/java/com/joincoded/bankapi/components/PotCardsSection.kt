@file:OptIn(ExperimentalMaterial3Api::class)

package com.joincoded.bankapi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joincoded.bankapi.data.AllocationType
import com.joincoded.bankapi.data.PotSummaryDto
import com.joincoded.bankapi.ui.theme.BankAPITheme
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.ui.theme.BokiColorUtils
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

@Composable
fun PotCardsSection(
    bankViewModel: BankViewModel,
    onPotCardClick: (PotSummaryDto) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val mainAccountSummary = bankViewModel.mainAccountSummary
    val pots = mainAccountSummary?.pots ?: emptyList()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (pots.isNotEmpty()) {
            // Section header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "",
                    style = BokiTheme.typography.titleRegular,
                    color = BokiTheme.colors.onBackground,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${pots.size} ${if (pots.size == 1) "pot" else "pots"}",
                    style = BokiTheme.typography.cardLabel,
                    color = BokiTheme.colors.onBackground.copy(alpha = 0.6f)
                )
            }

            // Pot cards - vertical like Apple Wallet
            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy((-40).dp), // Overlapping cards
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                itemsIndexed(pots) { index, pot ->
                    PotDebitCard(
                        pot = pot,
                        gradientIndex = index,
                        onClick = { onPotCardClick(pot) },
                        modifier = Modifier
                            .offset(y = (index * 8).dp) // Slight staggered effect
                    )
                }
            }
        } else {
            // Empty state
            EmptyPotsState()
        }
    }
}

@Composable
private fun PotDebitCard(
    pot: PotSummaryDto,
    gradientIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val potColors = BokiColorUtils.getPotColors(gradientIndex)
    val potGradient = Brush.horizontalGradient(potColors)
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = BokiTheme.shapes.card,
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(potGradient)
        ) {
            // Card content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section - Card Token (if available)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!pot.cardToken.isNullOrBlank()) {
                        Text(
                            text = "VIRTUAL CARD",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Middle section - Pot Name
                Text(
                    text = pot.name.uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Bottom section - Balance and Allocation
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Balance
                    Text(
                        text = "${pot.balance} KWD",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPotsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "No Pots Yet",
            style = BokiTheme.typography.titleRegular,
            color = BokiTheme.colors.onBackground.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Create pots to organize your savings and spending goals",
            style = BokiTheme.typography.cardLabel,
            color = BokiTheme.colors.onBackground.copy(alpha = 0.5f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// Helper function
private fun getAllocationText(allocationType: AllocationType, allocationValue: BigDecimal): String {
    return when (allocationType) {
        AllocationType.PERCENTAGE -> "${allocationValue.toInt()}%"
        AllocationType.FIXED -> {
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            "${formatter.format(allocationValue)}"
        }
        else -> "Manual transfers"
    }
}

// Preview Data
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun PotCardsSectionPreview() {
    BankAPITheme {
        val samplePots = listOf(
            PotSummaryDto(
                potId = 1L,
                name = "Emergency Fund",
                balance = BigDecimal("2500.00"),
                cardToken = "virtual_token_123",
                allocationType = AllocationType.PERCENTAGE,
                allocationValue = BigDecimal("10")
            ),
            PotSummaryDto(
                potId = 2L,
                name = "Vacation",
                balance = BigDecimal("850.50"),
                cardToken = null,
                allocationType = AllocationType.FIXED,
                allocationValue = BigDecimal("200")
            ),
            PotSummaryDto(
                potId = 3L,
                name = "Car Maintenance",
                balance = BigDecimal("150.00"),
                cardToken = "virtual_token_456",
                allocationType = AllocationType.FIXED,
                allocationValue = BigDecimal("0")
            )
        )

        // Mock the vertical card layout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BokiTheme.gradient)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Pots",
                        style = BokiTheme.typography.titleRegular,
                        color = BokiTheme.colors.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "${samplePots.size} pots",
                        style = BokiTheme.typography.cardLabel,
                        color = BokiTheme.colors.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Preview cards
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy((-40).dp)
                ) {
                    samplePots.forEachIndexed { index, pot ->
                        PotDebitCard(
                            pot = pot,
                            gradientIndex = index,
                            onClick = { },
                            modifier = Modifier.offset(y = (index * 8).dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun EmptyPotCardsSectionPreview() {
    BankAPITheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BokiTheme.gradient)
        ) {
            EmptyPotsState()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 180)
@Composable
fun SinglePotCardPreview() {
    BankAPITheme {
        val samplePot = PotSummaryDto(
            potId = 1L,
            name = "Emergency Fund",
            balance = BigDecimal("2500.00"),
            cardToken = "virtual_token_123",
            allocationType = AllocationType.PERCENTAGE,
            allocationValue = BigDecimal("10")
        )

        PotDebitCard(
            pot = samplePot,
            gradientIndex = 0,
            onClick = { }
        )
    }
}