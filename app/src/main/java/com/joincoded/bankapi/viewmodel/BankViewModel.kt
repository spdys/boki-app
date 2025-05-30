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
import com.joincoded.bankapi.network.RetrofitHelper
import kotlinx.coroutines.launch

class BankViewModel : ViewModel() {
    private val apiAuthService = RetrofitHelper.getAuthInstance()
    var token: String? by mutableStateOf(null)
    private val apiBankService = RetrofitHelper.getBankingInstance()

    fun getToken(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = apiAuthService.getToken(AuthenticationRequest(username, password))
                token = response.body()?.token
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    fun register(username: String, password: String){
        viewModelScope.launch {
            try {
                val response = apiAuthService.registerUser(UserCreationRequest(username, password))
            } catch (e: Exception){
                println(e)
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
            try {
                val response = apiBankService.submitKYC(
                    TokenResponse(token).getBearerToken(),
                    KYCRequest(fullName, phone, email, civilId, address, dateOfBirth)
                )
            } catch (e: Exception){
                println(e)
            }
        }

    }
}