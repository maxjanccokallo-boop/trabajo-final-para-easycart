package com.example.easycart.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.easycart.viewmodel.MainViewModel

@Composable
fun PaymentSuccessScreen(
    pdfPath: String?,
    navController: NavController,
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "✔ Pago Exitoso",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E88E5)
        )

        Spacer(Modifier.height(12.dp))

        Text("Tu pago fue procesado correctamente.")

        Spacer(Modifier.height(40.dp))

        // ------- DESCARGAR PDF -------
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (!pdfPath.isNullOrEmpty()) {
                    println("PDF almacenado en: $pdfPath")
                } else {
                    println("No se recibió PDF")
                }
            }
        ) {
            Text("Descargar boleta PDF")
        }

        Spacer(Modifier.height(15.dp))

        // ------- VOLVER -------
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }
        ) {
            Text("Volver al inicio")
        }
    }
}
