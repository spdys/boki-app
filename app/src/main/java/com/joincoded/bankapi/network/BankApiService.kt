package com.joincoded.bankapi.network


import com.joincoded.bankapi.data.AccountResponse
import com.joincoded.bankapi.data.AccountSummaryDto
import com.joincoded.bankapi.data.AuthenticationRequest
import com.joincoded.bankapi.data.AuthenticationResponse
import com.joincoded.bankapi.data.CardPaymentRequest
import com.joincoded.bankapi.data.CardPaymentResponse
import com.joincoded.bankapi.data.CreateAccountRequest
import com.joincoded.bankapi.data.KYCRequest
import com.joincoded.bankapi.data.KYCResponse
import com.joincoded.bankapi.data.PotDepositRequest
import com.joincoded.bankapi.data.PotDepositResponse
import com.joincoded.bankapi.data.PotRequest
import com.joincoded.bankapi.data.PotResponse
import com.joincoded.bankapi.data.PotTransferRequest
import com.joincoded.bankapi.data.PotTransferResponse
import com.joincoded.bankapi.data.TransactionHistoryRequest
import com.joincoded.bankapi.data.TransactionHistoryResponse
import com.joincoded.bankapi.data.TransferRequest
import com.joincoded.bankapi.data.TransferResponse
import com.joincoded.bankapi.data.UserCreationRequest
import com.joincoded.bankapi.data.UserCreationResponse
import com.joincoded.bankapi.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

import retrofit2.http.Path

interface BankApiService {

    // KYC endpoints
    @POST("api/v1/kyc")
    suspend fun submitKYC(
        @Body request: KYCRequest
    ): Response<KYCResponse>

    @GET("api/v1/kyc")
    suspend fun getKYC(): Response<KYCResponse>

    // Flag KYC omitted since its for admins only...

    // Account endpoints
    @POST("accounts/v1/create")
    suspend fun createAccount(
        @Body request: CreateAccountRequest
    ): Response<AccountResponse>

    @POST("accounts/v1/{accountId}/pots")
    suspend fun createPot(
        @Path("accountId") accountId: Long,
        @Body request: PotRequest
    ): Response<PotResponse>

    @POST("accounts/v1/{accountId}/pots/{potId}")
    suspend fun editPot(
        @Path("accountId") accountId: Long,
        @Path("potId") potId: Long,
        @Body request: PotRequest
    ): Response<PotResponse>

    @DELETE("accounts/v1/{accountId}/pots/{potId}/delete")
    suspend fun deletePot(
        @Path("accountId") accountId: Long,
        @Path("potId") potId: Long
    ): Response<Void>

    @GET("accounts/v1/{accountId}/summary")
    suspend fun getAccountSummary(
        @Path("accountId") accountId: Long
    ): Response<AccountSummaryDto>

    @GET("accounts/v1/accounts")
    suspend fun getAllAccounts(): Response<List<AccountResponse>>

    // Close account omitted because for admins only but maybe in UI a user can request?

    // Transactions endpoint
    // TODO: Transfer transaction

    // Deposit salary endpoint omitted

    // Pot to main
    @POST("transactions/v1/pot/withdrawal")
    suspend fun withdrawalToAccount(
        @Body request: PotTransferRequest
    ): Response<PotTransferResponse>

    // Deposit to pot
    @POST("transactions/v1/pot/deposit")
    suspend fun depositToPot(
        @Body request: PotDepositRequest
    ): Response<PotDepositResponse>

    // card purchases / we might not use it
    @POST("transactions/v1/purchase")
    suspend fun purchaseFromCard(
        @Body request: CardPaymentRequest
    ): Response<CardPaymentResponse>

    // History
    @POST("transactions/v1/history")
    suspend fun retrieveTransactionHistory(
        @Body request: TransactionHistoryRequest
    ) : Response<List<TransactionHistoryResponse>>

    @POST("transactions/v1/transfer")
    suspend fun transfer(
        @Body request: TransferRequest
    ) : Response<TransferResponse>

}

interface AuthApiService {

        @POST("api/v1/users/register")
        suspend fun registerUser(@Body creationRequest: UserCreationRequest): Response<UserCreationResponse>


        @POST("api/v1/users/auth/login")
        suspend fun getToken(@Body authRequest: AuthenticationRequest): Response<AuthenticationResponse>

    }




