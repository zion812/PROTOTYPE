package com.rostry.prototype.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rostry.prototype.data.local.dao.DailyLogDao
import com.rostry.prototype.data.local.dao.FarmAssetDao
import com.rostry.prototype.data.local.dao.OutboxDao
import com.rostry.prototype.data.local.dao.UserDao
import com.rostry.prototype.data.local.entity.DailyLogEntity
import com.rostry.prototype.data.local.entity.FarmAssetEntity
import com.rostry.prototype.data.local.entity.OutboxEntity
import com.rostry.prototype.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, FarmAssetEntity::class, DailyLogEntity::class, OutboxEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun farmAssetDao(): FarmAssetDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun outboxDao(): OutboxDao
}
