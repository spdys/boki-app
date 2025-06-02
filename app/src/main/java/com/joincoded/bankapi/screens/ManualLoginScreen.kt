package com.joincoded.bankapi.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joincoded.bankapi.utils.SharedPreferencesManager
import com.joincoded.bankapi.viewmodel.BankViewModel


@Composable
fun ManualLoginScreen(
    bankViewModel: BankViewModel,
    onLoginSuccess: () -> Unit,
    navigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val error by bankViewModel.error.collectAsState()
    val isLoading by bankViewModel.isLoading.collectAsState()
    val isSuccessful by bankViewModel.isSuccessful.collectAsState()

    // Remember username from last login, while clear password
    var username by remember { mutableStateOf(SharedPreferencesManager.getLastUsername(context)) }
    var password by remember { mutableStateOf("") }
    var showValidationErrors by remember { mutableStateOf(false) }

    // Get saved user's first name for greeting (from KYC data)
    val savedName = SharedPreferencesManager.getSavedUserName(context)
    val greetingText = if (savedName.isNotEmpty()) {
        "Welcome to your virtual wallet, $savedName!"
    } else {
        "Welcome to your virtual wallet!"
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            bankViewModel.clearStates()
        }
    }

    LaunchedEffect(isSuccessful) {
        if (isSuccessful) {
            // Save username for next login
            SharedPreferencesManager.saveLastUsername(context, username.trim())
            onLoginSuccess()
            bankViewModel.clearStates()
        }
    }

    // Input validation function
    fun validateAndLogin() {
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()

        if (trimmedUsername.isEmpty() || trimmedPassword.isEmpty()) {
            showValidationErrors = true
            return
        }

        showValidationErrors = false
        bankViewModel.getToken(trimmedUsername, trimmedPassword)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = greetingText,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                // clear validation errors when user starts typing
                if (showValidationErrors && it.trim().isNotEmpty()) {
                    showValidationErrors = false
                }
            },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && username.trim().isEmpty()
        )

        // show username error if validation failed
        if (showValidationErrors && username.trim().isEmpty()) {
            Text(
                text = "Username is required",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                // clear validation errors when user starts typing
                if (showValidationErrors && it.trim().isNotEmpty()) {
                    showValidationErrors = false
                }
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && password.trim().isEmpty()
        )

        // show password error if validation failed
        if (showValidationErrors && password.trim().isEmpty()) {
            Text(
                text = "Password is required",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { validateAndLogin() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Logging in..." else "Login")
        }

        Spacer(modifier = Modifier.height(12.dp))

    }
}