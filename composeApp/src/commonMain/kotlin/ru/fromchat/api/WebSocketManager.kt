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
import ru.fromchat.WS_API_HOST
import ru.fromchat.core.Logger
import kotlin.concurrent.Volatile
import kotlin.coroutines.suspendCoroutine

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

    fun connect() {
        Logger.d("WebSocketManager", "Connecting to WebSocket")
        if (connecting) return
        connecting = true

        scope.launch {
            while (isActive) {
                try {
                    ApiClient.http.webSocket(
                        method = HttpMethod.Get,
                        request = {
                            url("$WS_API_HOST/api/chat/ws")
                        }
                    ) {
                        session = this
                        connecting = false

                        Logger.d("WebSocketManager", "WebSocket connected")
                        for (frame in incoming) {
                            val text = (frame as? Frame.Text)?.readText() ?: continue
                            Logger.d("WebSocketManager", "Received payload: $text")
                            try {
                                val msg = json.decodeFromString<WebSocketMessage>(text)
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
        val session = session
        if (session != null) {
            try {
                session.send(Frame.Text(json.encodeToString(message)))
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