package com.joincoded.bankapi.screens

import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.joincoded.bankapi.R
import com.joincoded.bankapi.ui.theme.BokiSoftGray
import com.joincoded.bankapi.ui.theme.BokiTypography
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.util.concurrent.Executor

@Composable
fun getFragmentActivity(): FragmentActivity? {
    val context = LocalContext.current
    return context as? FragmentActivity
}

@Composable
fun LoginScreen(
    bankViewModel: BankViewModel,
    onLoginSuccess: () -> Unit,
    navigateToPinLogin: () -> Unit
) {
    val context = LocalContext.current
    val activity = getFragmentActivity()
    val executor: Executor = ContextCompat.getMainExecutor(context)

    // Collect states from BankViewModel
    val isLoading by bankViewModel.isLoading.collectAsState()
    val isSuccessful by bankViewModel.isSuccessful.collectAsState()
    val error by bankViewModel.error.collectAsState()

    val brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0D34), Color(0xFF141C58))
    )

    var dragOffset by remember { mutableFloatStateOf(0f) }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showManualLoginTitle by remember { mutableStateOf(false) }

    // Handle successful authentication from token
    LaunchedEffect(isSuccessful) {
        if (isSuccessful) {
            onLoginSuccess()
            bankViewModel.clearStates()
        }
    }

    // You can get user info from BankViewModel if needed
    // For now using placeholder greeting
    val greeting = "Welcome To Your Virtual Wallet!"

    val dragThreshold = with(LocalDensity.current) { 60.dp.toPx() }
    val loginSize: Dp by animateDpAsState(
        targetValue = if (dragOffset > dragThreshold) 80.dp else 60.dp,
        animationSpec = tween(300), label = "loginSize"
    )

    val fingerprintScale by animateFloatAsState(
        targetValue = if (dragOffset > dragThreshold) 1.1f else 1f,
        animationSpec = tween(300), label = "fingerprintScale"
    )

    val biometricPrompt = activity?.let {
        BiometricPrompt(it, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // You can add automatic token retrieval here if needed
                onLoginSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                showErrorDialog = true
            }

            override fun onAuthenticationFailed() {
                failedAttempts++
                showErrorDialog = true
            }
        })
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Boki Login")
        .setSubtitle("Use biometric authentication")
        .setNegativeButtonText("Cancel")
        .build()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    dragOffset += dragAmount
                    if (dragOffset > dragThreshold) {
                        biometricPrompt?.authenticate(promptInfo)
                    } else if (dragOffset < -dragThreshold) {
                        showManualLoginTitle = true
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.boki_logo_dark_mode),
                contentDescription = "Logo",
                modifier = Modifier.size(240.dp)
            )

            Text(
                text = greeting,
                color = Color.White,
                style = BokiTypography.titleRegular.copy(fontSize = 18.sp),
                modifier = Modifier.padding(top = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.chevron_right),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = 270f }
                            .alpha(0.5f),
                        tint = Color.White
                    )

                    Spacer(Modifier.height(12.dp))

                    val loginGradient = Brush.verticalGradient(
                        colors = listOf(Color(0xFF8C1515), Color(0xFFB02828))
                    )

                    Box(
                        modifier = Modifier
                            .size(loginSize)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(loginGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "login",
                                style = BokiTypography.titleRegular.copy(fontSize = 16.sp),
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.chevron_right),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = 90f }
                            .alpha(0.5f),
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.fingerprint),
                        contentDescription = "Fingerprint",
                        modifier = Modifier
                            .size(60.dp)
                            .scale(fingerprintScale),
                        tint = BokiSoftGray
                    )

                    // Show error from BankViewModel if any
                    error?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Error: $it",
                            color = Color.Red,
                            style = BokiTypography.titleRegular.copy(fontSize = 12.sp)
                        )
                    }
                }
            }
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = {
                    showErrorDialog = false
                    bankViewModel.clearStates()
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.error),
                        contentDescription = "Error Icon",
                        tint = BokiSoftGray
                    )
                },
                title = { Text("Biometric Login Failed") },
                text = { Text("Please try again or login manually.") },
                confirmButton = {
                    if (failedAttempts < 2) {
                        Button(onClick = {
                            showErrorDialog = false
                            bankViewModel.clearStates()
                            biometricPrompt?.authenticate(promptInfo)
                        }) {
                            Text("Try Again")
                        }
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showErrorDialog = false
                        bankViewModel.clearStates()
                        navigateToPinLogin()
                    }) {
                        Text("Login Manually")
                    }
                }
            )
        }
    }
}