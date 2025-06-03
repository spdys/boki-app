package com.joincoded.bankapi.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joincoded.bankapi.R
import com.joincoded.bankapi.ui.theme.BokiTheme
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

    // In-memory attempt tracking (no SharedPreferences needed)
    var failedAttempts by remember { mutableIntStateOf(0) }
    var isLocked by remember { mutableStateOf(false) }
    var lockoutTimeRemaining by remember { mutableIntStateOf(0) }

    // Remember username from last login, clear password
    var username by remember { mutableStateOf(SharedPreferencesManager.getLastUsername(context)) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showValidationErrors by remember { mutableStateOf(false) }

    // Saved user's first name for greeting
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
            // Username saving now happens in RegistrationScreen, not here
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BokiTheme.gradient)
    ) {
        // Header with Sign Up option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = navigateToRegister,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Boki Logo - Above greeting
            Image(
                painter = painterResource(id = R.drawable.boki_logo_dark_mode),
                contentDescription = "Boki Logo",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            // Greeting text - smaller
            Text(
                text = greetingText,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Show lockout message if locked
            if (isLocked) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Too many failed attempts",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "App will restart in ${lockoutTimeRemaining}s",
                            color = Color.White.copy(alpha = 0.8f)
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
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Warning: $attemptsRemaining ${if (attemptsRemaining == 1) "attempt" else "attempts"} remaining",
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    if (showValidationErrors && it.trim().isNotEmpty()) {
                        showValidationErrors = false
                    }
                },
                label = { Text("Username", color = Color.White.copy(alpha = 0.7f)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Username",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                isError = showValidationErrors && username.trim().isEmpty(),
                enabled = !isLocked,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.8f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    errorBorderColor = Color.Red.copy(alpha = 0.8f),
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (showValidationErrors && username.trim().isEmpty()) {
                Text(
                    text = "Username is required",
                    color = Color.Red.copy(alpha = 0.8f),
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
                    if (showValidationErrors && it.trim().isNotEmpty()) {
                        showValidationErrors = false
                    }
                },
                label = { Text("Password", color = Color.White.copy(alpha = 0.7f)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = showValidationErrors && password.trim().isEmpty(),
                enabled = !isLocked,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.8f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    errorBorderColor = Color.Red.copy(alpha = 0.8f),
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (showValidationErrors && password.trim().isEmpty()) {
                Text(
                    text = "Password is required",
                    color = Color.Red.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = { validateAndLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && !isLocked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isLoading) "Logging in..."
                    else if (isLocked) "Locked"
                    else "Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fingerprint Button - No background
            Button(
                onClick = { /* Handle fingerprint authentication */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLocked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Fingerprint",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Use Fingerprint",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? Create a new user ",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = navigateToRegister,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                ) {
                    Text(
                        text = "here",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                }
            }
        }
    }
}