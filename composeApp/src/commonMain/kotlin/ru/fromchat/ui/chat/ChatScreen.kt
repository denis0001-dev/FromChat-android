package ru.fromchat.ui.chat

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.decodeFromJsonElement
import ru.fromchat.api.ApiClient
import ru.fromchat.api.Message
import ru.fromchat.api.TypingData
import ru.fromchat.api.WebSocketManager
import ru.fromchat.api.WebSocketMessage
import ru.fromchat.core.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    panel: ChatPanel,
    currentUserId: Int?,
    modifier: Modifier = Modifier
) {
    var panelState by remember(panel) { mutableStateOf(panel.getState()) }
    
    // Observe state changes
    LaunchedEffect(panel) {
        panel.setOnStateChange { newState ->
            Logger.d("ChatScreen", "State change callback received: messages=${newState.messages.size}")
            // Force state update to trigger recomposition
            panelState = newState.copy() // Ensure new instance
            Logger.d("ChatScreen", "panelState updated: messages=${panelState.messages.size}")
        }
        // Initial state
        panelState = panel.getState()
    }
    
    // Debug: Log state changes
    LaunchedEffect(panelState.messages.size) {
        Logger.d("ChatScreen", "Messages count changed: ${panelState.messages.size}")
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // UI state
    var inputText by rememberSaveable { mutableStateOf("") }
    var replyTo by rememberSaveable { mutableStateOf<Message?>(null) }
    var editingMessage by rememberSaveable { mutableStateOf<Message?>(null) }
    var contextMenuState by remember { 
        mutableStateOf(
            ContextMenuState(
                isOpen = false,
                message = null,
                position = IntOffset(0, 0)
            )
        )
    }
    var typingUsers by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    // Collect WebSocket messages
    LaunchedEffect(Unit) {
        WebSocketManager.messages.collect { message ->
            Logger.d("ChatScreen", "Received WebSocket message: type=${message.type}, data=${message.data != null}")
            when (message.type) {
                "updates" -> {
                    // Handle batched updates
                    Logger.d("ChatScreen", "Processing updates message")
                    val data = message.data
                    if (data == null) {
                        Logger.w("ChatScreen", "Updates message has no data, skipping")
                        return@collect
                    }
                    Logger.d("ChatScreen", "Updates message has data, parsing...")
                    val json = ApiClient.json
                    try {
                        Logger.d("ChatScreen", "Parsing updates message")
                        val updatesMessage = json.decodeFromJsonElement<ru.fromchat.api.UpdatesMessage>(data)
                        Logger.d("ChatScreen", "Updates message parsed: ${updatesMessage.updates.size} updates")
                        // Process each update in the batch
                        updatesMessage.updates.forEach { update ->
                            Logger.d("ChatScreen", "Processing update: type=${update.type}, data=${update.data != null}")
                            val wsMessage = WebSocketMessage(
                                type = update.type,
                                data = update.data
                            )
                            when (update.type) {
                                "newMessage", "messageEdited", "messageDeleted" -> {
                                    Logger.d("ChatScreen", "Launching handleWebSocketMessage for ${update.type}")
                                    scope.launch {
                                        try {
                                            panel.handleWebSocketMessage(wsMessage)
                                        } catch (e: Exception) {
                                            Logger.e("ChatScreen", "Error handling WebSocket message: ${e.message}", e)
                                        }
                                    }
                                }
                                "typing" -> {
                                    val updateData = update.data ?: return@forEach
                                    val typingData = json.decodeFromJsonElement<TypingData>(updateData)
                                    if (typingData.userId != currentUserId) {
                                        typingUsers = typingUsers + (typingData.userId to typingData.username)
                                        // Remove after timeout
                                        scope.launch {
                                            kotlinx.coroutines.delay(3000)
                                            typingUsers = typingUsers - typingData.userId
                                        }
                                    }
                                }
                                "stopTyping" -> {
                                    val updateData = update.data ?: return@forEach
                                    val typingData = json.decodeFromJsonElement<TypingData>(updateData)
                                    typingUsers = typingUsers - typingData.userId
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Logger.e("ChatScreen", "Error parsing updates message: ${e.message}", e)
                        e.printStackTrace()
                    }
                }
                "newMessage", "messageEdited", "messageDeleted" -> {
                    scope.launch {
                        panel.handleWebSocketMessage(message)
                    }
                }
                "typing" -> {
                    val data = message.data ?: return@collect
                    val json = ApiClient.json
                    val typingData = json.decodeFromJsonElement<TypingData>(data)
                    if (typingData.userId != currentUserId) {
                        typingUsers = typingUsers + (typingData.userId to typingData.username)
                        // Remove after timeout
                        scope.launch {
                            kotlinx.coroutines.delay(3000)
                            typingUsers = typingUsers - typingData.userId
                        }
                    }
                }
            }
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(panelState.messages.size) {
        if (panelState.messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(panelState.messages.size - 1)
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Column {
                        Text(
                            text = panelState.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (typingUsers.isNotEmpty()) {
                            TypingIndicator(
                                typingUsers = typingUsers.values.toList(),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    if (panel.showCallButton()) {
                        IconButton(onClick = { /* TODO: Handle call */ }) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures {
                        // Close context menu on outside tap
                        if (contextMenuState.isOpen) {
                            contextMenuState = contextMenuState.copy(isOpen = false)
                        }
                    }
                }
        ) {
            if (panelState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Message list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        reverseLayout = false
                    ) {
                        items(
                            items = panelState.messages,
                            key = { it.id }
                        ) { message ->
                            val isAuthor = message.user_id == currentUserId
                            var tapPosition by remember { mutableStateOf(IntOffset(0, 0)) }
                            
                            MessageItem(
                                message = message,
                                isAuthor = isAuthor,
                                onLongPress = {
                                    contextMenuState = ContextMenuState(
                                        isOpen = true,
                                        message = message,
                                        position = tapPosition
                                    )
                                },
                                onTapPosition = { position ->
                                    tapPosition = position
                                }
                            )
                        }
                    }

                    // Chat input
                    ChatInput(
                        text = inputText,
                        onTextChange = { inputText = it },
                        onSend = { text ->
                            if (editingMessage != null) {
                                scope.launch {
                                    panel.handleEditMessage(editingMessage!!.id, text)
                                    editingMessage = null
                                }
                            } else {
                                scope.launch {
                                    panel.sendMessageWithImmediateDisplay(text, replyTo?.id)
                                    replyTo = null
                                }
                            }
                            inputText = ""
                        },
                        typingHandler = panel.getTypingHandler(),
                        replyTo = replyTo,
                        editingMessage = editingMessage,
                        onClearReply = { replyTo = null },
                        onClearEdit = { editingMessage = null },
                        modifier = Modifier.windowInsetsPadding(WindowInsets.ime)
                    )
                }
            }

            // Context menu
            MessageContextMenu(
                state = contextMenuState,
                isAuthor = contextMenuState.message?.user_id == currentUserId,
                onDismiss = { contextMenuState = contextMenuState.copy(isOpen = false) },
                onReply = { message ->
                    replyTo = message
                    editingMessage = null
                },
                onEdit = { message ->
                    editingMessage = message
                    inputText = message.content
                    replyTo = null
                },
                onDelete = { message ->
                    scope.launch {
                        panel.handleDeleteMessage(message.id)
                    }
                }
            )
        }
    }
}

