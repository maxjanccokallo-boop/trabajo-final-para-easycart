package com.example.easycart.ui.screens.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.easycart.data.model.Purchase
import com.example.easycart.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ===========================================================
// 🎨 PALETAS
// ===========================================================

private val ProfileHeaderGradientLight = Brush.horizontalGradient(
    listOf(Color(0xFF4A64F0), Color(0xFF8B5CF6))
)

private val ProfileHeaderGradientDark = Brush.horizontalGradient(
    listOf(Color(0xFF1A1A1A), Color(0xFF2A2A2A))
)

private val ProfileScreenBgLight = Brush.verticalGradient(
    listOf(Color(0xFFF7F6FB), Color(0xFFF1ECFF))
)

private val ProfileScreenBgDark = Brush.verticalGradient(
    listOf(Color(0xFF0D0D0D), Color(0xFF1A1A1A))
)

private val BottomWaveLight = Brush.horizontalGradient(
    listOf(Color(0xFF8B5CF6).copy(alpha = 0.25f), Color(0xFF4A64F0).copy(alpha = 0.25f))
)
private val BottomWaveDark = Brush.horizontalGradient(
    listOf(Color(0xFF333333), Color(0xFF222222))
)

// ===========================================================
// ⭐ PANTALLA PRINCIPAL
// ===========================================================
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    darkMode: Boolean,
    onLogout: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user

    val purchasesCount = uiState.purchases.size

    val boughtFromPurchases = uiState.purchases.sumOf { it.items.size }
    val boughtFinal =
        if (boughtFromPurchases > 0) boughtFromPurchases
        else uiState.cart.sumOf { it.quantity }

    val totalSpent = uiState.purchases.sumOf { it.total }

    val totalSavings = uiState.products.sumOf { p ->
        if (p.hasOffer && p.offerPrice != null && p.offerPrice < p.price)
            (p.price - p.offerPrice)
        else 0.0
    }

    val memberSince = user?.metadata?.creationTimestamp?.let(::formatDate) ?: "Sin fecha"

    val lastSyncText by remember { mutableStateOf(formatDateTime(System.currentTimeMillis())) }

    val width = LocalConfiguration.current.screenWidthDp
    val pad = if (width < 400) 12.dp else 16.dp
    val maxWidth = if (width >= 1000) 760.dp else Dp.Unspecified

    val bgBrush = if (darkMode) ProfileScreenBgDark else ProfileScreenBgLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            ProfileHeaderCard(
                email = user?.email ?: "Sin correo",
                memberSince = memberSince,
                purchasesCount = purchasesCount,
                productsBought = boughtFinal,
                totalSpent = totalSpent,
                totalSavings = totalSavings,
                darkMode = darkMode,
                onSettingsClick = {}
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = pad)
                    .widthIn(max = maxWidth),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                SyncSectionCard(
                    usersCount = if (user != null) 1 else 0,
                    purchasesCount = purchasesCount,
                    productsCount = uiState.products.size,
                    btDevicesCount = 4,
                    lastSyncText = lastSyncText,
                    darkMode = darkMode,
                    onSyncArduino = {},
                    onExport = {}
                )

                StatsSectionCard(
                    purchasesCount = purchasesCount,
                    productsBought = boughtFinal,
                    totalSpent = totalSpent,
                    totalSavings = totalSavings,
                    darkMode = darkMode
                )

                HistorySectionCard(
                    purchases = uiState.purchases,
                    darkMode = darkMode
                )

                AccountSectionCard(
                    accountType = "Correo Electrónico",
                    uid = user?.uid ?: "N/A",
                    darkMode = darkMode
                )

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Logout, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar Sesión")
                }

                InfoSectionCard(
                    title = "Sistema SmartCart",
                    text = "Tu cuenta está sincronizada con el sistema del carrito inteligente.",
                    darkMode = darkMode
                )
            }

            BottomWave(darkMode)
        }
    }
}

