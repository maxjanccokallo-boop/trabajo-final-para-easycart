package com.example.easycart.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PaymentSuccessScreen(navController: NavController) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "✔ Pago realizado",
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = Color(0xFF1E88E5)
        )

        Spacer(Modifier.height(12.dp))

        Text("Tu compra en efectivo fue registrada con éxito.")

        Spacer(Modifier.height(30.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate("products") }
        ) {
            Text("Volver a comprar")
        }
    }
}
