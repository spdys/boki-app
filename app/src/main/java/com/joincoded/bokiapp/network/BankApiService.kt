package com.joincoded.bokiapp.network

import com.joincoded.bokiapp.data.AuthenticationRequest
import com.joincoded.bokiapp.data.AuthenticationResponse
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