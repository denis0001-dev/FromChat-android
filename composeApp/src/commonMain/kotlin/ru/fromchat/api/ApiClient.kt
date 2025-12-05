package ru.fromchat.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.fromchat.API_HOST
import ru.fromchat.utils.failOnError
import kotlin.concurrent.Volatile

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

        install(WebSockets)
    }

    @Volatile
    var token: String? = null
        private set

    @Volatile
    var user: User? = null
        private set

    suspend fun login(request: LoginRequest) =
        http
            .post("$API_HOST/api/login") {
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
            .post("$API_HOST/api/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            .failOnError()

    suspend fun getMessages() =
        http
            .get("$API_HOST/api/get_messages") {
                contentType(ContentType.Application.Json)
            }
            .failOnError()
            .body<MessagesResponse>()

    suspend fun send(message: String) =
        http
            .post("$API_HOST/api/send_message") {
                contentType(ContentType.Application.Json)
                bearerAuth(token!!)
                setBody(SendMessageRequest(message))
            }
            .failOnError()
            .body<SendMessageResponse>()

    suspend fun editMessage(messageId: Int, content: String) =
        http
            .put("$API_HOST/api/edit_message/$messageId") {
                contentType(ContentType.Application.Json)
                bearerAuth(token!!)
                setBody(EditMessageRequest(content))
            }
            .failOnError()
            .body<SendMessageResponse>()

    suspend fun deleteMessage(messageId: Int) =
        http
            .delete("$API_HOST/api/delete_message/$messageId") {
                contentType(ContentType.Application.Json)
                bearerAuth(token!!)
            }
            .failOnError()
}