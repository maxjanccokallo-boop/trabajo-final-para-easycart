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
import androidx.navigation.NavController // Aunque no se usa directamente en esta función, mantenemos la importación si es necesaria en el archivo.

@Composable
fun PaymentSuccessScreen(
    // ⭐ CAMBIO CLAVE: Acepta el lambda de acción 'onDone' en lugar del NavController
    onDone: () -> Unit
) {
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
            // ⭐ Usamos el lambda 'onDone' para ejecutar la navegación segura
            onClick = { onDone() }
        ) {
            // ⭐ Cambiamos el texto para reflejar la acción de volver a Home/Inicio
            Text("Volver al inicio")
        }
    }
}