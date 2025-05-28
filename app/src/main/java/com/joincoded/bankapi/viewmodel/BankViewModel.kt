package com.joincoded.bankapi.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bankapi.data.AmountChange
import com.joincoded.bankapi.data.AuthenticationRequest
import com.joincoded.bankapi.data.User
import com.joincoded.bankapi.data.response.TokenResponse
import com.joincoded.bankapi.network.BankApiService
import com.joincoded.bankapi.network.RetrofitHelper
import kotlinx.coroutines.launch

class BankViewModel : ViewModel() {
    private val apiService = RetrofitHelper.getAuthInstance()
    var token: String? by mutableStateOf(null)

    fun getToken(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getToken(AuthenticationRequest(username, password))
                token = response.body()?.token
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}