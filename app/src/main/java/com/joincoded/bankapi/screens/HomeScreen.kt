package com.joincoded.bankapi.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.joincoded.bankapi.data.AccountSummaryDto
import com.joincoded.bankapi.data.AccountType
import com.joincoded.bankapi.data.AllocationType
import com.joincoded.bankapi.data.PotSummaryDto
import com.joincoded.bankapi.ui.theme.BokiTheme
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

    val totalBalance by remember { derivedStateOf { viewModel.totalBalance } }
    val mainCurrency by remember { derivedStateOf { viewModel.mainAccountSummary?.currency ?: "KWD" } }
    var balanceVisible by remember { mutableStateOf(true) }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            viewModel.getKYC()
            viewModel.fetchAccountsAndSummaries()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BokiTheme.gradient)
    ) {
        // Background decorative elements
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = (-30).dp, y = 50.dp)
                    .background(
                        BokiTheme.colors.secondary.copy(alpha = 0.08f),
                        BokiTheme.shapes.circle
                    )
            )

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .offset(x = 320.dp, y = 150.dp)
                    .background(
                        BokiTheme.colors.info.copy(alpha = 0.06f),
                        BokiTheme.shapes.circle
                    )
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = BokiTheme.colors.secondary,
                            strokeWidth = 3.dp
                        )
                    }
                } else if (error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = BokiTheme.shapes.card,
                        colors = CardDefaults.cardColors(
                            containerColor = BokiTheme.colors.error.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = error ?: "Unknown error",
                            color = BokiTheme.colors.error,
                            style = BokiTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    UserGreetingSection(
                        greeting = viewModel.getGreeting(),
                        userName = SharedPreferencesManager.getFirstName(context) ?: "User"
                    )
                }
            }

            if (!isLoading && error == null) {
                item {
                    BalanceOverviewCard(
                        totalBalance = totalBalance,
                        currency = mainCurrency,
                        balanceVisible = balanceVisible,
                        onToggleVisibility = { balanceVisible = !balanceVisible }
                    )
                }

                item {
                    SectionHeader(title = "Accounts")
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
                    userName = SharedPreferencesManager.getFirstName(context)
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
                        SectionHeader(title = "Savings Pots")
                    }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            itemsIndexed(pots) { index, pot ->
                                PotCard(
                                    pot = pot,
                                    index = index,
                                    balanceVisible = balanceVisible,
                                    currency = mainCurrency,
                                    onClick = onPotClicked
                                )
                            }
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
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun UserGreetingSection(greeting: String, userName: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$greeting,",
            style = BokiTheme.typography.bodyLarge,
            color = BokiTheme.colors.textSecondary
        )
        Text(
            text = userName,
            style = BokiTheme.typography.headlineLarge,
            color = BokiTheme.colors.onBackground,
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
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = BokiTheme.shapes.card,
                ambientColor = BokiTheme.colors.secondary.copy(alpha = 0.2f)
            ),
        shape = BokiTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = BokiTheme.colors.cardBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total Balance",
                    style = BokiTheme.typography.labelLarge,
                    color = BokiTheme.colors.textSecondary
                )
                IconButton(
                    onClick = onToggleVisibility,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = BokiTheme.colors.secondary
                    )
                ) {
                    Icon(
                        imageVector = if (balanceVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Balance"
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (balanceVisible) "$currency ${totalBalance.setScale(3)}" else "••••••",
                style = BokiTheme.typography.displaySmall,
                color = BokiTheme.colors.onBackground,
                fontWeight = FontWeight.Bold
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
            style = BokiTheme.typography.headlineMedium,
            color = BokiTheme.colors.onBackground,
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
            .shadow(
                elevation = 8.dp,
                shape = BokiTheme.shapes.card,
                ambientColor = BokiTheme.colors.secondary.copy(alpha = 0.1f)
            )
            .clickable { onClick(account) },
        shape = BokiTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = BokiTheme.colors.cardBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = account.accountType.name.replace("_", " "),
                style = BokiTheme.typography.labelLarge,
                color = BokiTheme.colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (balanceVisible) "${account.currency} ${account.balance.setScale(3)}" else "••••••",
                style = BokiTheme.typography.titleBold,
                color = BokiTheme.colors.onBackground,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = account.accountNumber,
                style = BokiTheme.typography.labelMedium,
                color = BokiTheme.colors.textSecondary
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
            .width(180.dp)
            .shadow(
                elevation = 8.dp,
                shape = BokiTheme.shapes.card,
                ambientColor = BokiTheme.colors.success.copy(alpha = 0.1f)
            )
            .clickable { onClick(pot) },
        shape = BokiTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = BokiTheme.colors.cardBackground
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BokiTheme.potGradient(index))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = pot.name,
                    style = BokiTheme.typography.titleRegular,
                    color = BokiTheme.colors.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (balanceVisible) "$currency ${pot.balance.setScale(3)}" else "••••••",
                    style = BokiTheme.typography.bodyLarge,
                    color = BokiTheme.colors.onBackground,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (pot.allocationType) {
                        AllocationType.PERCENTAGE -> "Percentage: ${(pot.allocationValue * BigDecimal(100)).setScale(0)}%"
                        AllocationType.FIXED -> "Fixed: ${pot.allocationValue.stripTrailingZeros().toPlainString()} $currency"
                    },
                    style = BokiTheme.typography.labelSmall,
                    color = BokiTheme.colors.textSecondary
                )
            }
        }
    }
}