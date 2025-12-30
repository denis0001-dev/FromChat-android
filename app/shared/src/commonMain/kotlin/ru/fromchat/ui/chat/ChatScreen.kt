package ru.fromchat.ui.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import kotlinx.serialization.json.decodeFromJsonElement
import org.jetbrains.compose.resources.stringResource
import ru.fromchat.Res
import ru.fromchat.api.ApiClient
import ru.fromchat.api.Message
import ru.fromchat.api.WebSocketManager
import ru.fromchat.api.WebSocketMessage
import ru.fromchat.api.WebSocketUpdatesData
import ru.fromchat.back
import ru.fromchat.core.Logger
import ru.fromchat.ui.LocalNavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun ChatScreen(
    panel: ChatPanel,
    currentUserId: Int?,
    modifier: Modifier = Modifier,
    scrollToMessageId: Int? = null
) {
    var panelState by remember(panel) { mutableStateOf(panel.getState()) }

    // Observe state changes
    LaunchedEffect(panel) {
        panel.setOnStateChange { newState ->
            Logger.d("ChatScreen", "State change callback received: messages=${newState.messages.size}, typingUsers=${newState.typingUsers.map { it.username }}")
            // Force state update to trigger recomposition
            panelState = newState.copy() // Ensure new instance
            Logger.d("ChatScreen", "panelState updated: messages=${panelState.messages.size}, typingUsers=${panelState.typingUsers.map { it.username }}")
        }
        // Initial state
        panelState = panel.getState()
    }

    // Debug: Log state changes
    LaunchedEffect(panelState.messages.size) {
        Logger.d("ChatScreen", "Messages count changed: ${panelState.messages.size}")
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val hazeState = rememberHazeState(blurEnabled = true)

    // Track initial load state and new messages for smooth scrolling
    var isInitialLoad by remember { mutableStateOf(true) }
    var lastMessageCount by remember { mutableStateOf(0) }
    var newlyAddedMessageIds by remember { mutableStateOf(setOf<Int>()) }

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

    val currentTypingUsers = panelState.typingUsers // Directly use from panelState
    LaunchedEffect(currentTypingUsers) {
        Logger.d("ChatScreen", "currentTypingUsers updated (from panelState): ${currentTypingUsers.map { it.username }}")
    }

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
                        val updatesMessage = json.decodeFromJsonElement<WebSocketUpdatesData>(data)
                        Logger.d("ChatScreen", "Updates message parsed: ${updatesMessage.updates.size} updates")
                        // Process each update in the batch
                        updatesMessage.updates.forEach { update ->
                            Logger.d("ChatScreen", "Processing update: type=${update.type}, data=${update.data != null}")
                            val wsMessage = WebSocketMessage(
                                type = update.type,
                                data = update.data
                            )
                            when (update.type) {
                                "newMessage", "messageEdited", "messageDeleted", "typing", "stopTyping", "statusUpdate", "suspended", "account_deleted" -> {
                                    Logger.d("ChatScreen", "Launching handleWebSocketMessage for ${update.type}")
                                    scope.launch {
                                        try {
                                            panel.handleWebSocketMessage(wsMessage)
                                        } catch (e: Exception) {
                                            Logger.e("ChatScreen", "Error handling WebSocket message: ${e.message}", e)
                                        }
                                    }
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
                else -> {
                    Logger.d("ChatScreen", "Unhandled top-level WebSocket message type: ${message.type}")
                }
            }
        }
    }

    // Track if user is at bottom for auto-scroll decisions
    // Use derivedStateOf to avoid frequent recompositions on every scroll change
    val isAtBottom by remember {
        derivedStateOf {
            if (panelState.messages.isNotEmpty()) {
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                val totalItems = panelState.messages.size - 1
                lastVisibleItem?.index == totalItems
            } else {
                true
            }
        }
    }

    // Scroll to specific message when requested (e.g., from notification click)
    LaunchedEffect(scrollToMessageId, panelState.messages) {
        scrollToMessageId?.let { messageId ->
            val messages = panelState.messages
            val messageIndex = messages.indexOfFirst { it.id == messageId }
            if (messageIndex != -1) {
                scope.launch {
                    listState.scrollToItem(
                        index = messages.size - 1 - messageIndex,
                        scrollOffset = 0
                    )
                }
            }
        }
    }

    // Handle message count changes for smooth scrolling and animations
    LaunchedEffect(panelState.messages.size) {
        val currentMessageCount = panelState.messages.size
        val previousMessageCount = lastMessageCount

        if (previousMessageCount in 1..<currentMessageCount) {
            // New messages added (not initial load)
            val newMessageCount = currentMessageCount - previousMessageCount
            val newMessages = panelState.messages.takeLast(newMessageCount)

            // Mark these messages as newly added for animation
            newlyAddedMessageIds = newMessages.map { it.id }.toSet()

            // Auto-scroll with spring animation if user is at bottom
            if (isAtBottom && currentMessageCount > 0) {
                scope.launch {
                    listState.animateScrollToItem(
                        index = currentMessageCount - 1
                    )
                }
            }
        } else if (currentMessageCount > 0 && isInitialLoad) {
            // Initial load - scroll instantly to bottom
            scope.launch {
                listState.scrollToItem(currentMessageCount - 1)
                isInitialLoad = false
            }
        }

        lastMessageCount = currentMessageCount
    }

    // Clear newly added message IDs after animation delay
    LaunchedEffect(newlyAddedMessageIds) {
        val ids = newlyAddedMessageIds
        if (ids.isNotEmpty()) {
            kotlinx.coroutines.delay(600) // Animation duration + buffer
            newlyAddedMessageIds = emptySet()
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            text = panelState.title,
                            style = MaterialTheme.typography.titleLarge
                        )

                        AnimatedContent(
                            targetState = currentTypingUsers.isNotEmpty(),
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "typing_status"
                        ) { hasTyping ->
                            if (hasTyping) {
                                TypingIndicator(
                                    typingUsers = currentTypingUsers.map { it.username },
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
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
                scrollBehavior = scrollBehavior,
                modifier = Modifier.hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.thin()
                ),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Column( // New Column to hold ChatInput below the LazyColumn
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.ime)
                    .fillMaxWidth()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.thin()
                    ) {
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 0f,
                            endIntensity = 1f
                        )
                    }
            ) {
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
                    onClearEdit = {
                        editingMessage = null
                        inputText = ""
                    },
                    hazeState = hazeState
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(), // Fill the entire space of the Box
                    verticalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.Bottom)
                ) {
                    item { Spacer(Modifier.height(innerPadding.calculateTopPadding())) } // Spacer for TopAppBar

                    items(
                        items = panelState.messages,
                        key = { it.id }
                    ) { message ->
                        var messagePosition by remember { mutableStateOf(IntOffset(0, 0)) }
                        var tapOffset by remember { mutableStateOf(Offset(0f, 0f)) }

                        Box(
                            modifier = Modifier
                                .hazeSource(hazeState)
                                .onGloballyPositioned { coordinates ->
                                    messagePosition = IntOffset(
                                        coordinates.positionInRoot().x.toInt(),
                                        coordinates.positionInRoot().y.toInt()
                                    )
                                }
                        ) {
                            MessageItem(
                                message = message,
                                isAuthor = message.user_id == currentUserId,
                                isNewlyAdded = newlyAddedMessageIds.contains(message.id),
                                onLongPress = {
                                    contextMenuState = ContextMenuState(
                                        isOpen = true,
                                        message = message,
                                        position = IntOffset(
                                            messagePosition.x + tapOffset.x.toInt(),
                                            messagePosition.y + tapOffset.y.toInt()
                                        )
                                    )
                                },
                                onTapPosition = { offset ->
                                    tapOffset = offset
                                }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(innerPadding.calculateBottomPadding())) } // Spacer for chat input
                }
            }

            // Context menu
            @Suppress("AssignedValueIsNeverRead")
            MessageContextMenu(
                state = contextMenuState,
                isAuthor = contextMenuState.message?.user_id == currentUserId,
                onDismiss = { contextMenuState = contextMenuState.copy(isOpen = false) },
                onReply = { message ->
                    replyTo = message
                    if (editingMessage != null) {
                        editingMessage = null
                        inputText = ""
                    }
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
                },
            )
        }
    }
}
