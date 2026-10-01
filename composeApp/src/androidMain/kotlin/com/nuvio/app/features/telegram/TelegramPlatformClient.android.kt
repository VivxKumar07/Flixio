package com.nuvio.app.features.telegram

import android.content.Context
import io.github.up9cloud.td.JsonClient
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal actual object TelegramPlatformClient {
    private var appContext: Context? = null
    private val serverStarted = AtomicBoolean(false)
    private var serverPort: Int = 0
    private var clientId: Int = 0
    private val waiters = ConcurrentHashMap<String, (String) -> Unit>()
    private val registeredFiles = ConcurrentHashMap<Int, RegisteredTelegramFile>()
    private var filesDir: File? = null
    private var databaseDir: File? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    actual val isSupported: Boolean get() = try {
        JsonClient.isLoaded()
    } catch (_: Throwable) {
        false
    }

    actual fun start(apiId: Int, apiHash: String, appVersion: String): Boolean {
        if (!isSupported) return false
        if (apiId <= 0 || apiHash.isBlank()) return false

        try {
            if (clientId == 0) {
                clientId = JsonClient.td_create_client_id()
            }
            startReceiveLoop()
            ensureServer()

            val baseDir = appContext?.let { File(it.filesDir, "telegram") }
                ?: File(System.getProperty("java.io.tmpdir") ?: ".", "flixio_telegram")
            databaseDir = File(baseDir, "db").apply { mkdirs() }
            filesDir = File(baseDir, "files").apply { mkdirs() }

            val setParams = buildJsonObject {
                put("@type", "setTdlibParameters")
                put("use_test_dc", false)
                put("database_directory", databaseDir?.absolutePath ?: "")
                put("files_directory", filesDir?.absolutePath ?: "")
                put("database_encryption_key", "")
                put("use_file_database", true)
                put("use_chat_info_database", true)
                put("use_message_database", true)
                put("use_secret_chats", false)
                put("api_id", apiId)
                put("api_hash", apiHash)
                put("system_language_code", "en")
                put("device_model", "Android")
                put("system_version", "Android")
                put("application_version", appVersion)
            }
            val res = request(setParams.toString(), timeoutSeconds = 20.0)
            return res != null
        } catch (_: Throwable) {
            return false
        }
    }

    actual fun restart(apiId: Int, apiHash: String, appVersion: String): Boolean {
        try {
            if (clientId != 0) {
                request(buildJsonObject { put("@type", "close") }.toString(), timeoutSeconds = 1.0)
            }
        } catch (_: Throwable) {}
        clientId = 0
        return start(apiId, apiHash, appVersion)
    }

    actual fun request(json: String, timeoutSeconds: Double): String? {
        if (!isSupported || clientId == 0) return null
        val token = UUID.randomUUID().toString()
        val modifiedJson = try {
            val element = Json.parseToJsonElement(json).jsonObject.toMutableMap()
            element["@extra"] = kotlinx.serialization.json.JsonPrimitive(token)
            JsonObject(element).toString()
        } catch (_: Throwable) {
            json
        }

        val latch = CountDownLatch(1)
        var result: String? = null
        waiters[token] = { response ->
            result = response
            latch.countDown()
        }

        try {
            JsonClient.td_send(clientId, modifiedJson)
            latch.await((timeoutSeconds * 1000).toLong(), TimeUnit.MILLISECONDS)
        } finally {
            waiters.remove(token)
        }
        return result
    }

    actual fun playbackUrl(fileId: Int, fileSize: Long, fileName: String, mimeType: String?): String? {
        if (!isSupported || fileId <= 0 || fileSize <= 0) return null
        val port = ensureServer() ?: return null
        registeredFiles[fileId] = RegisteredTelegramFile(
            fileId = fileId,
            size = fileSize,
            fileName = fileName,
            mimeType = mimeType ?: "video/mp4",
        )
        val encodedName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20")
        return "http://127.0.0.1:$port/telegram/$fileId/$encodedName"
    }

    actual fun cacheSizeBytes(): Long {
        return filesDir?.let { dir ->
            dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        } ?: 0L
    }

    actual fun clearCache() {
        filesDir?.listFiles()?.forEach { it.deleteRecursively() }
    }

    private fun startReceiveLoop() {
        thread(name = "telegram-receive", isDaemon = true) {
            while (isSupported) {
                try {
                    val raw = JsonClient.td_receive(1.0) ?: continue
                    val obj = try { Json.parseToJsonElement(raw).jsonObject } catch (_: Throwable) { null }
                    val extra = obj?.get("@extra")?.jsonPrimitive?.content
                    if (extra != null) {
                        waiters[extra]?.invoke(raw)
                    }
                } catch (_: Throwable) {
                    Thread.sleep(500)
                }
            }
        }
    }

    private fun ensureServer(): Int? {
        if (serverStarted.get()) return serverPort
        return try {
            val serverSocket = ServerSocket(0)
            serverPort = serverSocket.localPort
            serverStarted.set(true)
            thread(name = "telegram-http", isDaemon = true) {
                while (serverStarted.get()) {
                    try {
                        val client = serverSocket.accept()
                        thread(name = "telegram-client", isDaemon = true) {
                            handleClient(client)
                        }
                    } catch (_: Throwable) {
                        Thread.sleep(100)
                    }
                }
            }
            serverPort
        } catch (_: Throwable) {
            null
        }
    }

    private fun handleClient(client: Socket) {
        client.use { socket ->
            val input = socket.getInputStream()
            val output = socket.getOutputStream()
            val requestLine = input.readLine() ?: return
            val parts = requestLine.split(" ")
            if (parts.size < 2 || parts[0] != "GET") {
                output.write("HTTP/1.1 405 Method Not Allowed\r\n\r\n".toByteArray())
                return
            }
            val uri = parts[1]
            val segments = uri.trimStart('/').split('/')
            if (segments.size < 3 || segments[0] != "telegram") {
                output.write("HTTP/1.1 404 Not Found\r\n\r\n".toByteArray())
                return
            }
            val fileId = segments[1].toIntOrNull() ?: run {
                output.write("HTTP/1.1 400 Bad Request\r\n\r\n".toByteArray())
                return
            }
            val regFile = registeredFiles[fileId] ?: run {
                output.write("HTTP/1.1 404 Not Found\r\n\r\n".toByteArray())
                return
            }

            var rangeHeader: String? = null
            while (true) {
                val line = input.readLine() ?: break
                if (line.isEmpty()) break
                if (line.startsWith("Range:", ignoreCase = true)) {
                    rangeHeader = line.substringAfter(':').trim()
                }
            }

            val totalSize = regFile.size
            var start = 0L
            var end = totalSize - 1
            var isPartial = false

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                val spec = rangeHeader.removePrefix("bytes=").trim()
                val dash = spec.indexOf('-')
                if (dash != -1) {
                    val startStr = spec.substring(0, dash)
                    val endStr = spec.substring(dash + 1)
                    start = startStr.toLongOrNull() ?: 0L
                    end = endStr.toLongOrNull() ?: (totalSize - 1)
                    if (end >= totalSize) end = totalSize - 1
                    isPartial = true
                }
            }

            val contentLength = (end - start + 1).coerceAtLeast(0)
            val status = if (isPartial) "206 Partial Content" else "200 OK"
            val header = buildString {
                append("HTTP/1.1 $status\r\n")
                append("Content-Type: ${regFile.mimeType}\r\n")
                append("Accept-Ranges: bytes\r\n")
                append("Content-Length: $contentLength\r\n")
                if (isPartial) {
                    append("Content-Range: bytes $start-$end/$totalSize\r\n")
                }
                append("Connection: keep-alive\r\n\r\n")
            }
            output.write(header.toByteArray())
            output.flush()

            val localFile = filesDir?.let { File(it, regFile.fileName) }
            if (localFile != null && localFile.exists()) {
                localFile.inputStream().use { fileIn ->
                    fileIn.skip(start)
                    val buffer = ByteArray(64 * 1024)
                    var bytesRemaining = contentLength
                    while (bytesRemaining > 0) {
                        val read = fileIn.read(buffer, 0, minOf(buffer.size.toLong(), bytesRemaining).toInt())
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        bytesRemaining -= read
                    }
                    output.flush()
                }
            }
        }
    }

    private fun InputStream.readLine(): String? {
        val sb = StringBuilder()
        while (true) {
            val c = read()
            if (c == -1) return if (sb.isEmpty()) null else sb.toString()
            if (c == '\n'.code) return sb.toString().trimEnd('\r')
            sb.append(c.toChar())
        }
    }

    private data class RegisteredTelegramFile(
        val fileId: Int,
        val size: Long,
        val fileName: String,
        val mimeType: String,
    )
}
