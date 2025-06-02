@file:OptIn(ExperimentalMaterial3Api::class)

package com.joincoded.bankapi.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.joincoded.bankapi.components.BottomNavBar
import com.joincoded.bankapi.data.AccountType
import com.joincoded.bankapi.ui.theme.BokiColors
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.utils.SharedPreferencesManager
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.math.BigDecimal

// Card data class for Quick Pay
data class QuickPayCard(
    val id: String,
    val name: String,
    val type: QuickPayCardType,
    val balance: BigDecimal,
    val currency: String,
    val accountNumber: String,
    val gradient: List<Color>,
    val icon: ImageVector
)

enum class QuickPayCardType {
    ACCOUNT, POT
}

// Fixed sophisticated pot colors (6 options) - can be moved to your color file later
object SophisticatedCardColors {
    val cardColors = listOf(
        // Slate - Professional gray-blue
        listOf(Color(0xFF475569), Color(0xFF334155)),
        // Stone - Warm neutral
        listOf(Color(0xFF57534E), Color(0xFF44403C)),
        // Zinc - Cool neutral
        listOf(Color(0xFF52525B), Color(0xFF3F3F46)),
        // Neutral - Balanced gray
        listOf(Color(0xFF525252), Color(0xFF404040)),
        // Warm Gray - Subtle warmth
        listOf(Color(0xFF6B5B73), Color(0xFF544C57)),
        // Cool Blue-Gray - Sophisticated
        listOf(Color(0xFF64748B), Color(0xFF475569))
    )
}

