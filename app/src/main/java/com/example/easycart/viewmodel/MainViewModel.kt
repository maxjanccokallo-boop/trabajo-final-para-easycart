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
// Scan history
// ===========================================================
data class ScanEntry(
    val barcode: String = "",
    val label: String = "",
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

    val scanHistory: List<ScanEntry> = emptyList(),

    val ledState: LedState = LedState.RED
)

// ===========================================================
// VIEWMODEL
// ===========================================================
class MainViewModel(
    private val repository: EasyCartRepository   // ⬅ ESTE NOMBRE ES EL QUE TE FALTABA
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MainUiState(user = repository.currentUser())
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
        repository.listenProducts().collect { list ->
            _uiState.update { it.copy(products = list) }
        }
    }

    private fun observeOffers() = viewModelScope.launch {
        repository.listenOffers().collect { list ->
            _uiState.update { it.copy(offers = list) }
        }
    }

    private fun observeCart() = viewModelScope.launch {
        val uid = repository.currentUser()?.uid ?: return@launch

        repository.listenCart(uid).collect { items ->

            val total = items.sumOf { it.finalUnitPrice * it.quantity }

            _uiState.update {
                it.copy(cart = items, total = total)
            }
        }
    }

    // ===========================================================
    // LED
    // ===========================================================
    private fun flashLED(color: LedState, duration: Long = 1200) {
        viewModelScope.launch {
            _uiState.update { it.copy(ledState = color) }
            delay(duration)
            _uiState.update { it.copy(ledState = LedState.RED) }
        }
    }

    fun activateGreenLED() = flashLED(LedState.GREEN)
    fun activateYellowLED() = flashLED(LedState.YELLOW, 2500)
    fun activateRedLED() = flashLED(LedState.RED, 50)

    // ===========================================================
    // ESCANEO
    // ===========================================================
    fun onBarcodeScanned(rawBarcode: String) {
        val barcode = rawBarcode.trim()
        if (barcode.isBlank()) return

        viewModelScope.launch {

            val product = repository.findProductByBarcode(barcode)

            if (product != null) {

                addProductToCart(product)
                activateGreenLED()

                _uiState.update {
                    it.copy(
                        lastScanned = product.name,
                        scanError = null,
                        scanHistory = listOf(
                            ScanEntry(
                                barcode = barcode,
                                label = product.name,
                                success = true
                            )
                        ) + it.scanHistory
                    )
                }

                delay(1500)
                _uiState.update { it.copy(lastScanned = null) }

            } else {
                activateRedLED()

                _uiState.update {
                    it.copy(
                        scanError = "Producto no encontrado",
                        lastScanned = null,
                        scanHistory = listOf(
                            ScanEntry(
                                barcode = barcode,
                                label = "Producto no encontrado",
                                success = false
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
        val uid = repository.currentUser()?.uid ?: return@launch
        repository.addOrIncrementCartItem(uid, product)
    }

    fun increaseQuantity(item: CartItem) = viewModelScope.launch {
        val uid = repository.currentUser()?.uid ?: return@launch

        val product =
            uiState.value.products.firstOrNull { it.id == item.productId }
                ?: Product(
                    id = item.productId,
                    name = item.productName,
                    barcode = item.barcode,
                    price = item.unitPrice,
                    stock = item.maxStock,
                    expiresAt = item.expiresAt
                )

        repository.addOrIncrementCartItem(uid, product)
    }

    fun decreaseQuantity(item: CartItem) = viewModelScope.launch {
        val uid = repository.currentUser()?.uid ?: return@launch
        repository.decrementCartItem(uid, item.productId)
    }

    fun clearCart() = viewModelScope.launch {
        val uid = repository.currentUser()?.uid ?: return@launch
        repository.clearCart(uid)
    }

    // ===========================================================
    // REMOVE ITEM (si cantidad =1 lo borra)
    // ===========================================================
    fun removeItem(item: CartItem) {
        val uid = uiState.value.user?.uid ?: return
        viewModelScope.launch {
            repository.decrementCartItem(uid, item.productId)
        }
    }

    // ===========================================================
    // FINALIZAR COMPRA (respeta DESCUENTOS)
    // ===========================================================
    fun finalizePurchase(onResult: (Boolean) -> Unit) {
        val uid = uiState.value.user?.uid ?: return
        val cartNow = uiState.value.cart

        viewModelScope.launch {
            val ok = repository.finalizePurchase(uid, cartNow)
            onResult(ok)
        }
    }

    // ===========================================================
    // HISTORIAL
    // ===========================================================
    fun loadPurchases() {
        val uid = repository.currentUser()?.uid ?: return

        viewModelScope.launch {
            val list = repository.loadPurchases(uid)
            _uiState.update { it.copy(purchases = list) }
        }
    }

    fun clearScanHistory() {
        _uiState.update { it.copy(scanHistory = emptyList()) }
    }
}
