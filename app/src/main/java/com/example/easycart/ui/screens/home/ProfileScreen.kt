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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.easycart.data.model.Purchase
import com.example.easycart.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

// ===========================================================
// 🎨 PALETAS
// ===========================================================

private val ProfileHeaderGradientLight = Brush.horizontalGradient(
    listOf(Color(0xFF6A5BFF), Color(0xFF9F4CFF))
)
private val ProfileHeaderGradientDark = Brush.horizontalGradient(
    listOf(Color(0xFF1A1A1A), Color(0xFF2A2A2A))
)

private val ProfileScreenBgLight = Brush.verticalGradient(
    listOf(Color(0xFFF8F9FF), Color(0xFFF1ECFF))
)
private val ProfileScreenBgDark = Brush.verticalGradient(
    listOf(Color(0xFF0D0D0D), Color(0xFF1A1A1A))
)


// ===========================================================
// ⭐ PANTALLA PRINCIPAL COMPLETA
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
    val productsFromPurchases = uiState.purchases.sumOf { it.items.size }
    val productsBought =
        if (productsFromPurchases > 0) productsFromPurchases
        else uiState.cart.sumOf { it.quantity }

    val totalSpent = uiState.purchases.sumOf { it.total }

    val totalSavings = uiState.products.sumOf {
        if (it.hasOffer && it.offerPrice != null && it.offerPrice < it.price)
            (it.price - it.offerPrice)
        else 0.0
    }

    val memberSince = user?.metadata?.creationTimestamp?.let(::formatDate) ?: "Sin fecha"

    val bg = if (darkMode) ProfileScreenBgDark else ProfileScreenBgLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            ProfileHeader(
                email = user?.email ?: "Sin correo",
                memberSince = memberSince,
                purchasesCount = purchasesCount,
                productsBought = productsBought,
                totalSpent = totalSpent,
                totalSavings = totalSavings,
                darkMode = darkMode,
                onSettingsClick = {}
            )

            Column(
                Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                StatsSection(
                    purchasesCount,
                    productsBought,
                    totalSpent,
                    totalSavings,
                    darkMode
                )

                HistorySection(
                    purchases = uiState.purchases,
                    darkMode = darkMode
                )

                AccountInfoCard(
                    accountType = "Correo Electrónico",
                    uid = user?.uid ?: "N/A",
                    darkMode = darkMode
                )

                SmartCartInfoCard(darkMode)

                LogoutButton(onLogout)
            }
        }
    }
}


// ===========================================================
// 🎨 HEADER NUEVO EXACTO AL DISEÑO
// ===========================================================
@Composable
private fun ProfileHeader(
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            Modifier
                .background(gradient)
                .padding(20.dp)
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    ) {
                        Icon(Icons.Default.Settings, null, tint = Color.White)
                    }
                }

                // Avatar
                Box(
                    Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = if (darkMode) Color(0xFFD7C7FF) else Color(0xFF7B4CFF),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    email.substringBefore("@"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    email,
                    color = Color.White.copy(0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth, null,
                        tint = Color.White.copy(0.9f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Miembro desde $memberSince",
                        color = Color.White.copy(0.9f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HeaderStat("Compras", purchasesCount.toString(), Icons.Default.Inventory2)
                    HeaderStat("Productos", productsBought.toString(), Icons.Default.Category)
                    HeaderStat("Gastado", "S/ ${"%.2f".format(totalSpent)}", Icons.Default.ShoppingBag)
                    HeaderStat("Ahorro", "S/ ${"%.2f".format(totalSavings)}", Icons.Default.LocalOffer)
                }
            }
        }
    }
}

