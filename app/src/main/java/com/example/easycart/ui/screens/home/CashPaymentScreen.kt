package com.example.easycart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.easycart.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CashPaymentScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        // TÍTULO
        Text(
            "Confirmar Pago en Efectivo",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(14.dp))

        Text("Fecha: $date", color = Color.Gray)

        Spacer(Modifier.height(20.dp))


        // ============================
        //   BOLETA PREVIA
        // ============================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("🧾 Boleta de Compra", fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(12.dp))

                uiState.cart.forEach { item ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.productName} x${item.quantity}")
                        Text("S/ ${String.format("%.2f", item.totalPrice)}")
                    }

                    Spacer(Modifier.height(6.dp))
                }

                Spacer(Modifier.height(10.dp))
                Divider()
                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TOTAL", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "S/ ${String.format("%.2f", uiState.total)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1E88E5)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // BOTÓN DE CONFIRMAR
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            onClick = {
                viewModel.clearCart()        // Limpia el carrito
                navController.navigate("payment_success") {
                    popUpTo("cart") { inclusive = true }
                }
            }
        ) {
            Text("Confirmar pago")
        }
    }
}
