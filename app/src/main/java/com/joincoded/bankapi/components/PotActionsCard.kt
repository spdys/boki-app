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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.joincoded.bankapi.ui.theme.BankAPITheme
import com.joincoded.bankapi.ui.theme.BokiTheme

@Composable
fun PotActionsCard(
    onAddClick: () -> Unit,
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
                    .clickable { /* Withdraw onclick later */ }
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
                    .clickable { /* Transfer onclick later */ }
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

@Preview(showBackground = true)
@Composable
fun PotActionsCardPreview() {
    BankAPITheme {
        PotActionsCard(
            onAddClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}