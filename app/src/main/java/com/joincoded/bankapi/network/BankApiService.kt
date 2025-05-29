package com.joincoded.bankapi.network

import com.joincoded.bankapi.data.AmountChange
import com.joincoded.bankapi.data.User
import com.joincoded.bankapi.data.KYCRequest
import com.joincoded.bankapi.data.KYCResponse
import com.joincoded.bankapi.data.response.TokenResponse
import com.joincoded.bankapi.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface BankApiService {

    @POST(Constants.signupEndpoint)
    suspend fun signup(@Body user: User): Response<TokenResponse>

    @POST("kyc")
    suspend fun submitKYC(
        @Header("Authorization") token: String,
        @Body request: KYCRequest
    ): Response<KYCResponse>

    @PUT(Constants.depositEndpoint)
    suspend fun deposit(
        @Header(Constants.authorization) token: String?,
        @Body amountChange: AmountChange
    ): Response<Unit>
}
