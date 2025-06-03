package com.joincoded.bankapi.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.joincoded.bankapi.data.AccountSummaryDto
import com.joincoded.bankapi.data.AccountType
import com.joincoded.bankapi.data.AllocationType
import com.joincoded.bankapi.data.PotSummaryDto
import com.joincoded.bankapi.utils.SharedPreferencesManager
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.math.BigDecimal

@Composable
fun HomeScreen(
    viewModel: BankViewModel,
    onAccountClicked: (AccountSummaryDto) -> Unit,
    onPotClicked: (PotSummaryDto) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccessful by viewModel.isSuccessful.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

//    val userName by remember { derivedStateOf { viewModel.userName } }
    val totalBalance by remember { derivedStateOf { viewModel.totalBalance } }
    val mainCurrency by remember { derivedStateOf { viewModel.mainAccountSummary?.currency ?: "KWD" } }
    var balanceVisible by remember { mutableStateOf(true) }


    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            viewModel.getKYC()
            viewModel.fetchAccountsAndSummaries()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
            } else {
                UserGreetingSection(
                    greeting = viewModel.getGreeting(),
                    userName = SharedPreferencesManager.getFirstName(context) ?: "User"
                )
                BalanceOverviewCard(
                    totalBalance = totalBalance,
                    currency = mainCurrency,
                    balanceVisible = balanceVisible,
                    onToggleVisibility = { balanceVisible = !balanceVisible }
                )
                SectionHeader(title = "Accounts")

            }
        }

        items(viewModel.allAccountSummaries.sortedBy { it.accountType != AccountType.MAIN }) { summary ->
            AccountCard(
                account = summary,
                onClick = onAccountClicked,
                balanceVisible = balanceVisible
            )
        }

        viewModel.mainAccountSummary?.pots?.takeIf { it.isNotEmpty() }?.let { pots ->
            item {
                SectionHeader(title = "Pots")
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    itemsIndexed(pots.sortedBy { it.name.lowercase() }) { index, pot ->
                        PotCard(
                            pot = pot,
                            index = index,
                            balanceVisible = balanceVisible,
                            currency = mainCurrency,
                            onClick = onPotClicked
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserGreetingSection(greeting: String, userName: String) {
    Column {
        Text(
            text = "$greeting,",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BalanceOverviewCard(
    totalBalance: BigDecimal,
    currency: String,
    balanceVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Total Balance", style = MaterialTheme.typography.bodyMedium)
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (balanceVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Balance"
                    )
                }
            }
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = if (balanceVisible) "${totalBalance.setScale(3)} $currency" else "••••••",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AccountCard(
    account: AccountSummaryDto,
    onClick: (AccountSummaryDto) -> Unit,
    balanceVisible: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(account) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = account.accountType.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (balanceVisible) "${account.balance.setScale(3)} ${account.currency}" else "••••••",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = account.accountNumber,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun PotCard(
    pot: PotSummaryDto,
    onClick: (PotSummaryDto) -> Unit,
    index: Int,
    currency: String,
    balanceVisible: Boolean,
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick(pot) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(pot.name, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (balanceVisible) "${pot.balance.setScale(3)} $currency" else "••••••"
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = when (pot.allocationType) {
                    AllocationType.PERCENTAGE -> "Percentage: ${(pot.allocationValue * BigDecimal(100)).setScale(0)}%"
                    AllocationType.FIXED -> "Fixed: ${pot.allocationValue.stripTrailingZeros().toPlainString()} $currency"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}