package com.rostry.prototype.domain.model

data class Product(
    val productId: Long,
    val name: String,
    val breed: String,
    val price: Double,
    val imageUrl: String,
    val sellerName: String
)
