package com.example.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allReservations: Flow<List<Reservation>> = productDao.getAllReservations()

    fun getProductsByCategory(category: String): Flow<List<Product>> = 
        productDao.getProductsByCategory(category)

    suspend fun getProductById(id: Int): Product? = 
        productDao.getProductById(id)

    suspend fun insertProduct(product: Product) = 
        productDao.insertProduct(product)

    suspend fun updateProduct(product: Product) = 
        productDao.updateProduct(product)

    suspend fun deleteProduct(product: Product) = 
        productDao.deleteProduct(product)

    suspend fun deleteProductById(id: Int) = 
        productDao.deleteProductById(id)

    suspend fun insertReservation(reservation: Reservation) = 
        productDao.insertReservation(reservation)

    suspend fun deleteReservationById(id: Int) = 
        productDao.deleteReservationById(id)
}
