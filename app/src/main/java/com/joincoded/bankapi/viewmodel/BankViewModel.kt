package com.joincoded.bankapi.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.AccountSummaryDto
import com.joincoded.bankapi.data.AccountType
import com.joincoded.bankapi.data.AuthenticationRequest
import com.joincoded.bankapi.data.CreateAccountRequest
import com.joincoded.bankapi.data.KYCRequest
import com.joincoded.bankapi.data.PotDepositRequest
import com.joincoded.bankapi.data.PotSummaryDto
import com.joincoded.bankapi.data.TransactionHistoryRequest
import com.joincoded.bankapi.data.TransactionHistoryResponse
import com.joincoded.bankapi.data.UserCreationRequest
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.network.RetrofitHelper.parseErrorBody
import com.joincoded.bankapi.utils.SessionManager
import com.joincoded.bankapi.utils.SharedPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.math.BigDecimal
import java.util.Calendar

class BankViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

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

    var selectedPot by mutableStateOf<PotSummaryDto?>(null)
        private set

    var potTransactions by mutableStateOf<List<TransactionHistoryResponse>?>(null)
        private set

    var accountTransactions by mutableStateOf<List<TransactionHistoryResponse>?>(null)
        private set


    val totalBalance: BigDecimal
        get() = allAccountSummaries.sumOf {
            it.balance + (it.pots?.sumOf { pot -> pot.balance } ?: BigDecimal.ZERO)
        }

    //var userName by mutableStateOf<String?>(null)
    //   private set

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

    //  network error handling
    private fun handleNetworkError(e: Exception): String {
        return when (e) {
            is UnknownHostException -> "No internet connection. Please check your network."
            is ConnectException -> "Unable to connect to server. Please try again."
            is SocketTimeoutException -> "Session timeout. Please try again."
            else -> e.message ?: "Network error occurred."
        }
    }

    fun logout() {
        token = null
        SessionManager.token = null
        _isLoggedIn.value = false
        // Clear all saved data
        SharedPreferencesManager.clearAll(context)
        clearStates()
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
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
                _error.value = handleNetworkError(e)
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
                    _error.value = "Registration failed"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _isSuccessful.value = false
                _error.value = handleNetworkError(e)  // Enhanced error handling
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
                    // Save the full name after successful KYC
                    SharedPreferencesManager.saveUserName(context, fullName)
                    _isSuccessful.value = true
                    _isLoggedIn.value = true
                } else {
                    _error.value = parseErrorBody(response.errorBody())
                        ?: "KYC submission failed. Please check your information!"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _isSuccessful.value = false
                _error.value = handleNetworkError(e)  // Enhanced error handling
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
                    _error.value =
                        parseErrorBody(response.errorBody()) ?: "Failed to create main account"
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
                    _error.value =
                        parseErrorBody(response.errorBody()) ?: "Failed to create savings account"
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
                        _error.value = parseErrorBody(response.errorBody())
                            ?: "Failed to fetch account summary"
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

    fun selectPot(pot: PotSummaryDto) {
        selectedPot = pot
        potTransactions = null
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
                    val fullName = response.body()?.fullName ?: ""
                    SharedPreferencesManager.saveUserName(context, fullName)
                } else {
                    _error.value =
                        parseErrorBody(response.errorBody()) ?: "Failed to fetch KYC info"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Unable to fetch KYC info"
            }
        }
    }

    fun getPotTransactionHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response =
                    apiBankService.retrieveTransactionHistory(
                        TransactionHistoryRequest(
                            cardId = null,
                            accountId = null,
                            potId = selectedPot!!.potId
                        )
                    )
                if (response.isSuccessful) {
                    _isSuccessful.value = true
                    potTransactions = response.body()
                    _isLoading.value = false
                } else {
                    _error.value =
                        parseErrorBody(response.errorBody()) ?: "Failed to fetch pot transactions"
                    _isLoading.value = false
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to fetch pot transactions"
                _isLoading.value = false
            }
        }
    }

    fun getAccountTransactionHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiBankService.retrieveTransactionHistory(
                    TransactionHistoryRequest(
                        cardId = null,
                        accountId = selectedAccount!!.accountId,
                        potId = null
                    )
                )
                if (response.isSuccessful) {
                    _isSuccessful.value = true
                    accountTransactions = response.body()
                } else {
                    _error.value =
                        parseErrorBody(response.errorBody()) ?: "Failed to fetch account transactions"
                }
            } catch (e: Exception){
                _error.value = e.message ?: "Failed to fetch account transactions"

            }
        }
    }

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _amountError = MutableStateFlow<String?>(null)
    val amountError: StateFlow<String?> = _amountError.asStateFlow()


    fun clearAmount() {
        _amount.value = ""
        _amountError.value = null
    }

    fun updateAmount(newAmount: String) {
        _amount.value = newAmount
        // Clear error when user starts typing
        if (_amountError.value != null) {
            _amountError.value = null
        }
    }

    fun addToPot() {
        viewModelScope.launch {
            _isLoading.value = true
            val amountValue = validateAmount(amount.value ?: "")
            if (amountValue != null) {
                val response = apiBankService.depositToPot(PotDepositRequest(
                    sourceAccountId = mainAccountSummary!!.accountId,
                    destinationPotId = selectedPot!!.potId,
                    amount = amountValue
                ))
                Log.d("AddToPot", "Response code: ${response.code()}")
                Log.d("AddToPot", "Is successful: ${response.isSuccessful}")
                if (response.isSuccessful) {
                    _isSuccessful.value = true
                    _isLoading.value = false
                    Log.d("AddToPot", "Body: ${response.body()}")


                } else {
                    _error.value =
                        parseErrorBody(response.errorBody()) ?: "Failed add funds to pot"
                    _isLoading.value = false
                }
            }
        }
    }

    private fun validateAmount(input: String): BigDecimal? {
        return try {
            val value = input.toBigDecimal()
            when {
                value <= BigDecimal.ZERO -> {
                    _amountError.value = "Amount must be greater than 0"
                    null
                }
                value > (selectedAccount?.balance ?: BigDecimal.ZERO) -> {
                    _amountError.value = "Insufficient balance"
                    null
                }
                else -> {
                    _amountError.value = null
                    value
                }
            }
        } catch (e: NumberFormatException) {
            _amountError.value = "Please enter a valid amount"
            null
        }
    }
}