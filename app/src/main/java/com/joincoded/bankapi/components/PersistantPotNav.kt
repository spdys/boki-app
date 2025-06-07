@file:OptIn(ExperimentalMaterial3Api::class)

package com.joincoded.bankapi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.joincoded.bankapi.data.AllocationType
import com.joincoded.bankapi.data.PotSummaryDto
import com.joincoded.bankapi.data.AccountSummaryDto
import com.joincoded.bankapi.data.AccountType
import com.joincoded.bankapi.ui.theme.BankAPITheme
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.viewmodel.BankViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Composable
fun AppWithPersistentPotNav(
    bankViewModel: BankViewModel,
    content: @Composable () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content
        content()

        // Persistent navigation bar
        PersistentPotNavBar(
            onPotIconClick = {
                showBottomSheet = true
                scope.launch {
                    bottomSheetState.show()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
        )

        // Bottom sheet with PotCardsSection
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = bottomSheetState,
                containerColor = Color.Transparent,
                contentColor = BokiTheme.colors.onBackground,
                dragHandle = null,
            ) {
                PotBottomSheetContent(
                    bankViewModel = bankViewModel,
                    onDismiss = {
                        scope.launch {
                            bottomSheetState.hide()
                            showBottomSheet = false
                        }
                    },
                    onPotCardClick = { pot ->
                        // Select the pot in ViewModel and dismiss sheet
                        bankViewModel.selectPot(pot)
                        scope.launch {
                            bottomSheetState.hide()
                            showBottomSheet = false
                        }
                        // Note: Navigation will be handled by the HomeScreen's onPotClicked
                        // since the pot is now selected in the ViewModel
                    }
                )
            }
        }
    }
}

@Composable
private fun PersistentPotNavBar(
    onPotIconClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        FloatingActionButton(
            onClick = onPotIconClick,
            modifier = Modifier
                .size(56.dp)
                .shadow(8.dp, CircleShape),
            containerColor = BokiTheme.colors.primary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = "View Pots",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PotBottomSheetContent(
    bankViewModel: BankViewModel,
    onDismiss: () -> Unit,
    onPotCardClick: (PotSummaryDto) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                BokiTheme.gradient,
                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .padding(top = 8.dp)
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(
                    Color.White.copy(alpha = 0.3f),
                    RoundedCornerShape(2.dp)
                )
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Header
        Text(
            text = "Your Pots",
            style = BokiTheme.typography.titleRegular,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Pot cards section
        PotCardsSection(
            bankViewModel = bankViewModel,
            onPotCardClick = onPotCardClick
        )

        // Bottom padding for gesture area
        Spacer(modifier = Modifier.height(40.dp))
    }
}