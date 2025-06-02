package com.joincoded.bankapi.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.joincoded.bankapi.screens.KYCScreen
import com.joincoded.bankapi.screens.ManualLoginScreen
import com.joincoded.bankapi.screens.RegistrationScreen
import com.joincoded.bankapi.screens.HomeScreen
import com.joincoded.bankapi.screens.PotSummaryScreen
import com.joincoded.bankapi.testingcomposes.AccountSummaryScreen
import com.joincoded.bankapi.utils.Routes
import com.joincoded.bankapi.viewmodel.BankViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: BankViewModel = viewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

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
                ManualLoginScreen(
                    bankViewModel = viewModel,
                    onLoginSuccess = { },
                    navigateToRegister = { navController.navigate(Routes.registrationRoute) }
                )
            }

            composable(Routes.registrationRoute) {
                RegistrationScreen(
                    bankViewModel = viewModel,
                    onRegistrationSuccess = { navController.navigate(Routes.kycRoute) },
                    navigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Routes.kycRoute) {
                KYCScreen(
                    bankViewModel = viewModel,
                )
            }
        }

        // Main app navigation graph
        navigation(
            startDestination = Routes.homeRoute,
            route = Routes.mainGraph
        ) {
            composable(Routes.homeRoute) {
                HomeScreen(viewModel)
            }

            composable(Routes.accountDetailsRoute) {
                viewModel.selectedAccount?.let {
                    AccountSummaryScreen(accountSummary = it)
                }
            }

            composable(Routes.potDetailsRoute){
                PotSummaryScreen(
                    potSummary = viewModel.selectedPot!!,
                    currency = "KWD",
                    transactions = emptyList(),
                    onNavigateBack = {},
                )
            }

            composable(Routes.quickPayRoute) {
                Text("Quick Pay Screen")
            }
            composable(Routes.servicesRoute) {
                Text("Services Screen")
            }
        }
    }
}