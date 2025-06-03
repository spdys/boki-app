package com.joincoded.bankapi.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.joincoded.bankapi.data.AccountSummaryDto
import com.joincoded.bankapi.data.PotSummaryDto
import com.joincoded.bankapi.data.TransferRequest
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.math.BigDecimal

// Transfer destination types
sealed class TransferDestination {
    data class MyAccount(val account: AccountSummaryDto) : TransferDestination()
    data class MyPot(val pot: PotSummaryDto) : TransferDestination()
    data class ExternalContact(val name: String, val accountNumber: String, val bank: String) : TransferDestination()

    fun getDisplayName(): String = when (this) {
        is MyAccount -> "${account.accountType.name} Account"
        is MyPot -> "${pot.name} Pot"
        is ExternalContact -> name
    }

    fun getSubtitle(): String = when (this) {
        is MyAccount -> account.accountNumber.takeLast(4)
        is MyPot -> pot.name.take(4).uppercase()
        is ExternalContact -> "$bank • ${accountNumber.takeLast(4)}"
    }

    fun getBalance(): BigDecimal? = when (this) {
        is MyAccount -> account.balance
        is MyPot -> pot.balance
        is ExternalContact -> null // External contacts don't show balance
    }

    fun getId(): Long = when (this) {
        is MyAccount -> account.accountId
        is MyPot -> pot.potId
        is ExternalContact -> -1L // Dummy ID for external contacts
    }

    fun isExternal(): Boolean = this is ExternalContact
}

