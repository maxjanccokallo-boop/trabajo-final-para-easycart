package com.example.easycart.ui.screens.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easycart.viewmodel.MainViewModel

// ================================
// 🎨 GRADIENTES
// ================================
private val CartScreenBg = Brush.verticalGradient(
    listOf(Color(0xFFF7F6FB), Color(0xFFF1ECFF))
)

private val PurpleButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF6D5DF6), Color(0xFF9B4DFF))
)

private val GreenButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF10B981), Color(0xFF22C55E))
)

// ================================
// ⭐ CART SCREEN
// ================================
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val widthDp = LocalConfiguration.current.screenWidthDp
    val horizontalPad = if (widthDp < 400) 14.dp else 18.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CartScreenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPad, vertical = 12.dp)
        ) {

            // ============================
            // 🛒 CARRITO VACÍO
            // ============================
            if (uiState.cart.isEmpty()) {
                EmptyCartState(
                    onScanClick = { navController.navigate("scan") },
                    onCatalogClick = { navController.navigate("products") }
                )
                return@Column
            }

            // ============================
            // 🧾 LISTA DEL CARRITO
            // ============================
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 10.dp)
            ) {
                items(uiState.cart, key = { it.productId }) { item ->
                    CartItemCard(
                        name = item.productName,
                        price = item.unitPrice,
                        offerPrice = item.offerPrice,
                        hasOffer = item.hasOffer,
                        discountPercent = item.discountPercent,
                        quantity = item.quantity,
                        expiresText = item.expiresAt?.toDate()?.toString(),
                        totalItem = item.totalPrice,
                        onMinus = { viewModel.decreaseQuantity(item) },
                        onPlus = { viewModel.increaseQuantity(item) }
                    )
                }
            }

            // ============================
            // 💰 RESUMEN / TOTAL
            // ============================
            CartSummary(
                total = uiState.total,
                onPayClick = { navController.navigate("cash_payment") },
                onClearClick = { viewModel.clearCart() }
            )
        }
    }
}

// =====================================================================
// ⭐ VISTA VACIA
// =====================================================================
@Composable
private fun EmptyCartState(
    onScanClick: () -> Unit,
    onCatalogClick: () -> Unit
) { /* IGUAL QUE TU CÓDIGO, NO LO MUEVO */ }

// =====================================================================
// ⭐ ITEM CARD PRO — OPCIÓN A (OFERTAS COMPLETAS)
// =====================================================================
@Composable
private fun CartItemCard(
    name: String,
    price: Double,
    offerPrice: Double?,
    hasOffer: Boolean,
    discountPercent: Int?,
    quantity: Int,
    expiresText: String?,
    totalItem: Double,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Column(
            Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Imagen
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F2F4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFF9CA3AF))
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {

                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    // ========================
                    // PRECIO NORMAL + OFERTA
                    // ========================
                    if (hasOffer && offerPrice != null) {

                        // Precio tachado
                        Text(
                            text = "S/ ${"%.2f".format(price)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "S/ ${"%.2f".format(offerPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF6D5DF6)
                            )

                            if (discountPercent != null) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFF4C4C))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "-${discountPercent}%",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                    } else {
                        // Sin oferta
                        Text(
                            text = "S/ ${"%.2f".format(price)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!expiresText.isNullOrBlank()) {
                        Text(
                            text = "Vence: $expiresText",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4A64F0)
                        )
                    }
                }

                Text(
                    text = "S/ ${"%.2f".format(totalItem)}",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111827)
                )
            }

            Spacer(Modifier.height(10.dp))

            // ==========================
            // CONTROLES CANTIDAD
            // ==========================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMinus,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Disminuir")
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = onPlus,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar")
                }
            }
        }
    }
}

// =====================================================================
// ⭐ RESUMEN
// =====================================================================
@Composable
private fun CartSummary(
    total: Double,
    onPayClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", style = MaterialTheme.typography.titleMedium)
                Text(
                    "S/ ${"%.2f".format(total)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111827)
                )
            }

            Button(
                onClick = onPayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D5DF6))
            ) {
                Text("Proceder al pago", color = Color.White, fontWeight = FontWeight.Bold)
            }

            TextButton(
                onClick = onClearClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vaciar carrito", color = Color.Red)
            }
        }
    }
}
