package ru.fromchat.ui.chat

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import ru.fromchat.api.Message

data class ContextMenuState(
    val isOpen: Boolean = false,
    val message: Message? = null,
    val position: IntOffset = IntOffset(0, 0)
)

@Suppress("AssignedValueIsNeverRead")
@Composable
fun MessageContextMenu(
    state: ContextMenuState,
    isAuthor: Boolean,
    onDismiss: () -> Unit,
    onReply: (Message) -> Unit,
    onEdit: (Message) -> Unit,
    onDelete: (Message) -> Unit,
    modifier: Modifier = Modifier,
) {
    var shouldShowPopup by remember(state.message) { 
        mutableStateOf(state.isOpen && state.message != null)
    }
    val animationProgress = remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(state.isOpen) {
        if (state.isOpen) {
            // Enter animation
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(200)
            ) { value, _ ->
                animationProgress.floatValue = value
            }
        } else {
            // Exit animation
            animate(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = tween(150)
            ) { value, _ ->
                animationProgress.floatValue = value
            }
            kotlinx.coroutines.delay(150)
            shouldShowPopup = false
        }
    }
    
    // Show popup when opening
    LaunchedEffect(state.isOpen, state.message) {
        if (state.isOpen && state.message != null) {
            shouldShowPopup = true
            animationProgress.floatValue = 0f
        }
    }
    
    if (shouldShowPopup && state.message != null) {
        var popupSize by remember { mutableStateOf(Offset(0f, 0f)) }
        var popupPosition by remember { mutableStateOf(Offset(0f, 0f)) }

        // Calculate transform origin based on click position relative to popup
        val transformOriginX = if (popupSize.x > 0f) {
            val clickXInPopup = state.position.x - popupPosition.x.toInt()
            (clickXInPopup / popupSize.x).coerceIn(0f, 1f)
        } else 0f
        val transformOriginY = if (popupSize.y > 0f) {
            val clickYInPopup = state.position.y - popupPosition.y.toInt()
            (clickYInPopup / popupSize.y).coerceIn(0f, 1f)
        } else 0f

        val scale = animationProgress.floatValue * 0.2f + 0.8f
        val alpha = animationProgress.floatValue
        
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
                    .onGloballyPositioned { coordinates ->
                        popupSize = Offset(
                            coordinates.size.width.toFloat(),
                            coordinates.size.height.toFloat()
                        )
                        popupPosition = coordinates.positionInRoot()
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        alpha = alpha,
                        transformOrigin = TransformOrigin(transformOriginX, transformOriginY)
                    )
                    .shadow(8.dp, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Column {
                    // Reply button (always shown)
                    ContextMenuItem(
                        icon = Icons.AutoMirrored.Filled.Reply,
                        text = "Reply",
                        onClick = {
                            onReply(state.message)
                            onDismiss()
                        }
                    )

                    // Edit button (only for own messages)
                    if (isAuthor) {
                        ContextMenuItem(
                            icon = Icons.Default.Edit,
                            text = "Edit",
                            onClick = {
                                onEdit(state.message)
                                onDismiss()
                            }
                        )
                    }

                    // Delete button (only for own messages)
                    if (isAuthor) {
                        ContextMenuItem(
                            icon = Icons.Default.Delete,
                            text = "Delete",
                            onClick = {
                                onDelete(state.message)
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
            .fillMaxWidth()
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
