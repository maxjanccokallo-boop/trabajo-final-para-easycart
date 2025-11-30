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
// GRADIENTES LIGHT / DARK
// -------------------------
private val PromoGradient = Brush.horizontalGradient(
    listOf(Color(0xFFFF4F9A), Color(0xFFFF7A2F))
)

private val OffersBgLight = Brush.verticalGradient(
    listOf(Color(0xFFF7F6FB), Color(0xFFF1ECFF))
)

private val OffersBgDark = Brush.verticalGradient(
    listOf(Color(0xFF020617), Color(0xFF020617))
)

private val CardLight = Color.White
private val CardDark = Color(0xFF0F172A)
private val TextPrimaryDark = Color.White
private val TextSecondaryDark = Color(0xFF9CA3AF)


// -------------------------
// ENUM FILTRO
// -------------------------
private enum class OfferFilter(val label: String) {
    ALL("Todas"),
    FLASH("Flash"),
    DAY("Del Día")
}


// -------------------------
// RESPONSIVE
// -------------------------
@Composable
private fun getColumnCount(): Int {
    val width = LocalConfiguration.current.screenWidthDp
    return when {
        width < 600 -> 1
        width < 840 -> 2
        else -> 3
    }
}


// -------------------------
// PANTALLA COMPLETA
// -------------------------
@Composable
fun OffersScreen(
    viewModel: MainViewModel,
    darkMode: Boolean
) {

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

    val bg = if (darkMode) OffersBgDark else OffersBgLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {

            item { PromoHeaderCard(totalSavings) }

            item { OfferChips(selectedFilter, darkMode) { selectedFilter = it } }

            item { OfferCounts(filtered.size, hotProducts.size, darkMode) }

            item {
                if (hotProducts.isNotEmpty()) {
                    SuperHotRow(
                        hotList = hotProducts,
                        darkMode = darkMode
                    ) { p ->
                        p.barcode.let { viewModel.onBarcodeScanned(it) }
                    }
                }
            }

            item {
                if (columns == 1) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        filtered.forEach { product ->
                            OfferCardPro(
                                product = product,
                                darkMode = darkMode,
                                onAddClick = {
                                    product.barcode.let { viewModel.onBarcodeScanned(it) }
                                }
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filtered) { product ->
                            OfferCardPro(
                                product = product,
                                darkMode = darkMode,
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .shadow(
                22.dp,
                RoundedCornerShape(26.dp),
                ambientColor = Color.Black.copy(alpha = 0.20f),
                spotColor = Color.Black.copy(alpha = 0.20f)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(PromoGradient)
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

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

                Text(
                    "¡Ahorra hasta 50% hoy!",
                    color = Color.White.copy(alpha = 0.9f)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.18f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(Modifier.weight(1f)) {
                            Text(
                                "Ahorro total",
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                "S/ ${"%.2f".format(totalSavings)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }

                        Box(
                            modifier = Modifier
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
}


// -------------------------
// CHIPS
// -------------------------
@Composable
private fun OfferChips(
    selected: OfferFilter,
    darkMode: Boolean,
    onSelect: (OfferFilter) -> Unit
) {

    val cardBg = if (darkMode) CardDark else Color.White
    val textNormal = if (darkMode) TextSecondaryDark else Color.DarkGray
    val selectedBg = Color(0xFF7B61FF)
    val selectedFg = Color.White

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        OfferFilter.values().forEach { filter ->

            val isSelected = selected == filter

            val bg by animateColorAsState(
                targetValue = if (isSelected) selectedBg else cardBg,
                animationSpec = tween(200),
                label = ""
            )

            val fg by animateColorAsState(
                targetValue = if (isSelected) selectedFg else textNormal,
                animationSpec = tween(200),
                label = ""
            )

            Box(
                modifier = Modifier
                    .shadow(
                        if (isSelected) 10.dp else 4.dp,
                        RoundedCornerShape(18.dp)
                    )
                    .background(bg, RoundedCornerShape(18.dp))
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = filter.label,
                    color = fg,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


// -------------------------
// CONTADORES
// -------------------------
@Composable
private fun OfferCounts(
    filteredCount: Int,
    hotCount: Int,
    darkMode: Boolean
) {
    val textColor = if (darkMode) TextSecondaryDark else Color.DarkGray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$filteredCount ofertas", color = textColor)

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
    darkMode: Boolean,
    onAddClick: (Product) -> Unit
) {

    val textPrimary = if (darkMode) TextPrimaryDark else Color.Black

    Column(Modifier.padding(start = 16.dp)) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "🔥 Súper Hot",
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )

            Spacer(Modifier.weight(1f))

            Text(
                "Ver todas >",
                color = Color(0xFF7B61FF),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(hotList) { p ->
                HotCardMini(
                    product = p,
                    darkMode = darkMode,
                    onAdd = { onAddClick(p) }
                )
            }
        }
    }
}


// -------------------------
// CARD MINI
// -------------------------
@Composable
private fun HotCardMini(
    product: Product,
    darkMode: Boolean,
    onAdd: (Product) -> Unit
) {
    val base = product.price
    val offer = product.offerPrice ?: base
    val discount = (((base - offer) / base) * 100).roundToInt()

    val cardBg = if (darkMode) CardDark else CardLight
    val textPrimary = if (darkMode) TextPrimaryDark else Color.Black
    val textSecondary = if (darkMode) TextSecondaryDark else Color.Gray

    Card(
        modifier = Modifier
            .width(220.dp)
            .shadow(
                16.dp,
                RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.20f),
                spotColor = Color.Black.copy(alpha = 0.20f)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {

        Column {

            Box {
                Image(
                    painter =
                        if (product.imageUrl.isNotBlank())
                            rememberAsyncImagePainter(product.imageUrl)
                        else painterResource(R.drawable.offer_placeholder),
                    null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
                    contentScale = ContentScale.Crop
                )

                if (discount >= 30) {
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .background(Color(0xFFFF4D4D), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "🔥 HOT",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(Modifier.padding(14.dp)) {

                Text(
                    product.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = textPrimary
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "S/ ${"%.2f".format(offer)}",
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Antes S/ ${"%.2f".format(base)}",
                    textDecoration = TextDecoration.LineThrough,
                    color = textSecondary
                )

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = { onAdd(product) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B61FF)
                    )
                ) {
                    Text("Agregar", color = Color.White)
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
    darkMode: Boolean,
    onAddClick: () -> Unit
) {

    val base = product.price
    val offer = product.offerPrice ?: base
    val discount = (((base - offer) / base) * 100).roundToInt()

    val cardBg = if (darkMode) CardDark else CardLight
    val textPrimary = if (darkMode) TextPrimaryDark else Color.Black
    val textSecondary = if (darkMode) TextSecondaryDark else Color.Gray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                16.dp,
                RoundedCornerShape(22.dp),
                ambientColor = Color.Black.copy(alpha = 0.22f),
                spotColor = Color.Black.copy(alpha = 0.22f)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {

        Column {

            Box {
                Image(
                    painter =
                        if (product.imageUrl.isNotBlank())
                            rememberAsyncImagePainter(product.imageUrl)
                        else painterResource(R.drawable.offer_placeholder),
                    null,
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
                    contentScale = ContentScale.Crop
                )

                if (discount >= 30) {
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .background(Color(0xFFFF4D4D), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "🔥 HOT",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (discount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color(0xFFFFE6E6), RoundedCornerShape(14.dp))
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

            Column(Modifier.padding(18.dp)) {

                Text(
                    product.name,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    product.description ?: "Oferta especial disponible",
                    color = textSecondary,
                    maxLines = 2
                )

                Spacer(Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Column(Modifier.weight(1f)) {
                        Text(
                            "Antes S/ ${"%.2f".format(base)}",
                            color = textSecondary,
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
                        shape = RoundedCornerShape(16.dp),
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
