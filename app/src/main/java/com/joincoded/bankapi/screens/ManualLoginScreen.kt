package com.joincoded.bankapi.screens

import android.content.Intent
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

    //  in-memory attempt tracking (no SharedPreferences needed)
    var failedAttempts by remember { mutableIntStateOf(0) }
    var isLocked by remember { mutableStateOf(false) }
    var lockoutTimeRemaining by remember { mutableIntStateOf(0) }

    // Remember username from last login, clear password
    var username by remember { mutableStateOf(SharedPreferencesManager.getLastUsername(context)) }
    var password by remember { mutableStateOf("") }
    var showValidationErrors by remember { mutableStateOf(false) }

    //  saved user's first name for greeting
    val savedName = SharedPreferencesManager.getFirstName(context)
    val greetingText = if (savedName.isNotEmpty()) {
        "Welcome to your virtual wallet, $savedName!"
    } else {
        "Welcome to your virtual wallet!"
    }

    // Lockout countdown timer
    LaunchedEffect(isLocked) {
        if (isLocked && lockoutTimeRemaining > 0) {
            while (lockoutTimeRemaining > 0) {
                kotlinx.coroutines.delay(1000)
                lockoutTimeRemaining--
            }
            // After lockout ends, restart the app for security
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP.or(Intent.FLAG_ACTIVITY_NEW_TASK))
            context.startActivity(intent)
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    LaunchedEffect(error) {
        error?.let {
            // Handle login failure - increment attempts for credential errors only
            if (it.contains("Wrong credentials", ignoreCase = true) ||
                it.contains("Invalid", ignoreCase = true) ||
                it.contains("credentials", ignoreCase = true)) {

                failedAttempts++

                when (failedAttempts) {
                    1 -> {
                        Toast.makeText(context, "Invalid credentials. 2 attempts remaining", Toast.LENGTH_LONG).show()
                    }
                    2 -> {
                        Toast.makeText(context, "Invalid credentials. 1 attempt remaining", Toast.LENGTH_LONG).show()
                    }
                    3 -> {
                        // Trigger lockout
                        isLocked = true
                        lockoutTimeRemaining = 3 // 3 seconds
                        Toast.makeText(context, "Too many failed attempts. App will restart for security...", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                // Network errors - don't count as attempts
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
            bankViewModel.clearStates()
        }
    }

    LaunchedEffect(isSuccessful) {
        if (isSuccessful) {
            // Reset attempts on successful login
            failedAttempts = 0
            SharedPreferencesManager.saveLastUsername(context, username.trim())
            onLoginSuccess()
            bankViewModel.clearStates()
        }
    }

    // Input validation
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

        // Show lockout message if locked
        if (isLocked) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Too many failed attempts",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "App will restart in ${lockoutTimeRemaining}s",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Show attempts remaining warning
        if (!isLocked && failedAttempts > 0 && failedAttempts < 3) {
            val attemptsRemaining = 3 - failedAttempts
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Warning: $attemptsRemaining ${if (attemptsRemaining == 1) "attempt" else "attempts"} remaining",
                    modifier = Modifier.padding(12.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                if (showValidationErrors && it.trim().isNotEmpty()) {
                    showValidationErrors = false
                }
            },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && username.trim().isEmpty(),
            enabled = !isLocked
        )

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
                if (showValidationErrors && it.trim().isNotEmpty()) {
                    showValidationErrors = false
                }
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && password.trim().isEmpty(),
            enabled = !isLocked
        )

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
            enabled = !isLoading && !isLocked
        ) {
            Text(
                if (isLoading) "Logging in..."
                else if (isLocked) "Locked"
                else "Login"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}