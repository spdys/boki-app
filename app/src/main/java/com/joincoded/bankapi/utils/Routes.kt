package com.joincoded.bankapi.utils

class Routes {
    companion object {
        // Auth routes
        const val authGraph = "auth_graph"
        const val loginRoute = "login" // Biometric login (LoginScreen)
        const val manualLoginRoute = "manual_login" // Manual login fallback
        const val registrationRoute = "registration"
        const val kycRoute = "kyc"

        // Main app routes
        const val mainGraph = "main_graph"
        const val homeRoute = "home"
        const val accountDetailsRoute = "accountDetails"
        const val quickPayRoute = "quick_pay"
        const val servicesRoute = "services"
    }
}