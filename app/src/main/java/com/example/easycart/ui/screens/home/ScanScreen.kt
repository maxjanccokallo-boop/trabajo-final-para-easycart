package com.example.easycart.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easycart.viewmodel.MainViewModel

@Composable
fun ScanScreen(
    viewModel: MainViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    var barcodeField by remember { mutableStateOf(TextFieldValue("")) }

    // Autofocus para pistola
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F4F7))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // ---------------------------------------------------------------------------
        // 📡 DISPOSITIVOS ARDUINO
        // ---------------------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dispositivos Arduino", fontWeight = FontWeight.Bold)
                    Text("Ver todos", color = Color(0xFF2563EB))
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "Sin dispositivos conectados",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ---------------------------------------------------------------------------
        // 🎯 TARJETA PRINCIPAL DE ESCANEO (Figma)
        // ---------------------------------------------------------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {

            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    Icons.Filled.QrCode2,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp),
                    tint = Color(0xFF2563EB)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    "Escanear Producto",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    "El código de barras se genera automáticamente",
                    color = Color.Gray
                )

                Spacer(Modifier.height(20.dp))

                // Botón azul (pero tú lo usarás para abrir cámara después si deseas)
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Filled.CenterFocusWeak, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Escanear con Cámara")
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ---------------------------------------------------------------------------
        // 🔥 TU BARRA DE ESCANEO REAL CON PISTOLA USB — NO SE TOCA
        // ---------------------------------------------------------------------------
        Text("Escáner USB", fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CenterFocusWeak,
                    contentDescription = "scan icon",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(38.dp)
                )

                Spacer(Modifier.width(14.dp))

                TextField(
                    value = barcodeField,
                    onValueChange = { newValue ->
                        barcodeField = newValue
                        if ("\n" in newValue.text) {
                            val clean = newValue.text.replace("\n", "").trim()
                            viewModel.onBarcodeScanned(clean)
                            barcodeField = TextFieldValue("")
                        }
                    },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .focusable(true),
                    placeholder = { Text("Escanea aquí…") }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Feedback del scanner
        when (uiState.scanError) {
            null -> uiState.lastScanned?.let {
                SuccessMessage("Producto agregado: $it")
            }
            else -> ErrorMessage(uiState.scanError ?: "Error desconocido")
        }

        Spacer(Modifier.height(25.dp))

        // ---------------------------------------------------------------------------
        // 🔵 VERIFICACIÓN AUTOMÁTICA
        // ---------------------------------------------------------------------------
        VerificationCard()

        // ---------------------------------------------------------------------------
        // 🟢 CATEGORÍAS DE SALUD
        // ---------------------------------------------------------------------------
        HealthCategoryCard()

        // ---------------------------------------------------------------------------
        // 🟡 SIMULACIÓN ARDUINO
        // ---------------------------------------------------------------------------
        SimulationCard()
    }
}

@Composable
fun VerificationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FF))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("✔ Verificación Automática", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            Spacer(Modifier.height(10.dp))
            VerificationItem("Código de barras generado automáticamente")
            VerificationItem("Fecha de vencimiento verificada")
            VerificationItem("Estado del producto analizado")
            VerificationItem("Stock en tiempo real")
            VerificationItem("Categoría de salud identificada")
            VerificationItem("Productos vencidos son rechazados")
            VerificationItem("Sincronizado con Arduino vía Bluetooth")
        }
    }
}

@Composable
fun VerificationItem(text: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text("• ", color = Color(0xFF2563EB))
        Text(text)
    }
}

@Composable
fun HealthCategoryCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFFFF4))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("❤ Categorías de Salud", fontWeight = FontWeight.Bold, color = Color(0xFF43A047))
            Spacer(Modifier.height(10.dp))
            HealthItem("🟢 Saludable", "Recomendado para consumo regular")
            HealthItem("🟡 Moderado", "Consumir con moderación")
            HealthItem("🔴 No saludable", "Limitar su consumo")
        }
    }
}

@Composable
fun HealthItem(title: String, description: String) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(description, color = Color.Gray)
    }
}

@Composable
fun SimulationCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 40.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Simulación Arduino: Prueba el sensor infrarrojo",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8D6E63)
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { /* viewModel.simulateSensor() */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
            ) {
                Text("Simular Detección Sin Escaneo")
            }
        }
    }
}

@Composable
fun SuccessMessage(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFD4FCD4), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text("✔ $text", color = Color(0xFF2E7D32))
    }
}

@Composable
fun ErrorMessage(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFE0E0), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text("✖ $text", color = Color(0xFFC62828))
    }
}
