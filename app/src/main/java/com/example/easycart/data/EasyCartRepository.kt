package com.example.easycart.data

import com.example.easycart.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
class EasyCartRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    // ---------- AUTH ----------
    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String
    ): Result<Unit> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: return Result.failure(Exception("UID nulo"))
        val profile = UserProfile(
            uid = uid,
            fullName = name,
            email = email,
            phone = phone
        )
        db.collection("users").document(uid).set(profile).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun login(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun currentUser() = auth.currentUser



    // ---------- PRODUCTS ----------
    fun listenProducts(): Flow<List<Product>> = callbackFlow {
        val listener = db.collection("products")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    close(e)
                } else {
                    val list = snap?.toObjects(Product::class.java) ?: emptyList()
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun findProductByBarcode(barcode: String): Product? = try {
        val snap = db.collection("products")
            .whereEqualTo("barcode", barcode)
            .limit(1)
            .get()
            .await()
        snap.documents.firstOrNull()?.toObject(Product::class.java)
    } catch (e: Exception) {
        null
    }

    // ---------- CART ----------
    fun listenCart(uid: String): Flow<List<CartItem>> = callbackFlow {
        val listener = db.collection("carts")
            .document(uid)
            .collection("items")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    close(e)
                } else {
                    val list = snap?.toObjects(CartItem::class.java) ?: emptyList()
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addOrIncrementCartItem(uid: String, product: Product) {
        val itemsRef = db.collection("carts").document(uid).collection("items")
        val snap = itemsRef
            .whereEqualTo("productId", product.id)
            .limit(1)
            .get()
            .await()

        if (snap.isEmpty) {
            val id = itemsRef.document().id
            val item = CartItem(
                id = id,
                productId = product.id,
                productName = product.name,
                barcode = product.barcode,
                quantity = 1,
                unitPrice = product.price,
                expiresAt = product.expiresAt
            )
            itemsRef.document(id).set(item).await()
        } else {
            val doc = snap.documents.first()
            val current = doc.toObject(CartItem::class.java)
            val newQty = (current?.quantity ?: 1) + 1
            doc.reference.update("quantity", newQty).await()
        }
    }

    suspend fun clearCart(uid: String) {
        val items = db.collection("carts").document(uid).collection("items").get().await()
        val batch = db.batch()
        items.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    // ---------- OFFERS ----------
    fun listenOffers(): Flow<List<Offer>> = callbackFlow {
        val listener = db.collection("offers")
            .addSnapshotListener { snap, e ->
                if (e != null) close(e)
                else {
                    val list = snap?.toObjects(Offer::class.java) ?: emptyList()
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }
    fun logout() {
        auth.signOut()
    }
}

