package com.joincoded.bankapi.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.joincoded.bankapi.testingcomposes.SimpleRegistrationScreen
import com.joincoded.bankapi.testingcomposes.TokenLoginScreen
import com.joincoded.bankapi.utils.Routes
import com.joincoded.bankapi.viewmodel.BankViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: BankViewModel = viewModel()
) {
    // Observe auth state to determine start destination
    val isLoggedIn = viewModel.isLoggedIn.collectAsState().value

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Routes.mainGraph else Routes.authGraph
    ) {
        // Auth navigation graph
        navigation(
            startDestination = Routes.loginRoute,
            route = Routes.authGraph
        ) {
            composable(Routes.loginRoute) {

            }

            composable(Routes.registrationRoute) {

            }

            composable(Routes.kycRoute) {

            }
        }

        // Main app navigation graph (with bottom nav)
        navigation(
            startDestination = Routes.homeRoute,
            route = Routes.mainGraph
        ) {
            composable(Routes.homeRoute) {
                // This will be handled by MainScreen
            }
            composable(Routes.quickPayRoute) {
                // This will be handled by MainScreen
            }
            composable(Routes.servicesRoute) {
                // This will be handled by MainScreen
            }
        }
    }
}