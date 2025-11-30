package com.example.easycart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
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

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(BottomTab.Scan) }
    val uiState by viewModel.uiState.collectAsState()
    val isDark = viewModel.darkTheme.value

    // Nombre real
    val displayName = uiState.user?.displayName
        ?: uiState.user?.email?.substringBefore("@")
        ?: "Usuario"

    // Colores según tema
    val backgroundColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF5F7FB)
    val topBarColor = if (isDark) Color(0xFF0F172A) else Color(0xFFE8EAF2)
    val textPrimary = if (isDark) Color.White else Color(0xFF1E1E1E)
    val textSecondary = if (isDark) Color(0xFF10B981) else Color(0xFF0D9488)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                EasyCartBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            },
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(topBarColor)
                        .shadow(4.dp)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF14B8A6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayName.first().uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        // Nombre + estado
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = displayName,
                                color = textPrimary,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "• Sistema operativo",
                                color = textSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // BOTÓN: CAMBIO TEMA
                        IconButton(onClick = { viewModel.toggleTheme() }) {
                            Icon(
                                imageVector = Icons.Default.Brightness6,
                                tint = textPrimary,
                                contentDescription = "Cambiar Tema"
                            )
                        }

                        // BOTÓN: CONFIGURACIÓN
                        IconButton(onClick = {
                            navController.navigate("profile") // O tu ruta real
                        }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                tint = textPrimary,
                                contentDescription = "Configuración"
                            )
                        }
                    }
                }
            }
        ) { padding ->

            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {

                when (selectedTab) {
                    BottomTab.Scan -> ScanScreen(viewModel) {
                        selectedTab = BottomTab.Cart
                    }

                    BottomTab.Cart ->
                        CartScreen(viewModel, navController)

                    BottomTab.Products ->
                        ProductsScreen(viewModel)

                    BottomTab.Offers ->
                        OffersScreen(viewModel)

                    BottomTab.Bluetooth ->
                        BluetoothScreen(mainViewModel = viewModel)

                    BottomTab.Profile ->
                        ProfileScreen(viewModel = viewModel, onLogout = onLogout)
                }
            }
        }
    }
}
