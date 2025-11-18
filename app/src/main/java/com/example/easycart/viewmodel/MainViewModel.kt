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
enum class LedState {
    RED,
    YELLOW,
    GREEN
}

// ===========================================================
// UI STATE
// ===========================================================
data class MainUiState(
    val user: FirebaseUser? = null,
    val products: List<Product> = emptyList(),
    val cart: List<CartItem> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val total: Double = 0.0,
    val scanError: String? = null,
    val lastScanned: String? = null, // Usado para disparar la navegación al carrito
    val ledState: LedState = LedState.RED,
    val purchases: List<Purchase> = emptyList()
)

// ===========================================================
// VIEWMODEL
// ===========================================================
class MainViewModel(
    private val repo: EasyCartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState(user = repo.currentUser()))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeProducts()
        observeOffers()
        observeCart()
        loadPurchases()
    }

    // ===========================================================
    // FIRESTORE LISTENERS
    // ===========================================================

    private fun observeProducts() {
        viewModelScope.launch {
            repo.listenProducts().collect { list ->
                _uiState.update { it.copy(products = list) }
            }
        }
    }

    private fun observeOffers() {
        viewModelScope.launch {
            repo.listenOffers().collect { list ->
                _uiState.update { it.copy(offers = list) }
            }
        }
    }

    private fun observeCart() {
        val uid = repo.currentUser()?.uid ?: return
        viewModelScope.launch {
            repo.listenCart(uid).collect { items ->
                val total = items.sumOf { item ->
                    item.unitPrice * item.quantity
                }
                _uiState.update { it.copy(cart = items, total = total) }
            }
        }
    }

    // ===========================================================
    // LED CONTROL
    // ===========================================================
    fun activateGreenLED() {
        _uiState.update { it.copy(ledState = LedState.GREEN) }
        viewModelScope.launch {
            delay(1000)
            _uiState.update { it.copy(ledState = LedState.RED) }
        }
    }

    fun activateRedLED() {
        _uiState.update { it.copy(ledState = LedState.RED) }
    }

    fun activateYellowLED() {
        viewModelScope.launch {
            _uiState.update { it.copy(ledState = LedState.YELLOW) }
            delay(3000)
            _uiState.update { it.copy(ledState = LedState.RED) }
        }
    }

    // ===========================================================
    // ESCANEO UNIVERSAL
    // ===========================================================
    fun onBarcodeScanned(rawBarcode: String) {
        val barcode = rawBarcode.trim()
        if (barcode.isBlank()) return

        viewModelScope.launch {
            val product = repo.findProductByBarcode(barcode)

            if (product != null) {
                // ⭐ CORRECCIÓN CLAVE: Pasamos el producto individual ('product')
                // en lugar de la lista de productos ('products').
                addProductToCart(product)
                activateGreenLED()

                _uiState.update {
                    it.copy(
                        lastScanned = product.name,
                        scanError = null
                    )
                }

                // El delay largo evita que se bloqueen escaneos rápidos.
                viewModelScope.launch {
                    delay(2000)
                    _uiState.update { it.copy(lastScanned = null) }
                }

            } else {
                activateRedLED()
                _uiState.update {
                    it.copy(
                        scanError = "Producto no encontrado",
                        lastScanned = null
                    )
                }
            }
        }
    }

    // ===========================================================
    // CARRITO: AGREGAR / INCREMENTAR / DECREMENTAR
    // ===========================================================
    private fun addProductToCart(product: Product) {
        viewModelScope.launch {
            repo.addOrIncrementCartItem(
                uid = repo.currentUser()?.uid ?: return@launch,
                product = product
            )
        }
    }

    fun increaseQuantity(item: CartItem) {
        viewModelScope.launch {
            val uid = repo.currentUser()?.uid ?: return@launch

            // Buscamos el producto en la lista de productos local para obtener los datos correctos
            val productFromList = _uiState.value.products.firstOrNull { it.id == item.productId }

            val productToUpdate = productFromList ?: Product(
                id = item.productId,
                name = item.productName,
                barcode = item.barcode,
                price = item.unitPrice,
                stock = item.maxStock,
                expiresAt = item.expiresAt
            )

            repo.addOrIncrementCartItem(uid, productToUpdate)
        }
    }

    fun decreaseQuantity(item: CartItem) {
        viewModelScope.launch {
            val uid = repo.currentUser()?.uid ?: return@launch
            repo.decrementCartItem(uid, item.productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repo.clearCart(
                uid = repo.currentUser()?.uid ?: return@launch
            )
        }
    }

    // ===========================================================
    // FINALIZAR COMPRA
    // ===========================================================
    fun finalizePurchase() {
        viewModelScope.launch {

            val cart = _uiState.value.cart
            val user = repo.currentUser() ?: return@launch

            // Llama al repositorio para la transacción de pago/stock/limpieza
            val success = repo.finalizePurchase(user.uid, cart)

            if (success) {
                activateGreenLED()
                loadPurchases()
                // El observeCart se encarga de vaciar la UI porque Firestore se actualiza
            } else {
                activateRedLED()
                _uiState.update { it.copy(scanError = "Error al finalizar la compra y actualizar stock.") }
            }
        }
    }

    // ===========================================================
    // HISTORIAL DE COMPRAS
    // ===========================================================
    fun loadPurchases() {
        val user = repo.currentUser() ?: return

        viewModelScope.launch {
            val list = repo.loadPurchases(user.uid)
            _uiState.update { it.copy(purchases = list) }
        }
    }
}