package com.example.easycart.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycart.data.EasyCartRepository
import com.example.easycart.data.model.CartItem
import com.example.easycart.data.model.Offer
import com.example.easycart.data.model.Product
import com.example.easycart.data.model.Purchase
import com.example.easycart.di.AppModule
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class LedState { RED, YELLOW, GREEN }

data class ScanEntry(
    val barcode: String = "",
    val label: String = "",
    val success: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

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

class MainViewModel(
    private val repository: EasyCartRepository
) : ViewModel() {

    private val _darkTheme = MutableStateFlow(false)
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    fun toggleTheme() {
        _darkTheme.value = !_darkTheme.value
    }

    private val _uiState = MutableStateFlow(
        MainUiState(user = repository.currentUser())
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var permissionJob: Job? = null

    init {
        observeProducts()
        observeOffers()
        observeCart()
        loadPurchases()

        repository.onAuthStateChanged { firebaseUser ->
            _uiState.update { it.copy(user = firebaseUser) }
            if (firebaseUser != null) loadPurchases()
        }
    }

    private fun enviarPermiso() {
        Log.d("BT_FLOW", "MainViewModel: enviarPermiso() llamado.")
        permissionJob?.cancel()
        permissionJob = viewModelScope.launch {
            AppModule.bluetoothService.sendData("SI\n")
            delay(10000)
        }
    }

    fun onBarcodeScanned(rawBarcode: String) {
        val barcode = rawBarcode.trim()
        if (barcode.isBlank()) return

        viewModelScope.launch {
            val product = repository.findProductByBarcode(barcode)

            if (product != null) {
                enviarPermiso()
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

    private fun addProductToCart(product: Product) = viewModelScope.launch {
        val uid = repository.currentUser()?.uid ?: return@launch
        repository.addOrIncrementCartItem(uid, product)
    }

    fun increaseQuantity(item: CartItem) = viewModelScope.launch {
        val uid = repository.currentUser()?.uid ?: return@launch
        val product = uiState.value.products.firstOrNull { it.id == item.productId }
            ?: item.toProduct()
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

    fun removeItem(item: CartItem) {
        val uid = uiState.value.user?.uid ?: return
        viewModelScope.launch {
            repository.decrementCartItem(uid, item.productId)
        }
    }

    // ⭐⭐⭐ CORREGIDO — AHORA DEVUELVE ITEMS Y NO FALLA ⭐⭐⭐
    fun finalizePurchase(onResult: (Boolean, List<CartItem>) -> Unit) {
        val uid = uiState.value.user?.uid ?: return

        val purchasedItems = uiState.value.cart.map { it.copy() }

        viewModelScope.launch {
            val ok = repository.finalizePurchase(uid, purchasedItems)

            onResult(ok, purchasedItems)

            if (ok) clearCart()
        }
    }

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
            _uiState.update { it.copy(cart = items, total = total) }
        }
    }

    private fun flashLED(color: LedState, duration: Long = 1200) {
        viewModelScope.launch {
            _uiState.update { it.copy(ledState = color) }
            delay(duration)
            _uiState.update { it.copy(ledState = LedState.RED) }
        }
    }

    fun activateGreenLED() = flashLED(LedState.GREEN)
    fun activateRedLED() = flashLED(LedState.RED, 50)

    // ⭐⭐⭐ CORREGIDO — SIN ?: Y SIN CRASH DEL PDF ⭐⭐⭐
    private fun CartItem.toProduct(): Product {
        return Product(
            id = this.productId,
            name = this.productName,
            barcode = this.barcode,
            price = this.unitPrice,
            stock = this.maxStock,  // ← ESTA LÍNEA ESTABA MAL
            expiresAt = this.expiresAt
        )
    }

    fun refreshUser() {
        val newUser = repository.currentUser()
        _uiState.update { it.copy(user = newUser) }
    }
}