@Composable
fun TransferMoneyScreen(
    bankViewModel: BankViewModel,
    navController: NavHostController
) {
    // Get user's accounts and pots
    val allAccounts = bankViewModel.allAccountSummaries
    val allPots = allAccounts.flatMap { it.pots ?: emptyList() }

    // Create dummy external contacts for demo
    val externalContacts = remember {
        listOf(
            TransferDestination.ExternalContact("Noor", "1234567890", "KIB"),
            TransferDestination.ExternalContact("Ali", "2345678901", "Gulf Bank"),
            TransferDestination.ExternalContact("Asma", "3456789012", "NBK"),
            TransferDestination.ExternalContact("Mishal", "4567890123", "ABK")
        )
    }

    // Create sections
    val myDestinations = remember(allAccounts, allPots) {
        val destinations = mutableListOf<TransferDestination>()
        allAccounts.forEach { account ->
            destinations.add(TransferDestination.MyAccount(account))
        }
        allPots.forEach { pot ->
            destinations.add(TransferDestination.MyPot(pot))
        }
        destinations
    }

    // State management
    var selectedSource by remember { mutableStateOf<TransferDestination?>(null) }
    var selectedDestination by remember { mutableStateOf<TransferDestination?>(null) }
    var amount by remember { mutableStateOf("") }
    var showSourcePicker by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(TransferStep.SELECT_SOURCE) }

    // Loading and error states
    val isLoading by bankViewModel.isLoading.collectAsState()
    val isSuccessful by bankViewModel.isSuccessful.collectAsState()
    val error by bankViewModel.error.collectAsState()

    // Handle success
    LaunchedEffect(isSuccessful) {
        if (isSuccessful) {
            navController.navigate("success")
            bankViewModel.clearStates()
        }
    }

    // Handle error
    LaunchedEffect(error) {
        if (error != null) {
            // Error handling is done via UI state
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BokiTheme.gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            TransferHeader(
                onBackClick = {
                    if (currentStep == TransferStep.SELECT_SOURCE) {
                        navController.popBackStack()
                    } else {
                        currentStep = TransferStep.SELECT_SOURCE
                        selectedSource = null
                        selectedDestination = null
                        amount = ""
                    }
                },
                step = currentStep
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (currentStep) {
                TransferStep.SELECT_SOURCE -> {
                    SourceSelectionStep(
                        myDestinations = myDestinations,
                        onSourceSelected = { source ->
                            selectedSource = source
                            currentStep = TransferStep.SELECT_DESTINATION
                        }
                    )
                }

                TransferStep.SELECT_DESTINATION -> {
                    DestinationSelectionStep(
                        myDestinations = myDestinations.filter { it.getId() != selectedSource?.getId() },
                        externalContacts = externalContacts,
                        onDestinationSelected = { destination ->
                            selectedDestination = destination
                            currentStep = TransferStep.ENTER_AMOUNT
                        }
                    )
                }

                TransferStep.ENTER_AMOUNT -> {
                    AmountEntryStep(
                        source = selectedSource!!,
                        destination = selectedDestination!!,
                        amount = amount,
                        onAmountChange = { amount = it },
                        isLoading = isLoading,
                        error = error,
                        onTransfer = {
                            // Execute transfer
                            val sourceId = selectedSource!!.getId()
                            val destinationId = selectedDestination!!.getId()
                            val transferAmount = BigDecimal(amount)

                            if (selectedDestination!!.isExternal()) {
                                // For demo purposes,  show success for external transfers
                                navController.navigate("success")
                            } else {
                                // Real internal transfer
                                bankViewModel.transfer(
                                    TransferRequest(
                                        sourceId = sourceId,
                                        destinationId = destinationId,
                                        amount = transferAmount
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

enum class TransferStep {
    SELECT_SOURCE, SELECT_DESTINATION, ENTER_AMOUNT
}

@Composable
private fun TransferHeader(
    onBackClick: () -> Unit,
    step: TransferStep
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "Transfer Money",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when (step) {
                    TransferStep.SELECT_SOURCE -> "Select source account"
                    TransferStep.SELECT_DESTINATION -> "Choose destination"
                    TransferStep.ENTER_AMOUNT -> "Enter amount"
                },
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SourceSelectionStep(
    myDestinations: List<TransferDestination>,
    onSourceSelected: (TransferDestination) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "From",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(myDestinations) { destination ->
            TransferDestinationCard(
                destination = destination,
                showBalance = true,
                onClick = { onSourceSelected(destination) }
            )
        }
    }
}

@Composable
private fun DestinationSelectionStep(
    myDestinations: List<TransferDestination>,
    externalContacts: List<TransferDestination>,
    onDestinationSelected: (TransferDestination) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // My Accounts Section
        if (myDestinations.isNotEmpty()) {
            item {
                Text(
                    text = "Send to Accounts/Pots",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(myDestinations) { destination ->
                TransferDestinationCard(
                    destination = destination,
                    showBalance = true,
                    onClick = { onDestinationSelected(destination) }
                )
            }
        }

        // External Contacts Section
        item {
            Text(
                text = "Send to Others",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(externalContacts) { contact ->
            TransferDestinationCard(
                destination = contact,
                showBalance = false,
                onClick = { onDestinationSelected(contact) }
            )
        }
    }
}

@Composable
private fun AmountEntryStep(
    source: TransferDestination,
    destination: TransferDestination,
    amount: String,
    onAmountChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onTransfer: () -> Unit
) {
    val isValidAmount = amount.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true
    val hasEnoughBalance = if (source.isExternal()) true else {
        source.getBalance()?.let { balance ->
            amount.toDoubleOrNull()?.let { amountValue ->
                balance >= BigDecimal(amountValue)
            } ?: false
        } ?: false
    }
    val canTransfer = isValidAmount && hasEnoughBalance && !isLoading

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Transfer Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "From",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = source.getDisplayName(),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "To",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "To",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = destination.getDisplayName(),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Amount Input
        Text(
            text = "Amount",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "0.000",
                    color = Color.White.copy(alpha = 0.6f)
                )
            },
            suffix = {
                Text(
                    text = "KWD",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = when {
                    error != null -> Color.Red.copy(alpha = 0.8f)
                    isValidAmount && hasEnoughBalance -> Color.Green.copy(alpha = 0.8f)
                    amount.isNotBlank() -> Color.Red.copy(alpha = 0.8f)
                    else -> Color.White.copy(alpha = 0.4f)
                },
                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            isError = error != null || (amount.isNotBlank() && (!isValidAmount || !hasEnoughBalance))
        )

        // Error messages
        if (error != null) {
            Text(
                text = error,
                color = Color.Red.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        } else if (amount.isNotBlank() && !isValidAmount) {
            Text(
                text = "Please enter a valid amount",
                color = Color.Red.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        } else if (amount.isNotBlank() && isValidAmount && !hasEnoughBalance && !source.isExternal()) {
            Text(
                text = "Insufficient balance",
                color = Color.Red.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        // Available balance (only for internal accounts)
        if (!source.isExternal()) {
            source.getBalance()?.let { balance ->
                Text(
                    text = "Available: $balance KWD",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Transfer Button
        Button(
            onClick = onTransfer,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = canTransfer,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Transfer",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (destination.isExternal()) "Send Money" else "Transfer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TransferDestinationCard(
    destination: TransferDestination,
    showBalance: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (destination) {
                        is TransferDestination.MyAccount -> Icons.Default.AccountBalance
                        is TransferDestination.MyPot -> Icons.Default.Savings
                        is TransferDestination.ExternalContact -> Icons.Default.Person
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = destination.getDisplayName(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = destination.getSubtitle(),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )

                if (showBalance && !destination.isExternal()) {
                    destination.getBalance()?.let { balance ->
                        Text(
                            text = "$balance KWD",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Select",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}