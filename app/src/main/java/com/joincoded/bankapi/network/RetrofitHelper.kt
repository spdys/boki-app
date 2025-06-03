package com.joincoded.bankapi.network

import com.google.gson.Gson
import com.joincoded.bankapi.data.response.FailureResponse
import com.joincoded.bankapi.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    private fun getOkHttpClient(): OkHttpClient {
        val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(log)
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

    fun parseErrorBody(errorBody: ResponseBody?): String? {
        return try {
            val json = errorBody?.string()
            val parsed = Gson().fromJson(json, FailureResponse::class.java)
            parsed?.error // may still be null
        } catch (e: Exception) {
            null
        }
    }
}