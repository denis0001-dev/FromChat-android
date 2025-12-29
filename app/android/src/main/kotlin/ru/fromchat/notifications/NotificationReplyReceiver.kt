package ru.fromchat.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.RemoteInput
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.fromchat.api.ApiClient

@OptIn(DelicateCoroutinesApi::class)
class NotificationReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        RemoteInput.getResultsFromIntent(intent)?.getCharSequence("key_text_reply")?.toString()?.let {
            if (it.isNotBlank()) {
                Log.d("NotificationReply", "Received reply: $it")

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        ApiClient.sendMessage(it)
                        Log.d("NotificationReply", "Reply sent successfully")
                    } catch (e: Exception) {
                        Log.e("NotificationReply", "Failed to send reply", e)
                    }
                }
            }
        }
    }
}
