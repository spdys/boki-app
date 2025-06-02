package com.joincoded.bankapi.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.joincoded.bankapi.screens.ManualLoginScreen
import com.joincoded.bankapi.screens.RegistrationScreen
import com.joincoded.bankapi.screens.HomeScreen
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

    // smart routing: new users start at registration, existing users start at login
    val hasExistingUser = SharedPreferencesManager.hasExistingUser(context)
    val authStartDestination = if (hasExistingUser) Routes.loginRoute else Routes.registrationRoute

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Routes.mainGraph else Routes.authGraph
    ) {
        // Auth navigation graph
        navigation(
            startDestination = authStartDestination,
            route = Routes.authGraph
        ) {
            composable(Routes.loginRoute) {
                ManualLoginScreen(
                    bankViewModel = viewModel,
                    onLoginSuccess = {
                        // existing user logs in → direct to homepage
                        navController.navigate(Routes.mainGraph) {
                            popUpTo(Routes.authGraph) { inclusive = true }
                        }
                    },
                    navigateToRegister = { navController.navigate(Routes.registrationRoute) }
                )
            }

            composable(Routes.registrationRoute) {
                RegistrationScreen(
                    bankViewModel = viewModel,
                    onRegistrationSuccess = {
                        // new user signs up → direct to homepage
                        navController.navigate(Routes.mainGraph) {
                            popUpTo(Routes.authGraph) { inclusive = true }
                        }
                    },
                    navigateToLogin = { navController.navigate(Routes.loginRoute) }
                )
            }

            composable(Routes.kycRoute) {
                KYCScreen(
                    bankViewModel = viewModel,
                )
            }
        }

        // main app navigation graph
        navigation(
            startDestination = Routes.homeRoute,
            route = Routes.mainGraph
        ) {
            composable(Routes.homeRoute) {
                HomeScreen(viewModel)
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