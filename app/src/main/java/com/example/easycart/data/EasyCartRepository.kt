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

// Clase auxiliar para AuthViewModel
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
    // LOGIN / REGISTER (Placeholders)
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
    // ⭐ AÑADIR PRODUCTO AL CARRITO CON OFERTA
    // ===========================================================
    suspend fun addOrIncrementCartItem(uid: String, product: Product) {
        val productId =
            product.id ?: throw IllegalArgumentException("Product ID cannot be null")

        val cartItemRef = db.collection("carts")
            .document(uid)
            .collection("items")
            .document(productId)

        try {
            db.runTransaction { transaction ->

                val snapshot = transaction.get(cartItemRef)

                // ----------------------------
                // SI YA EXISTE, SOLO SUMAMOS
                // ----------------------------
                if (snapshot.exists()) {

                    val currentQuantity = snapshot.getLong("quantity")?.toInt() ?: 0
                    val newQuantity = currentQuantity + 1

                    if (newQuantity > product.stock) {
                        throw Exception("Stock insuficiente para ${product.name}")
                    }

                    val finalUnit =
                        if (product.hasOffer && product.offerPrice != null)
                            product.offerPrice!!
                        else product.price

                    transaction.update(
                        cartItemRef, mapOf(
                            "quantity" to newQuantity,
                            "totalPrice" to newQuantity * finalUnit
                        )
                    )

                    return@runTransaction
                }

                // ----------------------------
                // SI ES NUEVO → GUARDAMOS OFERTA
                // ----------------------------
                val discountPercent = when {
                    product.hasOffer && product.offerPrice != null ->
                        (((product.price - product.offerPrice) / product.price) * 100).toInt()

                    else -> null
                }

                val newItem = CartItem(
                    id = productId,
                    productId = productId,
                    productName = product.name,
                    barcode = product.barcode,
                    quantity = 1,

                    // precios
                    unitPrice = product.price,
                    hasOffer = product.hasOffer,
                    offerPrice = product.offerPrice,
                    discountPercent = discountPercent,

                    maxStock = product.stock,
                    expiresAt = product.expiresAt
                )

                transaction.set(cartItemRef, newItem, SetOptions.merge())
                null
            }.await()

        } catch (e: Exception) {
            println("🔥 Error en addOrIncrementCartItem: ${e.message}")
        }
    }

    // ===========================================================
    // ⭐ ESCUCHAR CARRITO EN TIEMPO REAL
    // ===========================================================
    fun listenCart(uid: String): Flow<List<CartItem>> = callbackFlow {
        val listener = db.collection("carts")
            .document(uid)
            .collection("items")
            .addSnapshotListener { snap, e ->
                if (e != null) close(e)
                else trySend(snap?.toObjects(CartItem::class.java) ?: emptyList())
            }

        awaitClose { listener.remove() }
    }

    // ===========================================================
    // BUSCAR PRODUCTO
    // ===========================================================
    suspend fun findProductByBarcode(barcode: String): Product? {
        return try {
            val snapshot = db.collection("products")
                .whereEqualTo("barcode", barcode)
                .limit(1)
                .get()
                .await()

            val doc = snapshot.documents.firstOrNull() ?: return null

            doc.toObject(Product::class.java)?.copy(id = doc.id)

        } catch (e: Exception) {
            println("🔥 Error buscando producto: ${e.message}")
            null
        }
    }

    // ===========================================================
    // ⭐ DECREMENTAR CANTIDAD
    // ===========================================================
    suspend fun decrementCartItem(uid: String, productId: String) {

        val cartItemRef = db.collection("carts")
            .document(uid)
            .collection("items")
            .document(productId)

        try {
            db.runTransaction { transaction ->

                val snap = transaction.get(cartItemRef)
                if (!snap.exists()) return@runTransaction

                val currentQuantity = snap.getLong("quantity")?.toInt() ?: 0
                val unitPrice = snap.getDouble("unitPrice") ?: 0.0
                val offer = snap.getDouble("offerPrice")
                val hasOffer = snap.getBoolean("hasOffer") ?: false

                val finalUnitPrice = if (hasOffer && offer != null) offer else unitPrice

                if (currentQuantity > 1) {

                    val newQty = currentQuantity - 1
                    transaction.update(
                        cartItemRef,
                        mapOf(
                            "quantity" to newQty,
                            "totalPrice" to newQty * finalUnitPrice
                        )
                    )

                } else {
                    transaction.delete(cartItemRef)
                }

                null
            }.await()

        } catch (e: Exception) {
            println("🔥 Error decrementCartItem: ${e.message}")
        }
    }

    // ===========================================================
    // VACIAR CARRITO
    // ===========================================================
    suspend fun clearCart(uid: String) {
        try {
            val items =
                db.collection("carts").document(uid).collection("items").get().await()

            val batch = db.batch()

            items.documents.forEach { batch.delete(it.reference) }

            batch.commit().await()

        } catch (e: Exception) {
            println("🔥 Error clearCart: ${e.message}")
        }
    }

    // ===========================================================
    // ⭐ FINALIZAR COMPRA (RESTAR STOCK + GUARDAR HISTORIAL)
    // ===========================================================
    suspend fun finalizePurchase(uid: String, cart: List<CartItem>): Boolean {
        if (cart.isEmpty()) return false

        return try {
            db.runTransaction { transaction ->

                // 1. Restar stock
                cart.forEach { item ->
                    val productRef = db.collection("products").document(item.productId)
                    val snap = transaction.get(productRef)

                    if (snap.exists()) {
                        val currentStock = snap.getLong("stock")?.toInt() ?: 0
                        val newStock = currentStock - item.quantity

                        if (newStock < 0) {
                            throw Exception("Stock insuficiente para ${item.productName}")
                        }
                        transaction.update(productRef, "stock", newStock)
                    }
                }

                // 2. Registrar compra
                val purchaseRef = db.collection("users")
                    .document(uid)
                    .collection("purchases")
                    .document()

                val purchaseData = mapOf(
                    "date" to Date(),
                    "total" to cart.sumOf { it.totalPrice },
                    "items" to cart.map {
                        mapOf(
                            "productId" to it.productId,
                            "name" to it.productName,
                            "quantity" to it.quantity,
                            "unitPrice" to it.finalUnitPrice
                        )
                    }
                )

                transaction.set(purchaseRef, purchaseData)
                null
            }.await()

            clearCart(uid)
            true

        } catch (e: Exception) {
            println("🔥 Error finalizePurchase: ${e.message}")
            false
        }
    }

    // ===========================================================
    // HISTORIAL
    // ===========================================================
    suspend fun loadPurchases(uid: String): List<Purchase> {
        return try {
            db.collection("users")
                .document(uid)
                .collection("purchases")
                .orderBy("date")
                .get()
                .await()
                .toObjects(Purchase::class.java)

        } catch (e: Exception) {
            println("🔥 Error loadPurchases: ${e.message}")
            emptyList()
        }
    }

    // ===========================================================
    // LISTEN PRODUCTS
    // ===========================================================
    fun listenProducts(): Flow<List<Product>> = callbackFlow {
        val listener = db.collection("products")
            .addSnapshotListener { snap, e ->
                if (e != null) close(e)
                else trySend(snap?.toObjects(Product::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    fun listenOffers(): Flow<List<Offer>> = callbackFlow { awaitClose {} }
}
