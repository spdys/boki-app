package com.joincoded.bankapi.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.joincoded.bankapi.ui.theme.BokiTheme
import androidx.compose.ui.tooling.preview.Preview
import com.joincoded.bankapi.data.AccountSummaryDto
import com.joincoded.bankapi.data.AccountType
import com.joincoded.bankapi.ui.theme.BankAPITheme
import java.math.BigDecimal

@Composable
fun AccountBalanceCard(
    accountSummary: AccountSummaryDto,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 15.dp,
                shape = BokiTheme.shapes.card,
                ambientColor = BokiTheme.colors.secondary.copy(alpha = 0.2f)
            ),
        shape = BokiTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = BokiTheme.colors.cardBackground
        ),
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Account Type Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = "Account Type",
                    tint = BokiTheme.colors.secondary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = accountSummary.accountType.name,
                    style = BokiTheme.typography.headlineMedium,
                    color = BokiTheme.colors.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Balance Label
            Text(
                text = "Available Balance",
                style = BokiTheme.typography.labelLarge,
                color = BokiTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main Balance - Using banking-specific typography
            Text(
                text = "${accountSummary.currency} ${String.format("%.3f", accountSummary.balance)}",
                style = BokiTheme.typography.balanceDisplay,
                color = BokiTheme.colors.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Account Number with banking-specific typography
            Text(
                text = "Acc Number: "+accountSummary.accountNumber,
                style = BokiTheme.typography.accountNumber,
                color = BokiTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            // Card Number (if available)
            accountSummary.cardNumber?.let { cardNum ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Card Number: ****-****-****-${cardNum.takeLast(4)}",
                    style = BokiTheme.typography.bodyMedium,
                    color = BokiTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountBalanceCardPreview() {
    val dummyAccount = AccountSummaryDto(
        accountId = 1,
        accountNumber = "123456789",
        accountType = AccountType.MAIN,
        balance = BigDecimal("4444"),
        cardNumber = "4644520199994444",
        currency = "KWD",
        isActive = true,
        pots = null,
    )

    BankAPITheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card with click
            AccountBalanceCard(
                accountSummary = dummyAccount,
                onClick = { /* Navigate to details */ }
            )

            // Card without click for comparison
            AccountBalanceCard(
                accountSummary = dummyAccount
            )
        }
    }
}