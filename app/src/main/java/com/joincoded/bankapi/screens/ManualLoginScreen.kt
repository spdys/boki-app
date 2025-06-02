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

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
            val firstName = username.trim().split(" ").firstOrNull() ?: username
            SharedPreferencesManager.saveUserName(context, firstName)
            onLoginSuccess()
            bankViewModel.clearStates()
        }
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
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { bankViewModel.getToken(username, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Logging in..." else "Login")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = navigateToRegister) {
            Text("Don't have an account? Register")
        }
    }
}