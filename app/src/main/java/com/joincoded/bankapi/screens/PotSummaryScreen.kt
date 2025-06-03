package com.joincoded.bankapi.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joincoded.bankapi.components.AddToPotDialog
import com.joincoded.bankapi.components.PotActionsCard
import com.joincoded.bankapi.components.TransactionBottomSheet
import com.joincoded.bankapi.components.TransactionSource
import com.joincoded.bankapi.components.TransferBetweenPotsDialog
import com.joincoded.bankapi.components.WithdrawFromPotDialog
import com.joincoded.bankapi.components.CreateOrEditPotPopup
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.data.AllocationType
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PotSummaryScreen(viewModel: BankViewModel) {
    val pot = viewModel.selectedPot
    if (pot == null) {
        Text("No pot selected")
        return
    }

    var showEditDialog by remember { mutableStateOf(false) }

    val parentAccount by remember { derivedStateOf { viewModel.mainAccountSummary } }
    val currency = parentAccount?.currency ?: "KWD"

    LaunchedEffect(Unit) {
        viewModel.getPotTransactionHistory()
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }

    val amount by viewModel.amount.collectAsState()
    val amountError by viewModel.amountError.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val showBottomSheet = remember { mutableStateOf(true) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            newValue != SheetValue.Hidden // prevent hiding
        }    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BokiTheme.gradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Pot Details",
                        style = BokiTheme.typography.headlineSmall,
                        color = BokiTheme.colors.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BokiTheme.colors.background.copy(alpha = 0.95f)
                ),
                actions = {
                    TextButton(onClick = { showEditDialog = true }) {
                        Text("Edit", color = BokiTheme.colors.secondary)
                    }
                }
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pot Balance Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 20.dp,
                            shape = BokiTheme.shapes.extraLarge,
                            ambientColor = BokiTheme.colors.secondary.copy(alpha = 0.2f)
                        ),
                    shape = BokiTheme.shapes.extraLarge,
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
                        // Pot Icon
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = "Savings Pot",
                            tint = BokiTheme.colors.secondary,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pot Name
                        Text(
                            text = pot.name,
                            style = BokiTheme.typography.headlineMedium,
                            color = BokiTheme.colors.onBackground,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Current Balance",
                            style = BokiTheme.typography.labelLarge,
                            color = BokiTheme.colors.textSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Balance using banking typography
                        Text(
                            text = "${pot.balance.setScale(3)} $currency",
                            style = BokiTheme.typography.balanceDisplay,
                            color = BokiTheme.colors.onBackground,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pot Details Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 15.dp,
                            shape = BokiTheme.shapes.extraLarge,
                            ambientColor = BokiTheme.colors.secondary.copy(alpha = 0.15f)
                        ),
                    shape = BokiTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = BokiTheme.colors.cardBackground
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Pot Information",
                            style = BokiTheme.typography.headlineSmall,
                            color = BokiTheme.colors.onBackground,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        // Allocation Type & Value
                        val allocationText = when (pot.allocationType) {
                            AllocationType.PERCENTAGE -> "${(pot.allocationValue * BigDecimal(100)).setScale(0)}%"
                            AllocationType.FIXED -> "${pot.allocationValue.stripTrailingZeros().toPlainString()} $currency"
                        }

                        PotDetailRow(
                            label = "Allocation Type",
                            value = pot.allocationType.name.replace("_", " "),
                            icon = Icons.Default.AccountBalance
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        PotDetailRow(
                            label = "Allocation Value",
                            value = allocationText,
                            icon = Icons.Default.Savings
                        )

                        // Card Token (if available)
                        pot.cardToken?.let { cardToken ->
                            Spacer(modifier = Modifier.height(16.dp))
                            PotDetailRow(
                                label = "Linked Card",
                                value = "**** **** **** ${cardToken.takeLast(4)}",
                                icon = Icons.Default.CreditCard
                            )
                        }
                    }
                }

                // Add spacing for transaction overlay
                Spacer(modifier = Modifier.height(120.dp))

                PotActionsCard(
                    onAddClick = { showAddDialog = true },
                    onWithdrawClick = {showWithdrawDialog = true},
                    onTransferClick = {showTransferDialog = true},
                    modifier = Modifier.padding(16.dp)
                )
                AddToPotDialog(
                    isVisible = showAddDialog,
                    amount = amount,
                    onAmountChange = { viewModel.updateAmount(it) },
                    onConfirm = {
                        viewModel.addToPot()
                        showAddDialog = false
                    },
                    onDismiss = {
                        showAddDialog = false
                        viewModel.clearAmount() // Clear amount when dismissed
                    },
                    isLoading = isLoading,
                    errorMessage = amountError
                )

                WithdrawFromPotDialog(
                    isVisible = showWithdrawDialog,
                    amount = amount,
                    onAmountChange = { viewModel.updateAmount(it) },
                    onConfirm = {
                        viewModel.withdrawFromPot()
                        showWithdrawDialog = false
                    },
                    onDismiss = {
                        showWithdrawDialog = false
                        viewModel.clearAmount()
                    },
                    isLoading = isLoading,
                    errorMessage = amountError
                )
                TransferBetweenPotsDialog(
                    isVisible = showTransferDialog,
                    amount = amount,
                    selectedDestinationPot = viewModel.selectedDestinationPot,
                    availablePots = viewModel.availableDestinationPots,
                    onAmountChange = { viewModel.updateAmount(it) },
                    onDestinationPotSelected = { viewModel.setDestinationPot(it) },
                    onConfirm = {
                        viewModel.transferBetweenPots()
                        showTransferDialog = false
                    },
                    onDismiss = {
                        showTransferDialog = false
                        viewModel.clearAmount()
                        viewModel.clearDestinationPot()
                    },
                    isLoading = isLoading,
                    errorMessage = amountError
                )
            }
        }

        if (showEditDialog) {
            val totalAllocated = viewModel.mainAccountSummary?.pots
                ?.filter { it.allocationType == AllocationType.PERCENTAGE }
                ?.sumOf {
                    if (it.potId == pot.potId) pot.allocationValue else it.allocationValue
                }?.let { "Total allocated so far: ${(it * BigDecimal(100)).toInt()}%" }

            CreateOrEditPotPopup(
                initialName = pot.name,
                initialType = pot.allocationType,
                initialValue = pot.allocationValue.toPlainString(),
                currency = currency,
                totalAllocatedText = totalAllocated,
                showDialog = showEditDialog,
                onDismiss = { showEditDialog = false },
                onConfirm = { name, type, value ->
                    viewModel.editPot(name, type, value)
                },
                validateInput = { name, value, type ->
                    viewModel.validatePotInputs(name, value, type, pot.potId)
                }
            )
        }
        if (showBottomSheet.value) {
            TransactionBottomSheet(
                viewModel = viewModel,
                onDismiss = {
                    showBottomSheet.value = false
                },
                transactionSource = TransactionSource.POT
            )
        }
    }
}

@Composable
private fun PotDetailRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = BokiTheme.colors.secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = BokiTheme.typography.bodyMedium,
                color = BokiTheme.colors.textSecondary
            )
        }

        Text(
            text = value,
            style = BokiTheme.typography.bodyMedium,
            color = BokiTheme.colors.onBackground,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
