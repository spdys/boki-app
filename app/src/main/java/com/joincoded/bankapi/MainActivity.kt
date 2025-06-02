package com.joincoded.bankapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.joincoded.bankapi.ui.theme.BankAPITheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joincoded.bankapi.data.AccountSummaryDto
import com.joincoded.bankapi.data.AccountType
import com.joincoded.bankapi.navigation.AppNavigation
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.math.BigDecimal


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val bankViewModel: BankViewModel = viewModel()
            val dummyAccount = AccountSummaryDto(
                accountId = 1,
                accountNumber = "123456789",
                accountType = AccountType.MAIN,
                balance = BigDecimal("4444"),
                cardNumber = "4644520199994444",
                currency = "KWD",
                isActive = true,
                pots = null,
            )
            BankAPITheme  {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = bankViewModel)
                }
            }
        }
    }
}
