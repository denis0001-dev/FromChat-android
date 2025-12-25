package ru.fromchat.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
private fun <T> AnimatedPreviewBar(
    state: T?,
    content: @Composable (T) -> Unit
) {
    AnimatedVisibility(
        visible = state != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        var lastState by remember { mutableStateOf(state) }

        LaunchedEffect(state) {
            if (state != null) {
                lastState = state
            }
        }

        if (state != null || lastState != null) {
            content(state ?: lastState!!)
        }
    }
}

@Composable
private fun PreviewBar(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 6.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
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
            @Suppress("AssignedValueIsNeverRead")
            typingJob = scope.launch {
                delay(3000) // 3 seconds
                typingHandler.stopTyping()
            }
        } else {
            typingJob?.cancel()
            typingHandler.stopTyping()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        val shape = RoundedCornerShape(24.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    Dp.Hairline,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape
                )
                .clip(shape)
                .hazeEffect(
                    state = hazeState,
                    style = HazeMaterials.thin()
                )
        ) {
            AnimatedPreviewBar(replyTo) { replyTo ->
                PreviewBar(
                    icon = Icons.AutoMirrored.Filled.Reply,
                    title = "Replying to ${replyTo.username}",
                    subtitle = replyTo.content.take(50) + if (replyTo.content.length > 50) "..." else "",
                    onClose = { onClearReply() }
                )
            }

            AnimatedPreviewBar(editingMessage) { message ->
                PreviewBar(
                    icon = Icons.Filled.Edit,
                    title = "Editing message",
                    subtitle = message.content.take(50) + if (message.content.length > 50) "..." else "",
                    onClose = { onClearEdit() }
                )
            }

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
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