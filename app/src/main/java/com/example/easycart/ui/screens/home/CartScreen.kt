package com.example.easycart.ui.screens.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easycart.data.model.CartItem
import com.example.easycart.ui.navigation.BottomTab
import com.example.easycart.ui.navigation.Screen
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
// ⭐ CART SCREEN PRO
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
                .padding(horizontal = horizontalPad, vertical = 10.dp)
        ) {

            // ============================
            // 🛒 CARRITO VACÍO
            // ============================
            if (uiState.cart.isEmpty()) {
                EmptyCartState(
                    onScanClick = {
                        // ir a tab Escanear sin usar ruta inexistente
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("targetTab", BottomTab.Scan.name)

                        navController.navigate(Screen.Home.route) { launchSingleTop = true }
                    },
                    onCatalogClick = {
                        // ir a tab Productos sin usar "products"
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("targetTab", BottomTab.Products.name)

                        navController.navigate(Screen.Home.route) { launchSingleTop = true }
                    }
                )
                return@Column
            }

            // ============================
            // 🧾 TÍTULO + SUBTÍTULO
            // ============================
            Text(
                text = "Mi Carrito",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )
            Text(
                text = "${uiState.cart.size} productos",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(Modifier.height(10.dp))

            // ============================
            // 🧾 LISTA DEL CARRITO
            // ============================
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(uiState.cart, key = { it.productId }) { item ->
                    CartItemCardPro(
                        item = item,
                        onMinus = { viewModel.decreaseQuantity(item) },
                        onPlus = { viewModel.increaseQuantity(item) },
                        onDelete = { viewModel.removeItem(item) }
                    )
                }
            }

            // ============================
            // 💰 RESUMEN / TOTAL
            // ============================
            CartSummaryPro(
                subtotal = uiState.cart.sumOf { it.quantity * it.unitPrice },
                total = uiState.cart.sumOf { it.totalPrice }, // YA usa descuento
                onPayClick = { navController.navigate("payment") },
                onClearClick = { viewModel.clearCart() }
            )
        }
    }
}

// =====================================================================
// ✅ ESTADO VACÍO PRO
// =====================================================================
@Composable
private fun EmptyCartState(
    onScanClick: () -> Unit,
    onCatalogClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(0xFFEDEBFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "Carrito vacío",
                tint = Color(0xFF6D5DF6),
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Carrito Vacío",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "¡Comienza a agregar tus productos favoritos!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(22.dp))

        GradientButton(
            text = "Escanear Producto",
            icon = Icons.Default.CenterFocusWeak,
            gradient = PurpleButtonGradient,
            onClick = onScanClick
        )

        Spacer(Modifier.height(12.dp))

        GradientButton(
            text = "Ver Catálogo",
            icon = Icons.Default.Inventory2,
            gradient = GreenButtonGradient,
            onClick = onCatalogClick
        )
    }
}

@Composable
private fun GradientButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.78f)
            .height(54.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(text, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// =====================================================================
// ✅ ITEM CARD PRO (ofertas + eliminar)
// =====================================================================
@Composable
private fun CartItemCardPro(
    item: CartItem,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onDelete: () -> Unit
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

            Row(verticalAlignment = Alignment.Top) {

                // Imagen placeholder
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F2F4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalMall,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    // Precio normal / oferta
                    if (item.hasOffer && item.offerPrice != null) {
                        Text(
                            text = "S/ ${"%.2f".format(item.unitPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "S/ ${"%.2f".format(item.offerPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF6D5DF6)
                            )

                            if (item.discountPercent != null) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFF4C4C))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "-${item.discountPercent}%",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "S/ ${"%.2f".format(item.unitPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "x${item.quantity} • ${item.barcode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    item.expiresAt?.toDate()?.let {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Vence: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4A64F0)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    IconButton(onClick = { /* favorito si quieres */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = null)
                    }
                    Text(
                        text = "S/ ${"%.2f".format(item.totalPrice)}",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    modifier = Modifier.weight(1f),
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

                    Spacer(Modifier.width(10.dp))

                    Text(
                        item.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.width(10.dp))

                    IconButton(
                        onClick = onPlus,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEDEBFF))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar", tint = Color(0xFF6D5DF6))
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFEDED))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F))
                }
            }
        }
    }
}

// =====================================================================
// ✅ RESUMEN PRO (subtotal vs total)
// =====================================================================
@Composable
private fun CartSummaryPro(
    subtotal: Double,
    total: Double,
    onPayClick: () -> Unit,
    onClearClick: () -> Unit
) {
    val ahorro = (subtotal - total).coerceAtLeast(0.0)

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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal", color = Color.Gray)
                Text("S/ ${"%.2f".format(subtotal)}", fontWeight = FontWeight.SemiBold)
            }

            if (ahorro > 0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ahorro por ofertas", color = Color(0xFF10B981))
                    Text("- S/ ${"%.2f".format(ahorro)}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                }
            }

            Divider()

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
