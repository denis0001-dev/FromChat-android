package ru.fromchat.ui.chat

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import ru.fromchat.api.ApiClient
import ru.fromchat.api.Message
import ru.fromchat.api.WebSocketManager
import ru.fromchat.api.apiRequest
import ru.fromchat.ui.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicChatScreen() {
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val messages = remember { mutableStateListOf<Message>() }

    LaunchedEffect(Unit) {
        val response = apiRequest {
            ApiClient.getMessages()
        }

        if (response.isSuccess) {
            messages.clear()
            messages += response.getOrThrow().messages
        }

        WebSocketManager.connect()
        WebSocketManager.setGlobalMessageHandler { msg ->
            scope.launch {
                try {
                    messages += Json.decodeFromJsonElement<Message>(msg.data!!)
                    delay(500)
                    scrollState.animateScrollTo(scrollState.maxValue)
                } catch (e: Exception) {
                    Log.e("PublicChatScreen", "Failed to process incoming message:", e)
                }
            }
        }

        delay(500)
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Public Chat",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.exclude(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                        )
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val message = rememberTextFieldState()

                BasicTextField(
                    state = message,
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 4),
                    decorator = { innerTextField ->
                        Box {
                            if (message.text.isEmpty()) {
                                Text(
                                    text = "Write a message...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                IconButton(
                    onClick = {
                        scope.launch {
                            val response = apiRequest {
                                ApiClient.send(message.text.toString())
                            }

                            message.setTextAndPlaceCursorAtEnd("")

                            if (response.isSuccess) {
                                messages += response.getOrThrow().message
                                delay(500)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            @Composable
            fun Message(
                text: String,
                isAuthor: Boolean,
                isRead: Boolean,
                timestamp: String
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = if (isAuthor) Arrangement.End else Arrangement.Start
                ) {
                    val background =
                        if (isAuthor)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer

                    Column(
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    if (isAuthor) 16.dp else 5.dp,
                                    16.dp,
                                    if (isAuthor) 5.dp else 16.dp,
                                    16.dp
                                )
                            )
                            .background(background)
                            .padding(10.dp)
                            .width(IntrinsicSize.Max)
                    ) {
                        Text(text)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.End),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = timestamp,
                                fontSize = 14.sp
                            )
                            if (isRead) {
                                Icon(
                                    imageVector = Icons.Filled.DoneAll,
                                    contentDescription = "Read",
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }

            messages.forEach {
                Message(
                    text = it.content,
                    isAuthor = it.username == ApiClient.user!!.username,
                    isRead = it.is_read,
                    timestamp = it.timestamp
                )
            }
        }
    }
}