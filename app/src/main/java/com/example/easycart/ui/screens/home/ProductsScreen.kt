package com.example.easycart.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easycart.viewmodel.MainViewModel
import com.example.easycart.di.AppModule
import com.example.easycart.viewmodel.MainViewModelFactory
import com.example.easycart.data.model.Product // Aseguramos la importación del modelo

@Composable
fun ProductsScreen(
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(AppModule.repo)
    )
) {

    val uiState by viewModel.uiState.collectAsState()
    var search by remember { mutableStateOf(TextFieldValue("")) }
    val searchQuery = search.text.lowercase().trim() // Limpiar y estandarizar la búsqueda

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // BUSCADOR
        TextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            placeholder = { Text("Buscar productos por nombre o código de barras...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            },
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF2F3F4),
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(Modifier.height(18.dp))

        // LISTA DE PRODUCTOS
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ⭐ LÓGICA DE FILTRADO MEJORADA: Busca por Nombre O por Barcode
            val filtered = if (searchQuery.isBlank()) {
                uiState.products
            } else {
                uiState.products.filter { p ->
                    // 1. Buscar por Nombre (contiene)
                    val matchesName = p.name.lowercase().contains(searchQuery)

                    // 2. Buscar por Código de Barras (exacto o contiene, si no es nulo)
                    val matchesBarcode = p.barcode?.contains(searchQuery) ?: false

                    matchesName || matchesBarcode
                }
            }

            items(filtered) { p ->
                ProductItemCard(p, viewModel)
            }
        }
    }
}

// ⭐ Nuevo Composable para organizar el código de la tarjeta del producto
@Composable
fun ProductItemCard(p: Product, viewModel: MainViewModel) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(Modifier.weight(1f)) {

                Text(
                    p.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Código: ${p.barcode ?: "N/A"}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    "S/ ${p.price}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E88E5)
                )

                Spacer(Modifier.height(6.dp))

                Row {
                    BadgeBox("Stock ${p.stock}", Color(0xFF4CAF50))
                    Spacer(Modifier.width(6.dp))
                    if (p.healthLabel.isNotBlank()) {
                        HealthBadge(p.healthLabel)
                    }
                }
            }

            Button(
                // ⭐ AL PRESIONAR AGREGAR: Usa el código de barras (barcode) para el escaneo/añadido
                onClick = {
                    val barcode = p.barcode
                    if (barcode != null) {
                        viewModel.onBarcodeScanned(barcode)
                    }
                    // Si el barcode es nulo, se podría mostrar un mensaje de error o usar el product.id
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Agregar")
            }
        }
    }
}


@Composable
fun BadgeBox(text: String, color: Color) {
    Box(
        Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun HealthBadge(label: String) {
    val color = when (label.lowercase()) {
        "saludable" -> Color(0xFF4CAF50)
        "moderado" -> Color(0xFFFFC107)
        else -> Color(0xFFE53935)
    }
    BadgeBox(label, color)
}