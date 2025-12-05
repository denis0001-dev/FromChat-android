package ru.fromchat.api

import ru.fromchat.core.Logger
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException

suspend inline fun <Response> apiRequest(
    onError: (String, Exception) -> Unit = { _, _ -> },
    onSuccess: (Response) -> Unit = {},
    request: suspend () -> Response
): Result<Response> {
    try {
        val response = request()
        onSuccess(response)
        return Result.success(response)
    } catch (e: ClientRequestException) {
        val message = if (e.response.status.value in arrayOf(401, 403)) {
            e.response.body<ErrorResponse>().detail
        } else {
            "Unexpected error"
        }

        Logger.e("API", "API request failed: $message", e)

        onError(message, e)
        return Result.failure(e)
    } catch (e: Exception) {
        Logger.e("API", "API request failed: ${e.message}", e)
        onError("Unexpected error", e)
        return Result.failure(e)
    }
}