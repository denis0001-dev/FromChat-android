package ru.fromchat.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.fromchat.utils.failOnError

object ApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    val http = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) {
                    // Replace with Logcat if needed
                    println(message)
                }
            }
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
            .post("https://fromchat.ru/api/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            .failOnError()
            .body<LoginResponse>()
            .also {
                token = it.token
                user = it.user
            }

    suspend fun register(request: RegisterRequest) =
        http
            .post("https://fromchat.ru/api/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            .failOnError()

    suspend fun getMessages() =
        http
            .get("https://fromchat.ru/api/get_messages") {
                contentType(ContentType.Application.Json)
            }
            .failOnError()
            .body<MessagesResponse>()

    suspend fun send(message: String) =
        http
            .post("https://fromchat.ru/api/send_message") {
                contentType(ContentType.Application.Json)
                bearerAuth(token!!)
                setBody(SendMessageRequest(message))
            }
            .failOnError()
            .body<SendMessageResponse>()
}