package com.example.easycart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easycart.viewmodel.MainViewModel
import com.example.easycart.viewmodel.LedState
import com.example.easycart.di.AppModule
import com.example.easycart.viewmodel.MainViewModelFactory

// ⭐ Longitud exacta del código de barras
const val FIXED_BARCODE_LENGTH = 12

@Composable
fun ScanScreen(
    viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(AppModule.repo)),
    onScanSuccess: () -> Unit // ⭐ 1. AÑADIMOS EL CALLBACK DE NAVEGACIÓN
) {
    val uiState by viewModel.uiState.collectAsState()
    var scannedText by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    // ⭐ LANZAMOS EL ENFOQUE AL INICIO
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // ⭐ 2. LÓGICA DE NAVEGACIÓN AUTOMÁTICA
    // Observa si hubo un escaneo exitoso (usamos lastScanned)
    LaunchedEffect(uiState.lastScanned) {
        if (uiState.lastScanned != null) {
            // Si el último escaneo fue exitoso (no es nulo), navegamos al carrito
            onScanSuccess()
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Text("Escáner USB", fontWeight = FontWeight.Bold)

        // ⭐ TEXTFIELD CON LÓGICA DE ESCANEO AUTOMÁTICO
        TextField(
            value = scannedText,
            onValueChange = { newCode ->

                scannedText = newCode

                val tempCode = newCode.trim()

                // ----------------------------------------------------
                // LÓGICA DE DETECCIÓN AUTOMÁTICA (12 DÍGITOS)
                // ----------------------------------------------------
                if (tempCode.length == FIXED_BARCODE_LENGTH) {

                    viewModel.onBarcodeScanned(tempCode) // Añade automáticamente
                    scannedText = "" // Limpia el campo

                } else if (newCode.endsWith("\n")) {
                    // Lógica de Enter como respaldo si la longitud no era 12.
                    val clean = newCode.trim()
                    if (clean.isNotEmpty()) {
                        viewModel.onBarcodeScanned(clean)
                    }
                    scannedText = ""
                }
            },
            label = { Text("Escanea aquí...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .focusable(true),
            leadingIcon = { Icon(Icons.Default.CenterFocusWeak, contentDescription = "Scan Icon") }
        )

        Spacer(Modifier.height(20.dp))

        // Muestra mensajes de éxito o error
        when (uiState.scanError) {
            null -> uiState.lastScanned?.let { SuccessMessage("Producto agregado: $it") }
            else -> ErrorMessage(uiState.scanError ?: "Error desconocido")
        }

        Spacer(Modifier.height(20.dp))

        // Muestra el estado del LED
        val color = when (uiState.ledState) {
            LedState.GREEN -> Color.Green
            LedState.RED -> Color.Red
            LedState.YELLOW -> Color.Yellow
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Estado LED: ")
            Box(Modifier.size(20.dp).background(color, CircleShape))
        }
    }
}

@Composable
fun SuccessMessage(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFD4FCD4), shape = CircleShape)
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
            .background(Color(0xFFFFE0E0), shape = CircleShape)
            .padding(12.dp)
    ) {
        Text("✖ $text", color = Color(0xFFC62828))
    }
}