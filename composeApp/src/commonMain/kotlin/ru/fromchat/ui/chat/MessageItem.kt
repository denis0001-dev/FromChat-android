package ru.fromchat.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.fromchat.api.Message
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun MessageItem(
    message: Message,
    isAuthor: Boolean,
    onLongPress: () -> Unit,
    onTapPosition: (IntOffset) -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            initialOffsetY = { 20 },
            animationSpec = tween(300)
        ),
        exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(
            targetOffsetY = { -10 },
            animationSpec = tween(200)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { offset ->
                            onTapPosition(
                                IntOffset(
                                    offset.x.toInt(),
                                    offset.y.toInt()
                                )
                            )
                            onLongPress()
                        }
                    )
                },
            horizontalArrangement = if (isAuthor) Arrangement.End else Arrangement.Start
        ) {
            if (!isAuthor) {
                // Profile picture
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    // TODO: Load profile picture
                    Text(
                        text = message.username.take(1).uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .widthIn(max = 280.dp),
                horizontalAlignment = if (isAuthor) Alignment.End else Alignment.Start
            ) {
                if (!isAuthor) {
                    // Username
                    Text(
                        text = message.username,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Reply preview
                message.reply_to?.let { replyTo ->
                    ReplyPreview(
                        replyTo = replyTo,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Message bubble
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = if (isAuthor) 20.dp else 8.dp,
                                bottomEnd = if (isAuthor) 8.dp else 20.dp
                            )
                        )
                        .background(
                            if (isAuthor) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        // Message content
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isAuthor) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )

                        // Timestamp and edited indicator
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTime(message.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = if (isAuthor) {
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                }
                            )
                            if (message.is_edited) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(edited)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = if (isAuthor) {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (isAuthor) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun ReplyPreview(
    replyTo: Message,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = replyTo.username,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp
            )
            Text(
                text = replyTo.content.take(50) + if (replyTo.content.length > 50) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun formatTime(timestamp: String): String {
    return try {
        val instant = Instant.parse(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        String.format(
            "%02d:%02d",
            localDateTime.hour,
            localDateTime.minute
        )
    } catch (e: Exception) {
        ""
    }
}

