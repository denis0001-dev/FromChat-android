package ru.fromchat.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import ru.fromchat.api.Message

data class ContextMenuState(
    val isOpen: Boolean = false,
    val message: Message? = null,
    val position: IntOffset = IntOffset(0, 0)
)

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun MessageContextMenu(
    state: ContextMenuState,
    isAuthor: Boolean,
    onDismiss: () -> Unit,
    onReply: (Message) -> Unit,
    onEdit: (Message) -> Unit,
    onDelete: (Message) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState
) {
    AnimatedVisibility(
        visible = state.isOpen,
        enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.8f, animationSpec = tween(250)),
        exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.8f, animationSpec = tween(150))
    ) {
        if (state.isOpen && state.message != null) {
            Popup(
                onDismissRequest = onDismiss,
                alignment = Alignment.TopStart,
                offset = state.position,
                properties = PopupProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                Surface(
                    modifier = modifier
                        .width(160.dp)
                        .shadow(8.dp, RoundedCornerShape(8.dp))
                        .hazeEffect(
                            state = hazeState,
                            style = HazeMaterials.thick()
                        ),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent
                ) {
                    Column {
                        // Reply button (always shown)
                        ContextMenuItem(
                            icon = Icons.AutoMirrored.Filled.Reply,
                            text = "Reply",
                            onClick = {
                                state.message?.let { onReply(it) }
                                onDismiss()
                            }
                        )

                        // Edit button (only for own messages)
                        if (isAuthor) {
                            HorizontalDivider()
                            ContextMenuItem(
                                icon = Icons.Default.Edit,
                                text = "Edit",
                                onClick = {
                                    state.message?.let { onEdit(it) }
                                    onDismiss()
                                }
                            )
                        }

                        // Delete button (only for own messages)
                        if (isAuthor) {
                            HorizontalDivider()
                            ContextMenuItem(
                                icon = Icons.Default.Delete,
                                text = "Delete",
                                onClick = {
                                    state.message?.let { onDelete(it) }
                                    onDismiss()
                                },
                                isError = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val iconColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}
