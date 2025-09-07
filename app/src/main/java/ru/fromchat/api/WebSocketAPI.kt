package ru.fromchat.api

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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import ru.fromchat.API_HOST

object WebSocketManager {
    // Config
    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    private val _messages = MutableSharedFlow<WebSocketMessage>(replay = 0, extraBufferCapacity = 64)
    val messages = _messages.asSharedFlow()

    @Volatile
    private var globalHandler: ((WebSocketMessage) -> Unit)? = null

    fun setGlobalMessageHandler(handler: ((WebSocketMessage) -> Unit)?) {
        globalHandler = handler
    }

    // State
    @Volatile private var connecting: Boolean = false
    @Volatile private var session: DefaultClientWebSocketSession? = null

    fun connect() {
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

                        for (frame in incoming) {
                            val text = (frame as? Frame.Text)?.readText() ?: continue
                            try {
                                val msg = json.decodeFromString(WebSocketMessage.serializer(), text)
                                globalHandler?.invoke(msg)
                                _messages.emit(msg)
                            } catch (_: Throwable) {
                                // ignore malformed
                            }
                        }
                    }
                } catch (_: Throwable) {
                    delay(3000)
                } finally {
                    session = null
                }
            }
        }
    }

    suspend fun send(message: WebSocketMessage) {
        val payload = json.encodeToString(WebSocketMessage.serializer(), message)
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
            send(Frame.Text(payload))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun request(message: WebSocketMessage, timeoutMs: Long = 10_000): WebSocketMessage? {
        val ch = Channel<WebSocketMessage>(capacity = 1)
        val handler: (WebSocketMessage) -> Unit = {
            ch.trySend(it)
        }
        setGlobalMessageHandler(handler)

        return try {
            send(message)
            withTimeout(timeoutMs) { ch.receive() }
        } catch (_: TimeoutCancellationException) {
            null
        } finally {
            setGlobalMessageHandler(null)
            ch.close()
        }
    }

    fun shutdown() {
        scope.cancel()
    }
}