@Composable
fun QuickPayScreen(
    bankViewModel: BankViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current

    // Get user name for greeting
    val savedUserName = SharedPreferencesManager.getSavedUserName(context)
    val displayName = if (savedUserName.isNotEmpty()) {
        SharedPreferencesManager.getFirstName(context)
    } else {
        "User"
    }

    // Create payment cards from accounts and pots
    val paymentCards = remember(bankViewModel.allAccountSummaries, bankViewModel.mainAccountSummary?.pots) {
        createQuickPayCards(bankViewModel)
    }

    var selectedCardIndex by remember { mutableStateOf(0) }
    var isNfcActive by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BokiTheme.gradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Clean Header Section
            QuickPayHeader(
                userName = displayName,
                onBackClick = { navController.popBackStack() },
                onNfcToggle = { isNfcActive = !isNfcActive },
                isNfcActive = isNfcActive
            )

            // Card Stack Section
            if (paymentCards.isNotEmpty()) {
                CardStackSection(
                    cards = paymentCards,
                    selectedIndex = selectedCardIndex,
                    onCardSelected = { selectedCardIndex = it },
                    modifier = Modifier.weight(1f)
                )

                // Payment Actions
                PaymentActionsSection(
                    selectedCard = paymentCards[selectedCardIndex],
                    isNfcActive = isNfcActive,
                    onPayClick = { card ->
                        handleQuickPayment(card, navController)
                    }
                )
            } else {
                // Empty state
                EmptyCardsState(
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Bottom Navigation
        BottomNavBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun QuickPayHeader(
    userName: String,
    onBackClick: () -> Unit,
    onNfcToggle: () -> Unit,
    isNfcActive: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BokiColors.BackgroundDark.copy(alpha = 0.9f),
                        Color.Transparent
                    )
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(BokiColors.CardTransparentDark, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = BokiColors.TextDark
                    )
                }

                Column {
                    Text(
                        text = "Quick Pay",
                        color = BokiColors.TextDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select card to pay, $userName",
                        color = BokiColors.TextDark.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            // NFC Toggle
            IconButton(
                onClick = onNfcToggle,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isNfcActive) BokiColors.AccentRed.copy(alpha = 0.2f) else BokiColors.CardTransparentDark,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Nfc,
                    contentDescription = "NFC",
                    tint = if (isNfcActive) BokiColors.AccentRed else BokiColors.TextDark.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun CardStackSection(
    cards: List<QuickPayCard>,
    selectedIndex: Int,
    onCardSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy((-160).dp),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        itemsIndexed(cards) { index, card ->
            val isSelected = index == selectedIndex
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.95f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "cardScale"
            )

            QuickPayCardItem(
                card = card,
                isSelected = isSelected,
                onClick = { onCardSelected(index) },
                modifier = Modifier
                    .scale(scale)
                    .graphicsLayer {
                        translationY = if (isSelected) 0f else (index - selectedIndex) * 12f
                        alpha = if (index <= selectedIndex + 2) 1f else 0.7f
                    }
            )
        }
    }
}

@Composable
private fun QuickPayCardItem(
    card: QuickPayCard,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pressScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .scale(pressScale)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isPressed = true },
                    onDragEnd = {
                        isPressed = false
                        onClick()
                    }
                ) { _, _ -> }
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 12.dp else 6.dp
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(card.gradient),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            // Subtle glassmorphism
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            radius = 400f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Card Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = card.icon,
                            contentDescription = card.name,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = card.type.name,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Contactless Symbol
                    Icon(
                        imageVector = Icons.Default.Contactless,
                        contentDescription = "Contactless",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Card Content
                Column {
                    Text(
                        text = card.name,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${card.balance.setScale(3)} ${card.currency}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.accountNumber,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Selection Indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(10.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun PaymentActionsSection(
    selectedCard: QuickPayCard,
    isNfcActive: Boolean,
    onPayClick: (QuickPayCard) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                BokiColors.CardTransparentDark,
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // NFC Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        if (isNfcActive) BokiColors.Success else BokiColors.SoftGray,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isNfcActive) "NFC Ready" else "NFC Disabled",
                color = BokiColors.TextDark.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Pay Button
        Button(
            onClick = { onPayClick(selectedCard) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BokiColors.AccentRed,
                disabledContainerColor = BokiColors.SoftGray
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = isNfcActive
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Contactless,
                    contentDescription = "Pay",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (isNfcActive) "Tap to Pay" else "Enable NFC to Pay",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Selected Card Info
        Text(
            text = "Selected: ${selectedCard.name} • ${selectedCard.balance.setScale(3)} ${selectedCard.currency}",
            color = BokiColors.TextDark.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EmptyCardsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CreditCard,
            contentDescription = "No cards",
            tint = BokiColors.TextDark.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No payment cards available",
            color = BokiColors.TextDark.copy(alpha = 0.6f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Add an account or pot to get started",
            color = BokiColors.TextDark.copy(alpha = 0.4f),
            fontSize = 14.sp
        )
    }
}

// Helper function to create payment cards with fixed sophisticated colors
private fun createQuickPayCards(bankViewModel: BankViewModel): List<QuickPayCard> {
    val cards = mutableListOf<QuickPayCard>()

    // Add account cards with sophisticated colors
    bankViewModel.allAccountSummaries.forEach { account ->
        cards.add(
            QuickPayCard(
                id = account.accountNumber,
                name = "${account.accountType.name} Account",
                type = QuickPayCardType.ACCOUNT,
                balance = account.balance,
                currency = account.currency,
                accountNumber = account.accountNumber,
                gradient = if (account.accountType == AccountType.MAIN) {
                    SophisticatedCardColors.cardColors[0]
                } else {
                    SophisticatedCardColors.cardColors[1]
                },
                icon = if (account.accountType == AccountType.MAIN) {
                    Icons.Default.AccountBalance
                } else {
                    Icons.Default.Savings
                }
            )
        )
    }

    // Add pot cards with fixed sophisticated colors
    bankViewModel.mainAccountSummary?.pots?.forEachIndexed { index, pot ->
        cards.add(
            QuickPayCard(
                id = pot.name,
                name = pot.name,
                type = QuickPayCardType.POT,
                balance = pot.balance,
                currency = bankViewModel.mainAccountSummary?.currency ?: "KWD",
                accountNumber = "Pot • ${pot.name}",
                gradient = SophisticatedCardColors.cardColors[(index + 2) % SophisticatedCardColors.cardColors.size],
                icon = getQuickPayPotIcon(pot.name)
            )
        )
    }

    return cards
}

// Helper function for pot icons (unique function name to avoid conflicts)
private fun getQuickPayPotIcon(potName: String): ImageVector {
    return when (potName.lowercase()) {
        "fuel", "car", "transport" -> Icons.Default.DirectionsCar
        "food", "dining", "restaurant", "fast food" -> Icons.Default.Restaurant
        "groceries", "shopping" -> Icons.Default.ShoppingCart
        "coffee", "cafe" -> Icons.Default.LocalCafe
        "housing", "home", "rent" -> Icons.Default.Home
        "bills", "utilities" -> Icons.Default.Receipt
        "entertainment", "fun" -> Icons.Default.Movie
        "health", "medical" -> Icons.Default.LocalHospital
        "education", "learning" -> Icons.Default.School
        "travel", "vacation" -> Icons.Default.Flight
        "savings", "emergency" -> Icons.Default.Savings
        "subscriptions" -> Icons.Default.Subscriptions
        else -> Icons.Default.AccountBalanceWallet
    }
}

// Handle payment action
private fun handleQuickPayment(card: QuickPayCard, navController: NavHostController) {
    // Navigate to payment confirmation or success screen
    navController.navigate("payment_success/${card.id}")
}