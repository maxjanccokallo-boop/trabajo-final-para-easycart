package com.example.easycart.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.easycart.data.model.Product
import com.example.easycart.viewmodel.MainViewModel

// -------------------------------------------
// 🎨 PALETA OFICIAL
// -------------------------------------------
private val LightBg = Brush.verticalGradient(
    listOf(Color(0xFFF7F6FB), Color(0xFFF1ECFF))
)
private val DarkBg = Brush.verticalGradient(
    listOf(Color(0xFF0F172A), Color(0xFF111827))
)

private val DarkCard = Color(0xFF1E293B)
private val LightCard = Color.White

private val TextDark = Color.White
private val TextLight = Color(0xFF111827)

private val SubtitleDark = Color(0xFF94A3B8)
private val SubtitleLight = Color(0xFF6B7280)

private val PurpleAccent = Color(0xFF6366F1)

// -------------------------------------------
private val Categories = listOf("Todos", "Lácteos", "Bebidas", "Snacks", "Frutas", "Aseo")

private enum class ViewMode { GRID, LIST }
private enum class SortMode(val label: String) {
    NAME_ASC("Nombre A-Z"),
    PRICE_ASC("Precio: Menor a Mayor"),
    PRICE_DESC("Precio: Mayor a Menor"),
    STOCK_DESC("Mayor Stock")
}

// -------------------------------------------
// 🌟 PRODUCTS SCREEN
// -------------------------------------------
@Composable
fun ProductsScreen(
    viewModel: MainViewModel,
    darkMode: Boolean      // <-- AÑADIDO
) {
    val uiState by viewModel.uiState.collectAsState()

    var search by remember { mutableStateOf(TextFieldValue("")) }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }
    var sortMode by remember { mutableStateOf(SortMode.NAME_ASC) }
    var sortExpanded by remember { mutableStateOf(false) }

    val query = search.text.lowercase().trim()

    // -------------------------
    // FILTRO
    // -------------------------
    val filtered = remember(uiState.products, query, selectedCategory) {
        uiState.products.filter { p ->

            val matchSearch =
                p.name.lowercase().contains(query) ||
                        (p.barcode?.contains(query) ?: false)

            val matchCat = when (selectedCategory) {
                "Todos" -> true
                "Lácteos" -> p.name.contains("leche", true)
                "Bebidas" -> p.name.contains("agua", true)
                "Snacks" -> p.name.contains("snack", true)
                "Frutas" -> p.name.contains("manzana", true)
                "Aseo" -> p.name.contains("jabon", true)
                else -> true
            }

            matchSearch && matchCat
        }
    }

    // -------------------------
    // ORDENAMIENTO
    // -------------------------
    val sorted = remember(filtered, sortMode) {
        when (sortMode) {
            SortMode.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            SortMode.PRICE_ASC -> filtered.sortedBy { it.price }
            SortMode.PRICE_DESC -> filtered.sortedByDescending { it.price }
            SortMode.STOCK_DESC -> filtered.sortedByDescending { it.stock }
        }
    }

    // -------------------------
    // UI GENERAL
    // -------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (darkMode) DarkBg else LightBg)
    ) {

        Column {

            ProductsHeader(count = sorted.size, darkMode = darkMode)

            Spacer(Modifier.height(10.dp))

            SearchBar(search, { search = it }, darkMode)

            Spacer(Modifier.height(10.dp))

            // TOOLBAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconToggleChip(
                    selected = viewMode == ViewMode.GRID,
                    icon = Icons.Default.GridView,
                    onClick = { viewMode = ViewMode.GRID },
                    darkMode = darkMode
                )

                Spacer(Modifier.width(8.dp))

                IconToggleChip(
                    selected = viewMode == ViewMode.LIST,
                    icon = Icons.Default.List,
                    onClick = { viewMode = ViewMode.LIST },
                    darkMode = darkMode
                )

                Spacer(Modifier.width(10.dp))

                // SORT CHIP
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { sortExpanded = true },
                        label = {
                            Text(
                                sortMode.label,
                                color = if (darkMode) TextDark else TextLight
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Sort, null, tint = PurpleAccent)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (darkMode) Color(0xFF0F172A) else Color(0xFFEFF0FF),
                            selectedContainerColor = if (darkMode) Color(0xFF0F172A) else Color(0xFFEFF0FF)
                        )
                    )

                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false },
                        modifier = Modifier.background(
                            if (darkMode) Color(0xFF1F2937) else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                    ) {
                        SortMode.values().forEach {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        it.label,
                                        color = if (darkMode) TextDark else TextLight
                                    )
                                },
                                onClick = {
                                    sortMode = it
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            CategoryRow(selectedCategory, { selectedCategory = it }, darkMode)

            Spacer(Modifier.height(12.dp))

            // LISTA O GRID
            if (viewMode == ViewMode.LIST) {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sorted) { p ->
                        ProductListCard(p, viewModel, darkMode)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(170.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sorted) { p ->
                        ProductGridCard(p, viewModel, darkMode)
                    }
                }
            }
        }
    }
}

// -------------------------------------------
// HEADER
// -------------------------------------------
@Composable
private fun ProductsHeader(count: Int, darkMode: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (darkMode)
                    Brush.verticalGradient(listOf(Color(0xFF1E2A40), Color(0xFF202A44)))
                else
                    Brush.verticalGradient(listOf(Color(0xFFDDE3FF), Color(0xFFE9EBFF)))
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Text(
                "Productos",
                color = if (darkMode) TextDark else TextLight,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "$count disponibles",
                color = if (darkMode) Color(0xFFCBD5E1) else SubtitleLight
            )
        }
    }
}

