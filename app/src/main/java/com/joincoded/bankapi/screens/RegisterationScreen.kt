package com.joincoded.bankapi.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joincoded.bankapi.viewmodel.BankViewModel

@Composable
fun RegistrationScreen(
    bankViewModel: BankViewModel,
    onRegistrationSuccess: () -> Unit,
    navigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val error by bankViewModel.error.collectAsState()
    val isLoading by bankViewModel.isLoading.collectAsState()
    val isSuccessful by bankViewModel.isSuccessful.collectAsState()
    val isLoggedIn by bankViewModel.isLoggedIn.collectAsState()

    // Form state
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showValidationErrors by remember { mutableStateOf(false) }
    var registrationStep by remember { mutableStateOf("idle") }

    // Validation logic
    val isUsernameValid = username.trim().length >= 3
    val isPasswordValid = password.length >= 6
    val doPasswordsMatch = password == confirmPassword && confirmPassword.isNotEmpty()
    val isFormValid = isUsernameValid && isPasswordValid && doPasswordsMatch

    // Handle registration flow
    LaunchedEffect(isSuccessful, registrationStep) {
        when {
            isSuccessful && registrationStep == "registering" -> {
                // Registration successful, now get token
                registrationStep = "getting_token"
                bankViewModel.getToken(username.trim(), password)
                bankViewModel.clearStates()
            }
            isSuccessful && registrationStep == "getting_token" -> {
                // Token successful, navigate to KYC
                onRegistrationSuccess()
                bankViewModel.clearStates()
            }
        }
    }

    // Handle network errors
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            when {
                errorMessage.contains("network", ignoreCase = true) ||
                        errorMessage.contains("connection", ignoreCase = true) ||
                        errorMessage.contains("timeout", ignoreCase = true) -> {
                    Toast.makeText(context, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                    bankViewModel.clearStates()
                }
                errorMessage.contains("already exists", ignoreCase = true) ||
                        errorMessage.contains("username", ignoreCase = true) -> {
                    Toast.makeText(context, "Username already taken. Please choose another.", Toast.LENGTH_LONG).show()
                    bankViewModel.clearStates()
                }
            }
        }
    }

    // Input validation
    fun validateAndRegister() {
        showValidationErrors = true
        if (isFormValid) {
            showValidationErrors = false
            registrationStep = "registering"
            bankViewModel.register(username.trim(), password)
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
            text = "Create your Boki account!",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Username Field
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                if (showValidationErrors && it.trim().length >= 3) {
                    showValidationErrors = false
                }
            },
            label = { Text("Username") },
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Username"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && !isUsernameValid,
            enabled = !isLoading
        )

        // Username error
        if (showValidationErrors && !isUsernameValid) {
            Text(
                text = "Username must be at least 3 characters",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (showValidationErrors && it.length >= 6) {
                    showValidationErrors = false
                }
            },
            label = { Text("Password") },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Password"
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && !isPasswordValid,
            enabled = !isLoading
        )

        // Password error
        if (showValidationErrors && !isPasswordValid) {
            Text(
                text = "Password must be at least 6 characters",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                if (showValidationErrors && it == password && it.isNotEmpty()) {
                    showValidationErrors = false
                }
            },
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Confirm Password"
                )
            },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = (showValidationErrors && !doPasswordsMatch) ||
                    (confirmPassword.isNotEmpty() && password != confirmPassword),
            enabled = !isLoading
        )

        // Confirm password error
        if ((showValidationErrors && !doPasswordsMatch) ||
            (confirmPassword.isNotEmpty() && password != confirmPassword)) {
            Text(
                text = "Passwords don't match",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        // Backend error
        error?.let { errorMessage ->
            if (!errorMessage.contains("network", ignoreCase = true) &&
                !errorMessage.contains("connection", ignoreCase = true) &&
                !errorMessage.contains("username", ignoreCase = true)) {

                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Register Button
        Button(
            onClick = { validateAndRegister() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                when (registrationStep) {
                    "registering" -> "Creating account..."
                    "getting_token" -> "Logging in..."
                    else -> "Create Account"
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login Link
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium
            )

            TextButton(onClick = navigateToLogin) {
                Text("Sign In")
            }
        }
    }
}