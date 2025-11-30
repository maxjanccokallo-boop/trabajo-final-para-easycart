package com.example.easycart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easycart.ui.navigation.BottomTab
import com.example.easycart.ui.components.EasyCartBottomBar
import com.example.easycart.viewmodel.MainViewModel

// Colores del header
private val DarkHeader = Color(0xFF0F172A)
private val LightHeader = Color(0xFFF1F5F9)

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel,
    darkMode: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(BottomTab.Scan) }
    val uiState by viewModel.uiState.collectAsState()

    // Sacamos el nombre ANTES del @
    val userEmail = uiState.user?.email ?: "usuario"
    val extractedName = userEmail.substringBefore("@")

    val headerColor = if (darkMode) DarkHeader else LightHeader
    val textColor = if (darkMode) Color.White else Color.Black

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .shadow(6.dp)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    // Avatar redondo con inicial
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = extractedName.first().uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            extractedName,
                            color = textColor,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "• Sistema operativo",
                            color = Color(0xFF34D399),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // Botón para cambiar tema
                    IconButton(onClick = onToggleTheme) {
                        Icon(Icons.Default.Brightness4, contentDescription = null, tint = textColor)
                    }

                    // Botón ajustes
                    IconButton(onClick = { /* abrir ajustes aquí */ }) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = textColor)
                    }
                }
            }
        },

        // BOTTOM BAR
        bottomBar = {
            EasyCartBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            // 🔥 CAMBIO DE TABS 100% compatible
            when (selectedTab) {

                BottomTab.Scan ->
                    ScanScreen(
                        viewModel = viewModel,
                        onScanSuccess = { selectedTab = BottomTab.Cart },
                        darkMode = darkMode   // ← AQUI le enviamos el modo real
                    )

                BottomTab.Cart ->
                    CartScreen(
                        navController = navController,
                        viewModel = viewModel,
                        darkMode = darkMode     // ← AQUÍ SE SOLUCIONA
                    )

                BottomTab.Products ->
                    ProductsScreen(
                        viewModel = viewModel,
                        darkMode = darkMode
                    )

                BottomTab.Offers ->
                    OffersScreen(viewModel,
                        darkMode = darkMode)

                BottomTab.Bluetooth ->
                    BluetoothScreen(
                        mainViewModel = viewModel,
                        darkMode = darkMode
                    )

                BottomTab.Profile ->
                    ProfileScreen(
                        viewModel = viewModel,
                        darkMode = darkMode,
                        onLogout = onLogout
                    )
            }
        }
    }
}
