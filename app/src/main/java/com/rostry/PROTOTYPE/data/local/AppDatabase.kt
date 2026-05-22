package com.rostry.prototype.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    companion object {
        // Stub migration: version 1 -> 2
        // val MIGRATION_1_2 = object : Migration(1, 2) {
        //     override fun migrate(db: SupportSQLiteDatabase) {
        //         db.execSQL("ALTER TABLE users ADD COLUMN new_field TEXT NOT NULL DEFAULT ''")
        //     }
        // }
    }
}
