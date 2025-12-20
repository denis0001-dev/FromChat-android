package ru.fromchat.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import ru.fromchat.core.config.Config
import ru.fromchat.utils.failOnError
import kotlin.concurrent.Volatile
import kotlin.time.Duration.Companion.milliseconds

/**
 * Creates a platform-specific HTTP client that supports WebSockets
 * The config block is applied to configure plugins like WebSockets, JSON, etc.
 */
expect fun createPlatformHttpClient(
    block: io.ktor.client.HttpClientConfig<*>.() -> Unit = {}
): HttpClient

object ApiClient {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    val http = createPlatformHttpClient {
        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.INFO
        }

        install(WebSockets) {
            pingInterval = 5000.milliseconds // Send a ping every 5 seconds to keep the connection alive
        }
    }

    @Volatile
    var token: String? = null

    @Volatile
    var user: User? = null

    suspend fun login(request: LoginRequest) =
        http
            .post("${Config.apiBaseUrl}/login") {
                contentType(ContentType.Application.Json)
                setBody(request.also { ru.fromchat.core.Logger.d("ApiClient", "Login request: $it") })
            }
            .failOnError()
            .body<LoginResponse>()
            .also {
                token = it.token
                user = it.user
            }

    suspend fun register(request: RegisterRequest) =
        http
            .post("${Config.apiBaseUrl}/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            .failOnError()

    suspend fun getMessages(limit: Int = 50, beforeId: Int? = null) =
        http
            .get("${Config.apiBaseUrl}/get_messages") {
                contentType(ContentType.Application.Json)
                bearerAuth(token ?: throw IllegalStateException("Not authenticated"))
                parameter("limit", limit)
                beforeId?.let { parameter("before_id", it) }
            }
            .failOnError()
            .body<MessagesResponse>()

    suspend fun send(message: String) =
        http
            .post("${Config.apiBaseUrl}/send_message") {
                contentType(ContentType.Application.Json)
                bearerAuth(token!!)
                setBody(SendMessageRequest(message))
            }
            .failOnError()
            .body<SendMessageResponse>()


    suspend fun logout(authToken: String) {
        // Don't throw on logout errors, just try to logout
        try {
            http.get("${Config.apiBaseUrl}/logout") {
                bearerAuth(authToken)
            }
        } catch (e: Exception) {
            // Ignore logout errors
        }
    }

    // WebSocket send helpers
    suspend fun sendMessage(content: String, replyToId: Int? = null, clientMessageId: String? = null) {
        val token = token ?: throw IllegalStateException("Not authenticated")
        WebSocketManager.send(
            WebSocketMessage(
                type = "sendMessage",
                credentials = WebSocketCredentials(
                    scheme = "Bearer",
                    credentials = token
                ),
                data = json.encodeToJsonElement(
                    WebSocketSendMessageRequest(
                        content = content,
                        reply_to_id = replyToId,
                        client_message_id = clientMessageId
                    )
                )
            )
        )
    }

    suspend fun editMessage(messageId: Int, content: String) {
        val token = token ?: throw IllegalStateException("Not authenticated")
        WebSocketManager.send(
            WebSocketMessage(
                type = "editMessage",
                credentials = WebSocketCredentials(
                    scheme = "Bearer",
                    credentials = token
                ),
                data = json.encodeToJsonElement(
                    WebSocketEditMessageRequest(
                        message_id = messageId,
                        content = content
                    )
                )
            )
        )
    }

    suspend fun deleteMessage(messageId: Int) {
        val token = token ?: throw IllegalStateException("Not authenticated")
        WebSocketManager.send(
            WebSocketMessage(
                type = "deleteMessage",
                credentials = WebSocketCredentials(
                    scheme = "Bearer",
                    credentials = token
                ),
                data = json.encodeToJsonElement(
                    WebSocketDeleteMessageRequest(
                        message_id = messageId
                    )
                )
            )
        )
    }

    suspend fun sendTyping() {
        val token = token ?: throw IllegalStateException("Not authenticated")
        try {
            WebSocketManager.send(
                WebSocketMessage(
                    type = "typing",
                    credentials = WebSocketCredentials(
                        scheme = "Bearer",
                        credentials = token
                    )
                )
            )
        } catch (e: Exception) {
            // Silently ignore if WebSocket is not connected yet
            // Typing indicators are not critical
        }
    }

    suspend fun sendStopTyping() {
        val token = token ?: throw IllegalStateException("Not authenticated")
        try {
            WebSocketManager.send(
                WebSocketMessage(
                    type = "stopTyping",
                    credentials = WebSocketCredentials(
                        scheme = "Bearer",
                        credentials = token
                    )
                )
            )
        } catch (e: Exception) {
            // Silently ignore if WebSocket is not connected yet
            // Typing indicators are not critical
        }
    }
}