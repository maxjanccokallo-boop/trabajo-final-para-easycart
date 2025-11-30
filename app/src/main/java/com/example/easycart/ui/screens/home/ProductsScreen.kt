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
// 🎨 PALETA OSCURA
// -------------------------------------------
private val DarkBg = Brush.verticalGradient(
    listOf(Color(0xFF0F172A), Color(0xFF111827))
)

private val HeaderGradient = Brush.verticalGradient(
    listOf(Color(0xFF1E2A40), Color(0xFF202A44))
)

private val PurpleAccent = Color(0xFF6366F1)
private val NavyCard = Color(0xFF1E293B)

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
// 🌟 PANTALLA PRINCIPAL
// -------------------------------------------
@Composable
fun ProductsScreen(viewModel: MainViewModel) {

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
            .background(DarkBg)
    ) {

        Column {

            ProductsHeader(sorted.size)

            Spacer(Modifier.height(10.dp))

            SearchBar(search) { search = it }

            Spacer(Modifier.height(10.dp))

            // ---------------------------------------
            // 🔘 TOOLBAR GRID / LIST + SORT
            // ---------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconToggleChip(
                    selected = viewMode == ViewMode.GRID,
                    icon = Icons.Default.GridView
                ) { viewMode = ViewMode.GRID }

                Spacer(Modifier.width(8.dp))

                IconToggleChip(
                    selected = viewMode == ViewMode.LIST,
                    icon = Icons.Default.List
                ) { viewMode = ViewMode.LIST }

                Spacer(Modifier.width(10.dp))

                // ---- CHIP DE ORDENAMIENTO (NEGRO)
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { sortExpanded = true },
                        label = { Text(sortMode.label, color = Color.White) },
                        leadingIcon = {
                            Icon(Icons.Default.Sort, null, tint = PurpleAccent)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF0F172A),          // Negro
                            selectedContainerColor = Color(0xFF0F172A),  // Negro seleccionado
                            labelColor = Color.White,
                            selectedLabelColor = Color.White
                        )
                    )


                    // ---- MENU DESPLEGABLE (NEGRO)
                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false },
                        modifier = Modifier
                            .background(Color(0xFF1F2937), RoundedCornerShape(12.dp))
                    ) {
                        SortMode.values().forEach {
                            DropdownMenuItem(
                                text = { Text(it.label, color = Color.White) },
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

            CategoryRow(selectedCategory) { selectedCategory = it }

            Spacer(Modifier.height(12.dp))

            // ---------------------------------------
            // LISTA O GRID
            // ---------------------------------------
            if (viewMode == ViewMode.LIST) {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sorted) { p ->
                        ProductListCard(p, viewModel)
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
                        ProductGridCard(p, viewModel)
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
private fun ProductsHeader(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderGradient)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Text(
                "Productos",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "$count disponibles",
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

// -------------------------------------------
// BUSCADOR
// -------------------------------------------
@Composable
private fun SearchBar(value: TextFieldValue, onChange: (TextFieldValue) -> Unit) {
    TextField(
        value = value,
        onValueChange = onChange,
        leadingIcon = {
            Icon(Icons.Default.Search, null, tint = PurpleAccent)
        },
        placeholder = { Text("Buscar productos...", color = Color(0xFF9CA3AF)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .height(54.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1F2937),
            unfocusedContainerColor = Color(0xFF1F2937),
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
private fun CategoryRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Categories.forEach { cat ->

            val isSelected = selected == cat
            val bg by animateColorAsState(
                if (isSelected) PurpleAccent else Color(0xFF1F2937),
                label = "categoryBg"
            )

            Box(
                modifier = Modifier
                    .background(bg, RoundedCornerShape(14.dp))
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(cat, color = Color.White)
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
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        if (selected) Color(0xFF2A3650) else Color(0xFF1E293B),
        label = "iconChip"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(bg, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            null,
            tint = if (selected) PurpleAccent else Color(0xFF94A3B8)
        )
    }
}

// -------------------------------------------
// CARD LISTA
// -------------------------------------------
@Composable
private fun ProductListCard(p: Product, viewModel: MainViewModel) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard)
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
                modifier = Modifier.width(72.dp)
            )

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(p.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text(p.barcode ?: "Sin código", color = Color(0xFF94A3B8))

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StockBadge(p.stock)
                    Spacer(Modifier.weight(1f))
                    Text("S/ ${p.price}", color = PurpleAccent, fontWeight = FontWeight.Bold)
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
private fun ProductGridCard(p: Product, viewModel: MainViewModel) {

    Card(
        modifier = Modifier.shadow(10.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard)
    ) {
        Column(Modifier.padding(12.dp)) {

            ProductImagePlaceholder(
                name = p.name,
                height = 110.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Text(p.name, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)

            Text(
                p.barcode ?: "Sin código",
                color = Color(0xFF94A3B8),
                maxLines = 1
            )

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                StockBadge(p.stock)
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B)),
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
@Composable
private fun StockBadge(stock: Int) {
    val bg = when {
        stock <= 5 -> Color(0xFF450A0A)
        stock <= 20 -> Color(0xFF422006)
        else -> Color(0xFF052E16)
    }

    val fg = when {
        stock <= 5 -> Color(0xFFFCA5A5)
        stock <= 20 -> Color(0xFFFCD34D)
        else -> Color(0xFF6EE7B7)
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text("Stock $stock", color = fg)
    }
}
