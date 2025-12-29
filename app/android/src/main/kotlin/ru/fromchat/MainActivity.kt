package ru.fromchat

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.fromchat.api.ApiClient
import ru.fromchat.api.MessagesResponse
import ru.fromchat.core.config.Config
import ru.fromchat.ui.App
import ru.fromchat.ui.isPublicChatVisible

class MainActivity : ComponentActivity() {
    private var scrollToMessageId by mutableStateOf<Int?>(null)
    private var startAtPublicChat by mutableStateOf(false)
    private var prevIsPublicChatVisible: Boolean? = null

    private fun handleIntent(intent: Intent?) {
        val messageId = intent?.getIntExtra("scroll_to_message_id", -1) ?: -1
        scrollToMessageId = if (messageId != -1) messageId else null
        startAtPublicChat = messageId != -1

        // Mark messages as read if clicked from notification
        if (intent?.getBooleanExtra("mark_message_read", false) == true) {
            markMessagesAsRead()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun markMessagesAsRead() {
        GlobalScope.launch {
            try {
                // Get all unread messages and mark them as read
                val messageIds = ApiClient.http
                    .get("${Config.apiBaseUrl}/messages/new")
                    .body<MessagesResponse>()
                    .messages
                    .map { it.id }

                if (messageIds.isNotEmpty()) {
                    ApiClient.http.post("${Config.apiBaseUrl}/messages/read") {
                        contentType(ContentType.Application.Json)
                        setBody(mapOf("messageIds" to messageIds))
                    }
                    Log.d("MainActivity", "Marked ${messageIds.size} messages as read: $messageIds")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to mark messages as read", e)
            }
        }
    }

    private fun checkGooglePlayServices(): Boolean {
        with (GoogleApiAvailability.getInstance()) {
            val resultCode = isGooglePlayServicesAvailable(this@MainActivity)

            if (resultCode == ConnectionResult.SUCCESS) {
                return true
            }

            if (isUserResolvableError(resultCode)) {
                getErrorDialog(
                    this@MainActivity,
                    resultCode,
                    9000
                )?.show()
            }

            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        // Handle initial intent
        handleIntent(intent)

        setContent {
            App(
                scrollToMessageId = scrollToMessageId,
                startAtPublicChat = startAtPublicChat
            )
        }

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {}.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        checkGooglePlayServices()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onPause() {
        super.onPause()
        prevIsPublicChatVisible = isPublicChatVisible
        isPublicChatVisible = false
    }

    override fun onResume() {
        super.onResume()
        isPublicChatVisible = prevIsPublicChatVisible ?: false
    }
}