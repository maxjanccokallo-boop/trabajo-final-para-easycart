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
import coil.compose.rememberAsyncImagePainter
import com.example.easycart.viewmodel.MainViewModel
import com.example.easycart.R

@Composable
fun OffersScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

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
        // LISTA REALES DESDE FIRESTORE
        // -------------------------
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            items(uiState.offers) { offer ->

                // 🔥 Buscar el producto real
                val product = uiState.products.find { it.id == offer.productId }

                OfferCard(
                    offer = offer,
                    productName = product?.name ?: "Producto desconocido",
                    productPrice = product?.price,
                    productBarcode = product?.barcode,
                    productImage = product?.imageUrl,
                    onAddClick = {
                        product?.barcode?.let {
                            viewModel.onBarcodeScanned(it)
                        }
                    }
                )
            }
        }
    }
}

/* ===========================================================
   🃏 TARJETA PROFESIONAL POR CADA OFERTA
=========================================================== */

@Composable
fun OfferCard(
    offer: com.example.easycart.data.model.Offer,
    productName: String,
    productPrice: Double?,
    productBarcode: String?,
    productImage: String?,
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
                    if (productImage != null && productImage.isNotBlank())
                        rememberAsyncImagePainter(productImage)
                    else painterResource(R.drawable.offer_placeholder),

                contentDescription = "offer image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(16.dp)) {

                Text(
                    offer.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    offer.description,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(14.dp))

                // -------------- PRECIOS ----------------
                val basePrice = productPrice ?: 0.0
                val discount = basePrice * (offer.discountPercent / 100.0)
                val finalPrice = basePrice - discount

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

                Spacer(Modifier.height(12.dp))

                // ------------ FECHA LIMITE ------------
                offer.validUntil?.toDate()?.let { date ->
                    Text(
                        "Válido hasta: $date",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF616161)
                    )
                }
            }
        }
    }
}
