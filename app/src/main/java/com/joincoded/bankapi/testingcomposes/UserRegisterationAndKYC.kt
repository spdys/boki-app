package com.joincoded.bankapi.testingcomposes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joincoded.bankapi.viewmodel.BankViewModel


@Composable
fun SimpleRegistrationScreen(viewModel: BankViewModel = viewModel()) {
    var showKYC by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccessful by viewModel.isSuccessful.collectAsState()
    val error by viewModel.error.collectAsState()

    // Registration fields
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // KYC fields
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var civilId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!showKYC) {
            // Registration Form
            Text("Register", style = MaterialTheme.typography.headlineMedium)

            // Show loading state
            if (isLoading) {
                Text(
                    text = "Loading...",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Show error state
            error?.let {
                Text(
                    text = "Error: $it",
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Show success state
            if (isSuccessful) {
                Text(
                    text = "Registration successful!",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            TextField(
                value = password,
                onValueChange = { password = it
                    if (error != null) viewModel.clearStates()},
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isLoading
            )

            Button(
                onClick = {
                    viewModel.clearStates()
                    viewModel.register(username, password)
                    if (isSuccessful){
                        viewModel.clearStates()
                        viewModel.getToken(username, password)
                    }
                    if (isSuccessful)
                        showKYC = true // Move to KYC after registration
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if(isLoading) "Please wait..." else "Register")
            }

        } else {
            // KYC Form
            Text("KYC Information", style = MaterialTheme.typography.headlineMedium)

            // Show loading state
            if (isLoading) {
                Text(
                    text = "Submitting KYC...",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Show error state
            error?.let {
                Text(
                    text = "Error: $it",
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Show success state
            if (isSuccessful) {
                Text(
                    text = "KYC submitted successfully!",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            TextField(
                value = fullName,
                onValueChange = { fullName = it
                    if (error != null) viewModel.clearStates()},
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            TextField(
                value = phone,
                onValueChange = { phone = it
                    if (error != null) viewModel.clearStates()},
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            TextField(
                value = email,
                onValueChange = { email = it
                    if (error != null) viewModel.clearStates()},
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            TextField(
                value = civilId,
                onValueChange = { civilId = it
                    if (error != null) viewModel.clearStates()},
                label = { Text("Civil ID") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            TextField(
                value = address,
                onValueChange = { address = it
                    if (error != null) viewModel.clearStates()},
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            TextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it
                    if (error != null) viewModel.clearStates()},
                label = { Text("Date of Birth") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Button(
                onClick = {
                    viewModel.clearStates()
                    viewModel.submitKYC(fullName, phone, email, civilId, address, dateOfBirth)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Please wait..." else "Submit KYC")            }
        }
    }
}