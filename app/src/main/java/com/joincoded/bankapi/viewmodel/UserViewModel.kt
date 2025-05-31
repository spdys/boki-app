package com.joincoded.bankapi.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.request.KYCRequest
import com.joincoded.bankapi.data.response.KYCResponse
import com.joincoded.bankapi.data.response.TokenResponse
import com.joincoded.bankapi.data.response.User
import com.joincoded.bankapi.data.response.getBearerToken
import com.joincoded.bankapi.network.BankApiService
import com.joincoded.bankapi.network.RetrofitHelper
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val api = RetrofitHelper.getInstance().create(BankApiService::class.java)

    var signupSuccess = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    var tokenResponse = mutableStateOf<TokenResponse?>(null)
    var kycResponse = mutableStateOf<KYCResponse?>(null)

    fun signupUser(user: User) {
        viewModelScope.launch {
            try {
                val response = api.signup(user)
                if (response.isSuccessful) {
                    tokenResponse.value = response.body()
                    signupSuccess.value = true
                } else {
                    errorMessage.value = "Signup failed"
                }
            } catch (e: Exception) {
                errorMessage.value = "Signup error: ${e.message}"
            }
        }
    }

    fun submitKYC(kycRequest: KYCRequest) {
        viewModelScope.launch {
            try {
                val token = tokenResponse.value?.getBearerToken() ?: return@launch
                val response = api.submitKYC(token, kycRequest)
                if (response.isSuccessful) {
                    kycResponse.value = response.body()
                } else {
                    errorMessage.value = "KYC submission failed"
                }
            } catch (e: Exception) {
                errorMessage.value = "KYC error: ${e.message}"
            }
        }
    }
}