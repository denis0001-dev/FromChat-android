package ru.fromchat.ui.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import ru.fromchat.api.ApiClient
import ru.fromchat.ui.isPublicChatVisible

@Composable
fun PublicChatScreen(scrollToMessageId: Int? = null) {
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

    // Track visibility for notifications
    DisposableEffect(Unit) {
        isPublicChatVisible = true
        onDispose {
            isPublicChatVisible = false
        }
    }

    // Render with ChatScreen
    ChatScreen(
        panel = panel,
        currentUserId = currentUserId,
        scrollToMessageId = scrollToMessageId
    )
}

