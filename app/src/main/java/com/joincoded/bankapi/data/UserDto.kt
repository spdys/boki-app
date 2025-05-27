package com.joincoded.bankapi.data

data class User(
    var username: String,
    var password: String,
    var image: String,
    var token: String?
)

data class UserCreationRequest(
    val username: String,
    val password: String
)

data class AuthenticationRequest(
    val username: String,
    val password: String
)

data class AuthenticationResponse(
    val token: String
)