// ===========================================================
// HEADER PRO
// ===========================================================
@Composable
private fun ProfileHeaderCard(
    email: String,
    memberSince: String,
    purchasesCount: Int,
    productsBought: Int,
    totalSpent: Double,
    totalSavings: Double,
    darkMode: Boolean,
    onSettingsClick: () -> Unit
) {

    val gradient = if (darkMode) ProfileHeaderGradientDark else ProfileHeaderGradientLight
    val pillBg = if (darkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.12f)
    val iconTint = if (darkMode) Color(0xFFD0BFFF) else Color(0xFF7B61FF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp)),
        shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .background(gradient)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Column(Modifier.weight(1f)) {
                        Text(
                            "Mi Perfil",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, null, tint = Color.White.copy(0.9f), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Miembro desde $memberSince",
                                color = Color.White.copy(0.9f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                    ) {
                        Icon(Icons.Default.Settings, null, tint = Color.White)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = pillBg),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        Box(
                            Modifier.size(88.dp).clip(CircleShape).background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = iconTint, modifier = Modifier.size(44.dp))
                        }

                        Text(
                            email,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val width = LocalConfiguration.current.screenWidthDp
                        val compact = width < 360

                        if (compact) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                HeaderStatPill("Compras", purchasesCount.toString(), Icons.Default.ShoppingCart)
                                HeaderStatPill("Productos", productsBought.toString(), Icons.Default.Widgets)
                                HeaderStatPill("Gastado", "S/ ${"%.2f".format(totalSpent)}", Icons.Default.ShoppingBag)
                                HeaderStatPill("Ahorro", "S/ ${"%.2f".format(totalSavings)}", Icons.Default.LocalOffer)
                            }
                        } else {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HeaderStatPill("Compras", purchasesCount.toString(), Icons.Default.ShoppingCart, Modifier.weight(1f))
                                HeaderStatPill("Productos", productsBought.toString(), Icons.Default.Widgets, Modifier.weight(1f))
                                HeaderStatPill("Gastado", "S/ ${"%.2f".format(totalSpent)}", Icons.Default.ShoppingBag, Modifier.weight(1f))
                                HeaderStatPill("Ahorro", "S/ ${"%.2f".format(totalSavings)}", Icons.Default.LocalOffer, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderStatPill(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(86.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(title, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


// ===========================================================
// SINCRONIZACIÓN
// ===========================================================
@Composable
private fun SyncSectionCard(
    usersCount: Int,
    purchasesCount: Int,
    productsCount: Int,
    btDevicesCount: Int,
    lastSyncText: String,
    darkMode: Boolean,
    onSyncArduino: () -> Unit,
    onExport: () -> Unit
) {

    val surface = if (darkMode) Color(0xFF1C1C1C) else Color.White
    val chip =
        if (darkMode) Color(0xFF262626)
        else Color(0xFFF7F7FF)

    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surface)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Refresh, null, tint = Color(0xFF7B61FF))
                Spacer(Modifier.width(8.dp))
                Text("Sincronización de Datos", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            val w = LocalConfiguration.current.screenWidthDp
            val compact = w < 360

            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SyncBoxPro("Usuarios", usersCount.toString(), chip)
                    SyncBoxPro("Boletas", purchasesCount.toString(), chip)
                    SyncBoxPro("Productos", productsCount.toString(), chip)
                    SyncBoxPro("Dispositivos BT", btDevicesCount.toString(), chip)
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SyncBoxPro("Usuarios", usersCount.toString(), chip, Modifier.weight(1f))
                    SyncBoxPro("Boletas", purchasesCount.toString(), chip, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SyncBoxPro("Productos", productsCount.toString(), chip, Modifier.weight(1f))
                    SyncBoxPro("Dispositivos BT", btDevicesCount.toString(), chip, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(14.dp))

            Text("Última sincronización:", fontWeight = FontWeight.SemiBold)
            Text(lastSyncText, color = Color.Gray, style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onSyncArduino,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Sincronizar con Arduino")
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
            ) {
                Icon(Icons.Default.Download, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Exportar Datos", color = Color.White)
            }
        }
    }
}

@Composable
private fun SyncBoxPro(
    title: String,
    value: String,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = Color.DarkGray)
            Spacer(Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}


// ===========================================================
// ESTADÍSTICAS
// ===========================================================
@Composable
private fun StatsSectionCard(
    purchasesCount: Int,
    productsBought: Int,
    totalSpent: Double,
    totalSavings: Double,
    darkMode: Boolean
) {

    val surface = if (darkMode) Color(0xFF1C1C1C) else Color.White

    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surface)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingBag, null, tint = Color(0xFF7B61FF))
                Spacer(Modifier.width(8.dp))
                Text("Mis Estadísticas", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            StatRowPro("🛒 Total de Compras", purchasesCount.toString())
            StatRowPro("📦 Productos Comprados", productsBought.toString())
            StatRowPro("💸 Total Gastado", "S/ ${"%.2f".format(totalSpent)}")
            StatRowPro("🏷️ Total Ahorrado", "S/ ${"%.2f".format(totalSavings)}")
        }
    }
}

@Composable
private fun StatRowPro(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, fontWeight = FontWeight.Bold)
    }
}


