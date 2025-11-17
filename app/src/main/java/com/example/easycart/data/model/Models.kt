package com.example.easycart.data.model

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

data class Product(
    val id: String = "",
    val name: String = "",
    val barcode: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val imageUrl: String = "",
    val category: String = "",
    val isHealthy: Boolean = true,
    val healthLabel: String = "", // Saludable / Moderado / No saludable
    val expiresAt: Timestamp? = null
)

data class CartItem(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val barcode: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val expiresAt: Timestamp? = null
) {
    val totalPrice: Double get() = unitPrice * quantity
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

/** Estado de sensores del carrito (para Arduino) */
data class CartStatus(
    val ledOn: Boolean = false,
    val irTriggered: Boolean = false,
    val barrierTriggered: Boolean = false,
    val weightKg: Double = 0.0,
    val lastUpdate: Timestamp = Timestamp.now()
)
