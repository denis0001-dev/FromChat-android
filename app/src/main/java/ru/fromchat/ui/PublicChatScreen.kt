package ru.fromchat.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import ru.fromchat.DATETIME_FORMAT
import ru.fromchat.R
import ru.fromchat.api.ApiClient
import ru.fromchat.api.Message
import ru.fromchat.api.SendMessageRequest
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
    val hazeState = remember { HazeState() }

    val messages = remember { mutableStateListOf<Message>() }
    val typingUsers = remember { mutableStateMapOf<Int, String>() }
    var loading by remember { mutableStateOf(true) }
    var selectedMessageId by remember { mutableStateOf<Int?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingMessageId by remember { mutableStateOf<Int?>(null) }
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    var menuMessageId by remember { mutableStateOf<Int?>(null) }
    var menuOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    var typingJob: Job? = null

    suspend fun scrollDown(animated: Boolean = true) {
        if (messages.isNotEmpty()) {
            if (animated) {
                listState.animateScrollToItem(messages.lastIndex)
            } else {
                listState.scrollToItem(messages.lastIndex)
            }
        }
    }

    fun sendTyping() {
        typingJob?.cancel()
        typingJob = scope.launch {
            try {
                WebSocketManager.send(
                    WebSocketMessage(
                        type = "typing",
                        credentials = WebSocketCredentials(
                            scheme = "Bearer",
                            credentials = ApiClient.token!!
                        ),
                        data = null
                    )
                )
                delay(3000)
                WebSocketManager.send(
                    WebSocketMessage(
                        type = "stopTyping",
                        credentials = WebSocketCredentials(
                            scheme = "Bearer",
                            credentials = ApiClient.token!!
                        ),
                        data = null
                    )
                )
            } catch (e: Exception) {
                Log.e("PublicChatScreen", "Failed to send typing", e)
            }
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
                when (msg.type) {
                    "newMessage" -> {
                        try {
                            val newMessage = ApiClient.json.decodeFromJsonElement<Message>(msg.data!!)
                            if (messages.none { it.id == newMessage.id }) {
                                messages += newMessage
                                scrollDown()
                            }
                        } catch (e: Exception) {
                            Log.e("PublicChatScreen", "Failed to process incoming message:", e)
                        }
                    }
                    "messageEdited" -> {
                        try {
                            val editedMessage = ApiClient.json.decodeFromJsonElement<Message>(msg.data!!)
                            val index = messages.indexOfFirst { it.id == editedMessage.id }
                            if (index >= 0) {
                                messages[index] = editedMessage
                            }
                        } catch (e: Exception) {
                            Log.e("PublicChatScreen", "Failed to process edited message:", e)
                        }
                    }
                    "messageDeleted" -> {
                        try {
                            val data = msg.data!!.jsonObject
                            val messageId = data["message_id"]?.toString()?.toIntOrNull()
                            if (messageId != null) {
                                messages.removeAll { it.id == messageId }
                            }
                        } catch (e: Exception) {
                            Log.e("PublicChatScreen", "Failed to process deleted message:", e)
                        }
                    }
                    "typing" -> {
                        try {
                            val data = msg.data!!.jsonObject
                            val userId = data["userId"]?.toString()?.toIntOrNull()
                            val username = data["username"]?.toString()
                            if (userId != null && username != null && userId != ApiClient.user?.id) {
                                typingUsers[userId] = username
                            }
                        } catch (e: Exception) {
                            Log.e("PublicChatScreen", "Failed to process typing:", e)
                        }
                    }
                    "stopTyping" -> {
                        try {
                            val data = msg.data!!.jsonObject
                            val userId = data["userId"]?.toString()?.toIntOrNull()
                            if (userId != null) {
                                typingUsers.remove(userId)
                            }
                        } catch (e: Exception) {
                            Log.e("PublicChatScreen", "Failed to process stop typing:", e)
                        }
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
            Column(
                modifier = Modifier
                    .hazeChild(state = hazeState)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.exclude(WindowInsetsSides.Top))
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                if (replyingToMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Replying to ${replyingToMessage!!.username}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = replyingToMessage!!.content.take(50) + if (replyingToMessage!!.content.length > 50) "..." else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { replyingToMessage = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Cancel reply",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val message = rememberTextFieldState()

                    LaunchedEffect(message.text) {
                        if (message.text.isNotEmpty()) {
                            sendTyping()
                        }
                    }

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
                                val messageText = message.text.trim()
                                if (messageText.isEmpty()) return@launch
                                
                                try {
                                    val response = WebSocketManager.request(
                                        WebSocketMessage(
                                            type = "sendMessage",
                                            credentials = WebSocketCredentials(
                                                scheme = "Bearer",
                                                credentials = ApiClient.token!!
                                            ),
                                            data = ApiClient.json.encodeToJsonElement(
                                                SendMessageRequest(
                                                    content = messageText.toString(),
                                                    reply_to_id = replyingToMessage?.id
                                                )
                                            )
                                        )
                                    )
                                    
                                    if (response != null && response.error == null) {
                                        message.setTextAndPlaceCursorAtEnd("")
                                        replyingToMessage = null
                                        typingJob?.cancel()
                                        WebSocketManager.send(
                                            WebSocketMessage(
                                                type = "stopTyping",
                                                credentials = WebSocketCredentials(
                                                    scheme = "Bearer",
                                                    credentials = ApiClient.token!!
                                                ),
                                                data = null
                                            )
                                        )
                                    } else {
                                        Log.e("PublicChatScreen", "WebSocket error: ${response?.error}")
                                    }
                                } catch (e: Exception) {
                                    Log.e("PublicChatScreen", "Failed to send message", e)
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
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(innerPadding)
            ) {
                items(messages, { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        isAuthor = message.user_id == ApiClient.user!!.id,
                        hazeState = hazeState,
                        onTap = { offset ->
                            menuMessageId = message.id
                            menuOffset = offset
                        },
                        onEdit = {
                            editingMessage = message
                            showEditDialog = true
                            menuMessageId = null
                        },
                        onDelete = {
                            deletingMessageId = message.id
                            showDeleteDialog = true
                            menuMessageId = null
                        },
                        onReply = {
                            replyingToMessage = message
                            menuMessageId = null
                        }
                    )
                }

                if (typingUsers.isNotEmpty()) {
                    item {
                        TypingIndicator(
                            users = typingUsers.values.toList(),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (menuMessageId != null) {
                val message = messages.find { it.id == menuMessageId }
                if (message != null) {
                    MessageMenu(
                        message = message,
                        offset = menuOffset,
                        onDismiss = { menuMessageId = null },
                        onEdit = {
                            editingMessage = message
                            showEditDialog = true
                            menuMessageId = null
                        },
                        onDelete = {
                            deletingMessageId = message.id
                            showDeleteDialog = true
                            menuMessageId = null
                        },
                        onReply = {
                            replyingToMessage = message
                            menuMessageId = null
                        },
                        hazeState = hazeState
                    )
                }
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

    if (showEditDialog && editingMessage != null) {
        EditMessageDialog(
            message = editingMessage!!,
            onDismiss = {
                showEditDialog = false
                editingMessage = null
            },
            onConfirm = { newContent ->
                scope.launch {
                    try {
                        apiRequest {
                            ApiClient.editMessage(editingMessage!!.id, newContent)
                        }
                        showEditDialog = false
                        editingMessage = null
                    } catch (e: Exception) {
                        Log.e("PublicChatScreen", "Failed to edit message", e)
                    }
                }
            }
        )
    }

    if (showDeleteDialog && deletingMessageId != null) {
        DeleteMessageDialog(
            onDismiss = {
                showDeleteDialog = false
                deletingMessageId = null
            },
            onConfirm = {
                scope.launch {
                    try {
                        apiRequest {
                            ApiClient.deleteMessage(deletingMessageId!!)
                        }
                        showDeleteDialog = false
                        deletingMessageId = null
                    } catch (e: Exception) {
                        Log.e("PublicChatScreen", "Failed to delete message", e)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun MessageBubble(
    message: Message,
    isAuthor: Boolean,
    hazeState: HazeState,
    onTap: (androidx.compose.ui.geometry.Offset) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReply: () -> Unit
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
        val background = if (isAuthor)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        else
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = if (isAuthor) 16.dp else 5.dp,
                        topEnd = 16.dp,
                        bottomEnd = if (isAuthor) 5.dp else 16.dp,
                        bottomStart = 16.dp
                    )
                )
                .hazeChild(state = hazeState)
                .background(background)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(
                    topStart = if (isAuthor) 16.dp else 5.dp,
                    topEnd = 16.dp,
                    bottomEnd = if (isAuthor) 5.dp else 16.dp,
                    bottomStart = 16.dp
                ))
                .clickable {
                    onTap(androidx.compose.ui.geometry.Offset(0f, 0f))
                }
                .width(IntrinsicSize.Max)
                .widthIn(max = rowWidth * 0.80f)
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                if (message.reply_to != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = message.reply_to.username,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.W600,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = message.reply_to.content,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (!isAuthor) {
                    Text(
                        text = message.username,
                        fontWeight = FontWeight.W600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(message.content)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.weight(1f))
                    if (message.is_edited) {
                        Text(
                            text = "(edited)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = Instant
                            .parse(message.utcTimestamp)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .format(DATETIME_FORMAT),
                        fontSize = 12.sp
                    )
                    if (isAuthor && message.is_read) {
                        Icon(
                            imageVector = Icons.Filled.DoneAll,
                            contentDescription = "Read",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator(
    users: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
        )
    ) {
        Text(
            text = if (users.size == 1) {
                "${users[0]} is typing..."
            } else {
                "${users.size} people are typing..."
            },
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MessageMenu(
    message: Message,
    offset: androidx.compose.ui.geometry.Offset,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReply: () -> Unit,
    hazeState: HazeState
) {
    val isAuthor = message.user_id == ApiClient.user!!.id

    Popup(
        onDismissRequest = onDismiss,
        offset = androidx.compose.ui.unit.IntOffset(offset.x.toInt(), offset.y.toInt()),
        properties = PopupProperties(focusable = true)
    ) {
        GlassSurface(
            hazeState = hazeState,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                MenuItem(
                    icon = Icons.AutoMirrored.Filled.Reply,
                    text = "Reply",
                    onClick = {
                        onReply()
                        onDismiss()
                    }
                )
                if (isAuthor) {
                    MenuItem(
                        icon = Icons.Default.Edit,
                        text = "Edit",
                        onClick = {
                            onEdit()
                            onDismiss()
                        }
                    )
                    MenuItem(
                        icon = Icons.Default.Delete,
                        text = "Delete",
                        onClick = {
                            onDelete()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(20.dp)
        )
        Text(text)
    }
}

@Composable
fun EditMessageDialog(
    message: Message,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(message.content) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Message") },
        text = {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current,
                singleLine = false
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.trim().isNotEmpty()) {
                        onConfirm(text.trim())
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteMessageDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Message") },
        text = { Text("Are you sure you want to delete this message?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
