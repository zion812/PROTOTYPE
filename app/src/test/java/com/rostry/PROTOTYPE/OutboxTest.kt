package com.rostry.prototype

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.rostry.prototype.data.local.AppDatabase
import com.rostry.prototype.data.local.dao.DailyLogDao
import com.rostry.prototype.data.local.dao.FarmAssetDao
import com.rostry.prototype.data.local.dao.OutboxDao
import com.rostry.prototype.data.local.entity.DailyLogEntity
import com.rostry.prototype.data.local.entity.OutboxEntity
import com.rostry.prototype.data.repo.FarmRepository
import com.rostry.prototype.domain.model.ENTITY_TYPE_DAILY_LOG
import com.rostry.prototype.domain.model.STATUS_PENDING
import com.rostry.prototype.sync.SyncManager
import com.rostry.prototype.telegram.TelegramApi
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@RunWith(RobolectricTestRunner::class)
class OutboxTest {

    private lateinit var db: AppDatabase
    private lateinit var dailyLogDao: DailyLogDao
    private lateinit var farmAssetDao: FarmAssetDao
    private lateinit var outboxDao: OutboxDao
    private lateinit var mockTelegramApi: TelegramApi
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var gson: Gson
    private lateinit var farmRepository: FarmRepository
    private lateinit var syncManager: SyncManager

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        dailyLogDao = db.dailyLogDao()
        farmAssetDao = db.farmAssetDao()
        outboxDao = db.outboxDao()
        gson = Gson()

        mockTelegramApi = mockk(relaxed = true)
        mockFirestore = mockk()

        farmRepository = FarmRepository(
            farmAssetDao = farmAssetDao,
            dailyLogDao = dailyLogDao,
            outboxDao = outboxDao,
            gson = gson
        )

        syncManager = SyncManager(
            outboxDao = outboxDao,
            farmAssetDao = farmAssetDao,
            dailyLogDao = dailyLogDao,
            telegramApi = mockTelegramApi,
            firestore = mockFirestore,
            gson = gson
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun getOutboxStatus(outboxId: String): String? {
        val cursor = db.openHelper.writableDatabase
            .query("SELECT status FROM outbox WHERE outboxId = ?", arrayOf(outboxId))
        cursor.use {
            return if (it.moveToFirst()) it.getString(0) else null
        }
    }

    private fun mockSuccessfulFirestore() {
        val mockCollection = mockk<CollectionReference>()
        val mockDocument = mockk<DocumentReference>()

        every { mockFirestore.collection(any()) } returns mockCollection
        every { mockCollection.document(any()) } returns mockDocument
        every { mockDocument.set(any<Any>()) } returns Tasks.forResult(null)
    }

    private fun mockFailingFirestore() {
        val mockCollection = mockk<CollectionReference>()
        val mockDocument = mockk<DocumentReference>()

        every { mockFirestore.collection(any()) } returns mockCollection
        every { mockCollection.document(any()) } returns mockDocument
        every { mockDocument.set(any<Any>()) } returns Tasks.forException<Void>(
            IOException("Firestore unavailable")
        )
    }

    @Test
    fun `createDailyLog inserts both DailyLogEntity and OutboxEntity`() = runTest {
        val log = DailyLogEntity(
            farmerId = "farmer1",
            assetId = "asset1",
            logDate = 1000L,
            feedKg = 5.0,
            mortalityCount = 1,
            photoUrl = null,
            notes = "Test daily log",
            createdAt = 1000L
        )

        farmRepository.createDailyLog(log)

        val savedLogs = dailyLogDao.getByFarmer("farmer1").first()
        assertEquals(1, savedLogs.size)
        assertEquals(log.logId, savedLogs[0].logId)
        assertEquals(5.0, savedLogs[0].feedKg ?: 0.0, 0.0)
        assertEquals(1, savedLogs[0].mortalityCount)
        assertTrue(savedLogs[0].dirty)

        val pendingItems = outboxDao.getPending()
        assertEquals(1, pendingItems.size)
        assertEquals(ENTITY_TYPE_DAILY_LOG, pendingItems[0].entityType)
        assertEquals(log.logId, pendingItems[0].entityId)
        assertEquals(STATUS_PENDING, pendingItems[0].status)

        val deserialized = gson.fromJson(pendingItems[0].payloadJson, DailyLogEntity::class.java)
        assertEquals(log.logId, deserialized.logId)
        assertEquals(5.0, deserialized.feedKg ?: 0.0, 0.0)
    }

    @Test
    fun `syncAll marks outbox COMPLETED after successful Firestore push`() = runTest {
        val entity = DailyLogEntity(
            farmerId = "farmer1",
            logDate = 2000L,
            createdAt = 2000L,
            notes = "Online entry"
        )
        dailyLogDao.upsert(entity)

        val payload = gson.toJson(entity)
        val outboxItem = OutboxEntity(
            entityType = ENTITY_TYPE_DAILY_LOG,
            entityId = entity.logId,
            payloadJson = payload,
            createdAt = 2000L
        )
        outboxDao.insert(outboxItem)

        mockSuccessfulFirestore()

        syncManager.syncAll()

        assertEquals("COMPLETED", getOutboxStatus(outboxItem.outboxId))

        val savedLogs = dailyLogDao.getByFarmer("farmer1").first()
        assertEquals(1, savedLogs.size)
        assertFalse(savedLogs[0].dirty)
    }

    @Test
    fun `syncAll marks outbox FAILED when Firestore push fails`() = runTest {
        val entity = DailyLogEntity(
            farmerId = "farmer1",
            logDate = 3000L,
            createdAt = 3000L
        )
        dailyLogDao.upsert(entity)

        val payload = gson.toJson(entity)
        val outboxItem = OutboxEntity(
            entityType = ENTITY_TYPE_DAILY_LOG,
            entityId = entity.logId,
            payloadJson = payload,
            createdAt = 3000L
        )
        outboxDao.insert(outboxItem)

        mockFailingFirestore()

        syncManager.syncAll()

        assertEquals("FAILED", getOutboxStatus(outboxItem.outboxId))

        val savedLogs = dailyLogDao.getByFarmer("farmer1").first()
        assertEquals(1, savedLogs.size)
        assertTrue(savedLogs[0].dirty)
    }

    @Test
    fun `syncAll skips photo upload when photoUrl is already tg`() = runTest {
        val entity = DailyLogEntity(
            farmerId = "farmer1",
            logDate = 4000L,
            createdAt = 4000L,
            photoUrl = "tg://fileId@channelId"
        )
        dailyLogDao.upsert(entity)

        val payload = gson.toJson(entity)
        val outboxItem = OutboxEntity(
            entityType = ENTITY_TYPE_DAILY_LOG,
            entityId = entity.logId,
            payloadJson = payload,
            createdAt = 4000L
        )
        outboxDao.insert(outboxItem)

        mockSuccessfulFirestore()

        syncManager.syncAll()

        coVerify(exactly = 0) { mockTelegramApi.uploadPhoto(any(), any()) }

        assertEquals("COMPLETED", getOutboxStatus(outboxItem.outboxId))

        val savedLogs = dailyLogDao.getByFarmer("farmer1").first()
        assertEquals("tg://fileId@channelId", savedLogs[0].photoUrl)
        assertFalse(savedLogs[0].dirty)
    }
}
