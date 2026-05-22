package com.rostry.prototype.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.rostry.prototype.data.connectivity.ConnectivityObserver
import com.rostry.prototype.data.local.AppDatabase
import com.rostry.prototype.data.local.dao.DailyLogDao
import com.rostry.prototype.data.local.dao.FarmAssetDao
import com.rostry.prototype.data.local.dao.OutboxDao
import com.rostry.prototype.data.local.dao.UserDao
import com.rostry.prototype.data.repo.FarmRepository
import com.rostry.prototype.data.repo.SyncRepository
import com.rostry.prototype.data.repo.UserRepository
import com.rostry.prototype.telegram.TelegramApi
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

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository =
        UserRepository(userDao)

    @Provides
    @Singleton
    fun provideFarmRepository(
        farmAssetDao: FarmAssetDao,
        dailyLogDao: DailyLogDao,
        outboxDao: OutboxDao,
        gson: Gson
    ): FarmRepository = FarmRepository(farmAssetDao, dailyLogDao, outboxDao, gson)

    @Provides
    @Singleton
    fun provideSyncRepository(
        outboxDao: OutboxDao,
        farmAssetDao: FarmAssetDao,
        dailyLogDao: DailyLogDao,
        telegramApi: TelegramApi,
        firestore: FirebaseFirestore,
        gson: Gson
    ): SyncRepository = SyncRepository(
        outboxDao, farmAssetDao, dailyLogDao, telegramApi, firestore, gson
    )

    @Provides
    @Singleton
    fun provideConnectivityObserver(
        @ApplicationContext context: Context,
        syncRepository: SyncRepository
    ): ConnectivityObserver = ConnectivityObserver(context, syncRepository)
}
