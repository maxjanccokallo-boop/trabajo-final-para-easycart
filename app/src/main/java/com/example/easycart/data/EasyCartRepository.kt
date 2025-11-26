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
    // LOGIN / REGISTER FAKE (mantengo tu lógica)
    // ===========================================================
    fun login(email: String, password: String, onResult: (AuthResult) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onResult(AuthResult(isSuccess = true))
            }
            .addOnFailureListener { e ->
                onResult(AuthResult(isSuccess = false, exceptionOrNull = { e }))
            }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        phone: String,
        onResult: (AuthResult) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onResult(AuthResult(isSuccess = true))
            }
            .addOnFailureListener { e ->
                onResult(AuthResult(isSuccess = false, exceptionOrNull = { e }))
            }
    }


    // ===========================================================
    // ⭐ AÑADIR PRODUCTO AL CARRITO (OFERTAS COMPLETAS)
    // ===========================================================
    suspend fun addOrIncrementCartItem(uid: String, product: Product) {

        val productId = product.id ?: throw IllegalArgumentException("Product ID cannot be null")

        val cartItemRef = db.collection("carts")
            .document(uid)
            .collection("items")
            .document(productId)

        try {
            db.runTransaction { transaction ->

                val snap = transaction.get(cartItemRef)

                val hasOffer = product.hasOffer
                val offerPrice = product.offerPrice
                val finalUnitPrice =
                    if (hasOffer && offerPrice != null) offerPrice else product.price

                val discountPercent =
                    if (hasOffer && offerPrice != null && product.price > 0)
                        (((product.price - offerPrice) / product.price) * 100).toInt()
                    else null

                // ----------------------------
                // SI YA EXISTE → sumar cantidad
                // ----------------------------
                if (snap.exists()) {

                    val currentQty = snap.getLong("quantity")?.toInt() ?: 0
                    val newQty = currentQty + 1

                    if (newQty > product.stock)
                        throw Exception("Stock insuficiente para ${product.name}")

                    transaction.update(
                        cartItemRef,
                        mapOf(
                            "quantity" to newQty,
                            "hasOffer" to hasOffer,
                            "offerPrice" to offerPrice,
                            "discountPercent" to discountPercent,
                            "unitPrice" to product.price,
                            "totalPrice" to newQty * finalUnitPrice
                        )
                    )

                    return@runTransaction
                }

                // ----------------------------
                // SI ES NUEVO → guardar info completa
                // ----------------------------
                val item = CartItem(
                    id = productId,
                    productId = productId,
                    productName = product.name,
                    barcode = product.barcode,

                    quantity = 1,
                    unitPrice = product.price,
                    hasOffer = hasOffer,
                    offerPrice = offerPrice,
                    discountPercent = discountPercent,

                    maxStock = product.stock,
                    expiresAt = product.expiresAt
                )

                transaction.set(cartItemRef, item, SetOptions.merge())
                null
            }.await()

        } catch (e: Exception) {
            println("🔥 Error en addOrIncrementCartItem: ${e.message}")
        }
    }


    // ===========================================================
    // ⭐ ESCUCHAR CARRITO
    // ===========================================================
    fun listenCart(uid: String): Flow<List<CartItem>> = callbackFlow {

        val listener = db.collection("carts")
            .document(uid)
            .collection("items")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    close(e)
                } else {
                    trySend(snap?.toObjects(CartItem::class.java) ?: emptyList())
                }
            }

        awaitClose { listener.remove() }
    }


    // ===========================================================
    // BUSCAR PRODUCTO POR CÓDIGO
    // ===========================================================
    suspend fun findProductByBarcode(barcode: String): Product? {
        return try {
            val snap = db.collection("products")
                .whereEqualTo("barcode", barcode)
                .limit(1)
                .get()
                .await()

            val doc = snap.documents.firstOrNull() ?: return null
            doc.toObject(Product::class.java)?.copy(id = doc.id)

        } catch (e: Exception) {
            println("🔥 Error buscando producto: ${e.message}")
            null
        }
    }


    // ===========================================================
    // ⭐ DECREMENTAR CANTIDAD (respeta oferta)
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

                val quantity = snap.getLong("quantity")?.toInt() ?: 0
                val unitPrice = snap.getDouble("unitPrice") ?: 0.0
                val offer = snap.getDouble("offerPrice")
                val hasOffer = snap.getBoolean("hasOffer") ?: false

                val finalUnitPrice = if (hasOffer && offer != null) offer else unitPrice

                if (quantity > 1) {
                    val newQty = quantity - 1
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
    // ⭐ ELIMINAR TODO / LIMPIAR CARRITO
    // ===========================================================
    suspend fun clearCart(uid: String) {
        try {
            val snap = db.collection("carts").document(uid).collection("items").get().await()

            val batch = db.batch()
            snap.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()

        } catch (e: Exception) {
            println("🔥 Error clearCart: ${e.message}")
        }
    }


    // ===========================================================
    // ⭐ FINALIZAR COMPRA (STOCK + HISTORIAL)
    // ===========================================================
    suspend fun finalizePurchase(uid: String, cart: List<CartItem>): Boolean {

        if (cart.isEmpty()) return false

        return try {

            db.runTransaction { transaction ->

                // ----------------------------
                // 1. RESTAR STOCK
                // ----------------------------
                cart.forEach { item ->

                    val prodRef = db.collection("products").document(item.productId)
                    val snap = transaction.get(prodRef)

                    if (snap.exists()) {
                        val stock = snap.getLong("stock")?.toInt() ?: 0
                        val newStock = stock - item.quantity

                        if (newStock < 0)
                            throw Exception("Stock insuficiente para ${item.productName}")

                        transaction.update(prodRef, "stock", newStock)
                    }
                }

                // ----------------------------
                // 2. GUARDAR COMPRA
                // ----------------------------
                val purchaseRef = db.collection("users")
                    .document(uid)
                    .collection("purchases")
                    .document()

                val data = mapOf(
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

                transaction.set(purchaseRef, data)
                null
            }.await()

            // Borrado fuera de la transacción
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
                else {
                    val list = snap!!.documents.mapNotNull { doc ->
                        doc.toObject(Product::class.java)?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }




    fun listenOffers(): Flow<List<Offer>> = callbackFlow { awaitClose {} }
}
