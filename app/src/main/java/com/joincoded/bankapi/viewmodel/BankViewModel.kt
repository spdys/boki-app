package com.joincoded.bankapi.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.AccountSummaryDto
import com.joincoded.bankapi.data.AccountType
import com.joincoded.bankapi.data.AuthenticationRequest
import com.joincoded.bankapi.data.CreateAccountRequest
import com.joincoded.bankapi.data.KYCRequest
import com.joincoded.bankapi.data.UserCreationRequest
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.network.RetrofitHelper.parseErrorBody
import com.joincoded.bankapi.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class BankViewModel : ViewModel() {

    // General states that can be used across the app
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSuccessful = MutableStateFlow(false)
    val isSuccessful = _isSuccessful.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Auth state
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()


    // API services
    private val apiAuthService = RetrofitHelper.getAuthInstance()
    private val apiBankService = RetrofitHelper.getBankingInstance()

    var token: String? by mutableStateOf(null)
        private set

    var mainAccountSummary by mutableStateOf<AccountSummaryDto?>(null)
        private set

    var selectedAccount by mutableStateOf<AccountSummaryDto?>(null)
        private set

    val totalBalance: BigDecimal
        get() = allAccountSummaries.sumOf { it.balance + (it.pots?.sumOf { pot -> pot.balance } ?: BigDecimal.ZERO) }

    var userName by mutableStateOf<String?>(null)
        private set

    var allAccountSummaries by mutableStateOf<List<AccountSummaryDto>>(emptyList())
        private set

    init {
        // Check if user is already logged in when ViewModel is created
        checkAuthState()
    }

    private fun checkAuthState() {
        val savedToken = SessionManager.token
        if (!savedToken.isNullOrEmpty()) {
            token = savedToken
            _isLoggedIn.value = true
        }
    }

    fun clearStates() {
        _isLoading.value = false
        _isSuccessful.value = false
        _error.value = null
    }

    fun logout() {
        token = null
        SessionManager.token = null
        _isLoggedIn.value = false
//        _accountSummary.value = null
        clearStates()
    }

    fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..22 -> "Good evening"
            else -> "Hello"
        }
    }

    fun getToken(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccessful.value = false
            try {
                val response = apiAuthService.getToken(AuthenticationRequest(username, password))
                val responseToken = response.body()?.token

                if (responseToken != null) {
                    token = responseToken
                    SessionManager.token = responseToken
                    _isLoggedIn.value = true
                    _isSuccessful.value = true
                } else {
                    _error.value = parseErrorBody(response.errorBody()) ?: "Wrong credentials"
                }

                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _isSuccessful.value = false
                _error.value = e.message ?: "Failed to log in"
            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccessful.value = false
            try {
                val response = apiAuthService.registerUser(UserCreationRequest(username, password))
                if (response.isSuccessful) {
                    _isSuccessful.value = true
                } else {
                    _error.value = parseErrorBody(response.errorBody()) ?: "Registration failed"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _isSuccessful.value = false
                _error.value = e.message ?: "Registration failed"
            }
        }
    }

    fun submitKYC(
        fullName: String,
        phone: String,
        email: String,
        civilId: String,
        address: String,
        dateOfBirth: String,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccessful.value = false
            try {
                val response = apiBankService.submitKYC(
                    KYCRequest(fullName, phone, email, civilId, address, dateOfBirth)
                )
                if (response.isSuccessful) {
                    _isSuccessful.value = true
                    _isLoggedIn.value = true
                } else {
                    _error.value = parseErrorBody(response.errorBody()) ?: "KYC submission failed"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _isSuccessful.value = false
                _error.value = e.message ?: "KYC submission failed"
            }
        }
    }

    fun autoCreateMainAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiBankService.createAccount(
                    CreateAccountRequest(accountType = AccountType.MAIN)
                )
                if (response.isSuccessful) {
                    _isSuccessful.value = true
                } else {
                    _error.value = parseErrorBody(response.errorBody()) ?: "Failed to create main account"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unable to create main account"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSavingsAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiBankService.createAccount(
                    CreateAccountRequest(accountType = AccountType.SAVINGS)
                )
                if (response.isSuccessful) {
                    _isSuccessful.value = true
                } else {
                    _error.value = parseErrorBody(response.errorBody()) ?: "Failed to create savings account"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unable to create savings account"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectAccountById(accountId: Long) {
        val localMatch = allAccountSummaries.find { it.accountId == accountId }
        if (localMatch != null) {
            selectedAccount = localMatch
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                _error.value = null
                _isSuccessful.value = false
                try {
                    val response = apiBankService.getAccountSummary(accountId)
                    if (response.isSuccessful) {
                        selectedAccount = response.body()
                        _isSuccessful.value = true
                    } else {
                        _error.value = parseErrorBody(response.errorBody()) ?: "Failed to fetch account summary"
                    }
                    _isLoading.value = false
                } catch (e: Exception) {
                    _isLoading.value = false
                    _isSuccessful.value = false
                    _error.value = e.message ?: "Unable to fetch account summary"
                }
            }
        }
    }

    fun fetchAccountsAndSummary() {
        viewModelScope.launch {
            try {
                val response = apiBankService.getAllAccounts()
                if (response.isSuccessful) {
                    val accounts = response.body() ?: emptyList()
                    val summaries = accounts.mapNotNull { account ->
                        val summaryResponse = apiBankService.getAccountSummary(account.id)
                        if (summaryResponse.isSuccessful) summaryResponse.body() else null
                    }
                    allAccountSummaries = summaries
                    val mainSummary = summaries.find { it.accountType == AccountType.MAIN }
                    mainAccountSummary = mainSummary
                } else {
                    _error.value = parseErrorBody(response.errorBody()) ?: "Failed to load accounts"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Could not load accounts"
            }
        }
    }

    fun getKYC() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiBankService.getKYC()
                if (response.isSuccessful) {
                    userName = response.body()?.fullName?.split(" ")?.firstOrNull()                } else {
                    _error.value = parseErrorBody(response.errorBody()) ?: "Failed to fetch KYC info"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Unable to fetch KYC info"
            }
        }
    }
}