@file:OptIn(ExperimentalMaterial3Api::class)

package com.joincoded.bankapi.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joincoded.bankapi.viewmodel.BankViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KYCScreen(
    bankViewModel: BankViewModel,
    onKYCSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val error by bankViewModel.error.collectAsState()
    val isLoading by bankViewModel.isLoading.collectAsState()
    val isSuccessful by bankViewModel.isSuccessful.collectAsState()

    // Form state
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var civilId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showValidationErrors by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

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
        if (isSuccessful) {
            Toast.makeText(context, "KYC submitted successfully!", Toast.LENGTH_SHORT).show()
            onKYCSuccess()
            bankViewModel.clearStates()
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
            bankViewModel.clearStates()
        }
    }

    // Validation and submit function
    fun validateAndSubmitKYC() {
        showValidationErrors = true
        if (isFormValid) {
            showValidationErrors = false
            bankViewModel.submitKYC(
                fullName = fullName.trim(),
                phone = phone.trim(),
                email = email.trim(),
                civilId = civilId.trim(),
                address = address.trim(),
                dateOfBirth = formatDateFromMillis(selectedDateMillis)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Complete your profile",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "We need some information to verify your identity",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Full Name Field
        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                if (showValidationErrors && it.trim().length >= 2) {
                    showValidationErrors = false
                }
            },
            label = { Text("Full Name") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = "Full Name")
            },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && !isFullNameValid,
            enabled = !isLoading
        )

        if (showValidationErrors && !isFullNameValid) {
            Text(
                text = "Full name must be at least 2 characters",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
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
            label = { Text("Phone Number") },
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = "Phone")
            },
            placeholder = { Text("9XXXXXXX") },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && !isPhoneValid,
            enabled = !isLoading
        )

        if (showValidationErrors && !isPhoneValid) {
            Text(
                text = "Enter valid Kuwaiti phone (starts with 9, 6, 5, or 4)",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
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
            label = { Text("Email Address") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email")
            },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && !isEmailValid,
            enabled = !isLoading
        )

        if (showValidationErrors && !isEmailValid) {
            Text(
                text = "Enter a valid email address",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
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
            label = { Text("Civil ID") },
            leadingIcon = {
                Icon(Icons.Default.CreditCard, contentDescription = "Civil ID")
            },
            placeholder = { Text("123456789012") },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && !isCivilIdValid,
            enabled = !isLoading
        )

        if (showValidationErrors && !isCivilIdValid) {
            Text(
                text = "Civil ID must be exactly 12 digits",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
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
            label = { Text("Address") },
            leadingIcon = {
                Icon(Icons.Default.Home, contentDescription = "Address")
            },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && !isAddressValid,
            enabled = !isLoading,
            maxLines = 2
        )

        if (showValidationErrors && !isAddressValid) {
            Text(
                text = "Address must be at least 5 characters",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
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
            label = { Text("Date of Birth") },
            placeholder = { Text("DD-MM-YYYY") },
            readOnly = true,
            trailingIcon = {
                IconButton(
                    onClick = { showDatePicker = true },
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationErrors && !isDateValid,
            enabled = !isLoading
        )

        if (showValidationErrors && !isDateValid) {
            Text(
                text = "Please select your date of birth",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = { validateAndSubmitKYC() },
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
            Text(if (isLoading) "Submitting..." else "Complete Profile")
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
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.padding(16.dp)
        )
    }
}