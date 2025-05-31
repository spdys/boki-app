package com.joincoded.bankapi.data.response

data class TokenResponse(
    val token: String
)

fun TokenResponse.getBearerToken(): String {
    return "Bearer $token"
}