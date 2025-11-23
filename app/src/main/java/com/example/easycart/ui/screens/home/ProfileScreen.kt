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

/* ===========================================================
   ✅ PROFILE SCREEN PRO
   - MISMA LÓGICA (uiState)
   - DISEÑO UNIFICADO con tus colores/forma
   - RESPONSIVE
   - SIN ROMPER NADA
=========================================================== */

// ================================
// 🎨 PALETA / GRADIENTES (como tu home)
// ================================
private val ProfileHeaderGradient = Brush.horizontalGradient(
    listOf(Color(0xFF4A64F0), Color(0xFF8B5CF6))
)

private val ProfileScreenBg = Brush.verticalGradient(
    listOf(Color(0xFFF7F6FB), Color(0xFFF1ECFF))
)

// Curvita decorativa de abajo (tipo tus imágenes)
private val BottomWaveGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFF8B5CF6).copy(alpha = 0.25f),
        Color(0xFF4A64F0).copy(alpha = 0.25f)
    )
)

/* ===========================================================
   ⭐ PANTALLA PRINCIPAL
=========================================================== */
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user

    // ---------------------------
    // 📌 Datos reales desde tu uiState (SIN CAMBIAR LÓGICA)
    // ---------------------------
    val purchasesCount = uiState.purchases.size

    // items en Purchase es List<Map<String, Any>>
    val productsBoughtFromPurchases = uiState.purchases.sumOf { it.items.size }
    val productsBought =
        if (productsBoughtFromPurchases > 0) productsBoughtFromPurchases
        else uiState.cart.sumOf { it.quantity }

    val totalSpent = uiState.purchases.sumOf { it.total }

    // ahorro total usando products (mismo criterio que ofertas)
    val totalSavings = remember(uiState.products) {
        uiState.products.sumOf { p ->
            if (p.hasOffer && p.offerPrice != null && p.offerPrice < p.price)
                (p.price - p.offerPrice)
            else 0.0
        }
    }

    // fecha real de creación (Firebase)
    val memberSince = remember(user) {
        user?.metadata?.creationTimestamp?.let(::formatDate) ?: "Sin fecha"
    }

    // última sync mostrada (solo UI, no rompe nada)
    val lastSyncText by remember {
        mutableStateOf(formatDateTime(System.currentTimeMillis()))
    }

    // Responsive padding
    val widthDp = LocalConfiguration.current.screenWidthDp
    val horizontalPad = if (widthDp < 400) 12.dp else 16.dp
    val contentMaxWidth = if (widthDp >= 1000) 760.dp else Dp.Unspecified

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileScreenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ✅ HEADER PRO
            ProfileHeaderCard(
                email = user?.email ?: "Sin correo",
                memberSince = memberSince,
                purchasesCount = purchasesCount,
                productsBought = productsBought,
                totalSpent = totalSpent,
                totalSavings = totalSavings,
                onSettingsClick = { /* navegas a settings si quieres */ }
            )

            // ✅ CONTENEDOR CENTRAL RESPONSIVE
            Column(
                modifier = Modifier
                    .padding(horizontal = horizontalPad)
                    .widthIn(max = contentMaxWidth),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ✅ SINCRONIZACIÓN
                SyncSectionCard(
                    usersCount = if (user != null) 1 else 0,
                    purchasesCount = purchasesCount,
                    productsCount = uiState.products.size,
                    btDevicesCount = 4,
                    lastSyncText = lastSyncText,
                    onSyncArduino = { /* tu lógica sync */ },
                    onExport = { /* tu lógica export */ }
                )

                // ✅ ESTADÍSTICAS
                StatsSectionCard(
                    purchasesCount = purchasesCount,
                    productsBought = productsBought,
                    totalSpent = totalSpent,
                    totalSavings = totalSavings
                )

                // ✅ HISTORIAL REAL
                HistorySectionCard(
                    purchases = uiState.purchases
                )

                // ✅ CUENTA
                AccountSectionCard(
                    accountType = "Correo Electrónico",
                    uid = user?.uid ?: "N/A"
                )

                // ✅ BOTÓN CERRAR SESIÓN
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // ✅ INFO SMARTCART
                InfoSectionCard(
                    title = "Sistema SmartCart",
                    text = "Tu cuenta está sincronizada con el sistema del carrito inteligente. Escaneos y compras seguras."
                )
            }

            // ✅ FORMA DECORATIVA ABAJO (como tu imagen)
            BottomWave()
        }
    }
}

