package com.example.easycart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.easycart.ui.navigation.BottomTab
import com.example.easycart.ui.theme.GreenPrimary
import com.example.easycart.viewmodel.MainViewModel
import com.example.easycart.viewmodel.LedState
import com.example.easycart.di.AppModule
import com.example.easycart.viewmodel.MainViewModelFactory

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(AppModule.repo)
    ),
    onLogout: () -> Unit
) {

    // ⭐ CRÍTICO: selectedTab define la pestaña actual
    var selectedTab by remember { mutableStateOf(BottomTab.Scan) }
    val uiState by viewModel.uiState.collectAsState()

    // Lógica del color del LED
    val ledColor = when (uiState.ledState) {
        LedState.GREEN -> Color.Green
        LedState.RED -> Color.Red
        LedState.YELLOW -> Color.Yellow
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(tab.label.first().toString()) },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreenPrimary)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Hola, ${uiState.user?.email ?: "usuario"}",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            "✓ Todo Correcto",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        // Muestra la cantidad total de items en el carrito
                        "🛒 ${uiState.cart.sumOf { it.quantity }}",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(Modifier.height(4.dp))

                // IMPLEMENTACIÓN DEL INDICADOR LED
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Estado LED:",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .size(14.dp)
                            .background(ledColor, shape = CircleShape)
                    )
                }
            }
        }
    ) { paddingValues ->

        Box(Modifier.padding(paddingValues)) {

            when (selectedTab) {

                BottomTab.Scan ->
                    ScanScreen(
                        viewModel = viewModel,
                        // ⭐ AÑADIDO: Si el escaneo es exitoso, cambiamos la pestaña a CART
                        onScanSuccess = {
                            selectedTab = BottomTab.Cart // ⭐ ¡Aquí está la magia!
                        }
                    )

                BottomTab.Cart ->
                    CartScreen(
                        viewModel = viewModel,
                        navController = navController
                    )

                BottomTab.Products ->
                    ProductsScreen(viewModel)

                BottomTab.Offers ->
                    OffersScreen(viewModel)

                BottomTab.Bluetooth ->
                    BluetoothScreen()

                BottomTab.Profile ->
                    ProfileScreen(
                        viewModel = viewModel,
                        onLogout = onLogout
                    )
            }
        }
    }
}