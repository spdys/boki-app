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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.joincoded.bankapi.components.TransactionCard
import com.joincoded.bankapi.data.AccountSummaryDto
import com.joincoded.bankapi.data.PotSummaryDto
import com.joincoded.bankapi.data.TransactionHistoryResponse
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

// Transaction source for dropdown selection
sealed class TransactionSource {
    data class Account(val account: AccountSummaryDto) : TransactionSource()
    data class Pot(val pot: PotSummaryDto) : TransactionSource()

    fun getDisplayName(): String = when (this) {
        is Account -> "${account.accountType.name} Account"
        is Pot -> "${pot.name} Pot"
    }

    fun getSubInfo(): String = when (this) {
        is Account -> "Balance: ${account.balance} KWD • ${account.accountNumber.takeLast(4)}"
        is Pot -> "Balance: ${pot.balance} KWD"
    }

    fun getId(): String = when (this) {
        is Account -> "account_${account.accountId}"
        is Pot -> "pot_${pot.potId}"
    }
}

// Filter and sort options
enum class SortOption(val displayName: String) {
    DATE_RECENT("Most Recent"),
    DATE_OLDEST("Oldest First"),
    AMOUNT_HIGH("Highest Amount"),
    AMOUNT_LOW("Lowest Amount")
}

enum class DirectionFilter(val displayName: String) {
    ALL("All Transactions"),
    INCOMING("Incoming Only"),
    OUTGOING("Outgoing Only")
}

enum class TypeFilter(val displayName: String) {
    ALL("All Types"),
    TRANSFER("Transfers"),
    DEPOSIT("Deposits"),
    WITHDRAWAL("Withdrawals"),
    PAYMENT("Payments")
}

data class DateRange(
    val startDate: LocalDate?,
    val endDate: LocalDate?
) {
    fun getDisplayText(): String = when {
        startDate == null && endDate == null -> "All Time"
        startDate != null && endDate != null -> "${startDate.format(DateTimeFormatter.ofPattern("MMM dd"))} - ${endDate.format(DateTimeFormatter.ofPattern("MMM dd"))}"
        startDate != null -> "From ${startDate.format(DateTimeFormatter.ofPattern("MMM dd"))}"
        endDate != null -> "Until ${endDate.format(DateTimeFormatter.ofPattern("MMM dd"))}"
        else -> "All Time"
    }
}

