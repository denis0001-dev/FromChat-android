package ru.fromchat.api

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
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
    val data: kotlinx.serialization.json.JsonElement? = null,
    val error: WebSocketError? = null
)