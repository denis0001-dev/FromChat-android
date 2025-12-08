package ru.fromchat.ui.chat

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.fromchat.Res
import ru.fromchat.typing_many
import ru.fromchat.typing_single
import ru.fromchat.typing_two

@Composable
fun TypingIndicator(
    typingUsers: List<String>,
    modifier: Modifier = Modifier
) {
    if (typingUsers.isEmpty()) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TypingDots()
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatTypingText(typingUsers),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun formatTypingText(typingUsers: List<String>): String {
    return when (typingUsers.size) {
        0 -> ""
        1 -> stringResource(Res.string.typing_single, typingUsers[0])
        2 -> stringResource(Res.string.typing_two, typingUsers[0], typingUsers[1])
        else -> stringResource(
            Res.string.typing_many,
            typingUsers[0],
            typingUsers[1],
            typingUsers.size - 2
        )
    }
}

@Composable
private fun TypingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_dots")
    
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1400
                0.3f at 0
                1f at 200
                0.3f at 400
            }
        ),
        label = "dot1"
    )
    
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1400
                0.3f at 200
                1f at 400
                0.3f at 600
            }
        ),
        label = "dot2"
    )
    
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1400
                0.3f at 400
                1f at 600
                0.3f at 800
            }
        ),
        label = "dot3"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Dot(alpha = dot1Alpha)
        Spacer(modifier = Modifier.width(2.dp))
        Dot(alpha = dot2Alpha)
        Spacer(modifier = Modifier.width(2.dp))
        Dot(alpha = dot3Alpha)
    }
}

@Composable
private fun Dot(alpha: Float) {
    Surface(
        modifier = Modifier
            .width(4.dp)
            .height(4.dp)
            .alpha(alpha),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary
    ) {}
}

