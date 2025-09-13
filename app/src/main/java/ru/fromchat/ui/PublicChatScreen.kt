package ru.fromchat.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import ru.fromchat.DATETIME_FORMAT
import ru.fromchat.R
import ru.fromchat.api.ApiClient
import ru.fromchat.api.Message
import ru.fromchat.api.SendMessageRequest
import ru.fromchat.api.SendMessageResponse
import ru.fromchat.api.WebSocketCredentials
import ru.fromchat.api.WebSocketManager
import ru.fromchat.api.WebSocketMessage
import ru.fromchat.api.apiRequest
import ru.fromchat.utils.exclude
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PublicChatScreen() {
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember { mutableStateListOf<Message>() }
    var loading by remember { mutableStateOf(true) }

    suspend fun scrollDown(animated: Boolean = true) {
        Log.d("PublicChatScreen", "Scrolling down")
        if (animated) {
            listState.animateScrollToItem(messages.lastIndex)
        } else {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    LaunchedEffect(Unit) {
        apiRequest {
            ApiClient.getMessages()
        }.onSuccess {
            messages.clear()
            messages += it.messages
        }

        WebSocketManager.connect()
        WebSocketManager.addGlobalMessageHandler { msg ->
            scope.launch {
                if (msg.type == "newMessage") {
                    try {
                        messages += Json.decodeFromJsonElement<Message>(msg.data!!)
                        scrollDown()
                    } catch (e: Exception) {
                        Log.e("PublicChatScreen", "Failed to process incoming message:", e)
                    }
                }
            }
        }

        delay(500)
        scrollDown(false)
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.public_chat),
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
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.exclude(WindowInsetsSides.Top))
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
                                    text = stringResource(R.string.message_placeholder),
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
                            runCatching {
                                Json.decodeFromJsonElement<SendMessageResponse>(
                                    WebSocketManager.request(
                                        WebSocketMessage(
                                            type = "sendMessage",
                                            credentials = WebSocketCredentials(
                                                scheme = "Bearer",
                                                credentials = ApiClient.token!!
                                            ),
                                            data = Json.encodeToJsonElement(
                                                SendMessageRequest(
                                                    content = "${message.text}"
                                                )
                                            )
                                        )
                                    )!!.data!!
                                )
                            }.getOrNull()?.let {
                                messages += it.message
                                scrollDown()
                            }

                            message.setTextAndPlaceCursorAtEnd("")
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
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(innerPadding)
            ) {
                @Composable
                fun Message(
                    text: String,
                    isAuthor: Boolean,
                    isRead: Boolean,
                    timestamp: String,
                    username: String
                ) {
                    var rowWidth by remember { mutableStateOf(0.dp) }
                    val density = LocalDensity.current

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .onSizeChanged {
                                rowWidth = with(density) {
                                    it.width.toDp()
                                }
                            },
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
                                        topStart = if (isAuthor) 16.dp else 5.dp,
                                        topEnd = 16.dp,
                                        bottomEnd = if (isAuthor) 5.dp else 16.dp,
                                        bottomStart = 16.dp
                                    )
                                )
                                .background(background)
                                .padding(10.dp)
                                .width(IntrinsicSize.Max)
                                .widthIn(max = rowWidth * 0.80f)
                        ) {
                            if (!isAuthor) {
                                Text(
                                    text = username,
                                    fontWeight = FontWeight.W600,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
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

                items(messages, { it.id }) {
                    Message(
                        text = it.content,
                        isAuthor = it.username == ApiClient.user!!.username,
                        isRead = it.is_read,
                        timestamp = Instant
                            .parse(it.utcTimestamp)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .format(DATETIME_FORMAT),
                        username = it.username
                    )
                }
            }

            AnimatedVisibility(
                visible = loading,
                enter = EnterTransition.None,
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(Modifier.size(70.dp))
                }
            }
        }
    }
}