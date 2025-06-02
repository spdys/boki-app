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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val parentAccount by remember { derivedStateOf { viewModel.mainAccountSummary } }
    val currency = parentAccount?.currency ?: "KWD"


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
            }
        }

        if (showEditDialog) {
            var newName by remember { mutableStateOf(pot.name) }
            var newType by remember { mutableStateOf(pot.allocationType) }
            var newValue by remember { mutableStateOf(pot.allocationValue.toPlainString()) }
            var validationError by remember { mutableStateOf<String?>(null) }

            // Compute whether the form values have changed
            val isFormChanged = newName != pot.name ||
                    newType != pot.allocationType ||
                    newValue != pot.allocationValue.toPlainString()

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Pot") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Pot Name") },
                            singleLine = true
                        )
                        DropdownMenuBox(
                            selected = newType,
                            onSelectedChange = { newType = it }
                        )
                        OutlinedTextField(
                            value = newValue,
                            onValueChange = { newValue = it },
                            label = { Text("Allocation Value") },
                            singleLine = true
                        )
                        if (validationError != null) {
                            Text(
                                text = validationError!!,
                                color = BokiTheme.colors.error,
                                style = BokiTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val value = newValue.toBigDecimalOrNull()
                            val error = if (value != null) {
                                viewModel.validatePotInputs(newName, value, newType, viewModel.selectedPot?.potId)
                            } else {
                                "Invalid number format"
                            }
                            if (error == null && value != null) {
                                viewModel.editPot(newName, newType, value)
                                showEditDialog = false
                                showConfirmationDialog = true
                            } else {
                                validationError = error
                            }
                        },
                        enabled = isFormChanged && validationError == null
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Confirmation dialog outside AlertDialog
        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = { Text("Update Scheduled") },
                text = {
                    Text("Allocation changes will take effect on the next salary deposit. The current balance remains unchanged.")
                },
                confirmButton = {
                    TextButton(onClick = { showConfirmationDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun PotDetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
private fun DropdownMenuBox(
    selected: AllocationType,
    onSelectedChange: (AllocationType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = AllocationType.values()

    Box {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Allocation Type") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name) },
                    onClick = {
                        onSelectedChange(type)
                        expanded = false
                    }
                )
            }
        }
    }
}