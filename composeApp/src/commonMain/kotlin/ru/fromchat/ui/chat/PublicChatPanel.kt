package ru.fromchat.ui.chat

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.decodeFromJsonElement
import ru.fromchat.api.ApiClient
import ru.fromchat.api.Message
import ru.fromchat.api.MessageDeletedData
import ru.fromchat.api.WebSocketMessage
import ru.fromchat.core.Logger

class PublicChatPanel(
    chatName: String,
    currentUserId: Int?,
    scope: CoroutineScope
) : ChatPanel(
    id = "public-$chatName",
    currentUserId = currentUserId,
    scope = scope
) {
    private val typingHandler = PublicChatTypingHandler(scope)
    private var messagesLoaded = false

    init {
        updateState { it.copy(title = chatName) }
    }

    override suspend fun sendMessage(content: String, replyToId: Int?) {
        ApiClient.sendMessage(content, replyToId)
    }

    override suspend fun loadMessages() {
        if (messagesLoaded) return

        setLoading(true)
        try {
            val response = ApiClient.getMessages(limit = 50)
            if (response.messages.isNotEmpty()) {
                clearMessages()
                response.messages.forEach { message ->
                    addMessage(message)
                }
            }
            setHasMoreMessages(false) // TODO: Implement has_more from API
            messagesLoaded = true
        } catch (e: Exception) {
            // Handle error
        } finally {
            setLoading(false)
        }
    }

    override suspend fun loadMoreMessages() {
        if (!_state.hasMoreMessages || _state.isLoadingMore) return

        val messages = _state.messages
        if (messages.isEmpty()) return

        val oldestMessage = messages.first()
        setLoadingMore(true)
        try {
            val response = ApiClient.getMessages(limit = 50, beforeId = oldestMessage.id)
            if (response.messages.isNotEmpty()) {
                // Prepend older messages (they come in reverse chronological order)
                updateState { currentState ->
                    currentState.copy(
                        messages = response.messages.reversed() + currentState.messages
                    )
                }
            }
            setHasMoreMessages(false) // TODO: Implement has_more from API
        } catch (e: Exception) {
            // Handle error
        } finally {
            setLoadingMore(false)
        }
    }

    override suspend fun handleWebSocketMessage(message: WebSocketMessage) {
        val json = ApiClient.json
        Logger.d("PublicChatPanel", "Handling WebSocket message: type=${message.type}")
        when (message.type) {
            "newMessage" -> {
                val data = message.data ?: return
                // Data is directly a Message, not wrapped
                val newMsg = json.decodeFromJsonElement<Message>(data)
                Logger.d("PublicChatPanel", "New message received: id=${newMsg.id}, content=${newMsg.content.take(50)}")

                // Check if this is a confirmation of a message we sent
                val isOurMessage = newMsg.user_id == currentUserId
                if (isOurMessage) {
                    // This is our message being confirmed, find the temp message and replace it
                    val tempMessages = _state.messages.filter { it.id < 0 }
                    for (tempMsg in tempMessages) {
                        if (tempMsg.content == newMsg.content) {
                            // Replace temp message with confirmed
                            updateState { currentState ->
                                currentState.copy(
                                    messages = currentState.messages.map { msg ->
                                        if (msg.id < 0 && msg.content == newMsg.content) {
                                            newMsg
                                        } else {
                                            msg
                                        }
                                    }
                                )
                            }
                            return
                        }
                    }
                }

                addMessage(newMsg)
            }
            "messageEdited" -> {
                val data = message.data ?: return
                // Data is directly a Message
                val editedMsg = json.decodeFromJsonElement<Message>(data)
                updateMessage(editedMsg.id) { editedMsg }
            }
            "messageDeleted" -> {
                val data = message.data ?: return
                // Data is { message_id: Int }
                val deletedData = json.decodeFromJsonElement<MessageDeletedData>(data)
                removeMessage(deletedData.message_id)
            }
            "typing" -> {
                // Typing status is handled in ChatScreen
            }
        }
    }

    override suspend fun handleEditMessage(messageId: Int, content: String) {
        ApiClient.editMessage(messageId, content)
    }

    override suspend fun handleDeleteMessage(messageId: Int) {
        // Remove immediately from UI
        deleteMessageImmediately(messageId)

        // Send delete request
        ApiClient.deleteMessage(messageId)
    }

    override fun showCallButton(): Boolean = false

    override fun getTypingHandler(): TypingHandler = typingHandler
}

