package com.joincoded.bankapi.network

import com.joincoded.bankapi.utils.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .build()
    }

    fun getAuthInstance(): AuthApiService {
        return Retrofit.Builder()
            .baseUrl(Constants.authBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(AuthApiService::class.java)
    }

    fun getBankingInstance(): BankApiService {
        return Retrofit.Builder()
            .baseUrl(Constants.bankingBaseUrl)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BankApiService::class.java)
    }
}