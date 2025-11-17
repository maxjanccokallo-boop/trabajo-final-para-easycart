package com.example.easycart.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.easycart.viewmodel.AuthViewModel
import com.example.easycart.viewmodel.MainViewModel
import com.example.easycart.ui.screens.auth.LoginScreen
import com.example.easycart.ui.screens.auth.RegisterScreen
import com.example.easycart.ui.screens.home.HomeScreen
import com.example.easycart.ui.screens.home.CashPaymentScreen
import com.example.easycart.ui.screens.home.PaymentSuccessScreen

@Composable
fun RootNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {

    NavHost(
        navController = navController,
        startDestination = if (mainViewModel.uiState.value.user == null)
            Screen.Login.route else Screen.Home.route
    ) {

        // -------------------------
        // LOGIN
        // -------------------------
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        // -------------------------
        // REGISTER
        // -------------------------
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // -------------------------
        // PAGO EN EFECTIVO
        // -------------------------
        composable("cash_payment") {
            CashPaymentScreen(navController, mainViewModel)
        }

        composable("payment_success") {
            PaymentSuccessScreen(navController)
        }

        // -------------------------
        // HOME SCREEN
        // -------------------------
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,   // 👈 AHORA SÍ LO ENVIAMOS
                viewModel = mainViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
