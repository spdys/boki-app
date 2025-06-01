package com.joincoded.bankapi.screens

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.joincoded.bankapi.R
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.viewmodel.BankViewModel

@Composable
fun ManualLoginScreen(
    bankViewModel: BankViewModel,
    onLoginSuccess: () -> Unit,
    navigateToRegister: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Collect states from BankViewModel
    val isLoading by bankViewModel.isLoading.collectAsState()
    val isSuccessful by bankViewModel.isSuccessful.collectAsState()
    val error by bankViewModel.error.collectAsState()

    // Form state
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Handle successful authentication
    LaunchedEffect(isSuccessful) {
        if (isSuccessful) {
            onLoginSuccess()
            bankViewModel.clearStates()
        }
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
                .verticalScroll(scrollState) // Add scroll capability
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
                text = "Welcome Back!",
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
                        onValueChange = { username = it },
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BokiTheme.colors.secondary,
                            focusedLabelColor = BokiTheme.colors.secondary,
                            cursorColor = BokiTheme.colors.secondary,
                            focusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedBorderColor = BokiTheme.colors.textSecondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = BokiTheme.colors.textSecondary
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BokiTheme.colors.secondary,
                            focusedLabelColor = BokiTheme.colors.secondary,
                            cursorColor = BokiTheme.colors.secondary,
                            focusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedBorderColor = BokiTheme.colors.textSecondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = BokiTheme.colors.textSecondary
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (username.isNotBlank() && password.isNotBlank()) {
                                    bankViewModel.getToken(username, password)
                                }
                            }
                        ),
                        singleLine = true
                    )

                    // Error Display
                    error?.let { errorMessage ->
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

                    Spacer(modifier = Modifier.height(32.dp))

                    // Login Button
                    Button(
                        onClick = {
                            if (username.isNotBlank() && password.isNotBlank()) {
                                bankViewModel.getToken(username, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = username.isNotBlank() && password.isNotBlank() && !isLoading,
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
                                text = "Sign In",
                                style = BokiTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register Link - Now properly visible
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