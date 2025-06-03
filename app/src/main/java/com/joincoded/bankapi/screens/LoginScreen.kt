package com.joincoded.bankapi.screens

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.joincoded.bankapi.R
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.utils.SharedPreferencesManager
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.util.concurrent.Executor

@Composable
fun LoginScreen(
    bankViewModel: BankViewModel,
    onLoginSuccess: () -> Unit,
    navigateToManualLogin: () -> Unit,
    navigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val executor: Executor = ContextCompat.getMainExecutor(context)

    val error by bankViewModel.error.collectAsState()
    val isLoading by bankViewModel.isLoading.collectAsState()
    val isSuccessful by bankViewModel.isSuccessful.collectAsState()

    // Get saved user's first name for greeting
    val savedName = SharedPreferencesManager.getFirstName(context)
    val greetingText = "Welcome to your virtual wallet"

    // Biometric setup
    val biometricPrompt = activity?.let {
        BiometricPrompt(it, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // Get saved username and trigger login
                val savedUsername = SharedPreferencesManager.getLastUsername(context)
                if (savedUsername.isNotEmpty()) {
                    // For biometric, we'll need to handle this differently
                    // This is a simplified version - you might need to store encrypted credentials
                    onLoginSuccess()
                } else {
                    navigateToManualLogin()
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Handle authentication error - redirect to manual login
                navigateToManualLogin()
            }

            override fun onAuthenticationFailed() {
                // Handle authentication failure - redirect to manual login
                navigateToManualLogin()
            }
        })
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric Authentication")
        .setSubtitle("Use your biometric credential to access your account")
        .setNegativeButtonText("Use Password")
        .build()

    // Handle successful login
    LaunchedEffect(isSuccessful) {
        if (isSuccessful) {
            onLoginSuccess()
            bankViewModel.clearStates()
        }
    }

    // Handle errors
    LaunchedEffect(error) {
        error?.let {
            // If there's an error, redirect to manual login
            navigateToManualLogin()
            bankViewModel.clearStates()
        }
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
            // Welcome Text - Above logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "WELCOME !",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "TO YOUR VIRTUAL WALLET",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Boki Logo
            Image(
                painter = painterResource(id = R.drawable.boki_logo_dark_mode),
                contentDescription = "Boki Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp)
            )

            // Welcome text under logo
            Text(
                text = greetingText,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // User Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.8f))
                    .padding(bottom = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Biometric Scanner - No square outline, just corner brackets
            Box(
                modifier = Modifier.padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                // Fingerprint icon
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Fingerprint Scanner",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(48.dp)
                )

                // Corner brackets only
                Box(modifier = Modifier.size(80.dp)) {
                    // Top-left bracket
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopStart)
                            .background(
                                Color.Transparent
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(16.dp)
                                .background(Color.White.copy(alpha = 0.6f))
                                .align(Alignment.TopStart)
                        )
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .background(Color.White.copy(alpha = 0.6f))
                                .align(Alignment.TopStart)
                        )
                    }

                    // Top-right bracket
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(16.dp)
                                .background(Color.White.copy(alpha = 0.6f))
                                .align(Alignment.TopEnd)
                        )
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .background(Color.White.copy(alpha = 0.6f))
                                .align(Alignment.TopEnd)
                        )
                    }

                    // Bottom-left bracket
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomStart)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(16.dp)
                                .background(Color.White.copy(alpha = 0.6f))
                                .align(Alignment.BottomStart)
                        )
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .background(Color.White.copy(alpha = 0.6f))
                                .align(Alignment.BottomStart)
                        )
                    }

                    // Bottom-right bracket
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(16.dp)
                                .background(Color.White.copy(alpha = 0.6f))
                                .align(Alignment.BottomEnd)
                        )
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .background(Color.White.copy(alpha = 0.6f))
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }

            // Instruction Text - Shorter
            Text(
                text = "Place your finger to login",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Authenticate Button
            Button(
                onClick = {
                    biometricPrompt?.authenticate(promptInfo)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
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
                    text = if (isLoading) "Authenticating..." else "Authenticate",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Manual Login Option
            TextButton(
                onClick = navigateToManualLogin,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
            ) {
                Text(
                    text = "Use Password Instead",
                    fontSize = 14.sp,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Option
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