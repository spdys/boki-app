package com.joincoded.bankapi.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.joincoded.bankapi.testingcomposes.SimpleRegistrationScreen
import com.joincoded.bankapi.testingcomposes.TokenLoginScreen
import com.joincoded.bankapi.utils.Routes
import com.joincoded.bankapi.viewmodel.BankViewModel

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController(), viewModel: BankViewModel = viewModel()) {
    NavHost(navController = navController, startDestination = Routes.registerationRoute ) {
        composable(Routes.loginRoute) {

                TokenLoginScreen(viewModel, loginSuccessful = {
                    navController.navigate(Routes.homeRoute)
                }
                )
        }
        composable(Routes.homeRoute) {
            Text("Home")

        }
        composable(Routes.registerationRoute) {
            SimpleRegistrationScreen()
        }
    }
}