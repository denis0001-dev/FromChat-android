package ru.fromchat.ui.chat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.fromchat.api.ApiClient
import kotlin.time.Duration.Companion.seconds

/**
 * Interface for handling typing indicators
 */
interface TypingHandler {
    fun sendTyping()
    fun stopTyping()
    fun handleTypingEvent(userId: Int, username: String)
    fun handleStopTypingEvent(userId: Int)
    val typingUsers: StateFlow<List<TypingUser>>
}

@Serializable
data class TypingUser(
    val userId: Int,
    val username: String
)

/**
 * Typing handler for public chat using WebSocket
 */
class PublicChatTypingHandler(
    private val scope: CoroutineScope
) : TypingHandler {
    private var stopTypingJob: Job? = null
    private val _typingUsers = MutableStateFlow<List<TypingUser>>(emptyList())
    override val typingUsers = _typingUsers.asStateFlow()

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
            delay(3.seconds) // 3 seconds
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

    override fun handleTypingEvent(userId: Int, username: String) {
        _typingUsers.update { currentUsers ->
            if (currentUsers.none { it.userId == userId }) {
                currentUsers + TypingUser(userId, username)
            } else {
                currentUsers
            }
        }
    }

    override fun handleStopTypingEvent(userId: Int) {
        _typingUsers.update { currentUsers ->
            currentUsers.filter { it.userId != userId }
        }
    }
}

