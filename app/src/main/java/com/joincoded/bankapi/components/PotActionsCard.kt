package com.joincoded.bankapi.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joincoded.bankapi.data.PotSummaryDto
import com.joincoded.bankapi.ui.theme.BankAPITheme
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.viewmodel.BankViewModel

@Composable
fun PotActionsCard(
    onAddClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onTransferClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = BokiTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = BokiTheme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Add Action
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAddClick() }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Add",
                    style = BokiTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50)
                )
            }

            // Withdraw Action
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onWithdrawClick() }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Withdraw",
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Withdraw",
                    style = BokiTheme.typography.bodyMedium,
                    color = Color(0xFFF44336)
                )
            }

            // Transfer Action
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTransferClick() }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Transfer",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Transfer",
                    style = BokiTheme.typography.bodyMedium,
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun AddToPotDialog(
    isVisible: Boolean,
    amount: String,
    onAmountChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Add to Pot",
                    style = BokiTheme.typography.titleBold,
                    color = BokiTheme.colors.onBackground
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = onAmountChange,
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = errorMessage != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BokiTheme.colors.primary,
                            focusedLabelColor = BokiTheme.colors.primary
                        )
                    )

                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = error,
                            color = Color(0xFFF44336),
                            style = BokiTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    enabled = !isLoading && amount.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BokiTheme.colors.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Add", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        color = BokiTheme.colors.textSecondary
                    )
                }
            }
        )
    }
}

@Composable
fun WithdrawFromPotDialog(
    isVisible: Boolean,
    amount: String,
    onAmountChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Withdraw from Pot",
                    style = BokiTheme.typography.titleBold,
                    color = BokiTheme.colors.onBackground
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = onAmountChange,
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = errorMessage != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BokiTheme.colors.primary,
                            focusedLabelColor = BokiTheme.colors.primary
                        )
                    )

                    errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = error,
                            color = Color(0xFFF44336),
                            style = BokiTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    enabled = !isLoading && amount.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BokiTheme.colors.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Withdraw", color = Color.Red)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        color = BokiTheme.colors.textSecondary
                    )
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferBetweenPotsDialog(
    isVisible: Boolean,
    amount: String?,
    selectedDestinationPot: PotSummaryDto?,
    availablePots: List<PotSummaryDto>,
    onAmountChange: (String) -> Unit,
    onDestinationPotSelected: (PotSummaryDto) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) onDismiss() },
            containerColor = BokiTheme.colors.cardBackground,
            shape = BokiTheme.shapes.card,
            title = {
                Text(
                    text = "Transfer Between Pots",
                    style = BokiTheme.typography.headlineSmall,
                    color = BokiTheme.colors.onBackground
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Amount Input
                    OutlinedTextField(
                        value = amount ?: "",
                        onValueChange = onAmountChange,
                        label = { Text("Transfer Amount") },
                        placeholder = { Text("0.000") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        isError = errorMessage != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BokiTheme.colors.secondary,
                            focusedLabelColor = BokiTheme.colors.secondary,
                            cursorColor = BokiTheme.colors.secondary,
                            errorBorderColor = BokiTheme.colors.error,
                            errorLabelColor = BokiTheme.colors.error
                        ),
                        shape = BokiTheme.shapes.medium
                    )

                    // Destination Pot Dropdown
                    DestinationPotDropdown(
                        selectedPot = selectedDestinationPot,
                        availablePots = availablePots,
                        onPotSelected = onDestinationPotSelected,
                        enabled = !isLoading
                    )

                    // Error Message
                    errorMessage?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = BokiTheme.colors.error.copy(alpha = 0.1f)
                            ),
                            shape = BokiTheme.shapes.small
                        ) {
                            Text(
                                text = error,
                                color = BokiTheme.colors.error,
                                style = BokiTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    enabled = !isLoading &&
                            selectedDestinationPot != null &&
                            !amount.isNullOrBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BokiTheme.colors.secondary,
                        contentColor = BokiTheme.colors.onPrimary
                    ),
                    shape = BokiTheme.shapes.medium
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = BokiTheme.colors.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isLoading) "Transferring..." else "Transfer",
                        style = BokiTheme.typography.labelMedium
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = BokiTheme.colors.textSecondary
                    )
                ) {
                    Text(
                        text = "Cancel",
                        style = BokiTheme.typography.labelMedium
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationPotDropdown(
    selectedPot: PotSummaryDto?,
    availablePots: List<PotSummaryDto>,
    onPotSelected: (PotSummaryDto) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded && enabled },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedPot?.name ?: "",
            onValueChange = { },
            readOnly = true,
            label = { Text("Destination Pot") },
            placeholder = { Text("Select destination pot") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BokiTheme.colors.secondary,
                focusedLabelColor = BokiTheme.colors.secondary,
                cursorColor = BokiTheme.colors.secondary
            ),
            shape = BokiTheme.shapes.medium
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (availablePots.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "No other pots available",
                            color = BokiTheme.colors.textSecondary,
                            style = BokiTheme.typography.bodyMedium
                        )
                    },
                    onClick = { }
                )
            } else {
                availablePots.forEach { pot ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = pot.name,
                                    style = BokiTheme.typography.bodyMedium,
                                    color = BokiTheme.colors.onBackground
                                )
                                Text(
                                    text = "Balance: ${pot.balance.setScale(3)}",
                                    style = BokiTheme.typography.labelSmall,
                                    color = BokiTheme.colors.textSecondary
                                )
                            }
                        },
                        onClick = {
                            onPotSelected(pot)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PotActionsCardPreview() {
    BankAPITheme {
        PotActionsCard(
            onAddClick = {},
            onWithdrawClick = {},
            onTransferClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}