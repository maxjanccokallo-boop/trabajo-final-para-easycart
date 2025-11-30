package com.example.easycart.ui.screens.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.easycart.data.model.Product
import com.example.easycart.viewmodel.MainViewModel
import kotlin.math.roundToInt


// -------------------------
// Estilos globales
// -------------------------
private val HeaderGradient = Brush.horizontalGradient(
    listOf(Color(0xFF7B61FF), Color(0xFFE740B5))
)

private val ScreenGradient = Brush.verticalGradient(
    listOf(Color(0xFFF7F6FB), Color(0xFFF1ECFF))
)

private val Categories = listOf("Todos", "Lácteos", "Bebidas", "Snacks", "Frutas", "Aseo")

private enum class ViewMode { GRID, LIST }

private enum class SortMode(val label: String) {
    NAME_ASC("Nombre A-Z"),
    PRICE_ASC("Precio: Menor a Mayor"),
    PRICE_DESC("Precio: Mayor a Menor"),
    STOCK_DESC("Mayor Stock")
}

@Composable
fun ProductsScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var search by remember { mutableStateOf(TextFieldValue("")) }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var sortMode by remember { mutableStateOf(SortMode.NAME_ASC) }
    var sortExpanded by remember { mutableStateOf(false) }

    val query = search.text.lowercase().trim()

    // -------------------------
    // Filtrado por texto + categoría
    // -------------------------
    val filtered = remember(uiState.products, query, selectedCategory) {
        uiState.products.filter { p ->
            val matchesSearch =
                p.name.lowercase().contains(query) ||
                        (p.barcode?.contains(query) ?: false)

            val matchesCategory = when (selectedCategory) {
                "Todos" -> true
                "Lácteos" -> p.name.contains("leche", true) ||
                        p.name.contains("yogurt", true) ||
                        p.name.contains("queso", true)

                "Bebidas" -> p.name.contains("agua", true) ||
                        p.name.contains("bebida", true) ||
                        p.name.contains("gaseosa", true)

                "Snacks" -> p.name.contains("snack", true) ||
                        p.name.contains("papas", true) ||
                        p.name.contains("galleta", true)

                "Frutas" -> p.name.contains("manzana", true) ||
                        p.name.contains("plátano", true) ||
                        p.name.contains("banana", true)

                "Aseo" -> p.name.contains("jabon", true) ||
                        p.name.contains("detergente", true) ||
                        p.name.contains("shampoo", true)

                else -> true
            }

            matchesSearch && matchesCategory
        }
    }

    // -------------------------
    // Ordenamiento
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
    // Métricas pro
    // -------------------------
    val stockTotal = sorted.sumOf { it.stock }
    val valueTotal = sorted.sumOf { it.stock * it.price }
    val avgPrice = if (sorted.isNotEmpty()) valueTotal / stockTotal.coerceAtLeast(1) else 0.0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenGradient)
    ) {
        Column {

            // HEADER degradado
            ProductsHeader(count = sorted.size)

            // Métricas (3 tarjetas)
            MetricsRow(
                stockTotal = stockTotal,
                valueTotal = valueTotal,
                avgPrice = avgPrice
            )

            Spacer(Modifier.height(10.dp))

            // Buscador
            SearchBar(
                search = search,
                onSearchChange = { search = it }
            )

            Spacer(Modifier.height(10.dp))

            // Toolbar: view toggle + sorter + btn action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Toggle grid/list
                IconToggleChip(
                    selected = viewMode == ViewMode.GRID,
                    icon = Icons.Default.GridView,
                    onClick = { viewMode = ViewMode.GRID }
                )
                Spacer(Modifier.width(8.dp))
                IconToggleChip(
                    selected = viewMode == ViewMode.LIST,
                    icon = Icons.Default.List,
                    onClick = { viewMode = ViewMode.LIST }
                )

                Spacer(Modifier.width(10.dp))

                // Sort dropdown
                Box {
                    FilterChip(
                        selected = true,
                        onClick = { sortExpanded = true },
                        label = { Text(sortMode.label) },
                        leadingIcon = {
                            Icon(Icons.Default.Sort, contentDescription = null)
                        }
                    )

                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false }
                    ) {
                        SortMode.values().forEach {
                            DropdownMenuItem(
                                text = { Text(it.label) },
                                onClick = {
                                    sortMode = it
                                    sortExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Botón acción (tipo descargar)
                FloatingActionButton(
                    onClick = { /* luego puedes poner exportar o refresh */ },
                    containerColor = Color(0xFF00C853),
                    contentColor = Color.White,
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape
                ) {
                    Text("↓", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Categorías horizontal
            CategoryRow(
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )

            Spacer(Modifier.height(12.dp))

            // Lista o Grid responsive
            if (viewMode == ViewMode.LIST) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sorted) { p ->
                        ProductListCard(p, viewModel)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 170.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sorted) { p ->
                        ProductGridCard(p, viewModel)
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// HEADER “Productos”
// ------------------------------------------------------------------
@Composable
private fun ProductsHeader(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderGradient)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Productos",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$count disponibles",
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallRoundButton("≡")
                SmallRoundButton("📊")
            }
        }
    }
}

@Composable
private fun SmallRoundButton(txt: String) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(Color.White.copy(alpha = 0.18f), CircleShape)
            .clickable { }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(txt, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

// ------------------------------------------------------------------
// MÉTRICAS
// ------------------------------------------------------------------
@Composable
private fun MetricsRow(stockTotal: Int, valueTotal: Double, avgPrice: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricCard(
            title = "Stock Total",
            value = stockTotal.toString(),
            color = Color(0xFFEAF2FF)
        )
        MetricCard(
            title = "Valor Total",
            value = "S/${valueTotal.roundToInt()}",
            color = Color(0xFFE8FFF0)
        )
        MetricCard(
            title = "Precio Prom.",
            value = "S/${"%.1f".format(avgPrice)}",
            color = Color(0xFFF4ECFF)
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier // <-- MODIFICACIÓN CLAVE: Acepta un Modifier externo
) {
    Card(
        // Aplica el Modifier que se le pase (incluyendo el weight).
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

// ------------------------------------------------------------------
// BUSCADOR PRO
// ------------------------------------------------------------------
@Composable
private fun SearchBar(search: TextFieldValue, onSearchChange: (TextFieldValue) -> Unit) {
    TextField(
        value = search,
        onValueChange = onSearchChange,
        placeholder = { Text("Buscar por nombre o código...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(54.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        )
    )
}

// ------------------------------------------------------------------
// CHIPS categorías
// ------------------------------------------------------------------
@Composable
private fun CategoryRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Categories.forEach { cat ->
            val isSelected = selected == cat
            val bg by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF7B61FF) else Color(0xFFF1F1F1),
                animationSpec = tween(220),
                label = "catbg"
            )
            val fg by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color(0xFF333333),
                animationSpec = tween(220),
                label = "catfg"
            )

            Box(
                modifier = Modifier
                    .background(bg, RoundedCornerShape(16.dp))
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(cat, color = fg, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ------------------------------------------------------------------
// Toggle chip iconos
// ------------------------------------------------------------------
@Composable
private fun IconToggleChip(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (selected) Color(0xFFEDE7FF) else Color(0xFFF4F4F4),
        label = "togglebg"
    )
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(bg, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) Color(0xFF7B61FF) else Color.Gray
        )
    }
}

// ------------------------------------------------------------------
// CARD LISTA
// ------------------------------------------------------------------
@Composable
private fun ProductListCard(p: Product, viewModel: MainViewModel) {
    val pressScale by animateFloatAsState(1f, label = "press")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .scale(pressScale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImagePlaceholder(name = p.name)

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(p.name, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(
                    p.barcode ?: "Sin código",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StockBadge(p.stock)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "S/ ${p.price}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A64F0)
                    )
                }
            }

            FilledIconButton(
                onClick = { p.barcode?.let { viewModel.onBarcodeScanned(it) } },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF7B61FF),
                    contentColor = Color.White
                ),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null)
            }
        }
    }
}

// ------------------------------------------------------------------
// CARD GRID
// ------------------------------------------------------------------
@Composable
private fun ProductGridCard(p: Product, viewModel: MainViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(7.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(10.dp)) {
            ProductImagePlaceholder(name = p.name, height = 110.dp)

            Spacer(Modifier.height(8.dp))

            Text(p.name, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(
                p.barcode ?: "Sin código",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1
            )

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                StockBadge(p.stock)
                Spacer(Modifier.weight(1f))
                Text(
                    "S/ ${p.price}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A64F0)
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { p.barcode?.let { viewModel.onBarcodeScanned(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Añadir")
            }
        }
    }
}

// ------------------------------------------------------------------
// Placeholder imagen sin romper tu modelo
// ------------------------------------------------------------------
@Composable
private fun ProductImagePlaceholder(name: String, height: Dp = 64.dp) {
    val colors = listOf(Color(0xFFEEE7FF), Color(0xFFE7F7FF), Color(0xFFFFF0E7))
    val bg = Brush.linearGradient(colors)

    Box(
        modifier = Modifier
            .width(height)
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.toString() ?: "P",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5E35B1),
            modifier = Modifier.alpha(0.8f)
        )
    }
}

@Composable
private fun StockBadge(stock: Int) {
    val (bg, fg) = when {
        stock <= 5 -> Color(0xFFFFE6E6) to Color(0xFFD32F2F)
        stock <= 20 -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
        else -> Color(0xFFE8FFF0) to Color(0xFF2E7D32)
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text("Stock $stock", color = fg, style = MaterialTheme.typography.labelSmall)
    }
}
