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

    private val allProducts = mockProducts

    private val _products = MutableStateFlow(allProducts)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    fun filterProducts(breed: String) {
        _selectedFilter.value = breed
        _products.value = if (breed == "All") allProducts
        else allProducts.filter { it.breed == breed }
    }
}

private val mockProducts = listOf(
    Product(
        productId = 1,
        name = "Premium Aseel Pair",
        breed = "Aseel",
        price = 2500.0,
        imageUrl = "https://images.unsplash.com/photo-1548550023-2bdb3c5beed7?w=400&h=300&fit=crop",
        sellerName = "Rostry Farm"
    ),
    Product(
        productId = 2,
        name = "Kadaknath Chicks (5)",
        breed = "Kadaknath",
        price = 1200.0,
        imageUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=400&h=300&fit=crop",
        sellerName = "Desi Poultry"
    ),
    Product(
        productId = 3,
        name = "Golden Kadaknath Rooster",
        breed = "Kadaknath",
        price = 1800.0,
        imageUrl = "https://images.unsplash.com/photo-1566492031773-4f4a4464ce7b?w=400&h=300&fit=crop",
        sellerName = "Heritage Birds"
    ),
    Product(
        productId = 4,
        name = "White Leghorn Layers (10)",
        breed = "Layers",
        price = 2200.0,
        imageUrl = "https://images.unsplash.com/photo-1576577303331-43cd43b7a29c?w=400&h=300&fit=crop",
        sellerName = "EggMaster Farms"
    ),
    Product(
        productId = 5,
        name = "Aseel Hen (Pure Breed)",
        breed = "Aseel",
        price = 1500.0,
        imageUrl = "https://images.unsplash.com/photo-1612170153139-6f881ff0672f?w=400&h=300&fit=crop",
        sellerName = "Rostry Farm"
    ),
    Product(
        productId = 6,
        name = "Rhode Island Red Layers",
        breed = "Layers",
        price = 2800.0,
        imageUrl = "https://images.unsplash.com/photo-1567690187648-ccf56b4a99f8?w=400&h=300&fit=crop",
        sellerName = "GreenField Poultry"
    ),
    Product(
        productId = 7,
        name = "Kadaknath Pair (M+F)",
        breed = "Kadaknath",
        price = 3200.0,
        imageUrl = "https://images.unsplash.com/photo-1598550878484-c1374e9e5241?w=400&h=300&fit=crop",
        sellerName = "Desi Poultry"
    ),
    Product(
        productId = 8,
        name = "Aseel Chicks (10)",
        breed = "Aseel",
        price = 1800.0,
        imageUrl = "https://images.unsplash.com/photo-1604848698032-2592f46e0eb8?w=400&h=300&fit=crop",
        sellerName = "Heritage Birds"
    ),
    Product(
        productId = 9,
        name = "Hybrid Layer Pack (20)",
        breed = "Layers",
        price = 4500.0,
        imageUrl = "https://images.unsplash.com/photo-1548550023-2bdb3c5beed7?w=400&h=300&fit=crop",
        sellerName = "EggMaster Farms"
    ),
    Product(
        productId = 10,
        name = "Aseel Rooster (Show Grade)",
        breed = "Aseel",
        price = 3500.0,
        imageUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=400&h=300&fit=crop",
        sellerName = "Rostry Farm"
    )
)
