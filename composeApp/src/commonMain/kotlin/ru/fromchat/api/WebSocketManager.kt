package ru.fromchat.api

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.fromchat.core.Logger
import ru.fromchat.core.config.Config
import kotlin.concurrent.Volatile
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object WebSocketManager {
    // Config
    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    private val _messages = MutableSharedFlow<WebSocketMessage>(replay = 0, extraBufferCapacity = 64)
    val messages = _messages.asSharedFlow()

    private val globalHandlers = mutableListOf<((WebSocketMessage) -> Unit)>()

    fun addGlobalMessageHandler(handler: ((WebSocketMessage) -> Unit)) {
        globalHandlers += handler
    }

    fun removeGlobalMessageHandler(handler: ((WebSocketMessage) -> Unit)) {
        globalHandlers -= handler
    }

    // State
    @Volatile
    private var connecting: Boolean = false
    @Volatile private var session: DefaultClientWebSocketSession? = null

    /**
     * Check if WebSocket is connected
     */
    fun isConnected(): Boolean = session != null

    /**
     * Wait for WebSocket connection with timeout
     */
    @OptIn(ExperimentalTime::class)
    suspend fun waitForConnection(timeoutMs: Long = 10000): Boolean {
        if (session != null) return true
        val startTime = Clock.System.now().toEpochMilliseconds()
        while (session == null && (Clock.System.now().toEpochMilliseconds() - startTime) < timeoutMs) {
            delay(100)
        }
        return session != null
    }

    fun connect() {
        Logger.d("WebSocketManager", "Connecting to WebSocket")
        if (connecting) return
        connecting = true

        scope.launch {
            while (isActive) {
                try {
                    val wsUrl = Config.getWebSocketUrl()
                    Logger.d("WebSocketManager", "Connecting to: $wsUrl")
                    ApiClient.http.webSocket(
                        method = HttpMethod.Get,
                        request = {
                            url(wsUrl)
                        }
                    ) {
                        session = this
                        connecting = false

                        Logger.d("WebSocketManager", "WebSocket connected")
                        for (frame in incoming) {
                            val text = (frame as? Frame.Text)?.readText() ?: continue
                            Logger.d("WebSocketManager", "Received payload: $text")
                            try {
                                // Check if this is an "updates" message - it has a different structure
                                val jsonTree = json.parseToJsonElement(text)
                                val messageType = jsonTree.jsonObject["type"]?.jsonPrimitive?.content
                                
                                val msg = if (messageType == "updates") {
                                    // Wrap in WebSocketMessage with the entire JSON tree as data
                                    WebSocketMessage(
                                        type = "updates",
                                        data = jsonTree
                                    )
                                } else {
                                    // Parse as regular WebSocketMessage
                                    json.decodeFromString<WebSocketMessage>(text)
                                }
                                
                                globalHandlers.forEach { it(msg) }
                                _messages.emit(msg)
                            } catch (e: Throwable) {
                                Logger.w("WebSocketManager", "Received malformed payload: ${e.message}", e)
                                // ignore malformed
                            }
                        }
                    }
                } catch (e: Throwable) {
                    Logger.w("WebSocketManager", "An error occurred: ${e.message}", e)
                    connecting = false
                    session = null
                    delay(3000)
                } finally {
                    Logger.w("WebSocketManager", "WebSocket disconnected")
                    session = null
                    connecting = false
                }
            }
        }
    }

    suspend fun send(message: WebSocketMessage) {
        // Wait for connection if not connected yet
        if (session == null) {
            if (!waitForConnection(5000)) {
                Logger.w("WebSocketManager", "Cannot send message: no active session after waiting")
                throw IllegalStateException("No active WebSocket session")
            }
        }
        
        val currentSession = session
        if (currentSession != null) {
            try {
                currentSession.send(Frame.Text(json.encodeToString(message)))
            } catch (e: Exception) {
                Logger.e("WebSocketManager", "Failed to send message: ${e.message}", e)
                throw e
            }
        } else {
            Logger.w("WebSocketManager", "Cannot send message: no active session")
            throw IllegalStateException("No active WebSocket session")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun request(message: WebSocketMessage, timeoutMs: Long = 10_000): WebSocketMessage? {
        Logger.d("WebSocketManager", "WebSocket request: $message")
        var handler: ((WebSocketMessage) -> Unit)? = null

        return try {
            // Check if we have a valid session before sending
            if (session == null) {
                Logger.w("WebSocketManager", "No active WebSocket session")
                return null
            }
            
            send(message)
            withTimeout(timeoutMs) {
                suspendCoroutine { continuation ->
                    handler = { response ->
                        // Only process responses that match our request type
                        if (response.type == message.type) {
                            continuation.resumeWith(Result.success(response))
                            removeGlobalMessageHandler(handler!!)
                        }
                    }
                    addGlobalMessageHandler(handler)
                }
            }
        } catch (_: TimeoutCancellationException) {
            Logger.w("WebSocketManager", "Request timed out")
            null
        } catch (e: Exception) {
            Logger.e("WebSocketManager", "Request failed: ${e.message}", e)
            null
        } finally {
            handler?.let { removeGlobalMessageHandler(it) }
        }
    }

    fun shutdown() {
        scope.cancel()
    }
}