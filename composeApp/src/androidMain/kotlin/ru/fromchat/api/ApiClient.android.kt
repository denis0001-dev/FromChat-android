package ru.fromchat.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Dns

actual fun createPlatformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(OkHttp) {
        engine {
            config {
                dns(Dns.SYSTEM)
            }
        }
        block()
    }
}
