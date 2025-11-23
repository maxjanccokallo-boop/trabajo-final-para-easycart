package com.example.easycart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycart.data.EasyCartRepository
import com.example.easycart.data.model.CartItem
import com.example.easycart.data.model.Offer
import com.example.easycart.data.model.Product
import com.example.easycart.data.model.Purchase
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// ===========================================================
// LED STATE
// ===========================================================
enum class LedState { RED, YELLOW, GREEN }

// ===========================================================
// ✅ Scan history model (NUEVO)
// ===========================================================
data class ScanEntry(
    val barcode: String = "",
    val label: String = "",          // nombre producto o mensaje
    val success: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

// ===========================================================
// UI STATE
// ===========================================================
data class MainUiState(
    val user: FirebaseUser? = null,
    val products: List<Product> = emptyList(),
    val cart: List<CartItem> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val purchases: List<Purchase> = emptyList(),

    val total: Double = 0.0,

    val scanError: String? = null,
    val lastScanned: String? = null,

    // ✅ HISTORIAL REAL DE ESCANEOS (NUEVO)
    val scanHistory: List<ScanEntry> = emptyList(),

    val ledState: LedState = LedState.RED
)

// ===========================================================
// VIEWMODEL
// ===========================================================
class MainViewModel(
    private val repo: EasyCartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUiState(user = repo.currentUser())
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeProducts()
        observeOffers()
        observeCart()
        loadPurchases()
    }

    // ===========================================================
    // OBSERVERS FIRESTORE
    // ===========================================================
    private fun observeProducts() = viewModelScope.launch {
        repo.listenProducts().collect { list ->
            _uiState.update { it.copy(products = list) }
        }
    }

    private fun observeOffers() = viewModelScope.launch {
        repo.listenOffers().collect { list ->
            _uiState.update { it.copy(offers = list) }
        }
    }

    private fun observeCart() = viewModelScope.launch {
        val uid = repo.currentUser()?.uid ?: return@launch

        repo.listenCart(uid).collect { items ->
            val total = items.sumOf { it.unitPrice * it.quantity }
            _uiState.update { it.copy(cart = items, total = total) }
        }
    }

    // ===========================================================
    // LED LOGIC (optimizada)
    // ===========================================================
    private fun flashLED(color: LedState, duration: Long = 1000) {
        viewModelScope.launch {
            _uiState.update { it.copy(ledState = color) }
            delay(duration)
            _uiState.update { it.copy(ledState = LedState.RED) }
        }
    }

    fun activateGreenLED() = flashLED(LedState.GREEN, 1000)
    fun activateRedLED() = flashLED(LedState.RED, 1)   // RED permanente
    fun activateYellowLED() = flashLED(LedState.YELLOW, 3000)

    // ===========================================================
    // ESCANEO
    // ===========================================================
    fun onBarcodeScanned(rawBarcode: String) {
        val barcode = rawBarcode.trim()
        if (barcode.isBlank()) return

        viewModelScope.launch {
            val product = repo.findProductByBarcode(barcode)

            if (product != null) {
                addProductToCart(product)
                activateGreenLED()

                // ✅ Guardar historial OK
                _uiState.update {
                    it.copy(
                        lastScanned = product.name,
                        scanError = null,
                        scanHistory = listOf(
                            ScanEntry(
                                barcode = barcode,
                                label = product.name,
                                success = true,
                                timestamp = System.currentTimeMillis()
                            )
                        ) + it.scanHistory
                    )
                }

                delay(1800)
                _uiState.update { it.copy(lastScanned = null) }

            } else {
                activateRedLED()

                // ✅ Guardar historial FAIL
                _uiState.update {
                    it.copy(
                        scanError = "Producto no encontrado",
                        lastScanned = null,
                        scanHistory = listOf(
                            ScanEntry(
                                barcode = barcode,
                                label = "Producto no encontrado",
                                success = false,
                                timestamp = System.currentTimeMillis()
                            )
                        ) + it.scanHistory
                    )
                }
            }
        }
    }

    // ===========================================================
    // CARRITO
    // ===========================================================
    private fun addProductToCart(product: Product) = viewModelScope.launch {
        val uid = repo.currentUser()?.uid ?: return@launch
        repo.addOrIncrementCartItem(uid, product)
    }

    fun increaseQuantity(item: CartItem) = viewModelScope.launch {
        val uid = repo.currentUser()?.uid ?: return@launch

        val productData =
            _uiState.value.products.firstOrNull { it.id == item.productId }
                ?: Product(
                    id = item.productId,
                    name = item.productName,
                    barcode = item.barcode,
                    price = item.unitPrice,
                    stock = item.maxStock,
                    expiresAt = item.expiresAt
                )

        repo.addOrIncrementCartItem(uid, productData)
    }

    fun decreaseQuantity(item: CartItem) = viewModelScope.launch {
        val uid = repo.currentUser()?.uid ?: return@launch
        repo.decrementCartItem(uid, item.productId)
    }

    fun clearCart() = viewModelScope.launch {
        repo.clearCart(uid = repo.currentUser()?.uid ?: return@launch)
    }

    // ===========================================================
    // FINALIZAR COMPRA
    // ===========================================================
    fun finalizePurchase() {
        viewModelScope.launch {
            val user = repo.currentUser() ?: return@launch
            val cart = _uiState.value.cart

            val success = repo.finalizePurchase(user.uid, cart)

            if (success) {
                activateGreenLED()
                loadPurchases()
            } else {
                activateRedLED()
                _uiState.update {
                    it.copy(scanError = "Error al finalizar la compra.")
                }
            }
        }
    }

    // ===========================================================
    // HISTORIAL COMPRAS
    // ===========================================================
    fun loadPurchases() {
        val uid = repo.currentUser()?.uid ?: return

        viewModelScope.launch {
            val list = repo.loadPurchases(uid)
            _uiState.update { it.copy(purchases = list) }
        }
    }

    // ✅ Opcional: limpiar historial desde UI
    fun clearScanHistory() {
        _uiState.update { it.copy(scanHistory = emptyList()) }
    }
}
