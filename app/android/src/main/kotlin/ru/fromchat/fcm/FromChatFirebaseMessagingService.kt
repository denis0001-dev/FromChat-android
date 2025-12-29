package ru.fromchat.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pr0gramm3r101.utils.settings.settings
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.fromchat.api.ApiClient
import ru.fromchat.core.config.Config
import ru.fromchat.notifications.NotificationHelper

@OptIn(DelicateCoroutinesApi::class)
class FromChatFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FromChatFCM", "onMessageReceived: from=${remoteMessage.from}, data=${remoteMessage.data}")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                NotificationHelper.fetchAndNotify(applicationContext)
            } catch (e: Exception) {
                Log.e("FromChatFCM", "onMessageReceived error: ${e.message}", e)
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FromChatFCM", "onNewToken: $token")
        GlobalScope.launch(Dispatchers.IO) {
            // Upload token to backend if authenticated, otherwise save locally (TODO: persist and upload on login)
            try {
                val authToken = ApiClient.token
                if (!authToken.isNullOrEmpty()) {
                    // Call backend endpoint to register token
                    try {
                        val resp = ApiClient.http.post("${Config.apiBaseUrl}/push/register") {
                            setBody(ApiClient.json.encodeToString(mapOf("token" to token)))
                        }
                        Log.d("FromChatFCM", "Uploaded FCM token to server: ${resp.status.value}")
                    } catch (e: Exception) {
                        Log.e("FromChatFCM", "Failed to upload token: ${e.message}", e)
                    }
                } else {
                    // Save to shared preferences for later upload (best-effort)
                    runCatching {
                        settings.putString("pending_fcm_token", token)
                    }
                }
            } catch (e: Exception) {
                Log.e("FromChatFCM", "onNewToken upload error: ${e.message}", e)
            }

            super.onNewToken(token)
        }
    }
}


