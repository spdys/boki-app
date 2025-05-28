package com.joincoded.bokiapp.data.response

data class TokenResponse(val token: String?) {
    fun getBearerToken(): String {
        return "Bearer $token"
    }
}
