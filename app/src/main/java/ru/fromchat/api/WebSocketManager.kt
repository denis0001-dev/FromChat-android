package ru.fromchat.api

import android.util.Log
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.http.encodedPath
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import ru.fromchat.API_HOST
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
    @Volatile private var connecting: Boolean = false
    @Volatile private var session: DefaultClientWebSocketSession? = null

    fun connect() {
        Log.d("WebSocketManager", "Connecting to WebSocket")
        if (connecting) return
        connecting = true

        scope.launch {
            while (isActive) {
                try {
                    ApiClient.http.webSocket(
                        method = HttpMethod.Get,
                        host = API_HOST,
                        request = {
                            url {
                                protocol = URLProtocol.WSS
                                host = "fromchat.ru"
                                encodedPath = "/api/chat/ws"
                            }
                        }
                    ) {
                        session = this

                        Log.d("WebSocketManager", "WebSocket connected")
                        for (frame in incoming) {
                            val text = (frame as? Frame.Text)?.readText() ?: continue
                            Log.d("WebSocketManager", "Received payload: $text")
                            try {
                                val msg = json.decodeFromString<WebSocketMessage>(text)
                                globalHandlers.forEach { it(msg) }
                                _messages.emit(msg)
                            } catch (e: Throwable) {
                                Log.w("WebSocketManager", "Received malformed payload:", e)
                                // ignore malformed
                            }
                        }
                    }
                } catch (e: Throwable) {
                    Log.w("WebSocketManager", "An error occurred:", e)
                    delay(3000)
                } finally {
                    Log.w("WebSocketManager", "WebSocket disconnected")
                    session = null
                }
            }
        }
    }

    suspend fun send(message: WebSocketMessage) {
        session?.send(Frame.Text(json.encodeToString(message)))
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun request(message: WebSocketMessage, timeoutMs: Long = 10_000): WebSocketMessage? {
        Log.d("WebSocketManager", "WebSocket request: $message")
        var handler: ((WebSocketMessage) -> Unit)? = null

        return try {
            send(message)
            withTimeout(timeoutMs) {
                suspendCoroutine { continuation ->
                    handler = {
                        continuation.resumeWith(Result.success(it))
                        removeGlobalMessageHandler(handler!!)
                    }
                    addGlobalMessageHandler(handler)
                }
            }
        } catch (_: TimeoutCancellationException) {
            Log.w("WebSocketManager", "Request timed out")
            null
        } finally {
            handler?.let { removeGlobalMessageHandler(it) }
        }
    }

    fun shutdown() {
        scope.cancel()
    }
}