@Composable
private fun HeaderStat(
    title: String,
    value: String,
    icon: ImageVector
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(vertical = 10.dp, horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
        Text(
            title,
            color = Color.White.copy(0.85f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}


// ===========================================================
// 📊 MIS ESTADÍSTICAS (ESTILO EXACTO A TU CAPTURA)
// ===========================================================
@Composable
private fun StatsSection(
    purchasesCount: Int,
    productsBought: Int,
    totalSpent: Double,
    totalSavings: Double,
    darkMode: Boolean
) {

    val cardColor = if (darkMode) Color(0xFF1C1C1C) else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {

        Column(Modifier.padding(18.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = Color(0xFF7B61FF))
                Spacer(Modifier.width(8.dp))
                Text("Mis Estadísticas", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(14.dp))

            StatRow("Total de Compras", purchasesCount.toString(), Color(0xFF6EC3FF), Icons.Default.CheckBox)
            StatRow("Productos Comprados", productsBought.toString(), Color(0xFFC788FF), Icons.Default.Inventory2)
            StatRow("Total Gastado", "S/ ${"%.2f".format(totalSpent)}", Color(0xFF6FE47B), Icons.Default.TrendingUp)
            StatRow("Total Ahorrado", "S/ ${"%.2f".format(totalSavings)}", Color(0xFFFFC965), Icons.Default.Sell)
        }
    }
}

@Composable
private fun StatRow(
    title: String,
    value: String,
    iconColor: Color,
    icon: ImageVector
) {

    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor)
            }

            Spacer(Modifier.width(10.dp))

            Text(title)
        }

        Text(value, fontWeight = FontWeight.Bold)
    }
}


// ===========================================================
// 🧾 HISTORIAL (ESTILO EXACTO A TU CAPTURA)
// ===========================================================
@Composable
private fun HistorySection(
    purchases: List<Purchase>,
    darkMode: Boolean
) {

    val cardColor = if (darkMode) Color(0xFF1C1C1C) else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {

        Column(Modifier.padding(18.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = Color(0xFFFF4B3E))
                Spacer(Modifier.width(8.dp))
                Text("Historial de Compras", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(14.dp))

            if (purchases.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay compras aún", color = Color.Gray)
                }
            } else {
                purchases.forEachIndexed { index, purchase ->
                    PurchaseItem(purchase)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun PurchaseItem(purchase: Purchase) {

    val amount = purchase.total
    val items = purchase.items.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9FF))
    ) {

        Column(Modifier.padding(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    Modifier.size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFDCD3FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShoppingBag, null, tint = Color(0xFF7B61FF))
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text("Compra realizada", fontWeight = FontWeight.Bold)
                    Text(formatDateTime(purchase.timestamp), color = Color.Gray)
                }

                Text(
                    "S/ ${"%.2f".format(amount)}",
                    color = Color(0xFF2ECC71),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))

            Text("Completada", color = Color(0xFF2ECC71), fontWeight = FontWeight.SemiBold)
            Text("$items items", color = Color.Gray)
        }
    }
}



// ===========================================================
// 🧷 INFORMACIÓN DE LA CUENTA
// ===========================================================
@Composable
private fun AccountInfoCard(
    accountType: String,
    uid: String,
    darkMode: Boolean
) {

    val cardColor = if (darkMode) Color(0xFF1C1C1C) else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {

        Column(Modifier.padding(18.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E88E5).copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Key, null, tint = Color(0xFF1E88E5))
                }

                Spacer(Modifier.width(10.dp))

                Text("Información de la Cuenta", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            Text("Tipo de cuenta", color = Color.Gray)
            Text(accountType, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            Text("ID de Usuario", color = Color.Gray)
            Text(uid, fontWeight = FontWeight.Bold)
        }
    }
}


// ===========================================================
// 🟢 SMARTCART
// ===========================================================
@Composable
private fun SmartCartInfoCard(darkMode: Boolean) {

    val cardColor = if (darkMode) Color(0xFF1C1C1C) else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {

        Row(
            Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                Modifier.size(14.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2ECC71))
            )

            Spacer(Modifier.width(10.dp))

            Column {
                Text("Sistema SmartCart", fontWeight = FontWeight.Bold)
                Text(
                    "Tu cuenta está sincronizada con el sistema del carrito inteligente.",
                    color = Color.Gray
                )
            }
        }
    }
}


// ===========================================================
// 🔴 BOTÓN DE CERRAR SESIÓN
// ===========================================================
@Composable
private fun LogoutButton(onLogout: () -> Unit) {

    Button(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Brush.horizontalGradient(
                listOf(Color(0xFFFF4B2B), Color(0xFFFF416C))
            ).toColor(),
            contentColor = Color.White
        )
    ) {
        Icon(Icons.Default.Logout, null)
        Spacer(Modifier.width(8.dp))
        Text("Cerrar Sesión")
    }
}

private fun Brush.toColor(): Color = Color.Transparent


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
