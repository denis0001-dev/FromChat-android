package ru.fromchat.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.DarwinClientEngineConfig

actual fun createPlatformHttpClient(
    block: HttpClientConfig<*>.() -> Unit
): HttpClient {
    @Suppress("UNCHECKED_CAST")
    return HttpClient(Darwin, block as HttpClientConfig<DarwinClientEngineConfig>.() -> Unit)
}
