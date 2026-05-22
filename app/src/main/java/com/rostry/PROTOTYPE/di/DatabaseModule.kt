package com.rostry.prototype.di

import android.content.Context
import androidx.room.Room
import com.rostry.prototype.data.local.AppDatabase
import com.rostry.prototype.data.local.dao.DailyLogDao
import com.rostry.prototype.data.local.dao.FarmAssetDao
import com.rostry.prototype.data.local.dao.OutboxDao
import com.rostry.prototype.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// SQLCipher: for production, replace Room.databaseBuilder with
// SupportFactory(passphrase) via openHelperFactory
// Requires: implementation "net.zetetic:android-database-sqlcipher:4.5.6"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "rostry_db"
    ).build()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideFarmAssetDao(db: AppDatabase): FarmAssetDao = db.farmAssetDao()

    @Provides
    fun provideDailyLogDao(db: AppDatabase): DailyLogDao = db.dailyLogDao()

    @Provides
    fun provideOutboxDao(db: AppDatabase): OutboxDao = db.outboxDao()
}