// ===========================================================
// HISTORIAL
// ===========================================================
@Composable
private fun HistorySectionCard(
    purchases: List<Purchase>,
    darkMode: Boolean
) {

    val surface = if (darkMode) Color(0xFF1C1C1C) else Color.White
    val oddBg = if (darkMode) Color(0xFF222222) else Color(0xFFF7F7FF)
    val evenBg = if (darkMode) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)

    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surface)
    ) {
        Column(
            Modifier.padding(16.dp).animateContentSize(tween(250))
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReceiptLong, null, tint = Color(0xFF7B61FF))
                Spacer(Modifier.width(8.dp))
                Text("Historial de Compras", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            if (purchases.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(110.dp), contentAlignment = Alignment.Center) {
                    Text("🛍  Aún no has realizado compras", color = Color.Gray)
                }
            } else {
                purchases.forEachIndexed { index, purchase ->
                    val bg by animateColorAsState(
                        if (index % 2 == 0) oddBg else evenBg,
                        tween(300),
                        label = ""
                    )
                    HistoryItemCard(purchase, bg, darkMode)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    purchase: Purchase,
    background: Color,
    darkMode: Boolean
) {
    val itemsCount = purchase.items.size
    val total = purchase.total

    val iconBg = if (darkMode) Color(0xFF2B2730) else Color(0xFFF3F0FF)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShoppingCartCheckout, null, tint = Color(0xFF7B61FF))
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text("Compra de S/ ${"%.2f".format(total)}", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(formatDateTime(purchase.timestamp), color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("$itemsCount items", color = Color.Gray)
                val avg = if (itemsCount > 0) total / itemsCount else 0.0
                Text("≈ S/ ${"%.2f".format(avg)} c/u", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


// ===========================================================
// CUENTA
// ===========================================================
@Composable
private fun AccountSectionCard(
    accountType: String,
    uid: String,
    darkMode: Boolean
) {
    val surface = if (darkMode) Color(0xFF1C1C1C) else Color.White

    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surface)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Key, null, tint = Color(0xFF7B61FF))
                Spacer(Modifier.width(8.dp))
                Text("Información de la Cuenta", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))
            Text("Tipo de cuenta", color = Color.Gray)
            Text(accountType, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))
            Text("ID de Usuario", color = Color.Gray)
            Text(uid, fontWeight = FontWeight.Bold)
        }
    }
}


// ===========================================================
// INFO
// ===========================================================
@Composable
private fun InfoSectionCard(
    title: String,
    text: String,
    darkMode: Boolean
) {
    val surface = if (darkMode) Color(0xFF1C1C1C) else Color.White

    Card(
        modifier = Modifier.fillMaxWidth().shadow(5.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(text, color = Color.Gray)
        }
    }
}


// ===========================================================
// DECORACIÓN
// ===========================================================
@Composable
private fun BottomWave(darkMode: Boolean) {

    val gradient = if (darkMode) BottomWaveDark else BottomWaveLight

    Spacer(Modifier.height(8.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(gradient, RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 26.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Bubble(Color.White.copy(alpha = 0.35f), 34.dp)
            Bubble(Color.White.copy(alpha = 0.22f), 22.dp)
            Bubble(Color.White.copy(alpha = 0.28f), 28.dp)
        }
    }
}

@Composable
private fun Bubble(color: Color, size: Dp) {
    Box(Modifier.size(size).clip(CircleShape).background(color))
}


// ===========================================================
// HELPERS
// ===========================================================
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy, h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
