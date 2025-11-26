package com.example.easycart.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.easycart.viewmodel.MainViewModel
import com.example.easycart.utils.InvoicePdfGenerator

@Composable
fun PaymentSuccessScreen(
    onDone: () -> Unit,
    viewModel: MainViewModel? = null
) {
    val context = LocalContext.current
    val uiState = viewModel?.uiState?.collectAsState()?.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "✔ Pago Exitoso",
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = Color(0xFF1E88E5)
        )

        Spacer(Modifier.height(12.dp))
        Text("Tu pago fue procesado correctamente.")

        Spacer(Modifier.height(40.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (uiState != null) {
                    val file = InvoicePdfGenerator.generateInvoicePdf(
                        context = context,
                        items = uiState.cart,
                        total = uiState.total
                    )

                    println("📄 PDF generado: ${file?.absolutePath}")
                }
            }
        ) {
            Text("Descargar boleta PDF")
        }

        Spacer(Modifier.height(15.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onDone() }
        ) {
            Text("Volver al inicio")
        }
    }
}
