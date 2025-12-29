package ru.fromchat.api

import com.pr0gramm3r101.utils.settings.secureSettings
import com.pr0gramm3r101.utils.settings.settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import ru.fromchat.core.config.Config
import ru.fromchat.fcm.uploadPendingFcmTokenIfAvailable
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
            pingInterval = 5000.milliseconds
        }

        defaultRequest {
            token?.let { authToken ->
                bearerAuth(authToken)
            }
        }

        // Handle HTTP errors and auth errors globally
        HttpResponseValidator {
            validateResponse { response ->
                if (response.status.value == 401 || response.status.value == 403) {
                    token = null
                    user = null
                    onAuthError?.let {
                        MainScope().launch {
                            it()
                        }
                    }
                }

                if (response.status.value !in (200..299) + 101) {
                    throw ClientRequestException(
                        response,
                        response.status.description
                    )
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
                settings.putInt("current_user_id", it.user.id)
                // Upload any pending FCM token after successful login
                MainScope().launch {
                    runCatching {
                        uploadPendingFcmTokenIfAvailable()
                    }
                }
            }

    suspend fun register(request: RegisterRequest) =
        http.post("${Config.apiBaseUrl}/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.also {
            // Upload any pending FCM token after successful registration
            MainScope().launch {
                runCatching {
                    uploadPendingFcmTokenIfAvailable()
                }
            }
        }

    suspend fun getMessages(limit: Int = 50, beforeId: Int? = null) =
        http
            .get("${Config.apiBaseUrl}/get_messages") {
                contentType(ContentType.Application.Json)
                parameter("limit", limit)
                beforeId?.let { parameter("before_id", it) }
            }
            .body<MessagesResponse>()

    // Validate token by fetching user profile
    suspend fun validateToken(): Boolean {
        try {
            http
                .get("${Config.apiBaseUrl}/api/user/profile")
            return true // Token is valid if no exception thrown
        } catch (e: ClientRequestException) {
            if (e.response.status.value == 401 || e.response.status.value == 403) {
                return false
            }

            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun logout() {
        runCatching {
            http.get("${Config.apiBaseUrl}/logout")
        }

        secureSettings.remove("auth_token")
        settings.remove("user_info")
        settings.remove("current_user_id")
        token = null
        user = null
    }

    fun getTokenSafely() = token ?: throw IllegalStateException("Not authenticated")

    // WebSocket send helpers
    suspend fun sendMessage(content: String, replyToId: Int? = null, clientMessageId: String? = null) {
        WebSocketManager.send(
            WebSocketMessage(
                type = "sendMessage",
                credentials = WebSocketCredentials(
                    scheme = "Bearer",
                    credentials = getTokenSafely()
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
        WebSocketManager.send(
            WebSocketMessage(
                type = "editMessage",
                credentials = WebSocketCredentials(
                    scheme = "Bearer",
                    credentials = getTokenSafely()
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
        WebSocketManager.send(
            WebSocketMessage(
                type = "deleteMessage",
                credentials = WebSocketCredentials(
                    scheme = "Bearer",
                    credentials = getTokenSafely()
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
        runCatching {
            WebSocketManager.send(
                WebSocketMessage(
                    type = "typing",
                    credentials = WebSocketCredentials(
                        scheme = "Bearer",
                        credentials = getTokenSafely()
                    )
                )
            )
        }
    }

    suspend fun sendStopTyping() {
        runCatching {
            WebSocketManager.send(
                WebSocketMessage(
                    type = "stopTyping",
                    credentials = WebSocketCredentials(
                        scheme = "Bearer",
                        credentials = getTokenSafely()
                    )
                )
            )
        }
    }
}