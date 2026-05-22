package com.rostry.prototype.telegram

import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import okhttp3.Response
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@RunWith(RobolectricTestRunner::class)
class TelegramApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var telegramApi: TelegramApi
    private lateinit var interceptor: MockWebServerInterceptor

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        interceptor = MockWebServerInterceptor(mockWebServer)
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        telegramApi = TelegramApi(client)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `uploadPhoto returns tg uri on HTTP 200`() = runBlocking {
        val json = """
            {
                "ok": true,
                "result": {
                    "message_id": 100,
                    "chat": { "id": -100123, "type": "channel" },
                    "date": 1700000000,
                    "photo": [
                        { "file_id": "small_id", "file_unique_id": "s1", "width": 90, "height": 90, "file_size": 500 },
                        { "file_id": "large_id", "file_unique_id": "l1", "width": 800, "height": 600, "file_size": 5000 }
                    ]
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val photo = File.createTempFile("test_upload", ".jpg").apply { deleteOnExit() }
        val result = telegramApi.uploadPhoto("-100test_channel", photo)

        assertTrue("Expected success but got failure: ${result.exceptionOrNull()?.message}", result.isSuccess)
        assertEquals("tg://large_id@-100test_channel", result.getOrNull())
    }

    @Test
    fun `uploadPhoto returns failure on non-ok JSON response`() = runBlocking {
        val json = """
            {
                "ok": false,
                "error_code": 400,
                "description": "Bad Request: chat not found"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val photo = File.createTempFile("test_upload_fail", ".jpg").apply { deleteOnExit() }
        val result = telegramApi.uploadPhoto("-100invalid", photo)

        assertTrue("Expected failure but got success", result.isFailure)
        assertTrue("Expected IOException but got ${result.exceptionOrNull()}", result.exceptionOrNull() is IOException)
        assertEquals(
            "Telegram API error: Bad Request: chat not found",
            result.exceptionOrNull()!!.message
        )
    }

    @Test
    fun `resolveUrl returns full HTTPS URL on success`() = runBlocking {
        val json = """
            {
                "ok": true,
                "result": {
                    "file_id": "test_file_id",
                    "file_unique_id": "uniq1",
                    "file_size": 12345,
                    "file_path": "photos/test_photo.jpg"
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val result = telegramApi.resolveUrl("test_file_id")

        assertTrue("Expected success but got failure: ${result.exceptionOrNull()?.message}", result.isSuccess)
        val url = result.getOrNull()!!
        assertTrue(url.startsWith("https://api.telegram.org/file/bot"))
        assertTrue(url.endsWith("/photos/test_photo.jpg"))
    }

    @Test
    fun `resolveUrl returns failure on network timeout`() = runBlocking {
        val timeoutServer = MockWebServer()
        timeoutServer.start()
        timeoutServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                Thread.sleep(100)
                return MockResponse()
            }
        }

        val timeoutClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MILLISECONDS)
            .readTimeout(1, TimeUnit.MILLISECONDS)
            .writeTimeout(1, TimeUnit.MILLISECONDS)
            .addInterceptor(MockWebServerInterceptor(timeoutServer))
            .build()

        val timeoutApi = TelegramApi(timeoutClient)
        val result = timeoutApi.resolveUrl("timeout_file_id")

        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull() is SocketTimeoutException ||
                result.exceptionOrNull() is IOException
        )

        timeoutServer.shutdown()
    }

    private class MockWebServerInterceptor(
        private val server: MockWebServer
    ) : okhttp3.Interceptor {
        override fun intercept(chain: okhttp3.Interceptor.Chain): Response {
            val original = chain.request()
            val newUrl = original.url.newBuilder()
                .scheme("http")
                .host(server.hostName)
                .port(server.port)
                .build()
            return chain.proceed(original.newBuilder().url(newUrl).build())
        }
    }
}
