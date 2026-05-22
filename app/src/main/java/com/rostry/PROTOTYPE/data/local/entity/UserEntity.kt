package com.rostry.prototype.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val email: String,
    val photoUrl: String,
    val farmName: String,
    val userType: String = "FARMER",
    val createdAt: Long,
    val dirty: Boolean
)
