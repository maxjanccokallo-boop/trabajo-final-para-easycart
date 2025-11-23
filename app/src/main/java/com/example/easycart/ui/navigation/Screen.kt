package com.example.easycart.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
}

enum class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {

    // 🔍 SCAN – animación de cámara + lector
    Scan(
        "scan",
        "Escanear",
        Icons.Outlined.CenterFocusWeak,
        Icons.Filled.CenterFocusWeak
    ),

    // 🛒 CART – icono lleno cuando está seleccionado
    Cart(
        "cart",
        "Carrito",
        Icons.Outlined.ShoppingCart,
        Icons.Filled.ShoppingCart
    ),

    // 🛍 PRODUCTS – más visual tipo tienda
    Products(
        "products",
        "Productos",
        Icons.Outlined.Storefront,
        Icons.Filled.Storefront
    ),

    // 🏷 OFFERS – icono de ofertas más profesional
    Offers(
        "offers",
        "Ofertas",
        Icons.Outlined.LocalOffer,
        Icons.Filled.LocalOffer
    ),

    // 📶 BLUETOOTH – conexión
    Bluetooth(
        "bluetooth",
        "Bluetooth",
        Icons.Outlined.Bluetooth,
        Icons.Filled.Bluetooth
    ),

    // 👤 PROFILE – usuario con borde
    Profile(
        "profile",
        "Perfil",
        Icons.Outlined.Person,
        Icons.Filled.Person
    )
}
