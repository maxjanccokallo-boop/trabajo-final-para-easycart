package com.example.easycart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easycart.ui.navigation.BottomTab
import com.example.easycart.ui.theme.GreenPrimary
import com.example.easycart.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    navController: NavController,   // 👈 CORRECTO
    viewModel: MainViewModel,       // 👈 CORRECTO
    onLogout: () -> Unit
) {

    var selectedTab by remember { mutableStateOf(BottomTab.Scan) }
    val uiState by viewModel.uiState.collectAsState()

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
                        "🛒 ${uiState.cart.sumOf { it.quantity }}",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    "Estado LED: ●",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->

        Box(Modifier.padding(paddingValues)) {

            when (selectedTab) {

                BottomTab.Scan ->
                    ScanScreen(viewModel)

                BottomTab.Cart ->
                    CartScreen(
                        viewModel = viewModel,
                        navController = navController     // 👈 MUY IMPORTANTE
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
