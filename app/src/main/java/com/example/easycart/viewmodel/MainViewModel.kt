package com.example.easycart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easycart.data.EasyCartRepository
import com.example.easycart.data.model.CartItem
import com.example.easycart.data.model.Offer
import com.example.easycart.data.model.Product
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val user: FirebaseUser? = null,
    val products: List<Product> = emptyList(),
    val cart: List<CartItem> = emptyList(),
    val offers: List<Offer> = emptyList(),
    val total: Double = 0.0,
    val scanError: String? = null,
    val lastScanned: String? = null,
)

class MainViewModel(
    private val repo: EasyCartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState(user = repo.currentUser()))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeProducts()
        observeOffers()
        observeCart()
    }

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
                val total = items.sumOf { it.totalPrice }
                _uiState.update { it.copy(cart = items, total = total) }
            }
        }
    }

    fun onBarcodeScanned(raw: String) {
        val barcode = raw.trim()
        if (barcode.isEmpty()) return

        viewModelScope.launch {

            try {

                val uid = repo.currentUser()?.uid ?: return@launch
                val product = repo.findProductByBarcode(barcode)

                if (product == null) {
                    _uiState.update {
                        it.copy(
                            scanError = "Producto no encontrado",
                            lastScanned = null
                        )
                    }
                } else {
                    repo.addOrIncrementCartItem(uid, product)
                    _uiState.update {
                        it.copy(
                            scanError = null,
                            lastScanned = product.name
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        scanError = "Error al escanear: ${e.message}",
                        lastScanned = null
                    )
                }
            }
        }
    }

    fun clearCart() {
        val uid = repo.currentUser()?.uid ?: return
        viewModelScope.launch { repo.clearCart(uid) }
    }

}


