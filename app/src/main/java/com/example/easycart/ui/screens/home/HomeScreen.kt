package com.example.easycart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.easycart.ui.navigation.BottomTab
import com.example.easycart.ui.components.EasyCartBottomBar
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
    // ⭐ Control de la pestaña seleccionada
    var selectedTab by remember { mutableStateOf(BottomTab.Scan) }
    val uiState by viewModel.uiState.collectAsState()

    // ⭐ LED animado
    val ledColor = when (uiState.ledState) {
        LedState.GREEN -> Color(0xFF22C55E)
        LedState.RED -> Color(0xFFEF4444)
        LedState.YELLOW -> Color(0xFFFACC15)
    }

    // ⭐ Fondo general más bonito
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FB))
    ) {

        Scaffold(
            bottomBar = {
                EasyCartBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            },
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GreenPrimary)
                        .shadow(6.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {

                    // ==========================================
                    // 🟢 FILA SUPERIOR (Nombre y carrito)
                    // ==========================================
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Hola, ${uiState.user?.email ?: "usuario"} 👋",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "✓ Todo Correcto",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            "🛒 ${uiState.cart.sumOf { it.quantity }}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // ==========================================
                    // 🟡 LED con estilo PRO
                    // ==========================================
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Estado LED:",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .size(16.dp)
                                .background(ledColor, shape = CircleShape)
                        )
                    }
                }
            }
        ) { paddingValues ->

            // ===============================
            // ⭐ CONTENIDO DINÁMICO DE TABS
            // ===============================
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {

                when (selectedTab) {

                    BottomTab.Scan ->
                        ScanScreen(
                            viewModel = viewModel,
                            onScanSuccess = {
                                selectedTab = BottomTab.Cart
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
}