@Composable
fun TransactionPickerScreen(
    bankViewModel: BankViewModel,
    navController: NavHostController
) {
    // Get all accounts and pots
    val allAccounts = bankViewModel.allAccountSummaries
    val allPots = allAccounts.flatMap { it.pots ?: emptyList() }

    // Create transaction sources
    val transactionSources = remember(allAccounts, allPots) {
        val sources = mutableListOf<TransactionSource>()
        allAccounts.forEach { account ->
            sources.add(TransactionSource.Account(account))
        }
        allPots.forEach { pot ->
            sources.add(TransactionSource.Pot(pot))
        }
        sources
    }

    // PERSISTENT filter states (remain when switching accounts)
    var searchQuery by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf(SortOption.DATE_RECENT) }
    var selectedDirection by remember { mutableStateOf(DirectionFilter.ALL) }
    var selectedType by remember { mutableStateOf(TypeFilter.ALL) }
    var maxTransactions by remember { mutableStateOf("50") }
    var dateRange by remember { mutableStateOf(DateRange(null, null)) }

    // Account/Pot selection (triggers re-render of transaction list)
    var selectedSource by remember { mutableStateOf<TransactionSource?>(transactionSources.firstOrNull()) }
    var showSourceDropdown by remember { mutableStateOf(false) }

    // UI state for filters
    var showSortMenu by remember { mutableStateOf(false) }
    var showDirectionMenu by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Mock transactions (replace with actual data from ViewModel based on selectedSource)
    val mockTransactions = remember(selectedSource) {
        generateMockTransactions(selectedSource)
    }

    // Apply filters and sorting (this is the 75% re-render when source changes)
    val filteredTransactions = remember(
        mockTransactions, searchQuery, selectedSort, selectedDirection,
        selectedType, maxTransactions, dateRange
    ) {
        var filtered = mockTransactions

        // Search by transaction name/description
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { transaction ->
                transaction.description?.contains(searchQuery, ignoreCase = true) == true ||
                        transaction.transactionType.contains(searchQuery, ignoreCase = true)
            }
        }

        // Filter by direction (Incoming/Outgoing)
        filtered = when (selectedDirection) {
            DirectionFilter.ALL -> filtered
            DirectionFilter.INCOMING -> filtered.filter {
                it.transactionType.uppercase() in listOf("CREDIT", "DEPOSIT", "TRANSFER_IN")
            }
            DirectionFilter.OUTGOING -> filtered.filter {
                it.transactionType.uppercase() in listOf("DEBIT", "WITHDRAWAL", "TRANSFER_OUT", "PAYMENT")
            }
        }

        // Filter by transaction type
        filtered = when (selectedType) {
            TypeFilter.ALL -> filtered
            TypeFilter.TRANSFER -> filtered.filter { it.transactionType.uppercase().contains("TRANSFER") }
            TypeFilter.DEPOSIT -> filtered.filter { it.transactionType.uppercase().contains("DEPOSIT") }
            TypeFilter.WITHDRAWAL -> filtered.filter { it.transactionType.uppercase().contains("WITHDRAWAL") }
            TypeFilter.PAYMENT -> filtered.filter { it.transactionType.uppercase().contains("PAYMENT") }
        }

        // Filter by date range
        if (dateRange.startDate != null || dateRange.endDate != null) {
            filtered = filtered.filter { transaction ->
                val transactionDate = OffsetDateTime.parse(transaction.createdAt).toLocalDate()
                val afterStart = dateRange.startDate?.let { transactionDate >= it } ?: true
                val beforeEnd = dateRange.endDate?.let { transactionDate <= it } ?: true
                afterStart && beforeEnd
            }
        }

        // Apply sorting
        val sorted = when (selectedSort) {
            SortOption.DATE_RECENT -> filtered.sortedByDescending { it.createdAt }
            SortOption.DATE_OLDEST -> filtered.sortedBy { it.createdAt }
            SortOption.AMOUNT_HIGH -> filtered.sortedByDescending { it.amount }
            SortOption.AMOUNT_LOW -> filtered.sortedBy { it.amount }
        }

        // Limit number of transactions
        val maxCount = maxTransactions.toIntOrNull() ?: 50
        sorted.take(maxCount)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BokiTheme.gradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            TransactionHeader(
                onBackClick = { navController.popBackStack() }
            )

            // Account/Pot Selector (TOP 25% - Fixed)
            AccountPotSelector(
                selectedSource = selectedSource,
                sources = transactionSources,
                showDropdown = showSourceDropdown,
                onDropdownToggle = { showSourceDropdown = !showSourceDropdown },
                onSourceSelected = { source ->
                    selectedSource = source
                    showSourceDropdown = false
                    // This triggers re-render of transaction list (75% of screen)
                },
                onDismiss = { showSourceDropdown = false }
            )

            // Filters Row (Persistent - stays when account changes)
            FiltersRow(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                selectedSort = selectedSort,
                selectedDirection = selectedDirection,
                selectedType = selectedType,
                maxTransactions = maxTransactions,
                dateRange = dateRange,
                showSortMenu = showSortMenu,
                showDirectionMenu = showDirectionMenu,
                showTypeMenu = showTypeMenu,
                onSortMenuToggle = { showSortMenu = !showSortMenu },
                onDirectionMenuToggle = { showDirectionMenu = !showDirectionMenu },
                onTypeMenuToggle = { showTypeMenu = !showTypeMenu },
                onSortSelected = { selectedSort = it; showSortMenu = false },
                onDirectionSelected = { selectedDirection = it; showDirectionMenu = false },
                onTypeSelected = { selectedType = it; showTypeMenu = false },
                onMaxTransactionsChange = { maxTransactions = it },
                onDateRangeClick = { showDatePicker = true }
            )

            // Transaction List (BOTTOM 75% - Re-renders when account changes)
            if (selectedSource != null) {
                TransactionList(
                    transactions = filteredTransactions,
                    selectedSource = selectedSource!!,
                    modifier = Modifier.weight(1f)
                )
            } else {
                EmptyTransactionState(
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Date Range Picker Dialog
        if (showDatePicker) {
            DateRangePickerDialog(
                currentRange = dateRange,
                onRangeSelected = { newRange ->
                    dateRange = newRange
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
private fun TransactionHeader(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "Transactions",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "View and filter transaction history",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun AccountPotSelector(
    selectedSource: TransactionSource?,
    sources: List<TransactionSource>,
    showDropdown: Boolean,
    onDropdownToggle: () -> Unit,
    onSourceSelected: (TransactionSource) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column {
            // Dropdown trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDropdownToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Account/Pot",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = selectedSource?.getDisplayName() ?: "Select account or pot",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    selectedSource?.let {
                        Text(
                            text = it.getSubInfo(),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
                Icon(
                    imageVector = if (showDropdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Dropdown",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Dropdown content
            if (showDropdown) {
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.2f)
                )

                sources.forEach { source ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSourceSelected(source) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (source) {
                                is TransactionSource.Account -> Icons.Default.AccountBalance
                                is TransactionSource.Pot -> Icons.Default.Savings
                            },
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = source.getDisplayName(),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = source.getSubInfo(),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }

                        if (selectedSource?.getId() == source.getId()) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.Green,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltersRow(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedSort: SortOption,
    selectedDirection: DirectionFilter,
    selectedType: TypeFilter,
    maxTransactions: String,
    dateRange: DateRange,
    showSortMenu: Boolean,
    showDirectionMenu: Boolean,
    showTypeMenu: Boolean,
    onSortMenuToggle: () -> Unit,
    onDirectionMenuToggle: () -> Unit,
    onTypeMenuToggle: () -> Unit,
    onSortSelected: (SortOption) -> Unit,
    onDirectionSelected: (DirectionFilter) -> Unit,
    onTypeSelected: (TypeFilter) -> Unit,
    onMaxTransactionsChange: (String) -> Unit,
    onDateRangeClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search by transaction name...",
                    color = Color.White.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White.copy(alpha = 0.8f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sort Filter
            FilterChip(
                selected = showSortMenu,
                onClick = onSortMenuToggle,
                label = { Text(selectedSort.displayName, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    labelColor = Color.White,
                    iconColor = Color.White
                )
            )

            // Direction Filter (Incoming/Outgoing)
            FilterChip(
                selected = showDirectionMenu,
                onClick = onDirectionMenuToggle,
                label = { Text(selectedDirection.displayName, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    labelColor = Color.White,
                    iconColor = Color.White
                )
            )

            // Type Filter
            FilterChip(
                selected = showTypeMenu,
                onClick = onTypeMenuToggle,
                label = { Text(selectedType.displayName, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    labelColor = Color.White,
                    iconColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date Range Filter
            FilterChip(
                selected = false,
                onClick = onDateRangeClick,
                label = { Text(dateRange.getDisplayText(), fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    labelColor = Color.White,
                    iconColor = Color.White
                )
            )

            // Max Transactions Input
            OutlinedTextField(
                value = maxTransactions,
                onValueChange = onMaxTransactionsChange,
                modifier = Modifier.width(100.dp),
                label = { Text("Max", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.8f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }

        // Dropdown Menus
        Box {
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { onSortMenuToggle() }
            ) {
                SortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.displayName) },
                        onClick = { onSortSelected(option) },
                        leadingIcon = if (selectedSort == option) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            DropdownMenu(
                expanded = showDirectionMenu,
                onDismissRequest = { onDirectionMenuToggle() }
            ) {
                DirectionFilter.entries.forEach { filter ->
                    DropdownMenuItem(
                        text = { Text(filter.displayName) },
                        onClick = { onDirectionSelected(filter) },
                        leadingIcon = if (selectedDirection == filter) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }

            DropdownMenu(
                expanded = showTypeMenu,
                onDismissRequest = { onTypeMenuToggle() }
            ) {
                TypeFilter.entries.forEach { filter ->
                    DropdownMenuItem(
                        text = { Text(filter.displayName) },
                        onClick = { onTypeSelected(filter) },
                        leadingIcon = if (selectedType == filter) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TransactionList(
    transactions: List<TransactionHistoryResponse>,
    selectedSource: TransactionSource,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Text(
                text = "${transactions.size} transactions for ${selectedSource.getDisplayName()}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(transactions) { transaction ->
            TransactionCard(
                transaction = transaction,
                currency = "KWD"
            )
        }

        if (transactions.isEmpty()) {
            item {
                EmptyTransactionState()
            }
        }
    }
}

@Composable
private fun EmptyTransactionState(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = "No Transactions",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No transactions found",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Try adjusting your filters or date range",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun DateRangePickerDialog(
    currentRange: DateRange,
    onRangeSelected: (DateRange) -> Unit,
    onDismiss: () -> Unit
) {
    // Simple date range picker dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date Range") },
        text = {
            Column {
                Text("Choose transaction date range:")
                Spacer(modifier = Modifier.height(16.dp))

                // Quick preset buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onRangeSelected(DateRange(
                                LocalDate.now().minusDays(7),
                                LocalDate.now()
                            ))
                        }
                    ) {
                        Text("Last 7 days", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onRangeSelected(DateRange(
                                LocalDate.now().minusMonths(1),
                                LocalDate.now()
                            ))
                        }
                    ) {
                        Text("Last month", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onRangeSelected(DateRange(null, null)) }
            ) {
                Text("Clear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Generate mock transactions based on selected source
private fun generateMockTransactions(source: TransactionSource?): List<TransactionHistoryResponse> {
    if (source == null) return emptyList()

    return listOf(
        TransactionHistoryResponse(
            id = 1L,
            amount = BigDecimal("25.000"),
            transactionType = "PAYMENT",
            description = "Coffee Shop Purchase",
            createdAt = LocalDateTime.now().minusHours(2).toString()
        ),
        TransactionHistoryResponse(
            id = 2L,
            amount = BigDecimal("150.000"),
            transactionType = "DEPOSIT",
            description = "Salary Payment",
            createdAt = LocalDateTime.now().minusDays(1).toString()
        ),
        TransactionHistoryResponse(
            id = 3L,
            amount = BigDecimal("75.500"),
            transactionType = "WITHDRAWAL",
            description = "ATM Withdrawal",
            createdAt = LocalDateTime.now().minusDays(2).toString()
        ),
        TransactionHistoryResponse(
            id = 4L,
            amount = BigDecimal("30.250"),
            transactionType = "TRANSFER_OUT",
            description = "Transfer to Noor",
            createdAt = LocalDateTime.now().minusDays(3).toString()
        ),
        TransactionHistoryResponse(
            id = 5L,
            amount = BigDecimal("100.000"),
            transactionType = "TRANSFER_IN",
            description = "Transfer from Ali",
            createdAt = LocalDateTime.now().minusDays(4).toString()
        )
    )
}