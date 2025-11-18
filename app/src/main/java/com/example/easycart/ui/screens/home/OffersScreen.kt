package com.example.easycart.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.easycart.viewmodel.MainViewModel
import com.example.easycart.R
import com.example.easycart.data.model.Product
import com.example.easycart.di.AppModule // ⭐ IMPORTADO
import com.example.easycart.viewmodel.MainViewModelFactory // ⭐ IMPORTADO


@Composable
fun OffersScreen(
    // ⭐ CORRECCIÓN APLICADA: Usando Factory
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(AppModule.repo)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    // CAMBIO CLAVE: Filtramos los productos que tienen offerPrice
    val productsWithOffers = remember(uiState.products) {
        uiState.products.filter { it.offerPrice != null }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // -------------------------
        // ENCABEZADO TIPO FIGMA
        // -------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF4F9A)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Promociones",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Aprovecha nuestras ofertas especiales",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // -------------------------
        // LISTA DE PRODUCTOS CON OFFERPRICE
        // -------------------------
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Usamos la lista de productos filtrada
            items(productsWithOffers) { product ->

                OfferCardProduct(
                    product = product,
                    onAddClick = {
                        // Asegúrate de que el ViewModel procese el escaneo correctamente
                        product.barcode.let {
                            viewModel.onBarcodeScanned(it)
                        }
                    }
                )
            }
        }
    }
}

/* ===========================================================
   🃏 TARJETA OPTIMIZADA PARA PRODUCTOS CON OFFERPRICE
=========================================================== */

@Composable
fun OfferCardProduct(
    product: Product,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column {
            // ---------------------------------------------------
            // IMAGEN REAL DEL PRODUCTO O PLACEHOLDER
            // ---------------------------------------------------
            Image(
                painter =
                    if (product.imageUrl != null && product.imageUrl.isNotBlank())
                        rememberAsyncImagePainter(product.imageUrl)
                    else painterResource(R.drawable.offer_placeholder),

                contentDescription = "product image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(16.dp)) {

                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    product.description ?: "Precio especial por tiempo limitado.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(14.dp))

                // -------------- PRECIOS ----------------
                val basePrice = product.price
                val finalPrice = product.offerPrice ?: basePrice

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Column(Modifier.weight(1f)) {
                        Text(
                            "S/ ${String.format("%.2f", basePrice)}",
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            "S/ ${String.format("%.2f", finalPrice)}",
                            color = Color(0xFF1E88E5),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onAddClick() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Agregar")
                    }
                }
            }
        }
    }
}