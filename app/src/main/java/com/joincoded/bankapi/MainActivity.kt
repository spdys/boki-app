package com.joincoded.bankapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.joincoded.bankapi.screens.LoginScreen
import com.joincoded.bankapi.ui.theme.BankAPITheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joincoded.bankapi.testingcomposes.SimpleRegistrationScreen
import com.joincoded.bankapi.testingcomposes.TokenLoginScreen
import com.joincoded.bankapi.viewmodel.BankViewModel


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val bankViewModel: BankViewModel = viewModel()
            BankAPITheme  {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {


                    SimpleRegistrationScreen(bankViewModel)

//                    LoginScreen(
//                        bankViewModel = bankViewModel,
//                        onLoginSuccess = {},
//                        navigateToPinLogin = {},
//                    )

                }
            }
        }
    }
}
