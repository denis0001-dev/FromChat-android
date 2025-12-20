package ru.fromchat.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp

actual fun createPlatformHttpClient(block: HttpClientConfig<*>.() -> Unit) = HttpClient(OkHttp, block)