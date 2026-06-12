package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Product
import com.example.data.ProductRepository
import com.example.data.Reservation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JoyasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository
    val productsState: StateFlow<List<Product>>
    val reservationsState: StateFlow<List<Reservation>>

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todas")
    val selectedCategory = _selectedCategory.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ProductRepository(database.productDao())

        productsState = repository.allProducts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        reservationsState = repository.allReservations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed initial elegant jewelry data if database is empty
        viewModelScope.launch {
            productsState.collectLatest { list ->
                if (list.isEmpty()) {
                    seedInitialProducts()
                }
            }
        }
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // Product Admin CRUD operations
    fun addProduct(
        name: String,
        description: String,
        price: Double,
        imageUrl: String,
        category: String,
        reference: String,
        material: String
    ) {
        viewModelScope.launch {
            val newProduct = Product(
                name = name,
                description = description,
                price = price,
                imageUrl = imageUrl,
                category = category,
                reference = reference,
                material = material,
                isAvailable = true
            )
            repository.insertProduct(newProduct)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun deleteProductById(id: Int) {
        viewModelScope.launch {
            repository.deleteProductById(id)
        }
    }

    // Reservation operations
    fun makeReservation(
        productId: Int,
        productName: String,
        productReference: String,
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        message: String,
        onSuccess: (Reservation) -> Unit
    ) {
        viewModelScope.launch {
            val reservation = Reservation(
                productId = productId,
                productName = productName,
                productReference = productReference,
                customerName = customerName,
                customerPhone = customerPhone,
                customerEmail = customerEmail,
                message = message,
                status = "Pendiente"
            )
            repository.insertReservation(reservation)
            onSuccess(reservation)
        }
    }

    fun deleteReservation(id: Int) {
        viewModelScope.launch {
            repository.deleteReservationById(id)
        }
    }

    private suspend fun seedInitialProducts() {
        val initialList = listOf(
            Product(
                name = "Anillo de Compromiso Eterno",
                description = "Anillo exclusivo de compromiso forjado en platino pulido de alta pureza. Presenta un diamante central corte cojín brillante de 1.5 quilates, rodeado de un micro-pavé de diamantes en la banda para un destello incomparable.",
                price = 3400.0,
                imageUrl = "img_ring",
                category = "Anillos",
                reference = "REF-NIC-001",
                material = "Platino 950"
            ),
            Product(
                name = "Colgante Diamante Solitario Gota",
                description = "Imponente collar de alta gama con un colgante solitario de diamante corte pera en forma de lágrima de 2.0 quilates. Cadena fina de platino con broche de seguridad invisible que descansa suavemente sobre el cuello.",
                price = 5100.0,
                imageUrl = "img_necklace",
                category = "Collares",
                reference = "REF-NIC-002",
                material = "Oro Blanco 18K"
            ),
            Product(
                name = "Brazalete Minimalista Obsidian",
                description = "Esposa de muñeca rígida de diseño asimétrico contemporáneo. Elaborada en platino con acabados cepillados que contrastan con cantos biselados altamente pulidos. Una pieza sobria y elegante de presencia imponente.",
                price = 2800.0,
                imageUrl = "img_bracelet",
                category = "Pulseras",
                reference = "REF-NIC-003",
                material = "Platino 950"
            ),
            Product(
                name = "Aros Drop de Perla & Diamante",
                description = "Pendientes colgantes delicadamente elaborados con perlas cultivadas del Mar del Sur de impecable lustre blanco. Engastados con pequeños diamantes talla brillante sobre un diseño floral articulado en oro blanco de 18K.",
                price = 1950.0,
                imageUrl = "img_earrings",
                category = "Aros",
                reference = "REF-NIC-004",
                material = "Oro Blanco 18K"
            )
        )
        for (product in initialList) {
            repository.insertProduct(product)
        }
    }

    // Factory Class
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JoyasViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return JoyasViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
