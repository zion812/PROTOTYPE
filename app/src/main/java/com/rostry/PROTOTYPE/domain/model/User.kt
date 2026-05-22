package com.rostry.prototype.domain.model

data class User(
    val userId: Long,
    val displayName: String,
    val email: String,
    val photoUrl: String,
    val farmName: String,
    val userType: String = "FARMER"
)
