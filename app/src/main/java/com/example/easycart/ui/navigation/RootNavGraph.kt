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
import com.example.easycart.ui.screens.home.PaymentScreen

@Composable
fun RootNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,

    // ⭐ AHORA ESTÁN ACEPTADOS
    darkMode: Boolean,
    onToggleTheme: () -> Unit
) {

    NavHost(
        navController = navController,
        startDestination = if (mainViewModel.uiState.value.user == null)
            Screen.Login.route else Screen.Home.route
    ) {

        // LOGIN
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // REGISTRO
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

        // PAGO EFECTIVO
        composable("cash_payment") {
            CashPaymentScreen(navController, mainViewModel)
        }

        // PAGO ÉXITO
        composable("payment_success") {
            PaymentSuccessScreen(
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // PAGO GENERAL
        composable("payment") {
            PaymentScreen(navController, mainViewModel)
        }

        // ⭐⭐⭐ HOME SCREEN CON TEMA GLOBAL ⭐⭐⭐
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                viewModel = mainViewModel,

                // 🔥 Modo claro/oscuro que controlas desde MainActivity
                darkMode = darkMode,
                onToggleTheme = onToggleTheme,

                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
