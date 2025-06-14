package com.joincoded.bankapi.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.joincoded.bankapi.data.CardPaymentRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.AccountSummaryDto
import com.joincoded.bankapi.data.AccountType
import com.joincoded.bankapi.data.AllocationType
import com.joincoded.bankapi.data.AuthenticationRequest
import com.joincoded.bankapi.data.CreateAccountRequest
import com.joincoded.bankapi.data.KYCRequest
import com.joincoded.bankapi.data.PotDepositRequest
import com.joincoded.bankapi.data.PotRequest
import com.joincoded.bankapi.data.PotSummaryDto
import com.joincoded.bankapi.data.PotTransferRequest
import com.joincoded.bankapi.data.TransactionHistoryRequest
import com.joincoded.bankapi.data.TransactionHistoryResponse
import com.joincoded.bankapi.data.TransferRequest
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

    var allAccountSummaries by mutableStateOf<List<AccountSummaryDto>>(emptyList())
        private set

    // QuickPay NFC state
    private val isNFCEnabled = MutableStateFlow(false)
    val nfcEnabled = isNFCEnabled.asStateFlow()

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
                    _error.value = "Registration failed" // declare error type
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

    fun getKYC() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiBankService.getKYC()
                if (response.isSuccessful) {
                    val fullName = response.body()?.fullName ?: ""
                    SharedPreferencesManager.saveUserName(context, fullName)} else {
                    _error.value = parseErrorBody(response.errorBody()) ?: "Failed to fetch KYC info"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Unable to fetch KYC info"
            }
        }
    }


    fun fetchAccountsAndSummaries() {
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

    fun selectAccount(account: AccountSummaryDto) {
        selectedAccount = account
    }

    fun selectPot(pot: PotSummaryDto) {
        selectedPot = pot
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

    fun validatePotInputs(
        name: String,
        value: BigDecimal,
        type: AllocationType,
        currentPotId: Long?
    ): String? {
        if (name.isBlank()) return "Pot name cannot be empty"
        if (value <= BigDecimal.ZERO) return "Allocation value must be greater than zero"
        if (type == AllocationType.PERCENTAGE && value >= BigDecimal.ONE) return "Percentage cannot exceed 100%"
        if (type == AllocationType.PERCENTAGE) {
            val currentPotIdSet = currentPotId ?: -1
            val totalPercentage = mainAccountSummary?.pots
                ?.filter { it.allocationType == AllocationType.PERCENTAGE && it.potId != currentPotIdSet }
                ?.sumOf { it.allocationValue } ?: BigDecimal.ZERO
            if (totalPercentage + value > BigDecimal.ONE) {
                return "Total percentage for all pots cannot be more than 100%"
            }
        }
        if (mainAccountSummary?.pots?.any {
                val sameName = it.name.equals(name, ignoreCase = true)
                val samePot = currentPotId != null && it.potId == currentPotId
                sameName && !samePot
            } == true) return "Another pot with this name already exists"
        return null
    }



    fun createPot(name: String, type: AllocationType, value: BigDecimal) {
        val accountId = mainAccountSummary?.accountId

        if (accountId == null) {
            _error.value = "Invalid account ID"
            return
        }

        val validationError = validatePotInputs(name, value, type, null)
        if (validationError != null) {
            _error.value = validationError
            return
        }

        val request = PotRequest(name.trim(), type, value)

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccessful.value = false
            try {
                val response = apiBankService.createPot(accountId, request)

                if (response.isSuccessful) {
                    val updated = response.body()
                    updated?.let {
                        selectedAccount = apiBankService.getAccountSummary(accountId).body()
                        _isSuccessful.value = true
                    }
                } else {
                    _error.value = parseErrorBody(response.errorBody()) ?: "Failed to create pot"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unable to create pot"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun editPot(updatedName: String, updatedType: AllocationType, updatedValue: BigDecimal) {
        val accountId = mainAccountSummary?.accountId
        val potId = selectedPot?.potId

        if (accountId == null || potId == null) {
            _error.value = "Invalid account or pot ID"
            return
        }

        val validationError = validatePotInputs(updatedName, updatedValue, updatedType, selectedPot?.potId)
        if (validationError != null) {
            _error.value = validationError
            return
        }

        val request = PotRequest(updatedName.trim(), updatedType, updatedValue)

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccessful.value = false
            try {
                val response = apiBankService.editPot(accountId, potId, request)
                if (response.isSuccessful) {
                    val updated = response.body()
                    if (updated != null) {
                        selectedPot = PotSummaryDto(
                            potId = selectedPot!!.potId,
                            name = updated.name,
                            balance = selectedPot!!.balance,
                            cardToken = selectedPot!!.cardToken,
                            allocationType = updated.allocationType,
                            allocationValue = updated.allocationValue
                        )
                        _isSuccessful.value = true
                    }
                } else {
                    _error.value = parseErrorBody(response.errorBody()) ?: "Failed to update pot"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unable to update pot"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePot() {
        val accountId = mainAccountSummary?.accountId
        val potId = selectedPot?.potId

        if (accountId == null || potId == null) {
            _error.value = "Invalid account or pot ID"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccessful.value = false
            try {
                val response = apiBankService.deletePot(accountId, potId)
                if (response.isSuccessful) {
                    _isSuccessful.value = true
                } else {
                    _error.value = parseErrorBody(response.errorBody()) ?: "Failed to delete pot"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unable to delete pot"
            } finally {
                _isLoading.value = false
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
            try {
                val amountValue = validateAmount(amount.value)
                if (amountValue != null) {
                    val request = PotDepositRequest(
                        sourceAccountId = mainAccountSummary!!.accountId,
                        destinationPotId = selectedPot!!.potId,
                        amount = amountValue
                    )

                    val response = apiBankService.depositToPot(request)
                    if (response.isSuccessful) {
                        val updated = response.body()
                        if (updated != null) {
                            selectedPot = PotSummaryDto(
                                potId = selectedPot!!.potId,
                                name = selectedPot!!.name,
                                balance = updated.newPotBalance,
                                cardToken = selectedPot!!.cardToken,
                                allocationType = selectedPot!!.allocationType,
                                allocationValue = selectedPot!!.allocationValue
                            )
                            _isSuccessful.value = true
                        } else {
                            _error.value =
                                parseErrorBody(response.errorBody()) ?: "Failed to add funds to pot"
                        }
                    } else {
                        _error.value = "Invalid amount entered"
                    }
                } else {
                    _error.value = "Unable to update pot"

                }
                } catch (e: Exception) {
                    _error.value = "Network error: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
    }

    fun withdrawFromPot() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val amountValue = validateAmount(amount.value ?: "")
                if (amountValue != null) {
                    val request = PotTransferRequest(
                        sourcePotId = selectedPot!!.potId,
                        amount = amountValue
                    )
                    val response = apiBankService.withdrawalToAccount(request)

                    if (response.isSuccessful) {
                        val updated = response.body()
                        if (updated != null) {
                            selectedPot = PotSummaryDto(
                                potId = selectedPot!!.potId,
                                name = selectedPot!!.name,
                                balance = updated.newPotBalance,
                                cardToken = selectedPot!!.cardToken,
                                allocationType = selectedPot!!.allocationType,
                                allocationValue = selectedPot!!.allocationValue
                            )
                            _isSuccessful.value = true
                        } else {
                            _error.value =
                                parseErrorBody(response.errorBody()) ?: "Failed to withdraw from pot"
                        }
                    } else {
                        _error.value = "Invalid amount entered"
                    }
                } else {
                    _error.value = "Unable to withdraw from pot"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    var selectedDestinationPot by mutableStateOf<PotSummaryDto?>(null)
        private set
    val availableDestinationPots: List<PotSummaryDto>
        get() = mainAccountSummary?.pots?.filter { it.potId != selectedPot?.potId } ?: emptyList()

    fun setDestinationPot(pot: PotSummaryDto?) {
        selectedDestinationPot = pot
    }
    fun clearDestinationPot() {
        selectedDestinationPot = null
    }

    fun transferBetweenPots() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val amountValue = validateAmount(amount.value ?: "")
                val destinationPot = selectedDestinationPot

                when {
                    amountValue == null -> {
                        _error.value = "Invalid amount entered"
                    }
                    destinationPot == null -> {
                        _error.value = "Please select a destination pot"
                    }
                    selectedPot == null -> {
                        _error.value = "Source pot not selected"
                    }
                    else -> {
                        val request = TransferRequest(
                            sourceId = selectedPot!!.potId,
                            destinationId = destinationPot.potId,
                            amount = amountValue
                        )
                        val response = apiBankService.transfer(request)

                        if (response.isSuccessful) {
                            val updated = response.body()
                            if (updated != null) {
                                selectedPot = PotSummaryDto(
                                    potId = selectedPot!!.potId,
                                    name = selectedPot!!.name,
                                    balance = updated.newBalanceAfter,
                                    cardToken = selectedPot!!.cardToken,
                                    allocationType = selectedPot!!.allocationType,
                                    allocationValue = selectedPot!!.allocationValue
                                )
                                _isSuccessful.value = true
                            } else {
                                _error.value =
                                    parseErrorBody(response.errorBody()) ?: "Failed to transfer between pots"
                            }
                        } else {
                            _error.value = "Invalid amount entered"
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
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
                value > (mainAccountSummary!!.balance ?: BigDecimal.ZERO) -> {
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
    fun makeCardPayment(request: CardPaymentRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccessful.value = false
            try {
                // AuthInterceptor automatically adds authorization
                val response = apiBankService.purchaseFromCard(request)

                if (response.isSuccessful) {
                    _isSuccessful.value = true
                    // CRITICAL: Refresh account data after successful payment
                    // This updates balances and ensures transaction history is current
                    fetchAccountsAndSummaries()
                } else {
                    // Backend handles all validation - just show the error
                    _error.value = parseErrorBody(response.errorBody()) ?: "Payment declined"
                }
            } catch (e: Exception) {
                _error.value = handleNetworkError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun authenticateForPayment(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiAuthService.getToken(AuthenticationRequest(username, password))

                if (response.isSuccessful && response.body()?.token != null) {
                    _isLoading.value = false
                    onSuccess()
                } else {
                    _error.value = parseErrorBody(response.errorBody()) ?: "Wrong credentials"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = handleNetworkError(e)
                _isLoading.value = false
            }
        }
    }
    fun getCardholderName(): String {
        val fullName = SharedPreferencesManager.getSavedUserName(context)
        return if (fullName.isNotEmpty()) {
            fullName.trim()
        } else {
            "Card Holder"
        }
    }
    fun toggleNfc() {
        isNFCEnabled.value = !isNFCEnabled.value
    }
    fun resetPaymentFlow() {
        isNFCEnabled.value = false
        clearStates()
    }
    fun transfer(request: TransferRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccessful.value = false
            try {
                val response = apiBankService.transfer(request)

                if (response.isSuccessful) {
                    _isSuccessful.value = true
                    //  updates balances and ensures transaction history is current
                    fetchAccountsAndSummaries()
                } else {
                    // Backend handles all validation - just show the error
                    _error.value = parseErrorBody(response.errorBody()) ?: "Transfer failed"
                }
            } catch (e: Exception) {
                _error.value = handleNetworkError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}


