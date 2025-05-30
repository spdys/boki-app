package com.joincoded.bankapi.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PinLoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter Username and PIN", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

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
            label = { Text("PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // TEMP: Hardcoded for testing — replace this when backend is ready
                if (username == "admin" && password == "1234") {
                    onLoginSuccess()
                } else {
                    error = true
                }

                // TODO: Replace temp login with real login API call
                /*
                val loginRequest = LoginRequest(username = username, password = password)
                viewModelScope.launch {
                    try {
                        val response = api.login(loginRequest)
                        if (response.isSuccessful) {
                            token = response.body()
                            onLoginSuccess()
                        } else {
                            error = true
                        }
                    } catch (e: Exception) {
                        error = true
                    }
                }
                */

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        if (error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Invalid credentials", color = MaterialTheme.colorScheme.error)
        }
    }
}
