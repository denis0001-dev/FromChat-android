package ru.fromchat.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.DarwinClientEngineConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.headers

actual fun createPlatformHttpClient(
    block: HttpClientConfig<*>.() -> Unit
): HttpClient {
    @Suppress("UNCHECKED_CAST")
    return HttpClient(Darwin) {
        // Configure default request headers to ensure UTF-8 encoding
        defaultRequest {
            contentType(ContentType.Application.Json)
            headers {
                append("Accept-Charset", "utf-8")
                append("Content-Type", "application/json; charset=utf-8")
            }
        }

        // Add timeout configuration
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
        }

        // Apply the passed configuration block
        block(this)
    }
}