// -------------------------------------------
// BUSCADOR
// -------------------------------------------
@Composable
private fun SearchBar(
    value: TextFieldValue,
    onChange: (TextFieldValue) -> Unit,
    darkMode: Boolean
) {
    TextField(
        value = value,
        onValueChange = onChange,
        leadingIcon = {
            Icon(Icons.Default.Search, null, tint = PurpleAccent)
        },
        placeholder = {
            Text(
                "Buscar productos...",
                color = if (darkMode) SubtitleDark else SubtitleLight
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .height(54.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = if (darkMode) DarkCard else Color(0xFFEFF0FF),
            unfocusedContainerColor = if (darkMode) DarkCard else Color(0xFFEFF0FF),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = PurpleAccent
        )
    )
}

// -------------------------------------------
// CATEGORÍAS
// -------------------------------------------
@Composable
private fun CategoryRow(selected: String, onSelect: (String) -> Unit, darkMode: Boolean) {

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Categories.forEach { cat ->

            val isSelected = selected == cat

            val bg by animateColorAsState(
                if (isSelected) PurpleAccent
                else if (darkMode) Color(0xFF1F2937)
                else Color(0xFFE8E9FF),
                label = "categoryBg"
            )

            val labelColor = if (darkMode) TextDark else TextLight

            Box(
                modifier = Modifier
                    .background(bg, RoundedCornerShape(14.dp))
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(cat, color = if (isSelected) Color.White else labelColor)
            }
        }
    }
}

// -------------------------------------------
// ICON CHIP
// -------------------------------------------
@Composable
private fun IconToggleChip(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    darkMode: Boolean
) {
    val bg by animateColorAsState(
        if (selected)
            if (darkMode) Color(0xFF2A3650) else Color(0xFFC6CBFF)
        else
            if (darkMode) Color(0xFF1E293B) else Color(0xFFE8EAFF),
        label = "iconChip"
    )

    val tint = if (selected) PurpleAccent else if (darkMode) SubtitleDark else SubtitleLight

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(bg, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint)
    }
}

// -------------------------------------------
// CARD LISTA
// -------------------------------------------
@Composable
private fun ProductListCard(p: Product, viewModel: MainViewModel, darkMode: Boolean) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkMode) DarkCard else LightCard
        )
    ) {

        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            ProductImagePlaceholder(
                name = p.name,
                height = 64.dp,
                modifier = Modifier.width(72.dp),
                darkMode = darkMode
            )

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {

                Text(
                    p.name,
                    color = if (darkMode) TextDark else TextLight,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    p.barcode ?: "Sin código",
                    color = if (darkMode) SubtitleDark else SubtitleLight
                )

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StockBadge(p.stock, darkMode)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "S/ ${p.price}",
                        color = PurpleAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            FilledIconButton(
                onClick = { p.barcode?.let { viewModel.onBarcodeScanned(it) } },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PurpleAccent,
                    contentColor = Color.White
                ),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.AddShoppingCart, null)
            }
        }
    }
}

// -------------------------------------------
// CARD GRID
// -------------------------------------------
@Composable
private fun ProductGridCard(p: Product, viewModel: MainViewModel, darkMode: Boolean) {

    Card(
        modifier = Modifier.shadow(10.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (darkMode) DarkCard else LightCard
        )
    ) {
        Column(Modifier.padding(12.dp)) {

            ProductImagePlaceholder(
                name = p.name,
                height = 110.dp,
                modifier = Modifier.fillMaxWidth(),
                darkMode = darkMode
            )

            Spacer(Modifier.height(8.dp))

            Text(
                p.name,
                color = if (darkMode) TextDark else TextLight,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                p.barcode ?: "Sin código",
                color = if (darkMode) SubtitleDark else SubtitleLight,
                maxLines = 1
            )

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                StockBadge(p.stock, darkMode)
                Spacer(Modifier.weight(1f))
                Text("S/ ${p.price}", color = PurpleAccent, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { p.barcode?.let { viewModel.onBarcodeScanned(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
            ) {
                Icon(Icons.Default.AddShoppingCart, null)
                Spacer(Modifier.width(6.dp))
                Text("Añadir", color = Color.White)
            }
        }
    }
}

// -------------------------------------------
// IMAGE PLACEHOLDER
// -------------------------------------------
@Composable
private fun ProductImagePlaceholder(
    name: String,
    height: Dp,
    modifier: Modifier = Modifier,
    darkMode: Boolean
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(if (darkMode) Color(0xFF0F172A) else Color(0xFFEFF0FF)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            color = PurpleAccent,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.alpha(0.8f)
        )
    }
}

// -------------------------------------------
// STOCK BADGE
// -------------------------------------------
@Composable
private fun StockBadge(stock: Int, darkMode: Boolean) {

    val bg = when {
        stock <= 5 -> if (darkMode) Color(0xFF450A0A) else Color(0xFFFFE6E6)
        stock <= 20 -> if (darkMode) Color(0xFF422006) else Color(0xFFFFF8E1)
        else -> if (darkMode) Color(0xFF052E16) else Color(0xFFE9FFF2)
    }

    val fg = when {
        stock <= 5 -> Color(0xFFFCA5A5)
        stock <= 20 -> Color(0xFFFCD34D)
        else -> Color(0xFF16A34A)
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text("Stock $stock", color = fg)
    }
}
