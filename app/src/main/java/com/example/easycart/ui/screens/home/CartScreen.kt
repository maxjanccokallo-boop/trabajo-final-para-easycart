package com.example.easycart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easycart.viewmodel.MainViewModel
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove

@Composable
fun CartScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ============================
        // 🛒 CARRITO VACÍO
        // ============================
        if (uiState.cart.isEmpty()) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = "Carrito vacío",
                    modifier = Modifier.size(90.dp),
                    tint = Color(0xFFBFC7D1)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Carrito Vacío",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF66707A)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Escanea productos para agregarlos al carrito",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

        } else {

            // ============================
            // 🧾 LISTA DEL CARRITO
            // ============================
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(uiState.cart) { item ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {

                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // FOTO
                                Box(
                                    modifier = Modifier
                                        .size(55.dp)
                                        .background(Color(0xFFEDEEF0), RoundedCornerShape(12.dp))
                                )

                                Spacer(Modifier.width(12.dp))

                                Column(
                                    Modifier.weight(1f)
                                ) {

                                    Text(
                                        item.productName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        "x${item.quantity}  •  S/ ${item.unitPrice}",
                                        color = Color.Gray
                                    )

                                    item.expiresAt?.toDate()?.let { date ->
                                        Text(
                                            "Vence: $date",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF3A5FCD)
                                        )
                                    }
                                }

                                Text(
                                    "S/ ${"%.2f".format(item.totalPrice)}",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            // ============================
                            // ➕➖ CONTROLES DE CANTIDAD
                            // ============================
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                IconButton(onClick = { viewModel.decreaseQuantity(item) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Disminuir")
                                }

                                Text(
                                    item.quantity.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(onClick = { viewModel.increaseQuantity(item) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Aumentar")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ============================
            // 💰 TOTAL
            // ============================
            Text(
                "Total: S/ ${"%.2f".format(uiState.total)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(12.dp))

            // ============================
            // BOTÓN PAGAR EN EFECTIVO
            // ============================

            Button(
                onClick = {
                    navController.navigate("cash_payment")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Proceder al pago")
            }

            Spacer(Modifier.height(6.dp))

            TextButton(
                onClick = { viewModel.clearCart() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vaciar carrito", color = Color.Red)
            }
        }
    }
}
