package com.example.easycart.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

data class Product(
    @DocumentId
    val id: String? = null,
    val name: String = "",
    val barcode: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val imageUrl: String = "",
    val category: String = "",
    val isHealthy: Boolean = true,
    val healthLabel: String = "",
    val expiresAt: Timestamp? = null,
    val description: String? = null,
    val hasOffer: Boolean = false,
    val offerPrice: Double? = null
)

data class CartItem(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val barcode: String = "",
    val quantity: Int = 1,

    // ⭐ precio normal
    val unitPrice: Double = 0.0,

    // ⭐ SI EL PRODUCTO TIENE OFERTA (Firebase)
    val hasOffer: Boolean = false,
    val offerPrice: Double? = null,
    val discountPercent: Int? = null,

    val maxStock: Int = 0,
    val expiresAt: Timestamp? = null
) {
    val finalUnitPrice: Double
        get() = if (hasOffer && offerPrice != null) offerPrice else unitPrice

    val totalPrice: Double
        get() = quantity * finalUnitPrice
}

data class Offer(
    val id: String = "",
    val productId: String = "",
    val title: String = "",
    val description: String = "",
    val discountPercent: Int = 0,
    val validUntil: Timestamp? = null,
    val imageUrl: String = ""
)

data class CartStatus(
    val ledOn: Boolean = false,
    val irTriggered: Boolean = false,
    val barrierTriggered: Boolean = false,
    val weightKg: Double = 0.0,
    val lastUpdate: Timestamp = Timestamp.now()
)

data class Purchase(
    val total: Double = 0.0,
    val items: List<Map<String, Any>> = emptyList(),
    val timestamp: Long = 0
)
