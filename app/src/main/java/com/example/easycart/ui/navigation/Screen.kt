package com.example.easycart.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
}

enum class BottomTab(val route: String, val label: String) {
    Scan("scan", "Escanear"),
    Cart("cart", "Carrito"),
    Products("products", "Productos"),
    Offers("offers", "Ofertas"),
    Bluetooth("bluetooth", "Bluetooth"),
    Profile("profile", "Perfil")
}