/* ===========================================================
   ✅ HEADER PRO (mismo estilo que inicio)
=========================================================== */
@Composable
private fun ProfileHeaderCard(
    email: String,
    memberSince: String,
    purchasesCount: Int,
    productsBought: Int,
    totalSpent: Double,
    totalSavings: Double,
    onSettingsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp)),
        shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(ProfileHeaderGradient)
                .padding(horizontal = 18.dp, vertical = 18.dp)
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
                            Icon(
                                Icons.Default.CalendarMonth,
                                null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Miembro desde $memberSince",
                                color = Color.White.copy(alpha = 0.9f),
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
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                tint = Color(0xFF7B61FF),
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Text(
                            email,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val widthDp = LocalConfiguration.current.screenWidthDp
                        val isCompact = widthDp < 360

                        if (isCompact) {
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
                                HeaderStatPill(
                                    "Compras",
                                    purchasesCount.toString(),
                                    Icons.Default.ShoppingCart,
                                    modifier = Modifier.weight(1f)
                                )
                                HeaderStatPill(
                                    "Productos",
                                    productsBought.toString(),
                                    Icons.Default.Widgets,
                                    modifier = Modifier.weight(1f)
                                )
                                HeaderStatPill(
                                    "Gastado",
                                    "S/ ${"%.2f".format(totalSpent)}",
                                    Icons.Default.ShoppingBag,
                                    modifier = Modifier.weight(1f)
                                )
                                HeaderStatPill(
                                    "Ahorro",
                                    "S/ ${"%.2f".format(totalSavings)}",
                                    Icons.Default.LocalOffer,
                                    modifier = Modifier.weight(1f)
                                )
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
            modifier = Modifier.fillMaxSize().padding(12.dp),
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

/* ===========================================================
   ✅ SINCRONIZACIÓN
=========================================================== */
@Composable
private fun SyncSectionCard(
    usersCount: Int,
    purchasesCount: Int,
    productsCount: Int,
    btDevicesCount: Int,
    lastSyncText: String,
    onSyncArduino: () -> Unit,
    onExport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Refresh, null, tint = Color(0xFF7B61FF))
                Spacer(Modifier.width(8.dp))
                Text("Sincronización de Datos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(12.dp))

            val widthDp = LocalConfiguration.current.screenWidthDp
            val compact = widthDp < 360

            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SyncBoxPro("Usuarios", usersCount.toString(), Color(0xFFE9FFF1))
                    SyncBoxPro("Boletas", purchasesCount.toString(), Color(0xFFEAF2FF))
                    SyncBoxPro("Productos", productsCount.toString(), Color(0xFFF4ECFF))
                    SyncBoxPro("Dispositivos BT", btDevicesCount.toString(), Color(0xFFFFF6E5))
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SyncBoxPro("Usuarios", usersCount.toString(), Color(0xFFE9FFF1), modifier = Modifier.weight(1f))
                    SyncBoxPro("Boletas", purchasesCount.toString(), Color(0xFFEAF2FF), modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SyncBoxPro("Productos", productsCount.toString(), Color(0xFFF4ECFF), modifier = Modifier.weight(1f))
                    SyncBoxPro("Dispositivos BT", btDevicesCount.toString(), Color(0xFFFFF6E5), modifier = Modifier.weight(1f))
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

            Spacer(Modifier.height(12.dp))

            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E7)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Los datos se guardan localmente. Para producción usa Firebase, Supabase o MongoDB.",
                    Modifier.padding(10.dp),
                    color = Color(0xFF8A6D3B),
                    style = MaterialTheme.typography.bodySmall
                )
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
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            Spacer(Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/* ===========================================================
   ✅ ESTADÍSTICAS
=========================================================== */
@Composable
private fun StatsSectionCard(
    purchasesCount: Int,
    productsBought: Int,
    totalSpent: Double,
    totalSavings: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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

/* ===========================================================
   ✅ HISTORIAL
=========================================================== */
@Composable
private fun HistorySectionCard(
    purchases: List<Purchase>
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                Box(
                    Modifier.fillMaxWidth().height(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛍  Aún no has realizado compras", color = Color.Gray)
                }
            } else {
                purchases.forEachIndexed { idx, p ->
                    val bg by animateColorAsState(
                        targetValue = if (idx % 2 == 0) Color(0xFFF7F7FF) else Color(0xFFFFFFFF),
                        animationSpec = tween(300),
                        label = "historyBG"
                    )
                    HistoryItemCard(p, bg)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    purchase: Purchase,
    background: Color
) {
    val itemsCount = purchase.items.size
    val total = purchase.total

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF3F0FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShoppingCartCheckout, null, tint = Color(0xFF7B61FF))
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text("Compra de S/ ${"%.2f".format(total)}", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(formatDateTime(purchase.timestamp), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("$itemsCount items", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(2.dp))
                val avg = if (itemsCount > 0) total / itemsCount else 0.0
                Text("≈ S/ ${"%.2f".format(avg)} c/u", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/* ===========================================================
   ✅ CUENTA
=========================================================== */
@Composable
private fun AccountSectionCard(
    accountType: String,
    uid: String
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Key, null, tint = Color(0xFF7B61FF))
                Spacer(Modifier.width(8.dp))
                Text("Información de la Cuenta", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))
            Text("Tipo de cuenta", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            Text(accountType, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))
            Text("ID de Usuario", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            Text(uid, fontWeight = FontWeight.Bold)
        }
    }
}

/* ===========================================================
   ✅ INFO
=========================================================== */
@Composable
private fun InfoSectionCard(
    title: String,
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(5.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(text, color = Color.Gray)
        }
    }
}

/* ===========================================================
   ✅ DECORACIÓN ABAJO
=========================================================== */
@Composable
private fun BottomWave() {
    Spacer(Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(BottomWaveGradient, RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 26.dp),
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
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(color)
    )
}

/* ===========================================================
   ✅ Helpers fecha
=========================================================== */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy, h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
