package ru.fromchat.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.fromchat.Res
import ru.fromchat.api.Message
import ru.fromchat.message_placeholder

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: (String) -> Unit,
    typingHandler: TypingHandler,
    replyTo: Message? = null,
    editingMessage: Message? = null,
    onClearReply: () -> Unit,
    onClearEdit: () -> Unit,
    hazeState: HazeState
) {
    val scope = rememberCoroutineScope()
    var typingJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Handle typing indicator
    LaunchedEffect(text) {
        if (text.isNotBlank()) {
            typingJob?.cancel()
            typingHandler.sendTyping()
            typingJob = scope.launch {
                delay(3000) // 3 seconds
                typingHandler.stopTyping()
            }
        } else {
            typingJob?.cancel()
            typingHandler.stopTyping()
        }
    }

    Column(
        Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Reply preview
        replyTo?.let { reply ->
            ReplyPreviewBar(
                replyTo = reply,
                onClose = onClearReply,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .hazeEffect(hazeState, style = HazeMaterials.thick())
            )
        }

        // Edit preview
        editingMessage?.let { edit ->
            EditPreviewBar(
                message = edit,
                onClose = onClearEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .hazeEffect(hazeState, style = HazeMaterials.thick())
            )
        }

        // Input field with blur
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val shape = RoundedCornerShape(24.dp)

                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            Dp.Hairline,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape
                        )
                        .clip(shape)
                        .hazeEffect(
                            state = hazeState,
                            style = HazeMaterials.thin()
                        ),
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.message_placeholder),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    shape = shape,
                    maxLines = 5,
                    singleLine = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    trailingIcon = {
                        val offset = with(LocalDensity.current) { 20.dp.toPx().toInt() }

                        AnimatedVisibility(
                            visible = text.isNotBlank(),
                            enter = slideInHorizontally(
                                initialOffsetX = { it + offset },
                                animationSpec = tween(durationMillis = 300)
                            ),
                            exit = slideOutHorizontally(
                                targetOffsetX = { it + offset },
                                animationSpec = tween(durationMillis = 200)
                            )
                        ) {
                            Box(Modifier.padding(end = 5.dp)) {
                                FilledIconButton(
                                    onClick = {
                                        onSend(text.trim())
                                        onTextChange("")
                                        typingHandler.stopTyping()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ReplyPreviewBar(
    replyTo: Message,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier, // hazeEffect moved to ChatInput where it's called
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Replying to ${replyTo.username}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = replyTo.content.take(50) + if (replyTo.content.length > 50) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EditPreviewBar(
    message: Message,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier, // hazeEffect moved to ChatInput where it's called
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Editing message",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message.content.take(50) + if (message.content.length > 50) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
