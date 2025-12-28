package ru.fromchat.api

import com.pr0gramm3r101.utils.settings.secureSettings
import com.pr0gramm3r101.utils.settings.settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
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

        // Set default auth header for all requests
        defaultRequest {
            token?.let { authToken ->
                bearerAuth(authToken)
            }
        }

        // Handle HTTP errors and auth errors globally
        HttpResponseValidator {
            validateResponse { response ->
                // Handle auth errors
                if (response.status.value == 401 || response.status.value == 403) {
                    // Clear invalid token and notify about auth error
                    token = null
                    user = null
                    onAuthError?.invoke()
                }

                // Allow WebSocket upgrade responses (101 Switching Protocols)
                if (response.status.value == 101) {
                    return@validateResponse
                }

                // Throw exception for non-2xx status codes (like failOnError())
                if (response.status.value !in 200..299) {
                    throw io.ktor.client.plugins.ClientRequestException(response, response.status.description)
                }
            }
        }
    }

    @Volatile
    var token: String? = null

    @Volatile
    var user: User? = null

    // Global auth error handler
    var onAuthError: (() -> Unit)? = null

    // Load persisted token and user info
    suspend fun loadPersistedData() {
        try {
            val savedToken = secureSettings.getString("auth_token", "")
            token = savedToken
            if (!token.isNullOrEmpty()) {
                val userInfo = settings.getString("user_info", "")
                if (userInfo.isNotEmpty()) {
                    user = json.decodeFromString(userInfo)
                }
            }
        } catch (e: Exception) {
            ru.fromchat.core.Logger.e("ApiClient", "Error loading persisted data", e)
        }
    }


    suspend fun login(request: LoginRequest) =
        http
            .post("${Config.apiBaseUrl}/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            .body<LoginResponse>()
            .also {
                token = it.token
                user = it.user
                secureSettings.putString("auth_token", it.token)
                settings.putString("user_info", json.encodeToString(it.user))
            }

    suspend fun register(request: RegisterRequest) =
        http
            .post("${Config.apiBaseUrl}/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

    suspend fun getMessages(limit: Int = 50, beforeId: Int? = null) =
        http
            .get("${Config.apiBaseUrl}/get_messages") {
                contentType(ContentType.Application.Json)
                parameter("limit", limit)
                beforeId?.let { parameter("before_id", it) }
            }
            .body<MessagesResponse>()

    suspend fun send(message: String) =
        http
            .post("${Config.apiBaseUrl}/send_message") {
                contentType(ContentType.Application.Json)
                setBody(SendMessageRequest(message))
            }
            .body<SendMessageResponse>()

    // Validate token by fetching user profile
    suspend fun validateToken(): Boolean {
        try {
            http
                .get("${Config.apiBaseUrl}/api/user/profile")
            return true // Token is valid if no exception thrown
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            // Check if it's an auth error (401/403)
            if (e.response.status.value == 401 || e.response.status.value == 403) {
                return false // Token is invalid
            }
            // For other HTTP errors, re-throw (don't treat as token invalid)
            throw e
        } catch (e: Exception) {
            // For network/other errors, re-throw (don't treat as token invalid)
            throw e
        }
    }


    suspend fun logout() {
        try {
            http.get("${Config.apiBaseUrl}/logout")
        } catch (e: Exception) {
            // Ignore logout errors
        }

        secureSettings.remove("auth_token")
        settings.remove("user_info")
        token = null
        user = null
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