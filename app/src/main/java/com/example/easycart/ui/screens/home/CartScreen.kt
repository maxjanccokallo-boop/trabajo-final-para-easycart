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
// 🎨 PALETA OFICIAL EASY CART
// ================================
private val DarkBg = Color(0xFF020617)
private val LightBg = Brush.verticalGradient(listOf(Color(0xFFF7F6FB), Color(0xFFF1ECFF)))

private val DarkCard = Color(0xFF0F172A)
private val LightCard = Color.White

private val DarkText = Color.White
private val LightText = Color(0xFF111827)

private val SubtitleDark = Color(0xFF9CA3AF)
private val SubtitleLight = Color.Gray

// Tus gradientes originales (NO se tocan)
private val PurpleButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF6D5DF6), Color(0xFF9B4DFF))
)
private val GreenButtonGradient = Brush.horizontalGradient(
    listOf(Color(0xFF10B981), Color(0xFF22C55E))
)

// ================================
// ⭐ CART SCREEN CON DARK MODE
// ================================
@Composable
fun CartScreen(
    navController: NavController,
    viewModel: MainViewModel,
    darkMode: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()

    val widthDp = LocalConfiguration.current.screenWidthDp
    val horizontalPad = if (widthDp < 400) 14.dp else 18.dp

    val bgModifier = if (darkMode)
        Modifier.background(DarkBg)
    else
        Modifier.background(LightBg)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(bgModifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPad, vertical = 10.dp)
        ) {

            // 🛒 CARRITO VACÍO
            if (uiState.cart.isEmpty()) {
                EmptyCartState(
                    onScanClick = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("targetTab", BottomTab.Scan.name)
                        navController.navigate(Screen.Home.route) { launchSingleTop = true }
                    },
                    onCatalogClick = {
                        navController.currentBackStackEntry
                            ?.savedStateHandle?.set("targetTab", BottomTab.Products.name)
                        navController.navigate(Screen.Home.route) { launchSingleTop = true }
                    },
                    darkMode = darkMode
                )
                return@Column
            }

            // 🧾 TÍTULO
            Text(
                text = "Mi Carrito",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = if (darkMode) DarkText else LightText
            )
            Text(
                text = "${uiState.cart.size} productos",
                style = MaterialTheme.typography.bodySmall,
                color = if (darkMode) SubtitleDark else SubtitleLight
            )

            Spacer(Modifier.height(10.dp))

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
                        onMinus = { viewModel.decreaseQuantity(item) }, // NO SE USA
                        onPlus = { viewModel.increaseQuantity(item) },   // NO SE USA
                        onDelete = { viewModel.removeItem(item) },
                        darkMode = darkMode
                    )
                }
            }

            CartSummaryPro(
                subtotal = uiState.cart.sumOf { it.quantity * it.unitPrice },
                total = uiState.cart.sumOf { it.totalPrice },
                onPayClick = { navController.navigate("payment") },
                onClearClick = { viewModel.clearCart() },
                darkMode = darkMode
            )
        }
    }
}

// =====================================================================
// 🛒 ESTADO VACÍO
// =====================================================================
@Composable
private fun EmptyCartState(
    onScanClick: () -> Unit,
    onCatalogClick: () -> Unit,
    darkMode: Boolean
) {
    val titleColor = if (darkMode) DarkText else LightText
    val subtitleColor = if (darkMode) SubtitleDark else SubtitleLight
    val circleBg = if (darkMode) DarkCard else Color(0xFFEDEBFF)

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
                .background(circleBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFF6D5DF6), modifier = Modifier.size(72.dp))
        }

        Spacer(Modifier.height(18.dp))

        Text("Carrito Vacío", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = titleColor)
        Text("¡Comienza a agregar tus productos favoritos!", style = MaterialTheme.typography.bodyMedium, color = subtitleColor, textAlign = TextAlign.Center)

        Spacer(Modifier.height(22.dp))

        GradientButton("Escanear Producto", Icons.Default.CenterFocusWeak, PurpleButtonGradient, onScanClick)
        Spacer(Modifier.height(12.dp))
        GradientButton("Ver Catálogo", Icons.Default.Inventory2, GreenButtonGradient, onCatalogClick)
    }
}

// =====================================================================
// 🔘 BOTÓN GRADIENTE
// =====================================================================
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
// 🧾 ITEM DEL CARRITO — NUEVO (SIN + / -)
// =====================================================================
@Composable
private fun CartItemCardPro(
    item: CartItem,
    onMinus: () -> Unit, // no usado pero necesario
    onPlus: () -> Unit,  // no usado pero necesario
    onDelete: () -> Unit,
    darkMode: Boolean
) {
    val cardColor = if (darkMode) DarkCard else LightCard
    val titleColor = if (darkMode) DarkText else LightText
    val subtitle = if (darkMode) SubtitleDark else SubtitleLight
    val pillBg = if (darkMode) Color(0xFF1E293B) else Color(0xFFF3F4F6)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.Top) {

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (darkMode) DarkBg else Color(0xFFF1F2F4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocalMall, contentDescription = null, tint = subtitle)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {

                    Text(item.productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = titleColor
                    )

                    Spacer(Modifier.height(4.dp))

                    if (item.hasOffer && item.offerPrice != null) {
                        Text("S/ ${"%.2f".format(item.unitPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitle,
                            textDecoration = TextDecoration.LineThrough
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("S/ ${"%.2f".format(item.offerPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF6D5DF6)
                            )
                        }
                    } else {
                        Text("S/ ${"%.2f".format(item.unitPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(pillBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Cantidad: ${item.quantity}",
                            fontWeight = FontWeight.Medium,
                            color = titleColor
                        )
                    }

                    Text("Código: ${item.barcode}", style = MaterialTheme.typography.bodySmall, color = subtitle)
                }

                Column(horizontalAlignment = Alignment.End) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F))
                    }
                    Text("S/ ${"%.2f".format(item.totalPrice)}",
                        fontWeight = FontWeight.Black,
                        color = titleColor)
                }
            }
        }
    }
}

// =====================================================================
// 🧾 RESUMEN FINAL
// =====================================================================
@Composable
private fun CartSummaryPro(
    subtotal: Double,
    total: Double,
    onPayClick: () -> Unit,
    onClearClick: () -> Unit,
    darkMode: Boolean
) {
    val cardColor = if (darkMode) DarkCard else LightCard
    val textPrimary = if (darkMode) DarkText else LightText
    val subtitle = if (darkMode) SubtitleDark else SubtitleLight
    val ahorro = (subtotal - total).coerceAtLeast(0.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal", color = subtitle)
                Text("S/ ${"%.2f".format(subtotal)}", fontWeight = FontWeight.SemiBold, color = textPrimary)
            }

            if (ahorro > 0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ahorro por ofertas", color = Color(0xFF10B981))
                    Text("- S/ ${"%.2f".format(ahorro)}",
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(color = if (darkMode) Color(0xFF1E293B) else Color.LightGray)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleMedium, color = textPrimary)
                Text("S/ ${"%.2f".format(total)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = textPrimary
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

            TextButton(onClick = onClearClick, modifier = Modifier.fillMaxWidth()) {
                Text("Vaciar carrito", color = Color.Red)
            }
        }
    }
}
