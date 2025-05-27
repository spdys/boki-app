package com.joincoded.bankapi.network

import com.joincoded.bankapi.data.AuthenticationRequest
import com.joincoded.bankapi.data.AuthenticationResponse
import com.joincoded.bankapi.data.UserCreationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BankApiService {

    // TODO: for pots controllers

}

interface AuthApiService {

    // @POST("register")
    // TODO need to change backend to make user registration return object instead of string

    @POST("api/v1/users/auth/login")
    suspend fun getToken(@Body authRequest: AuthenticationRequest) : Response<AuthenticationResponse>

}