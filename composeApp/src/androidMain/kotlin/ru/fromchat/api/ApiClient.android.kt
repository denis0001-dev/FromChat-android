package ru.fromchat.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpConfig

actual fun createPlatformHttpClient(
    block: HttpClientConfig<*>.() -> Unit
): HttpClient {
    @Suppress("UNCHECKED_CAST")
    return HttpClient(OkHttp, block as HttpClientConfig<OkHttpConfig>.() -> Unit)
}
