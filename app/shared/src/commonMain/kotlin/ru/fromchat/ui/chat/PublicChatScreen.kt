package ru.fromchat.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import ru.fromchat.api.ApiClient

@Composable
fun PublicChatScreen() {
    val scope = rememberCoroutineScope()
    val currentUserId = ApiClient.user?.id

    // Create panel instance
    val panel = remember {
        PublicChatPanel(
            chatName = "General Chat",
            currentUserId = currentUserId,
            scope = scope
        )
    }

    // Load messages on first appear
    LaunchedEffect(Unit) {
        scope.launch {
            panel.loadMessages()
        }
    }

    // Render with ChatScreen
    ChatScreen(
        panel = panel,
        currentUserId = currentUserId
    )
}

