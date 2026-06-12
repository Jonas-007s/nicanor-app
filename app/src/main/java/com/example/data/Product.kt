package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String = "", // Can be web URL, local uri, or default pre-loaded item code
    val category: String, // "Anillos", "Collares", "Pulseras", "Aros"
    val reference: String, // SKU/Reference code (e.g., "REF-NIK-101")
    val material: String, // "Oro Blanco 18K", "Oro Amarillo 18K", "Platino 950", "Plata 925"
    val isAvailable: Boolean = true,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "reservations")
data class Reservation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val productName: String,
    val productReference: String,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String,
    val message: String,
    val reservationDate: Long = System.currentTimeMillis(),
    val status: String = "Pendiente" // "Pendiente", "Confirmado", "Cancelado"
)
