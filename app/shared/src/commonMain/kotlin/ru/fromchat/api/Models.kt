package ru.fromchat.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val display_name: String,
    val password: String,
    val confirm_password: String
)

@Serializable
data class ErrorResponse(
    val detail: String
)

@Serializable
data class User(
    val id: Int,
    val created_at: String,
    val last_seen: String,
    val online: Boolean,
    val username: String,
    val admin: Boolean? = null,
    val bio: String? = null,
    val profile_picture: String? = null
)

@Serializable
data class LoginResponse(
    val user: User,
    val token: String
)

@Serializable
data class MessagesResponse(
    val status: String,
    val messages: List<Message>
)

@Serializable
data class Message(
    val id: Int,
    val user_id: Int,
    val content: String,
    val timestamp: String,
    val is_read: Boolean,
    val is_edited: Boolean,
    val username: String,
    val profile_picture: String? = null,
    val verified: Boolean? = null,
    val reply_to: Message? = null,
    val client_message_id: String? = null,
    val reactions: List<ReactionData>? = null
)

@Serializable
data class SendMessageRequest(
    val content: String,
    val reply_to_id: Int? = null
)

@Serializable
data class EditMessageRequest(
    val content: String
)

@Serializable
data class SendMessageResponse(
    val status: String,
    val message: Message
)

// WebSocket types mirror frontend src/core/types.d.ts
@Serializable
data class WebSocketCredentials(
    val scheme: String,
    val credentials: String
)

@Serializable
data class WebSocketError(
    val code: Int,
    val detail: String
)

@Serializable
data class WebSocketMessage(
    val type: String,
    val credentials: WebSocketCredentials? = null,
    val data: JsonElement? = null,
    val error: WebSocketError? = null
)

// WebSocket message data types
@Serializable
data class NewMessageData(
    val message: Message
)

@Serializable
data class MessageEditedData(
    val message: Message
)

@Serializable
data class MessageDeletedData(
    val message_id: Int
)

@Serializable
data class TypingData(
    val userId: Int,
    val username: String
)

// Batched updates message
@Serializable
data class UpdateItem(
    val type: String,
    val data: JsonElement? = null
)

@Serializable
data class UpdatesMessage(
    val type: String,
    val seq: Int,
    val updates: List<UpdateItem>
)

// WebSocket request types
@Serializable
data class WebSocketSendMessageRequest(
    val content: String,
    val reply_to_id: Int? = null,
    val client_message_id: String? = null
)

@Serializable
data class WebSocketEditMessageRequest(
    val message_id: Int,
    val content: String
)

@Serializable
data class WebSocketDeleteMessageRequest(
    val message_id: Int
)