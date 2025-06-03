package com.joincoded.bankapi.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Collect states from BankViewModel
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
        "Welcome back, $savedName!"
    } else {
        "Welcome Back!"
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BokiTheme.gradient)
    ) {
        // Background decorative elements
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Floating accent circles for visual interest
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = (-20).dp, y = 100.dp)
                    .background(
                        BokiTheme.colors.secondary.copy(alpha = 0.1f),
                        BokiTheme.shapes.circle
                    )
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = 300.dp, y = 200.dp)
                    .background(
                        BokiTheme.colors.info.copy(alpha = 0.08f),
                        BokiTheme.shapes.circle
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top spacing - responsive to screen size
            Spacer(modifier = Modifier.height(60.dp))

            // Logo Section
            Card(
                modifier = Modifier
                    .size(180.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = BokiTheme.shapes.circle,
                        ambientColor = BokiTheme.colors.secondary.copy(alpha = 0.3f),
                        spotColor = BokiTheme.colors.secondary.copy(alpha = 0.3f)
                    ),
                shape = BokiTheme.shapes.circle,
                colors = CardDefaults.cardColors(
                    containerColor = BokiTheme.colors.cardBackground
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.boki_logo_dark_mode),
                        contentDescription = "Boki Logo",
                        modifier = Modifier.size(120.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Welcome Text
            Text(
                text = greetingText,
                style = BokiTheme.typography.displayMedium,
                color = BokiTheme.colors.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Sign in to your account",
                style = BokiTheme.typography.bodyLarge,
                color = BokiTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Show lockout message if locked
            if (isLocked) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = BokiTheme.shapes.medium
                        ),
                    shape = BokiTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = BokiTheme.colors.error.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Too many failed attempts",
                            fontWeight = FontWeight.Bold,
                            color = BokiTheme.colors.error,
                            style = BokiTheme.typography.labelLarge
                        )
                        Text(
                            text = "App will restart in ${lockoutTimeRemaining}s",
                            color = BokiTheme.colors.error,
                            style = BokiTheme.typography.bodyMedium
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
                        .padding(bottom = 16.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = BokiTheme.shapes.medium
                        ),
                    shape = BokiTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = BokiTheme.colors.cardBackground
                    )
                ) {
                    Text(
                        text = "Warning: $attemptsRemaining ${if (attemptsRemaining == 1) "attempt" else "attempts"} remaining",
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Medium,
                        color = BokiTheme.colors.error,
                        style = BokiTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Login Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 15.dp,
                        shape = BokiTheme.shapes.extraLarge,
                        ambientColor = BokiTheme.colors.secondary.copy(alpha = 0.2f)
                    ),
                shape = BokiTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = BokiTheme.colors.cardBackground
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            if (showValidationErrors && it.trim().isNotEmpty()) {
                                showValidationErrors = false
                            }
                        },
                        label = {
                            Text(
                                "Username",
                                style = BokiTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Username",
                                tint = BokiTheme.colors.secondary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BokiTheme.shapes.medium,
                        isError = showValidationErrors && username.trim().isEmpty(),
                        enabled = !isLocked,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BokiTheme.colors.secondary,
                            focusedLabelColor = BokiTheme.colors.secondary,
                            cursorColor = BokiTheme.colors.secondary,
                            focusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedBorderColor = BokiTheme.colors.textSecondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = BokiTheme.colors.textSecondary,
                            errorBorderColor = BokiTheme.colors.error,
                            errorLabelColor = BokiTheme.colors.error
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )

                    if (showValidationErrors && username.trim().isEmpty()) {
                        Text(
                            text = "Username is required",
                            color = BokiTheme.colors.error,
                            style = BokiTheme.typography.bodySmall,
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
                        label = {
                            Text(
                                "Password",
                                style = BokiTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Password",
                                tint = BokiTheme.colors.secondary
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible }
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = BokiTheme.colors.textSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = BokiTheme.shapes.medium,
                        isError = showValidationErrors && password.trim().isEmpty(),
                        enabled = !isLocked,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BokiTheme.colors.secondary,
                            focusedLabelColor = BokiTheme.colors.secondary,
                            cursorColor = BokiTheme.colors.secondary,
                            focusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedBorderColor = BokiTheme.colors.textSecondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = BokiTheme.colors.textSecondary,
                            errorBorderColor = BokiTheme.colors.error,
                            errorLabelColor = BokiTheme.colors.error
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                validateAndLogin()
                            }
                        ),
                        singleLine = true
                    )

                    if (showValidationErrors && password.trim().isEmpty()) {
                        Text(
                            text = "Password is required",
                            color = BokiTheme.colors.error,
                            style = BokiTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    // Error Display (for non-validation errors)
                    error?.let { errorMessage ->
                        if (!errorMessage.contains("Wrong credentials", ignoreCase = true) &&
                            !errorMessage.contains("Invalid", ignoreCase = true) &&
                            !errorMessage.contains("credentials", ignoreCase = true)) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = BokiTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = BokiTheme.colors.error.copy(alpha = 0.1f)
                                )
                            ) {
                                Text(
                                    text = errorMessage,
                                    color = BokiTheme.colors.error,
                                    style = BokiTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Login Button
                    Button(
                        onClick = { validateAndLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading && !isLocked,
                        shape = BokiTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BokiTheme.colors.secondary,
                            contentColor = BokiTheme.colors.onPrimary,
                            disabledContainerColor = BokiTheme.colors.textSecondary.copy(alpha = 0.3f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = BokiTheme.colors.onPrimary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                text = if (isLocked) "Locked" else "Sign In",
                                style = BokiTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No account? ",
                    style = BokiTheme.typography.bodyMedium,
                    color = BokiTheme.colors.textSecondary
                )

                TextButton(
                    onClick = navigateToRegister,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = BokiTheme.colors.secondary
                    )
                ) {
                    Text(
                        text = "Register",
                        style = BokiTheme.typography.labelLarge
                    )
                }
            }

            // Bottom spacing to ensure register link is always accessible
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}