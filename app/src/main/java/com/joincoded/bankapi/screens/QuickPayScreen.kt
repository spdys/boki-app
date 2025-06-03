@file:OptIn(ExperimentalMaterial3Api::class)

package com.joincoded.bankapi.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.joincoded.bankapi.components.BottomNavBar
import com.joincoded.bankapi.data.AccountType
import com.joincoded.bankapi.data.CardPaymentRequest
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.viewmodel.BankViewModel
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.math.BigDecimal
import java.util.concurrent.Executor

// Banking card data class
data class BankingCard(
    val id: String,
    val cardName: String,
    val maskedCardNumber: String,
    val cardType: String,
    val gradientIndex: Int,
    val icon: ImageVector,
    val isVirtual: Boolean = false
)

@Composable
fun QuickPayScreen(
    bankViewModel: BankViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val executor: Executor = ContextCompat.getMainExecutor(context)
    val haptic = LocalHapticFeedback.current

    // State management
    var selectedCardIndex by remember { mutableIntStateOf(-1) }
    var isCardSelected by remember { mutableStateOf(false) }
    val isNfcActive by bankViewModel.nfcEnabled.collectAsState()
    val isPaymentProcessing by bankViewModel.isLoading.collectAsState()
    val isPaymentSuccessful by bankViewModel.isSuccessful.collectAsState()
    var showSuccessOverlay by remember { mutableStateOf(false) }

    // Handle error state
    val errorState by bankViewModel.error.collectAsState()
    val currentError = errorState

    // Get cardholder name
    val cardholderName = bankViewModel.getCardholderName()

    // Create banking cards
    val bankingCards = remember(bankViewModel.allAccountSummaries, bankViewModel.mainAccountSummary?.pots) {
        createBankingCards(bankViewModel, cardholderName)
    }

    // Biometric setup
    val biometricPrompt = activity?.let {
        BiometricPrompt(it, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (selectedCardIndex >= 0) {
                    val selectedCard = bankingCards[selectedCardIndex]
                    val request = CardPaymentRequest(
                        cardNumberOrToken = selectedCard.id,
                        amount = BigDecimal("25.000"), // Demo amount from NFC/Postman
                        destinationId = 1L // Merchant ID
                    )
                    bankViewModel.makeCardPayment(request)
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Handled by ViewModel
            }

            override fun onAuthenticationFailed() {
                // Handled by ViewModel
            }
        })
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Authenticate Payment")
        .setSubtitle("Use your biometric credential to authorize this payment")
        .setNegativeButtonText("Cancel")
        .build()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BokiTheme.gradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            QuickPayHeader(
                onBackClick = {
                    if (isCardSelected) {
                        // Return to card stack
                        isCardSelected = false
                        selectedCardIndex = -1
                        bankViewModel.resetPaymentFlow()
                    } else {
                        navController.popBackStack()
                    }
                },
                onNfcToggle = {
                    if (isCardSelected) {
                        bankViewModel.toggleNfc()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                isNfcActive = isNfcActive,
                showBackArrow = isCardSelected
            )

            if (bankingCards.isNotEmpty()) {
                // Card stack or selected card
                if (isCardSelected && selectedCardIndex >= 0) {
                    // Show selected card centered
                    SelectedCardView(
                        card = bankingCards[selectedCardIndex],
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Show card stack
                    CardStackView(
                        cards = bankingCards,
                        onCardSelected = { index ->
                            selectedCardIndex = index
                            isCardSelected = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Payment action section (only when card is selected)
                if (isCardSelected && selectedCardIndex >= 0) {
                    PaymentActionSection(
                        isNfcActive = isNfcActive,
                        isProcessing = isPaymentProcessing,
                        selectedCard = bankingCards[selectedCardIndex],
                        onPayClick = {
                            if (isNfcActive && !isPaymentProcessing) {
                                biometricPrompt?.authenticate(promptInfo)
                            }
                        }
                    )
                }
            } else {
                EmptyCardsState(modifier = Modifier.weight(1f))
            }
        }

        // Bottom Navigation
        BottomNavBar(
            navController = navController,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Monitor payment success
        LaunchedEffect(isPaymentSuccessful) {
            if (isPaymentSuccessful) {
                showSuccessOverlay = true
                bankViewModel.clearStates()
            }
        }

        // Success/Error overlays
        if (showSuccessOverlay) {
            PaymentSuccessOverlay(
                onDismiss = {
                    showSuccessOverlay = false
                    isCardSelected = false
                    selectedCardIndex = -1
                    bankViewModel.resetPaymentFlow()
                    navController.popBackStack()
                }
            )
        }

        currentError?.let { errorMessage ->
            PaymentErrorOverlay(
                error = errorMessage,
                onDismiss = { bankViewModel.clearStates() }
            )
        }
    }
}

@Composable
private fun QuickPayHeader(
    onBackClick: () -> Unit,
    onNfcToggle: () -> Unit,
    isNfcActive: Boolean,
    showBackArrow: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(
                imageVector = if (showBackArrow) Icons.Default.ArrowBack else Icons.Default.Close,
                contentDescription = if (showBackArrow) "Back" else "Close",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = "QuickPay",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isNfcActive) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                    CircleShape
                )
                .pointerInput(Unit) {
                    detectTapGestures { onNfcToggle() }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Contactless,
                contentDescription = "NFC",
                tint = if (isNfcActive) Color.White else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CardStackView(
    cards: List<BankingCard>,
    onCardSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy((-80).dp),
        contentPadding = PaddingValues(vertical = 40.dp)
    ) {
        itemsIndexed(cards) { index, card ->
            val scale by animateFloatAsState(
                targetValue = when (index) {
                    0 -> 1f
                    1 -> 0.95f
                    2 -> 0.9f
                    else -> 0.85f
                },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "cardScale"
            )

            val alpha by animateFloatAsState(
                targetValue = when (index) {
                    0 -> 1f
                    1 -> 0.9f
                    2 -> 0.8f
                    else -> 0.7f
                },
                animationSpec = tween(300),
                label = "cardAlpha"
            )

            BankCard(
                card = card,
                onClick = { onCardSelected(index) },
                modifier = Modifier
                    .scale(scale)
                    .graphicsLayer {
                        this.alpha = alpha
                        translationY = index * 20f
                    }
            )
        }
    }
}

@Composable
private fun SelectedCardView(
    card: BankingCard,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        BankCard(
            card = card,
            onClick = { },
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun BankCard(
    card: BankingCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardGradient = BokiTheme.quickPayCardGradient(card.gradientIndex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(Unit) {
                detectTapGestures { onClick() }
            },
        shape = BokiTheme.shapes.card,
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardGradient, BokiTheme.shapes.card)
        ) {
            // Subtle texture overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            radius = 400f
                        )
                    )
            )

            // Card header with chip and contactless
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = card.icon,
                        contentDescription = "Chip",
                        tint = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Contactless symbol
                Icon(
                    imageVector = Icons.Default.Contactless,
                    contentDescription = "Contactless",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Virtual card indicator
            if (card.isVirtual) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(
                            Color.White.copy(alpha = 0.25f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "VIRTUAL",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Card name positioned in lower left for readability
            Text(
                text = card.cardName,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 50.dp)
            )

            // Card number positioned bottom right
            Text(
                text = card.maskedCardNumber,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 2.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun PaymentActionSection(
    isNfcActive: Boolean,
    isProcessing: Boolean,
    selectedCard: BankingCard,
    onPayClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.4f),
                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Ready indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        if (isNfcActive) Color.Green else Color.Gray,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isNfcActive) "Ready to Pay" else "Enable Contactless",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Pay button
        Button(
            onClick = onPayClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isNfcActive) Color.White else Color.Gray.copy(alpha = 0.3f),
                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(25.dp),
            enabled = isNfcActive && !isProcessing
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Touch ID",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Pay with Touch ID",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Selected card info
        Text(
            text = "${selectedCard.cardName} • ${selectedCard.maskedCardNumber.takeLast(4)}",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun EmptyCardsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CreditCard,
            contentDescription = "No cards",
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Payment Methods",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PaymentSuccessOverlay(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .pointerInput(Unit) {
                detectTapGestures { onDismiss() }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = Color.Green,
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = "Payment Complete",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap to continue",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PaymentErrorOverlay(error: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .pointerInput(Unit) {
                detectTapGestures { onDismiss() }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Payment Declined",
                tint = Color.Red,
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = "Payment Declined",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = error,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

// Helper functions
private fun createBankingCards(
    bankViewModel: BankViewModel,
    cardholderName: String
): List<BankingCard> {
    val cards = mutableListOf<BankingCard>()
    var gradientIndex = 0

    // Add account cards
    bankViewModel.allAccountSummaries.forEach { account ->
        cards.add(
            BankingCard(
                id = account.accountNumber,
                cardName = account.accountType.name,
                maskedCardNumber = maskCardNumber(account.cardNumber ?: account.accountNumber),
                cardType = account.accountType.name,
                gradientIndex = gradientIndex % 6,
                icon = getAccountIcon(account.accountType),
                isVirtual = false
            )
        )
        gradientIndex++
    }

    // Add pot virtual cards
    bankViewModel.mainAccountSummary?.pots?.forEach { pot ->
        cards.add(
            BankingCard(
                id = pot.cardToken ?: "POT_${pot.potId}",
                cardName = pot.name.uppercase(),
                maskedCardNumber = maskCardNumber(pot.cardToken ?: "Virtual Card"),
                cardType = pot.name.uppercase(),
                gradientIndex = gradientIndex % 6,
                icon = Icons.Default.AccountBalanceWallet,
                isVirtual = true
            )
        )
        gradientIndex++
    }

    return cards
}

private fun maskCardNumber(cardNumber: String): String {
    if (cardNumber.length < 4) return "•••• •••• •••• ••••"
    val lastFour = cardNumber.takeLast(4)
    return "•••• •••• •••• $lastFour"
}

private fun getAccountIcon(accountType: AccountType): ImageVector {
    return when (accountType) {
        AccountType.MAIN -> Icons.Default.CreditCard
        AccountType.SAVINGS -> Icons.Default.Savings
    }
}