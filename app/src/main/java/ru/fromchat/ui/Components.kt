package ru.fromchat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@Composable
fun RowHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .size(30.dp),
        )
        androidx.compose.material3.Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        androidx.compose.material3.Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f),
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    borderWidth: androidx.compose.ui.unit.Dp = 1.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(shape)
            .hazeChild(state = hazeState)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape)
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
    ) {
        content()
    }
}
