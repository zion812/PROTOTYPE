package com.rostry.prototype.ui.market

import androidx.lifecycle.ViewModel
import com.rostry.prototype.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MarketplaceViewModel @Inject constructor() : ViewModel() {

    private val _products = MutableStateFlow(mockProducts)
    val products: StateFlow<List<Product>> = _products.asStateFlow()
}

private val mockProducts = listOf(
    Product(
        productId = 1,
        name = "Aseel Pair (1M+1F)",
        breed = "Aseel",
        price = 2500.0,
        imageUrl = "https://images.unsplash.com/photo-1548550023-2bdb3c5beed7?w=400&h=300&fit=crop",
        sellerName = "Rostry Farm",
        phoneNumber = "919876543210"
    ),
    Product(
        productId = 2,
        name = "Kadaknath Chicks (5)",
        breed = "Kadaknath",
        price = 1200.0,
        imageUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=400&h=300&fit=crop",
        sellerName = "Desi Poultry",
        phoneNumber = "919812345678"
    ),
    Product(
        productId = 3,
        name = "Golden Kadaknath Rooster",
        breed = "Kadaknath",
        price = 1800.0,
        imageUrl = "https://images.unsplash.com/photo-1566492031773-4f4a4464ce7b?w=400&h=300&fit=crop",
        sellerName = "Heritage Birds",
        phoneNumber = "918765432109"
    ),
    Product(
        productId = 4,
        name = "White Leghorn Layers (10)",
        breed = "Layer",
        price = 2200.0,
        imageUrl = "https://images.unsplash.com/photo-1576577303331-43cd43b7a29c?w=400&h=300&fit=crop",
        sellerName = "EggMaster Farms",
        phoneNumber = "919999888777"
    ),
    Product(
        productId = 5,
        name = "Aseel Hen Pure Breed",
        breed = "Aseel",
        price = 1500.0,
        imageUrl = "https://images.unsplash.com/photo-1612170153139-6f881ff0672f?w=400&h=300&fit=crop",
        sellerName = "Rostry Farm",
        phoneNumber = "919876543210"
    ),
    Product(
        productId = 6,
        name = "Broiler Chicks (50)",
        breed = "Broiler",
        price = 3500.0,
        imageUrl = "https://images.unsplash.com/photo-1567690187648-ccf56b4a99f8?w=400&h=300&fit=crop",
        sellerName = "GreenField Poultry",
        phoneNumber = "917788995566"
    )
)
