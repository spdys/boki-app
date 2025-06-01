package com.joincoded.bankapi.testingcomposes

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joincoded.bankapi.ui.theme.BokiTheme


import com.joincoded.bankapi.ui.theme.BokiTypography
import java.math.BigDecimal

@Composable
fun AccountSummaryScreen(
    accountSummary: AccountSummaryDto,
    modifier: Modifier = Modifier
) {


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BokiTheme.gradient)
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
                .shadow(6.dp, BokiTheme.shapes.card)
                .padding(bottom = 24.dp),
            shape = BokiTheme.shapes.card,
            colors = CardDefaults.cardColors(
                containerColor = BokiTheme.colors.cardBackground
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Account Number
                Text(
                    text = "Account Number",
                    color = BokiTheme.colors.textSecondary,
                    style = BokiTheme.typography.labelMedium
                )
                Text(
                    text = accountSummary.accountNumber,
                    color = BokiTheme.colors.onBackground,
                    style = BokiTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Account Type
                Text(
                    text = "Account Type",
                    color = BokiTheme.colors.textSecondary,
                    style = BokiTheme.typography.labelMedium
                )
                Text(
                    text = accountSummary.accountType.name.replace("_", " "),
                    color = BokiTheme.colors.onBackground,
                    style = BokiTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Balance
                Text(
                    text = "Balance",
                    color = BokiTheme.colors.textSecondary,
                    style = BokiTheme.typography.labelMedium
                )
                Text(
                    text = "${accountSummary.currency} ${accountSummary.balance}",
                    color = BokiTheme.colors.onBackground,
                    style = BokiTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Card Number (if available)
                accountSummary.cardNumber?.let { cardNumber ->
                    Text(
                        text = "Card Number",
                        color = BokiTheme.colors.textSecondary,
                        style = BokiTheme.typography.labelMedium
                    )
                    Text(
                        text = "**** **** **** ${cardNumber.takeLast(4)}",color = BokiTheme.colors.onBackground,
                        style = BokiTheme.typography.bodyMedium,
                    )
                }
            }
        }

        // Pots Section
        if (!accountSummary.pots.isNullOrEmpty()) {
            Text(
                text = "Savings Pots",
                color = Color.White,
                style = BokiTypography.titleRegular.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(accountSummary.pots) { pot ->
                    PotCard(
                        pot = pot,
                        currency = accountSummary.currency
                    )
                }
            }
        } else {
            // Empty state for pots
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No savings pots available",
                    color = BokiTheme.colors.error,
                    style = BokiTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun PotCard(
    pot: PotSummaryDto,
    currency: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val cardHeight by animateDpAsState(
        targetValue = if (isExpanded) 120.dp else 80.dp,
        animationSpec = tween(300),
        label = "potCardHeight"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pot.name,
                    color = Color.White,
                    style = BokiTypography.titleRegular.copy(fontSize = 16.sp)
                )
                Text(
                    text = "$currency ${pot.balance}",
                    color = Color.White,
                    style = BokiTypography.titleRegular.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                // Allocation Info
                val allocationText = when (pot.allocationType) {
                    AllocationType.PERCENTAGE -> "${pot.allocationValue}% allocation"
                    AllocationType.FIXED -> "$currency ${pot.allocationValue} allocation"
                }
                Text(
                    text = allocationText,
                    color = BokiTheme.colors.onBackground,
                    style = BokiTheme.typography.bodyMedium
                )

                // Card Token Info
                pot.cardToken?.let { token ->
                    Text(
                        text = "Card: **** ${token.takeLast(4)}",
                        color = BokiTheme.colors.textSecondary,
                        style = BokiTypography.titleRegular.copy(fontSize = 12.sp),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountSummaryScreenPreview() {
    val samplePots = listOf(
        PotSummaryDto(
            potId = 1L,
            name = "Emergency Fund",
            balance = BigDecimal("2500.00"),
            cardToken = "1234567890123456",
            allocationType = AllocationType.PERCENTAGE,
            allocationValue = BigDecimal("15.0")
        ),
        PotSummaryDto(
            potId = 2L,
            name = "Vacation Savings",
            balance = BigDecimal("1200.50"),
            cardToken = null,
            allocationType = AllocationType.FIXED,
            allocationValue = BigDecimal("200.00")
        ),
        PotSummaryDto(
            potId = 3L,
            name = "New Car Fund",
            balance = BigDecimal("850.75"),
            cardToken = "9876543210987654",
            allocationType = AllocationType.PERCENTAGE,
            allocationValue = BigDecimal("10.0")
        )
    )

    val sampleAccount = AccountSummaryDto(
        accountId = 1L,
        accountNumber = "ACC123456789",
        accountType = AccountType.SAVINGS,
        balance = BigDecimal("15750.25"),
        cardNumber = "1234567890123456",
        currency = "USD",
        isActive = true,
        pots = samplePots
    )

    AccountSummaryScreen(accountSummary = sampleAccount)
}