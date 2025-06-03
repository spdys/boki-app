package com.joincoded.bankapi.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joincoded.bankapi.data.AllocationType
import com.joincoded.bankapi.ui.theme.BokiTheme
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun CreateOrEditPotPopup(
    initialName: String = "",
    initialType: AllocationType = AllocationType.FIXED,
    initialValue: String = "0",
    currency: String = "KWD",
    totalAllocated: BigDecimal,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, AllocationType, BigDecimal) -> Unit,
    validateInput: (String, BigDecimal, AllocationType) -> String?,
    onDelete: (() -> Unit)? = null,
) {
    if (!showDialog) return

    var name by remember { mutableStateOf(initialName) }
    var type by remember { mutableStateOf(initialType) }
    var valueText by remember { mutableStateOf(initialValue.toBigDecimalOrNull()?.stripTrailingZeros()?.toPlainString() ?: "0") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var sliderPosition by remember { mutableFloatStateOf(valueText.toFloatOrNull() ?: 0f) }
    var showConfirmation by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    val isFormChanged = name != initialName ||
            type != initialType ||
            valueText.toBigDecimalOrNull()?.compareTo(initialValue.toBigDecimalOrNull()?.stripTrailingZeros()) != 0

    // Validation effect
    LaunchedEffect(name, type, valueText) {
        val parsed = valueText.toBigDecimalOrNull()
        errorText = if (parsed != null) validateInput(name, parsed, type) else "Invalid number format"
    }

    if (!showConfirmation) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(if (initialName.isBlank()) "Create New Pot" else "Edit Pot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Pot Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AllocationTypeDropdown(selected = type, onSelectedChange = {
                        if (type != it) {
                            type = it
                            valueText = if (it == AllocationType.PERCENTAGE) "0.0" else "0"
                            sliderPosition = 0f
                        }
                    })

                    if (type == AllocationType.PERCENTAGE) {
                        LaunchedEffect(sliderPosition) {
                            valueText = sliderPosition.toBigDecimal()
                                .setScale(2, RoundingMode.HALF_UP)
                                .toPlainString()
                        }

                        Column(Modifier.fillMaxWidth()) {
                            Text("Allocation Percentage: ${(sliderPosition * 100).toInt()}%")

                            Slider(
                                value         = sliderPosition,
                                onValueChange = { sliderPosition = it },
                                valueRange    = 0f..1f,
                                steps         = 19
                            )

                            val total =
                                if (onDelete != null) totalAllocated + sliderPosition.toBigDecimal()
                                else totalAllocated          // static for create-mode

                            Text(
                                text  = "Total allocated so far: ${(total * BigDecimal(100)).toInt()}%",
                                style = BokiTheme.typography.labelSmall,
                                color = BokiTheme.colors.textSecondary
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = valueText,
                                onValueChange = { valueText = it },
                                label = { Text("Allocation Value") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currency,
                                style = BokiTheme.typography.headlineSmall,
                                color = BokiTheme.colors.onBackground
                            )
                        }
                    }

                    errorText?.let {
                        Text(
                            text = it,
                            color = BokiTheme.colors.error,
                            style = BokiTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                if (onDelete != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showDeleteConfirmation = true }) {
                            Text("Delete", color = BokiTheme.colors.error)
                        }

                        Row {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    valueText.toBigDecimalOrNull()?.let {
                                        onConfirm(name, type, it)
                                        showConfirmation = true
                                    }
                                },
                                enabled = isFormChanged && errorText == null
                            ) {
                                Text("Save")
                            }
                        }
                    }
                } else {
                    TextButton(
                        onClick = {
                            valueText.toBigDecimalOrNull()?.let {
                                onConfirm(name, type, it)
                                showConfirmation = true
                            }
                        },
                        enabled = isFormChanged && errorText == null
                    ) {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                if (onDelete == null) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = {
                showConfirmation = false
                onDismiss()
            },
            title = { Text("Update Scheduled") },
            text = {
                Text("Allocation changes will take effect on the next salary deposit. The current balance remains unchanged.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmation = false
                    onDismiss()
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this pot? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    deleteError = null
                    onDelete?.invoke()
                }) {
                    Text("Delete", color = BokiTheme.colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (deleteError != null) {
        AlertDialog(
            onDismissRequest = { deleteError = null },
            title = { Text("Delete Failed") },
            text = { Text(deleteError!!) },
            confirmButton = {
                TextButton(onClick = { deleteError = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun AllocationTypeDropdown(
    selected: AllocationType,
    onSelectedChange: (AllocationType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = AllocationType.entries.toTypedArray()

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