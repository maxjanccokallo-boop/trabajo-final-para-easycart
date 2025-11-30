package com.example.easycart.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.easycart.R
import com.example.easycart.data.model.Product
import com.example.easycart.viewmodel.MainViewModel
import kotlin.math.roundToInt

// -------------------------
// GRADIENTES
// -------------------------
private val PromoGradient = Brush.horizontalGradient(
    listOf(Color(0xFFFF4F9A), Color(0xFFFF7A2F))
)

private val ScreenGradientOffers = Brush.verticalGradient(
    listOf(Color(0xFFF7F6FB), Color(0xFFF1ECFF))
)

// -------------------------
// ENUM FILTRO
// -------------------------
private enum class OfferFilter(val label: String) {
    ALL("Todas"),
    FLASH("Flash"),
    DAY("Del Día")
}

// -------------------------
// RESPONSIVE — CORREGIDO
// -------------------------
@Composable
private fun getColumnCount(): Int {
    val width = LocalConfiguration.current.screenWidthDp
    return when {
        width < 600 -> 1     // celulares
        width < 840 -> 2     // tablets pequeñas
        else -> 3            // pantallas grandes
    }
}

// -------------------------
// PANTALLA COMPLETA
// -------------------------
@Composable
fun OffersScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val productsWithOffers = remember(uiState.products) {
        uiState.products.filter { it.hasOffer && it.offerPrice != null }
    }

    var selectedFilter by remember { mutableStateOf(OfferFilter.ALL) }

    val totalSavings = remember(productsWithOffers) {
        productsWithOffers.sumOf { p ->
            if (p.offerPrice != null && p.offerPrice < p.price)
                p.price - p.offerPrice
            else 0.0
        }
    }

    val filtered = remember(productsWithOffers, selectedFilter) {
        when (selectedFilter) {
            OfferFilter.ALL -> productsWithOffers
            OfferFilter.FLASH -> productsWithOffers.filter {
                val d = ((it.price - (it.offerPrice ?: it.price)) / it.price) * 100
                d >= 30
            }
            OfferFilter.DAY -> productsWithOffers.filter {
                val d = ((it.price - (it.offerPrice ?: it.price)) / it.price) * 100
                d in 10.0..29.9
            }
        }
    }

    val hotProducts = remember(productsWithOffers) {
        productsWithOffers.sortedByDescending {
            (it.price - (it.offerPrice ?: it.price)) / it.price
        }.take(5)
    }

    val columns = getColumnCount()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenGradientOffers)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {

            item { PromoHeaderCard(totalSavings) }

            item { OfferChips(selectedFilter) { selectedFilter = it } }

            item { OfferCounts(filtered.size, hotProducts.size) }

            item {
                if (hotProducts.isNotEmpty()) {
                    SuperHotRow(hotProducts) { p ->
                        p.barcode.let { viewModel.onBarcodeScanned(it) }
                    }
                }
            }

            item {
                if (columns == 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        filtered.forEach { product ->
                            OfferCardPro(
                                product = product,
                                onAddClick = {
                                    product.barcode.let { viewModel.onBarcodeScanned(it) }
                                }
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered) { product ->
                            OfferCardPro(
                                product = product,
                                onAddClick = {
                                    product.barcode.let { viewModel.onBarcodeScanned(it) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------
// HEADER PRO
// -------------------------
@Composable
private fun PromoHeaderCard(totalSavings: Double) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(PromoGradient)
                .padding(20.dp)
        ) {
            Column {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalOffer, null, tint = Color.White)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Ofertas Especiales",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text("¡Ahorra hasta 50% hoy!", color = Color.White)

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Ahorro total",
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            "S/ ${"%.2f".format(totalSavings)}",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Percent, null, tint = Color(0xFFFF4F9A))
                    }
                }
            }
        }
    }
}

// -------------------------
// CHIPS
// -------------------------
@Composable
private fun OfferChips(selected: OfferFilter, onSelect: (OfferFilter) -> Unit) {

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        OfferFilter.values().forEach { filter ->

            val bg by animateColorAsState(
                targetValue = if (selected == filter) Color(0xFF7B61FF) else Color.White,
                animationSpec = tween(250),
                label = ""
            )

            val fg by animateColorAsState(
                targetValue = if (selected == filter) Color.White else Color.DarkGray,
                animationSpec = tween(250),
                label = ""
            )

            Box(
                modifier = Modifier
                    .shadow(if (selected == filter) 6.dp else 2.dp, RoundedCornerShape(16.dp))
                    .background(bg, RoundedCornerShape(16.dp))
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(filter.label, color = fg, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// -------------------------
// CONTADORES
// -------------------------
@Composable
private fun OfferCounts(filteredCount: Int, hotCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$filteredCount ofertas", color = Color.DarkGray)

        Spacer(Modifier.weight(1f))

        if (hotCount > 0) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFFFE6E6), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    "🔥 $hotCount HOT",
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------
// SUPER HOT
// -------------------------
@Composable
private fun SuperHotRow(
    hotList: List<Product>,
    onAddClick: (Product) -> Unit
) {
    Column(Modifier.padding(start = 16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔥 Súper Hot", fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("Ver todas >", color = Color(0xFF7B61FF))
        }

        Spacer(Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(hotList) { p -> HotCardMini(p) { onAddClick(p) } }
        }
    }
}

// -------------------------
// CARD MINI
// -------------------------
@Composable
private fun HotCardMini(product: Product, onAdd: (Product) -> Unit) {
    val base = product.price
    val offer = product.offerPrice ?: base
    val discount = (((base - offer) / base) * 100).roundToInt().coerceAtLeast(0)

    Card(
        modifier = Modifier
            .width(210.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column {

            Box {
                Image(
                    painter =
                        if (product.imageUrl.isNotBlank())
                            rememberAsyncImagePainter(product.imageUrl)
                        else painterResource(R.drawable.offer_placeholder),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentScale = ContentScale.Crop
                )

                if (discount >= 30) {
                    Box(
                        Modifier.padding(8.dp)
                            .background(Color(0xFFFF4D4D), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("🔥 HOT", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier.padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FavoriteBorder, null)
                }
            }

            Column(Modifier.padding(10.dp)) {
                Text(product.name, maxLines = 1, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(
                    "S/ ${"%.2f".format(offer)}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
                Text(
                    "Antes S/ ${"%.2f".format(base)}",
                    color = Color.Gray,
                    textDecoration = TextDecoration.LineThrough
                )

                Spacer(Modifier.height(6.dp))

                Button(
                    onClick = { onAdd(product) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
                ) {
                    Text("Agregar")
                }
            }
        }
    }
}

// -------------------------
// CARD PRINCIPAL
// -------------------------
@Composable
private fun OfferCardPro(
    product: Product,
    onAddClick: () -> Unit
) {
    val base = product.price
    val offer = product.offerPrice ?: base
    val discount = (((base - offer) / base) * 100).roundToInt().coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column {

            // Imagen
            Box {
                Image(
                    painter =
                        if (product.imageUrl.isNotBlank())
                            rememberAsyncImagePainter(product.imageUrl)
                        else painterResource(R.drawable.offer_placeholder),
                    contentDescription = null,
                    modifier = Modifier.height(180.dp).fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )

                if (discount >= 30) {
                    Box(
                        Modifier.padding(10.dp)
                            .background(Color(0xFFFF4D4D), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("🔥 HOT", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                if (discount > 0) {
                    Box(
                        Modifier.padding(10.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color(0xFFFFE6E6), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "-$discount%",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Contenido
            Column(Modifier.padding(16.dp)) {

                Text(product.name, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(6.dp))

                Text(
                    product.description ?: "Oferta especial disponible",
                    color = Color.Gray,
                    maxLines = 2
                )

                Spacer(Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Column(Modifier.weight(1f)) {
                        Text(
                            "Antes S/ ${"%.2f".format(base)}",
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            "Ahora S/ ${"%.2f".format(offer)}",
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    Button(
                        onClick = onAddClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7B61FF),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Agregar")
                    }
                }
            }
        }
    }
}
