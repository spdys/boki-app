@file:OptIn(ExperimentalMaterial3Api::class)

package com.joincoded.bankapi.screens

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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joincoded.bankapi.R
import com.joincoded.bankapi.ui.theme.BokiTheme
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KYCScreen(
    viewModel: BankViewModel,
    onKYCSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Collect states from BankViewModel
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccessful by viewModel.isSuccessful.collectAsState()

    // Form state
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var civilId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showValidationErrors by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val hasCreatedMainAccount = remember { mutableStateOf(false) }

    // Date formatting helper
    fun formatDateFromMillis(millis: Long?): String {
        return millis?.let {
            val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            formatter.format(Date(it))
        } ?: ""
    }

    // Validation logic
    val isFullNameValid = fullName.trim().length >= 2
    val isPhoneValid = phone.matches(Regex("^[9654]\\d{7}$")) // Kuwait format
    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isCivilIdValid = civilId.length == 12 && civilId.all { it.isDigit() }
    val isAddressValid = address.trim().length >= 5
    val isDateValid = selectedDateMillis != null

    val isFormValid = isFullNameValid && isPhoneValid && isEmailValid &&
            isCivilIdValid && isAddressValid && isDateValid

    // Handle successful KYC
    LaunchedEffect(isSuccessful) {
        if (isSuccessful && !hasCreatedMainAccount.value) {
            hasCreatedMainAccount.value = true
            viewModel.autoCreateMainAccount()
            Toast.makeText(context, "KYC submitted successfully!", Toast.LENGTH_SHORT).show()
            onKYCSuccess()
            viewModel.clearStates()
        }
    }

    // Handle errors
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            when {
                errorMessage.contains("network", ignoreCase = true) ||
                        errorMessage.contains("connection", ignoreCase = true) -> {
                    Toast.makeText(context, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                }
                errorMessage.contains("phone", ignoreCase = true) -> {
                    Toast.makeText(context, "Invalid Kuwaiti phone number", Toast.LENGTH_LONG).show()
                }
                errorMessage.contains("email", ignoreCase = true) -> {
                    Toast.makeText(context, "Invalid email format", Toast.LENGTH_LONG).show()
                }
                errorMessage.contains("civil", ignoreCase = true) -> {
                    Toast.makeText(context, "Invalid Civil ID format", Toast.LENGTH_LONG).show()
                }
                errorMessage.contains("date", ignoreCase = true) -> {
                    Toast.makeText(context, "Invalid date format", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
            viewModel.clearStates()
        }
    }

    // Validation and submit function
    fun validateAndSubmitKYC() {
        showValidationErrors = true
        if (isFormValid) {
            showValidationErrors = false
            viewModel.submitKYC(
                fullName = fullName.trim(),
                phone = phone.trim(),
                email = email.trim(),
                civilId = civilId.trim(),
                address = address.trim(),
                dateOfBirth = formatDateFromMillis(selectedDateMillis)
            )
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
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = (-20).dp, y = 100.dp)
                    .background(
                        BokiTheme.colors.info.copy(alpha = 0.1f),
                        BokiTheme.shapes.circle
                    )
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = 300.dp, y = 300.dp)
                    .background(
                        BokiTheme.colors.secondary.copy(alpha = 0.08f),
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
            Spacer(modifier = Modifier.height(40.dp))

            // Logo Section
            Card(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = BokiTheme.shapes.circle,
                        ambientColor = BokiTheme.colors.info.copy(alpha = 0.3f),
                        spotColor = BokiTheme.colors.info.copy(alpha = 0.3f)
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
                        modifier = Modifier.size(90.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Welcome Text
            Text(
                text = "Complete Your Profile",
                style = BokiTheme.typography.displayMedium,
                color = BokiTheme.colors.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "We need some information to verify your identity",
                style = BokiTheme.typography.bodyLarge,
                color = BokiTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // KYC Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 15.dp,
                        shape = BokiTheme.shapes.extraLarge,
                        ambientColor = BokiTheme.colors.info.copy(alpha = 0.2f)
                    ),
                shape = BokiTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = BokiTheme.colors.cardBackground
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Full Name Field
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            if (showValidationErrors && it.trim().length >= 2) {
                                showValidationErrors = false
                            }
                        },
                        label = {
                            Text(
                                "Full Name",
                                style = BokiTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Full Name",
                                tint = BokiTheme.colors.info
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BokiTheme.shapes.medium,
                        isError = showValidationErrors && !isFullNameValid,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BokiTheme.colors.info,
                            focusedLabelColor = BokiTheme.colors.info,
                            cursorColor = BokiTheme.colors.info,
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

                    if (showValidationErrors && !isFullNameValid) {
                        Text(
                            text = "Full name must be at least 2 characters",
                            color = BokiTheme.colors.error,
                            style = BokiTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Phone Field
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            if (it.length <= 8) phone = it
                            if (showValidationErrors && it.matches(Regex("^[9654]\\d{7}$"))) {
                                showValidationErrors = false
                            }
                        },
                        label = {
                            Text(
                                "Phone Number",
                                style = BokiTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = "Phone",
                                tint = BokiTheme.colors.info
                            )
                        },
                        placeholder = { Text("9XXXXXXX") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BokiTheme.shapes.medium,
                        isError = showValidationErrors && !isPhoneValid,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BokiTheme.colors.info,
                            focusedLabelColor = BokiTheme.colors.info,
                            cursorColor = BokiTheme.colors.info,
                            focusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedBorderColor = BokiTheme.colors.textSecondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = BokiTheme.colors.textSecondary,
                            errorBorderColor = BokiTheme.colors.error,
                            errorLabelColor = BokiTheme.colors.error
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )

                    if (showValidationErrors && !isPhoneValid) {
                        Text(
                            text = "Enter valid Kuwaiti phone (starts with 9, 6, 5, or 4)",
                            color = BokiTheme.colors.error,
                            style = BokiTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (showValidationErrors && android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                                showValidationErrors = false
                            }
                        },
                        label = {
                            Text(
                                "Email Address",
                                style = BokiTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = "Email",
                                tint = BokiTheme.colors.info
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BokiTheme.shapes.medium,
                        isError = showValidationErrors && !isEmailValid,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BokiTheme.colors.info,
                            focusedLabelColor = BokiTheme.colors.info,
                            cursorColor = BokiTheme.colors.info,
                            focusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedBorderColor = BokiTheme.colors.textSecondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = BokiTheme.colors.textSecondary,
                            errorBorderColor = BokiTheme.colors.error,
                            errorLabelColor = BokiTheme.colors.error
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )

                    if (showValidationErrors && !isEmailValid) {
                        Text(
                            text = "Enter a valid email address",
                            color = BokiTheme.colors.error,
                            style = BokiTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Civil ID Field
                    OutlinedTextField(
                        value = civilId,
                        onValueChange = {
                            if (it.length <= 12 && it.all { char -> char.isDigit() }) {
                                civilId = it
                                if (showValidationErrors && it.length == 12) {
                                    showValidationErrors = false
                                }
                            }
                        },
                        label = {
                            Text(
                                "Civil ID",
                                style = BokiTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = "Civil ID",
                                tint = BokiTheme.colors.info
                            )
                        },
                        placeholder = { Text("123456789012") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BokiTheme.shapes.medium,
                        isError = showValidationErrors && !isCivilIdValid,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BokiTheme.colors.info,
                            focusedLabelColor = BokiTheme.colors.info,
                            cursorColor = BokiTheme.colors.info,
                            focusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedBorderColor = BokiTheme.colors.textSecondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = BokiTheme.colors.textSecondary,
                            errorBorderColor = BokiTheme.colors.error,
                            errorLabelColor = BokiTheme.colors.error
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true
                    )

                    if (showValidationErrors && !isCivilIdValid) {
                        Text(
                            text = "Civil ID must be exactly 12 digits",
                            color = BokiTheme.colors.error,
                            style = BokiTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Address Field
                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            if (showValidationErrors && it.trim().length >= 5) {
                                showValidationErrors = false
                            }
                        },
                        label = {
                            Text(
                                "Address",
                                style = BokiTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = "Address",
                                tint = BokiTheme.colors.info
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BokiTheme.shapes.medium,
                        isError = showValidationErrors && !isAddressValid,
                        enabled = !isLoading,
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BokiTheme.colors.info,
                            focusedLabelColor = BokiTheme.colors.info,
                            cursorColor = BokiTheme.colors.info,
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
                        )
                    )

                    if (showValidationErrors && !isAddressValid) {
                        Text(
                            text = "Address must be at least 5 characters",
                            color = BokiTheme.colors.error,
                            style = BokiTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date of Birth Field with Calendar Icon
                    OutlinedTextField(
                        value = formatDateFromMillis(selectedDateMillis),
                        onValueChange = { },
                        label = {
                            Text(
                                "Date of Birth",
                                style = BokiTheme.typography.labelMedium
                            )
                        },
                        placeholder = { Text("DD-MM-YYYY") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { showDatePicker = true },
                                enabled = !isLoading
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "Select date",
                                    tint = BokiTheme.colors.info
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = BokiTheme.shapes.medium,
                        isError = showValidationErrors && !isDateValid,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BokiTheme.colors.info,
                            focusedLabelColor = BokiTheme.colors.info,
                            cursorColor = BokiTheme.colors.info,
                            focusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedTextColor = BokiTheme.colors.onBackground,
                            unfocusedBorderColor = BokiTheme.colors.textSecondary.copy(alpha = 0.5f),
                            unfocusedLabelColor = BokiTheme.colors.textSecondary,
                            errorBorderColor = BokiTheme.colors.error,
                            errorLabelColor = BokiTheme.colors.error
                        )
                    )

                    if (showValidationErrors && !isDateValid) {
                        Text(
                            text = "Please select your date of birth",
                            color = BokiTheme.colors.error,
                            style = BokiTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    // Backend error (for non-toast errors)
                    error?.let { errorMessage ->
                        if (!errorMessage.contains("network", ignoreCase = true) &&
                            !errorMessage.contains("connection", ignoreCase = true) &&
                            !errorMessage.contains("phone", ignoreCase = true) &&
                            !errorMessage.contains("email", ignoreCase = true) &&
                            !errorMessage.contains("civil", ignoreCase = true) &&
                            !errorMessage.contains("date", ignoreCase = true)) {

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

                    // Submit Button
                    Button(
                        onClick = { validateAndSubmitKYC() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        shape = BokiTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BokiTheme.colors.info,
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
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isLoading) "Submitting..." else "Complete Profile",
                            style = BokiTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Modal Date Picker
    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = { dateMillis ->
                selectedDateMillis = dateMillis
                if (showValidationErrors && dateMillis != null) {
                    showValidationErrors = false
                }
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = BokiTheme.colors.info
                )
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = BokiTheme.colors.textSecondary
                )
            ) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.padding(16.dp),
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = BokiTheme.colors.info,
                todayDateBorderColor = BokiTheme.colors.info
            )
        )
    }
}