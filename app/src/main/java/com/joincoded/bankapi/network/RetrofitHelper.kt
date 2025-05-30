package com.joincoded.bankapi.network

import com.joincoded.bankapi.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    fun getAuthInstance(): BankApiService.AuthApiService {
        return Retrofit.Builder()
            .baseUrl(Constants.authBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(BankApiService.AuthApiService::class.java)
    }

    fun getBankingInstance(): BankApiService {
        return Retrofit.Builder()
            .baseUrl(Constants.bankingBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(BankApiService::class.java)
    }

}