package com.rostry.prototype.data.repo

import com.rostry.prototype.data.local.dao.UserDao
import com.rostry.prototype.data.local.entity.UserEntity
import com.rostry.prototype.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun saveUser(user: User): Result<Unit> = runCatching {
        userDao.upsert(user.toEntity().copy(dirty = true))
    }

    fun getUser(userId: Long): Flow<User?> =
        userDao.observeById(userId).map { it?.toDomain() }

    suspend fun updateFarmName(userId: Long, farmName: String): Result<Unit> = runCatching {
        val entity = userDao.getById(userId) ?: throw Exception("User not found")
        userDao.upsert(entity.copy(farmName = farmName, dirty = true))
    }

    private fun User.toEntity() = UserEntity(
        userId = userId,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl,
        farmName = farmName,
        userType = userType,
        dirty = dirty
    )

    private fun UserEntity.toDomain() = User(
        userId = userId,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl,
        farmName = farmName,
        userType = userType,
        dirty = dirty
    )
}
