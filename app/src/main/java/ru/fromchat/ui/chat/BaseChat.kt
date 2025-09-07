package ru.fromchat.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import ru.fromchat.api.WebSocketManager
import ru.fromchat.api.WebSocketMessage

data class ChatMessage(val author: String, val content: String)

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun BaseChat(
    chatTitle: String
) {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var composing by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        WebSocketManager.connect()
        WebSocketManager.setGlobalMessageHandler { msg ->
            // Placeholder: append raw data content if present
            val text = msg.data?.toString() ?: return@setGlobalMessageHandler
            messages.add(ChatMessage(author = "Server", content = text))
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Text(chatTitle)

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(messages) { m ->
                    Text("${m.author}: ${m.content}")
                }
            }

            Row {
                OutlinedTextField(
                    value = composing,
                    onValueChange = { composing = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Сообщение") }
                )
                Button(onClick = {
                    if (composing.isNotBlank()) {
                        // Note: server schema for public chat messages is not specified in the repo.
                        // We send a generic WebSocketMessage to match frontend type contract.
                        val payload = WebSocketMessage(
                            type = "public_chat_message",
                            data = buildJsonObject {
                                put("content", JsonPrimitive(composing))
                                put("chatName", JsonPrimitive(chatTitle))
                            }
                        )
                        // Fire and forget
                        GlobalScope.launch { WebSocketManager.send(payload) }
                        messages.add(ChatMessage(author = "Вы", content = composing))
                        composing = ""
                    }
                }) { Text("Отправить") }
            }
        }
    }
}