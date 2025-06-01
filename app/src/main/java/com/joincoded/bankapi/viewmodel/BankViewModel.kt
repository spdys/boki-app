package com.joincoded.bankapi.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.AuthenticationRequest
import com.joincoded.bankapi.data.KYCRequest
import com.joincoded.bankapi.data.UserCreationRequest
import com.joincoded.bankapi.network.RetrofitHelper
import com.joincoded.bankapi.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
                    _error.value = "Wrong credentials"
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
                    _error.value = "Registration failed"
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
                    _error.value = "KYC submission failed"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _isSuccessful.value = false
                _error.value = e.message ?: "KYC submission failed"
            }
        }
    }

}