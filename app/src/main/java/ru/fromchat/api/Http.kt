package ru.fromchat.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.fromchat.API_HOST

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

    private fun apiUrlBuilder(): URLBuilder {
        return URLBuilder().apply {
            protocol = io.ktor.http.URLProtocol.HTTPS
            host = API_HOST
        }
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        // Endpoint exists in web: POST /login with {username,password}
        val response = http.post("https://fromchat.ru/api/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<LoginResponse>()
        token = response.token
        return response
    }

    suspend fun register(request: RegisterRequest) {
        // Endpoint exists in web: POST /register with {username,password,confirm_password}
        http.post("https://fromchat.ru/api/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}