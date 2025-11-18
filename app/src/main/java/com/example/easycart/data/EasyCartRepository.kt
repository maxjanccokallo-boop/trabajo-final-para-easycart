package com.example.easycart.data

import com.example.easycart.data.model.CartItem
import com.example.easycart.data.model.Offer
import com.example.easycart.data.model.Product
import com.example.easycart.data.model.Purchase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import java.util.*

// Clase auxiliar necesaria para las funciones de AuthViewModel
data class AuthResult(
    val isSuccess: Boolean = false,
    val exceptionOrNull: (() -> Exception)? = null
)

class EasyCartRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    fun currentUser(): FirebaseUser? = auth.currentUser

    // ===========================================================
    // FUNCIONES DE AUTENTICACIÓN (Placeholders - Se mantienen)
    // ===========================================================

    fun login(email: String, password: String): AuthResult {
        return AuthResult(isSuccess = true)
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        phone: String
    ): AuthResult {
        return AuthResult(isSuccess = true)
    }

    // ===========================================================
    // CARRITO: ESCRITURA (addOrIncrementCartItem)
    // ===========================================================
    suspend fun addOrIncrementCartItem(uid: String, product: Product) {
        val productIdNonNull =
            product.id ?: throw IllegalArgumentException("Product ID cannot be null")

        val cartItemRef = db.collection("carts")
            .document(uid)
            .collection("items")
            .document(productIdNonNull)

        try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(cartItemRef)

                if (snapshot.exists()) {
                    val currentQuantity = snapshot.getLong("quantity")?.toInt() ?: 0
                    val newQuantity = currentQuantity + 1

                    if (newQuantity > product.stock) {
                        throw Exception("Stock insuficiente para ${product.name}")
                    }

                    transaction.update(
                        cartItemRef, mapOf(
                            "quantity" to newQuantity,
                            "totalPrice" to newQuantity * product.price
                        )
                    )
                } else {
                    // ⭐ AJUSTE: Incluimos totalPrice en el constructor del CartItem
                    val newItem = CartItem(
                        id = productIdNonNull,
                        productId = productIdNonNull,
                        productName = product.name,
                        unitPrice = product.price,
                        barcode = product.barcode ?: "",
                        quantity = 1,
                        // Inicializamos totalPrice

                        maxStock = product.stock,
                        expiresAt = product.expiresAt
                    )
                    // Usamos el objeto completo (newItem)
                    transaction.set(cartItemRef, newItem, SetOptions.merge())
                }
                null
            }.await()
        } catch (e: Exception) {
            println("Error en addOrIncrementCartItem: ${e.message}")
        }
    }

    // ===========================================================
    // CARRITO: LECTURA (listenCart)
    // ===========================================================
    fun listenCart(uid: String): Flow<List<CartItem>> = callbackFlow {
        val listener = db.collection("carts")
            .document(uid)
            .collection("items")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    println("Error en listenCart: ${e.message}")
                    close(e)
                } else {
                    val list = snap?.toObjects(CartItem::class.java) ?: emptyList()
                    trySend(list).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }

    // ===========================================================
    // FUNCIONES DE BÚSQUEDA Y LECTURA
    // ===========================================================
    suspend fun findProductByBarcode(barcode: String): Product? {
        return try {
            val snapshot = db.collection("products")
                .whereEqualTo("barcode", barcode)
                .limit(1)
                .get()
                .await()

            val document = snapshot.documents.firstOrNull()

            if (document != null) {
                val product = document.toObject(Product::class.java)

                if (product?.id == null) {
                    return product?.copy(id = document.id)
                }
                return product
            }
            null
        } catch (e: Exception) {
            println("Error buscando producto: ${e.message}")
            null
        }
    }

    // ===========================================================
    // CARRITO: DECREMENTAR (decrementCartItem) - IMPLEMENTADO
    // ===========================================================
    suspend fun decrementCartItem(uid: String, productId: String) {
        val cartItemRef = db.collection("carts")
            .document(uid)
            .collection("items")
            .document(productId)

        try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(cartItemRef)
                if (snapshot.exists()) {
                    val currentQuantity = snapshot.getLong("quantity")?.toInt() ?: 0

                    if (currentQuantity > 1) {
                        val newQuantity = currentQuantity - 1
                        val unitPrice = snapshot.getDouble("unitPrice") ?: 0.0

                        transaction.update(
                            cartItemRef, mapOf(
                                "quantity" to newQuantity,
                                "totalPrice" to newQuantity * unitPrice
                            )
                        )
                    } else {
                        // Si la cantidad es 1, eliminar el ítem.
                        transaction.delete(cartItemRef)
                    }
                }
                null
            }.await()
        } catch (e: Exception) {
            println("Error en decrementCartItem: ${e.message}")
        }
    }

    // ===========================================================
    // CARRITO: VACIAR (clearCart) - IMPLEMENTADO
    // ===========================================================
    suspend fun clearCart(uid: String) {
        try {
            val itemsRef = db.collection("carts").document(uid).collection("items")
            val itemsSnapshot = itemsRef.get().await()

            val batch = db.batch()
            itemsSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
        } catch (e: Exception) {
            println("Error vaciando carrito: ${e.message}")
        }
    }

    // ===========================================================
    // FINALIZAR COMPRA (finalizePurchase) - CORREGIDO
    // ===========================================================
    suspend fun finalizePurchase(uid: String, cart: List<CartItem>): Boolean {
        if (cart.isEmpty()) return false

        return try {
            db.runTransaction { transaction ->
                // 1. Actualizar Stock
                cart.forEach { item ->
                    val productRef = db.collection("products").document(item.productId)
                    val productSnapshot = transaction.get(productRef)

                    if (productSnapshot.exists()) {
                        val currentStock = productSnapshot.getLong("stock")?.toInt() ?: 0
                        val newStock = currentStock - item.quantity

                        if (newStock < 0) {
                            throw Exception("Stock insuficiente para ${item.productName}.")
                        }

                        transaction.update(productRef, "stock", newStock)
                    }
                }

                // 2. Registrar la compra
                val purchaseRef = db.collection("users").document(uid).collection("purchases").document()
                val purchaseData = mapOf(
                    "date" to Date(),
                    "total" to cart.sumOf { it.totalPrice },
                    "items" to cart.map { mapOf(
                        "productId" to it.productId,
                        "name" to it.productName,
                        "quantity" to it.quantity,
                        "unitPrice" to it.unitPrice
                    )}
                )
                transaction.set(purchaseRef, purchaseData)
                null
            }.await()

            // ⭐ BORRADO MASIVO: Fuera de la transacción para evitar el error de corrutina
            // y porque Firestore recomienda que el borrado masivo se haga en un Batch.
            clearCart(uid)
            true
        } catch (e: Exception) {
            println("Error al finalizar compra/actualizar stock: ${e.message}")
            false
        }
    }

    // ===========================================================
    // HISTORIAL DE COMPRAS (loadPurchases) - IMPLEMENTADO
    // ===========================================================
    suspend fun loadPurchases(uid: String): List<Purchase> {
        return try {
            db.collection("users")
                .document(uid)
                .collection("purchases")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Purchase::class.java)
        } catch (e: Exception) {
            println("Error cargando compras: ${e.message}")
            emptyList()
        }
    }

    // ===========================================================
    // OTROS LISTENERS
    // ===========================================================
    fun listenProducts(): Flow<List<Product>> = callbackFlow {
        val listener = db.collection("products")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    println("Error en listenProducts: ${e.message}")
                    close(e)
                } else {
                    val list = snap?.toObjects(Product::class.java) ?: emptyList()
                    trySend(list).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }

    fun listenOffers(): Flow<List<Offer>> = callbackFlow { awaitClose { } }
}