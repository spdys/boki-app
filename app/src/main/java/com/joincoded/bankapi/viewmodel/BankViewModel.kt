package com.joincoded.bankapi.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.AuthenticationRequest
import com.joincoded.bankapi.data.KYCRequest
import com.joincoded.bankapi.data.UserCreationRequest
import com.joincoded.bankapi.data.response.TokenResponse
import com.joincoded.bankapi.data.response.getBearerToken
import com.joincoded.bankapi.network.RetrofitHelper
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

    fun clearStates() {
        _isLoading.value = false
        _isSuccessful.value = false
        _error.value = null
    }

    // Can then use these generic states in composable
    /*

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
     */
    private val apiAuthService = RetrofitHelper.getAuthInstance()
    var token: String? by mutableStateOf(null)
    private val apiBankService = RetrofitHelper.getBankingInstance()

    fun getToken(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccessful.value = false
            try {
                val response = apiAuthService.getToken(AuthenticationRequest(username, password))
                token = response.body()?.token
                _isLoading.value = false
                _isSuccessful.value =true
            } catch (e: Exception) {
                _isLoading.value = false
                _isSuccessful.value = false
                _error.value = e.message ?: "Failed to get token"            }
        }
    }

    fun register(username: String, password: String){
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccessful.value = false
            try {
                val response = apiAuthService.registerUser(UserCreationRequest(username, password))
                _isLoading.value = false
                _isSuccessful.value = true
            } catch (e: Exception){
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
                    TokenResponse(token).getBearerToken(),
                    KYCRequest(fullName, phone, email, civilId, address, dateOfBirth)
                )
                _isLoading.value = false
                _isSuccessful.value = true
            } catch (e: Exception){
                _isLoading.value = false
                _isSuccessful.value = false
                _error.value = e.message ?: "KYC submission failed"
            }

        }

    }
}