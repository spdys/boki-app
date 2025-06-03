package com.joincoded.bankapi.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.joincoded.bankapi.screens.KYCScreen
import com.joincoded.bankapi.screens.LoginScreen // Biometric login
import com.joincoded.bankapi.screens.ManualLoginScreen
import com.joincoded.bankapi.screens.RegistrationScreen
import com.joincoded.bankapi.screens.HomeScreen
import com.joincoded.bankapi.screens.PotSummaryScreen
import com.joincoded.bankapi.screens.AccountSummaryScreen
import com.joincoded.bankapi.utils.Routes
import com.joincoded.bankapi.utils.SharedPreferencesManager
import com.joincoded.bankapi.viewmodel.BankViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: BankViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    // Smart routing: new users start at registration, existing users start at biometric login
    val hasExistingUser = SharedPreferencesManager.hasExistingUser(context)
    val authStartDestination = if (hasExistingUser) Routes.loginRoute else Routes.registrationRoute

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Routes.mainGraph else Routes.authGraph
    ) {
        // Auth navigation graph
        navigation(
            startDestination = Routes.manualLoginRoute,
            route = Routes.authGraph
        ) {
            // Biometric Login Screen (Primary for returning users)
            composable(Routes.loginRoute) {
                LoginScreen(
                    bankViewModel = viewModel,
                    onLoginSuccess = {
                        // Successful biometric login → go to home
                        navController.navigate(Routes.mainGraph) {
                            popUpTo(Routes.authGraph) { inclusive = true }
                        }
                    },
                    navigateToManualLogin = {
                        // Biometric failed/user chose manual → go to manual login
                        navController.navigate(Routes.manualLoginRoute)
                    },
                    navigateToRegister = {
                        // No existing user → go to registration
                        navController.navigate(Routes.registrationRoute)
                    }
                )
            }

            // Manual Login Screen (Fallback from biometric)
            composable(Routes.manualLoginRoute) {
                ManualLoginScreen(
                    bankViewModel = viewModel,
                    onLoginSuccess = {
                        // Successful manual login → go to home
                        navController.navigate(Routes.mainGraph) {
                            popUpTo(Routes.authGraph) { inclusive = true }
                        }
                    },
                    navigateToRegister = {
                        navController.navigate(Routes.registrationRoute)
                    }
                    //After 3 failed attempts, app restarts and goes back to biometric
                )
            }

            // Registration Screen (For new users)
            composable(Routes.registrationRoute) {
                RegistrationScreen(
                    bankViewModel = viewModel,
                    onRegistrationSuccess = {
                        // New user completes registration → go to KYC
                        navController.navigate(Routes.kycRoute)
                    },
                    navigateToLogin = {
                        // User has account → go to biometric login
                        navController.navigate(Routes.loginRoute)
                    }
                )
            }

            // KYC Screen (After registration)
            composable(Routes.kycRoute) {
                KYCScreen(
                    viewModel = viewModel,
                    onKYCSuccess = {
                        //  KYC completed → go to home
                        navController.navigate(Routes.mainGraph) {
                            popUpTo(Routes.authGraph) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Main app navigation graph
        navigation(
            startDestination = Routes.homeRoute,
            route = Routes.mainGraph
        ) {
            composable(Routes.homeRoute) {
                HomeScreen(
                    viewModel = viewModel,
                    onAccountClicked = { account ->
                        viewModel.selectAccount(account)
                        navController.navigate(Routes.accountDetailsRoute)
                    },
                    onPotClicked = { pot ->
                        viewModel.selectPot(pot)
                        navController.navigate(Routes.potDetailsRoute)
                    }
                )
            }

            composable(Routes.accountDetailsRoute) {
                AccountSummaryScreen(viewModel = viewModel)
            }

            composable(Routes.potDetailsRoute){
                PotSummaryScreen(viewModel = viewModel)
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