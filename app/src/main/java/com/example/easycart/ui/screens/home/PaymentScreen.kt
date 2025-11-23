package com.example.easycart.ui.screens.home

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easycart.viewmodel.MainViewModel
import androidx.compose.foundation.BorderStroke
import com.example.easycart.utils.QrGenerator

private val PaymentBg = Brush.verticalGradient(
    listOf(Color(0xFFF7F6FB), Color(0xFFF1ECFF))
)

@Composable
fun PaymentScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selected by remember { mutableStateOf("yape") }
    var isLoading by remember { mutableStateOf(false) }
    var showVisaForm by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(PaymentBg)
            .padding(16.dp)
    ) {

        Column(Modifier.fillMaxSize()) {

            Text("Método de Pago", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(12.dp))

            // ---- QR YAPE
            PaymentOptionCard(
                title = "Pagar con Yape (QR)",
                subtitle = "Escanea el QR desde tu app",
                icon = Icons.Default.QrCode,
                isSelected = selected == "yape",
                onSelect = {
                    selected = "yape"
                    showVisaForm = false
                }
            )

            Spacer(Modifier.height(10.dp))

            // ---- VISA
            PaymentOptionCard(
                title = "Pagar con Visa",
                subtitle = "Tarjeta débito o crédito",
                icon = Icons.Default.CreditCard,
                isSelected = selected == "visa",
                onSelect = {
                    selected = "visa"
                    showVisaForm = true
                }
            )

            Spacer(Modifier.height(18.dp))

            // QR ✔✔✔
            if (selected == "yape") {
                QRSection(amount = uiState.total)
            }

            // FORMULARIO VISA ✔✔✔
            if (selected == "visa" && showVisaForm) {
                VisaFormSection(
                    onSaved = {
                        showVisaForm = false
                    }
                )
            }

            Spacer(Modifier.height(25.dp))

            Button(
                onClick = {
                    isLoading = true
                    viewModel.finalizePurchase { ok ->
                        isLoading = false
                        if (ok) navController.navigate("payment_success")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Text("Confirmar pago", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =====================================================================
// ⭐ Tarjeta VISA - Formulario
// =====================================================================
@Composable
fun VisaFormSection(onSaved: () -> Unit) {
    var number by remember { mutableStateOf("") }
    var exp by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = number,
            onValueChange = { number = it },
            label = { Text("Número de tarjeta") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = exp,
            onValueChange = { exp = it },
            label = { Text("MM/YY") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = cvv,
            onValueChange = { cvv = it },
            label = { Text("CVV") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { onSaved() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Guardar tarjeta")
        }
    }
}

// =====================================================================
// ⭐ QR DE PAGO YAPE
// =====================================================================
@Composable
fun QRSection(amount: Double) {
    val url = "https://yape.com.pe/pay?amount=${"%.2f".format(amount)}&ref=EASYCART123"

    val qrBitmap = remember { QrGenerator.generate(url) }

    if (qrBitmap != null) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Escanea el QR para pagar con Yape", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR Pago Yape",
                modifier = Modifier.size(220.dp)
            )
        }
    }
}

// =====================================================================
// ⭐ Componente opción de pago
// =====================================================================
@Composable
private fun PaymentOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val bg = if (isSelected) Color(0xFFEDEBFF) else Color.White
    val border = if (isSelected) BorderStroke(2.dp, Color(0xFF6D5DF6)) else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = border
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF6D5DF6))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            RadioButton(selected = isSelected, onClick = onSelect)
        }
    }
}
