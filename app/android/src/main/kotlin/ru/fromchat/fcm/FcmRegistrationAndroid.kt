package ru.fromchat.fcm

import android.util.Log
import com.pr0gramm3r101.utils.settings.settings
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.fromchat.api.ApiClient
import ru.fromchat.core.config.Config

suspend fun uploadPendingFcmTokenIfAvailable() = withContext(Dispatchers.IO) {
    try {
        val pending = settings.getString("pending_fcm_token", "")

        // Only upload if we have auth token
        if (ApiClient.token.isNullOrEmpty() || pending.isBlank()) {
            Log.d("FcmReg", "Auth token missing or no FCM token; deferring FCM token upload")
            return@withContext
        }

        try {
            ApiClient.http.post("${Config.apiBaseUrl}/push/register") {
                header("Content-Type", "application/json")
                setBody(ApiClient.json.encodeToString(mapOf("token" to pending)))
            }

            settings.remove("pending_fcm_token")
        } catch (e: Exception) {
            Log.e("FcmReg", "Failed to upload pending FCM token: ${e.message}")
        }
    } catch (e: Exception) {
        Log.e("FcmReg", "uploadPendingFcmTokenIfAvailable error: ${e.message}")
    }
}
