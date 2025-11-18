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

    // ⭐ Ya no necesitamos CART_ROUTE aquí ya que la navegación es interna del HomeScreen
    // val CART_ROUTE = "cart_tab"

    NavHost(
        navController = navController,
        startDestination = if (mainViewModel.uiState.value.user == null)
            Screen.Login.route else Screen.Home.route
    ) {

        // -------------------------
        // LOGIN / REGISTER / PAYMENT SCREENS (Sin cambios)
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
        composable("cash_payment") {
            CashPaymentScreen(navController, mainViewModel)
        }
        composable("payment_success") {
            PaymentSuccessScreen(
                onDone = {
                    navController.popBackStack(Screen.Home.route, false)
                }
            )
        }

        // -------------------------
        // HOME SCREEN (CONTENEDOR DE TABS)
        // -------------------------
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                viewModel = mainViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
                // ⭐ SE ELIMINA EL PARÁMETRO onNavigateToCartTab
            )
        }
    }
}