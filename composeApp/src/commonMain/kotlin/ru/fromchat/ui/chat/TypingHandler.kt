package ru.fromchat.ui.chat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.fromchat.api.ApiClient

/**
 * Interface for handling typing indicators
 */
interface TypingHandler {
    fun sendTyping()
    fun stopTyping()
}

/**
 * Typing handler for public chat using WebSocket
 */
class PublicChatTypingHandler(
    private val scope: CoroutineScope
) : TypingHandler {
    private var stopTypingJob: Job? = null

    override fun sendTyping() {
        scope.launch {
            try {
                ApiClient.sendTyping()
            } catch (e: Exception) {
                // Ignore errors
            }
        }

        // Cancel existing stop typing job
        stopTypingJob?.cancel()

        // Schedule stop typing after delay
        stopTypingJob = scope.launch {
            delay(3000) // 3 seconds
            stopTyping()
        }
    }

    override fun stopTyping() {
        stopTypingJob?.cancel()
        stopTypingJob = null
        scope.launch {
            try {
                ApiClient.sendStopTyping()
            } catch (e: Exception) {
                // Ignore errors
            }
        }
    }
}

