package ru.fromchat.api

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException

suspend inline fun <Response> apiRequest(
    onError: (String, Exception) -> Unit,
    onSuccess: (Response) -> Unit,
    request: suspend () -> Response
): Result<Response> {
    try {
        val response = request()
        onSuccess(response)
        return Result.success(response)
    } catch (e: ClientRequestException) {
        onError(
            if (e.response.status.value in arrayOf(401, 403)) {
                e.response.body<ErrorResponse>().detail
            } else {
                "Unexpected error"
            },
            e
        )
        return Result.failure(e)
    } catch (e: Exception) {
        onError("Unexpected error", e)
        return Result.failure(e)
    }
}