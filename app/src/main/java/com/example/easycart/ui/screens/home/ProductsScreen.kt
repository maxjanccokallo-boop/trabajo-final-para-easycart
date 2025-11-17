package com.example.easycart.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.easycart.viewmodel.MainViewModel

@Composable
fun ProductsScreen(viewModel: MainViewModel) {

    val uiState by viewModel.uiState.collectAsState()
    var search by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // -------------------------
        // 🔍 Barra de búsqueda
        // -------------------------
        TextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            placeholder = { Text("Buscar productos…") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            },
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF8F9FA),
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(Modifier.height(18.dp))

        // -------------------------
        // 🛍 Lista de productos
        // -------------------------
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            val filtered = uiState.products.filter {
                it.name.lowercase().contains(search.text.lowercase())
            }

            items(filtered) { p ->

                Card(
                    Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // -------- Información del producto --------
                        Column(
                            Modifier.weight(1f)
                        ) {

                            Text(
                                p.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Código: ${p.barcode}",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                "S/ ${p.price}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF1E88E5),
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(4.dp))

                            // --- Badges de estado ---
                            Row {
                                BadgeBox("Stock ${p.stock}", Color(0xFF4CAF50))
                                Spacer(Modifier.width(6.dp))

                                if (p.healthLabel.isNotBlank()) {
                                    HealthBadge(p.healthLabel)
                                }
                            }
                        }

                        // -------- Botón de AGREGAR --------
                        Button(
                            onClick = {
                                // Simula escaneo REAL → agrega al carrito
                                viewModel.onBarcodeScanned(p.barcode)
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E88E5)
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Agregar")
                        }
                    }
                }
            }
        }
    }
}


// -------------------------
// 🎨 Badge Stock
// -------------------------
@Composable
fun BadgeBox(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, color = color, fontWeight = FontWeight.SemiBold)
    }
}

// -------------------------
// ❤️ Badge saludable / moderado / no saludable
// -------------------------
@Composable
fun HealthBadge(label: String) {

    val color = when (label.lowercase()) {
        "saludable" -> Color(0xFF4CAF50)
        "moderado" -> Color(0xFFFFC107)
        else -> Color(0xFFE53935)
    }

    BadgeBox(label, color)
}
