package ru.fromchat

import android.app.Application
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.pr0gramm3r101.utils.UtilsLibrary
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.fromchat.api.ApiClient
import ru.fromchat.api.WebSocketManager
import ru.fromchat.core.config.Config
import ru.fromchat.fcm.uploadPendingFcmTokenIfAvailable
import ru.fromchat.notifications.NotificationHelper

class App: Application() {
    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchAndNotify() {
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                NotificationHelper.fetchAndNotify(applicationContext)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        UtilsLibrary.init(this)

        WebSocketManager.addGlobalMessageHandler { msg ->
            runCatching {
                if (msg.type == "newMessage") {
                    fetchAndNotify()
                } else if (msg.type == "updates") {
                    msg.data?.jsonObject?.get("updates")?.jsonArray?.let { updates ->
                        var shouldFetch = false
                        for (item in updates) {
                            if (
                                item
                                    .jsonObject["type"]
                                    ?.jsonPrimitive
                                    ?.content
                                in arrayOf("newMessage", "dmNew")
                            ) {
                                shouldFetch = true
                                break
                            }
                        }

                        if (shouldFetch) fetchAndNotify()
                    }
                }
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                ApiClient.loadPersistedData()
            }

            runCatching {
                uploadPendingFcmTokenIfAvailable()
            }

            // If we have an auth token, try to get current FCM token and register it immediately
            runCatching {
                val auth = ApiClient.token
                if (!auth.isNullOrEmpty()) {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        GlobalScope.launch(Dispatchers.IO) {
                            if (task.isSuccessful) {
                                try {
                                    val resp = ApiClient.http.post(
                                        "${Config.apiBaseUrl}/push/register"
                                    ) {
                                        header("Content-Type", "application/json")
                                        setBody(
                                            ApiClient.json.encodeToString(
                                                mapOf("token" to task.result)
                                            )
                                        )
                                    }
                                    Log.d(
                                        "AppFCM",
                                        "Registered existing FCM token on startup, status=${resp.status.value}"
                                    )
                                } catch (e: Exception) {
                                    Log.e(
                                        "AppFCM",
                                        "Failed to register FCM token on startup: ${e.message}"
                                    )
                                }
                            } else {
                                Log.w(
                                    "AppFCM",
                                    "FirebaseMessaging token fetch failed on startup: ${task.exception?.message}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}