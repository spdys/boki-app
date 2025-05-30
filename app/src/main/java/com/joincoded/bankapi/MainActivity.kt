package com.joincoded.bankapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.joincoded.bankapi.navigation.AppNavigation
import com.joincoded.bankapi.screens.LoginScreen
import com.joincoded.bankapi.ui.theme.BankAPITheme
import com.joincoded.bankapi.viewmodel.UserViewModel
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BankAPITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        userViewModel = userViewModel,
                        onLoginSuccess = { /* TODO: Navigate to home screen */ },
                        navigateToPinLogin = { /* TODO: Navigate to PIN screen */ }
                    )

                    AppNavigation()
                }
            }
        }
    }
}
