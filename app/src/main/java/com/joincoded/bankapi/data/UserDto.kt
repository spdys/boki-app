package com.joincoded.bankapi.data

data class User(
    var username: String,
    var password: String,
    var image: String,
    var token: String?
)

data class TokenResponse(val token: String?) {
    fun getBearerToken(): String {
        return "Bearer $token"
    }
}
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

data class UserCreationResponse(
    val message: String
)


data class AuthenticationRequest(
    val username: String,
    val password: String
)

data class AuthenticationResponse(
    val token: String
)