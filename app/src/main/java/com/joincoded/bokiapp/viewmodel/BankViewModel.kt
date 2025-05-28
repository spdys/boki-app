package com.joincoded.bokiapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joincoded.bokiapp.data.AuthenticationRequest
import com.joincoded.bokiapp.network.RetrofitHelper
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