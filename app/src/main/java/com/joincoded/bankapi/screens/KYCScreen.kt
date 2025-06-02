package com.joincoded.bankapi.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.joincoded.bankapi.R
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.viewmodel.BankViewModel
import kotlinx.coroutines.delay

@Composable
fun KYCScreen(
    bankViewModel: BankViewModel,
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val isLoading by bankViewModel.isLoading.collectAsState()
    val isSuccessful by bankViewModel.isSuccessful.collectAsState()
    val error by bankViewModel.error.collectAsState()
    val isLoggedIn by bankViewModel.isLoggedIn.collectAsState()


    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var civilId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    val hasCreatedMainAccount = remember { mutableStateOf(false) }


    val isFormValid = fullName.isNotBlank() && phone.isNotBlank() &&
            email.isNotBlank() && civilId.isNotBlank() &&
            address.isNotBlank() && dateOfBirth.isNotBlank()

    LaunchedEffect(isSuccessful) {
        if (isSuccessful && !hasCreatedMainAccount.value) {
            bankViewModel.autoCreateMainAccount()
            hasCreatedMainAccount.value = true
            delay(1500)
            bankViewModel.clearStates()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BokiTheme.gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo
            Card(
                modifier = Modifier.size(140.dp).shadow(20.dp, BokiTheme.shapes.circle),
                shape = BokiTheme.shapes.circle,
                colors = CardDefaults.cardColors(containerColor = BokiTheme.colors.cardBackground)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.boki_logo_dark_mode),
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Identity Verification",
                style = BokiTheme.typography.displayMedium,
                color = BokiTheme.colors.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Complete your account setup",
                style = BokiTheme.typography.bodyMedium,
                color = BokiTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth().shadow(15.dp, BokiTheme.shapes.extraLarge),
                shape = BokiTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = BokiTheme.colors.cardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Form Fields
                    KYCTextField(fullName, { fullName = it }, "Full Name", Icons.Default.Person, focusManager)
                    KYCTextField(phone, { phone = it }, "Phone", Icons.Default.Phone, focusManager, KeyboardType.Phone)
                    KYCTextField(email, { email = it }, "Email", Icons.Default.Email, focusManager, KeyboardType.Email)
                    KYCTextField(civilId, { civilId = it }, "Civil ID", Icons.Default.CreditCard, focusManager, KeyboardType.Number)
                    KYCTextField(address, { address = it }, "Address", Icons.Default.Home, focusManager)
                    KYCTextField(dateOfBirth, { dateOfBirth = it }, "Date of Birth", Icons.Default.CalendarToday, focusManager, KeyboardType.Number, isLast = true) {
                        if (isFormValid) bankViewModel.submitKYC(fullName, phone, email, civilId, address, dateOfBirth)
                    }

                    // Error Display
                    error?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BokiTheme.colors.error.copy(alpha = 0.1f))
                        ) {
                            Text(
                                text = it,
                                color = BokiTheme.colors.error,
                                style = BokiTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // In your KYC screen, replace the submit button section with:
                    if (isSuccessful) {
                        // Success Acknowledgment
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = BokiTheme.colors.success.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = BokiTheme.colors.success,
                                    modifier = Modifier.size(48.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "KYC Submitted Successfully!",
                                    style = BokiTheme.typography.headlineSmall,
                                    color = BokiTheme.colors.success,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Taking you to your account...",
                                    style = BokiTheme.typography.bodyMedium,
                                    color = BokiTheme.colors.textSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = BokiTheme.colors.success
                                )
                            }
                        }
                    } else {
                        // Your existing submit button
                        Button(
                            onClick = { if (isFormValid) bankViewModel.submitKYC(fullName, phone, email, civilId, address, dateOfBirth) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = isFormValid && !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = BokiTheme.colors.info)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BokiTheme.colors.onPrimary)
                            } else {
                                Text("Complete Verification", style = BokiTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun KYCTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    focusManager: FocusManager,
    keyboardType: KeyboardType = KeyboardType.Text,
    isLast: Boolean = false,
    onDone: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = BokiTheme.typography.labelMedium) },
        leadingIcon = { Icon(icon, contentDescription = label, tint = BokiTheme.colors.info) },
        modifier = Modifier.fillMaxWidth(),
        shape = BokiTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BokiTheme.colors.info,
            focusedLabelColor = BokiTheme.colors.info,
            cursorColor = BokiTheme.colors.info,
            focusedTextColor = BokiTheme.colors.onBackground,
            unfocusedTextColor = BokiTheme.colors.onBackground,
            unfocusedBorderColor = BokiTheme.colors.textSecondary.copy(alpha = 0.5f),
            unfocusedLabelColor = BokiTheme.colors.textSecondary
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = { focusManager.clearFocus(); onDone() }
        ),
        singleLine = true
    )
}