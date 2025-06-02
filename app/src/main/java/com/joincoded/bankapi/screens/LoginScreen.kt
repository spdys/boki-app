package com.joincoded.bankapi.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
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
fun getFragmentActivity(): FragmentActivity? {
    val context = LocalContext.current
    return context as? FragmentActivity
}

@Composable
fun LoginScreen(
    bankViewModel: BankViewModel,
    onLoginSuccess: () -> Unit,
    navigateToManualLogin: () -> Unit,
    navigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val activity = getFragmentActivity()
    val executor: Executor = ContextCompat.getMainExecutor(context)
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val isLoading by bankViewModel.isLoading.collectAsState()
    val isSuccessful by bankViewModel.isSuccessful.collectAsState()

    // Drag and animation states
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var firstName by remember { mutableStateOf("") }
    var isNearTarget by remember { mutableStateOf(false) }
    var biometricAvailable by remember { mutableStateOf(true) }

    // Smart routing - check if user exists
    LaunchedEffect(Unit) {
        if (!SharedPreferencesManager.hasExistingUser(context)) {
            navigateToRegister()
            return@LaunchedEffect
        }
        firstName = SharedPreferencesManager.getFirstName(context)

        // Check biometric availability
        val biometricManager = BiometricManager.from(context)
        biometricAvailable = when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    LaunchedEffect(isSuccessful) {
        if (isSuccessful) {
            onLoginSuccess()
            bankViewModel.clearStates()
        }
    }

    // Drag thresholds and target zones
    val upThreshold = with(density) { -100.dp.toPx() }
    val downThreshold = with(density) { 150.dp.toPx() }
    val targetZone = with(density) { 120.dp.toPx() } // Zone near fingerprint for effects

    // animations for visual feedback
    val circleScale by animateFloatAsState(
        targetValue = if (isNearTarget) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "circleScale"
    )

    val circleElevation by animateDpAsState(
        targetValue = if (isNearTarget) 20.dp else 12.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "circleElevation"
    )

    val fingerprintScale by animateFloatAsState(
        targetValue = if (isNearTarget) 1.3f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fingerprintScale"
    )

    // Biometric setup with error handling
    val biometricPrompt = activity?.let {
        BiometricPrompt(it, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                failedAttempts = 0
                onLoginSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                when (errorCode) {
                    BiometricPrompt.ERROR_NO_BIOMETRICS,
                    BiometricPrompt.ERROR_HW_NOT_PRESENT,
                    BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                        // Hardware issue - go directly to manual
                        navigateToManualLogin()
                    }
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        // User cancelled - stay on screen
                        return
                    }
                    else -> {
                        // Authentication failed
                        failedAttempts++
                        showErrorDialog = true
                    }
                }
            }

            override fun onAuthenticationFailed() {
                failedAttempts++
                showErrorDialog = true
            }
        })
    }

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Boki Login")
        .setSubtitle("Use your biometric authentication")
        .setNegativeButtonText("Cancel")
        .build()

    // Trigger biometric authentication
    fun triggerBiometric() {
        if (!biometricAvailable) {
            navigateToManualLogin()
            return
        }
        biometricPrompt?.authenticate(promptInfo)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BokiTheme.gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Card(
                modifier = Modifier
                    .size(280.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.boki_logo_dark_mode),
                        contentDescription = "Boki Logo",
                        modifier = Modifier.size(200.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            //greeting with first name
            if (firstName.isNotEmpty()) {
                Text(
                    text = "Welcome, $firstName!",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "to your virtual wallet",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .size(80.dp)
                .scale(fingerprintScale)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isNearTarget)
                        BokiTheme.colors.secondary.copy(alpha = 0.3f)
                    else
                        Color.White.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.fingerprint),
                contentDescription = "Fingerprint Scanner",
                tint = if (isNearTarget) Color.White else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(48.dp)
            )
        }

        // Draggable login circle
        Box(
            modifier = Modifier
                .offset(y = dragOffsetY.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 200.dp)
                .size(80.dp)
                .scale(circleScale)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onDragEnd = {
                            when {
                                dragOffsetY < upThreshold / density.density -> {
                                    // Swipe up - go to manual login
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navigateToManualLogin()
                                }
                                dragOffsetY > downThreshold / density.density -> {
                                    // Swipe down - trigger biometric
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    triggerBiometric()
                                }
                            }
                            // Reset position and state
                            dragOffsetY = 0f
                            isNearTarget = false
                        }
                    ) { _, dragAmount ->
                        dragOffsetY += dragAmount / density.density

                        // Check if near target for visual feedback
                        val newNearTarget = dragOffsetY > (targetZone / density.density)
                        if (newNearTarget != isNearTarget) {
                            isNearTarget = newNearTarget
                            if (newNearTarget) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    }
                }
                .shadow(
                    elevation = circleElevation,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(
                    if (isNearTarget)
                        BokiTheme.colors.secondary.copy(alpha = 0.9f)
                    else
                        BokiTheme.colors.secondary
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Profile",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Error Dialog
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
                        tint = BokiTheme.colors.secondary
                    )
                },
                title = { Text("Biometric Authentication Failed") },
                text = null,
                confirmButton = {
                    if (failedAttempts == 1) {
                        // First attempt - show "Try Again"
                        Button(
                            onClick = {
                                showErrorDialog = false
                                bankViewModel.clearStates()
                                triggerBiometric()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BokiTheme.colors.secondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Try Again")
                        }
                    } else {
                        // Second attempt - only "Enter Password"
                        Button(
                            onClick = {
                                showErrorDialog = false
                                bankViewModel.clearStates()
                                navigateToManualLogin()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BokiTheme.colors.secondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enter Password")
                        }
                    }
                },
                dismissButton = {
                    if (failedAttempts == 1) {
                        OutlinedButton(
                            onClick = {
                                showErrorDialog = false
                                bankViewModel.clearStates()
                                navigateToManualLogin()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enter Password")
                        }
                    }
                    // Second attempt has no dismiss button - only "Enter Password"
                }
            )
        }

        // No biometric available fallback
        if (!biometricAvailable && firstName.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "Biometric authentication not available. Swipe up for manual login.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
