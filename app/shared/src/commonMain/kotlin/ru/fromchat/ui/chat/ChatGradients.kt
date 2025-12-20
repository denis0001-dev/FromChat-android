package ru.fromchat.ui.chat

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * Get gradient brush for own messages
 */
fun getMessageGradient(isDark: Boolean): Brush {
    return if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF9333EA),
                Color(0xFF6366F1),
                Color(0xFF2F68C5)
            ),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFB794F6),
                Color(0xFF818CF8),
                Color(0xFF60A5FA)
            ),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
        )
    }
}

/**
 * Get background gradient brushes for chat background
 */
fun getBackgroundGradients(isDark: Boolean): List<Brush> {
    return if (isDark) {
        listOf(
            Brush.radialGradient(
                colors = listOf(
                    Color(0x26DBA1F9),
                    Color.Transparent
                ),
                center = androidx.compose.ui.geometry.Offset(0.2f, 0.8f),
                radius = 500f
            ),
            Brush.radialGradient(
                colors = listOf(
                    Color(0x26F3B7BE),
                    Color.Transparent
                ),
                center = androidx.compose.ui.geometry.Offset(0.8f, 0.2f),
                radius = 500f
            ),
            Brush.radialGradient(
                colors = listOf(
                    Color(0x1AD0C1DA),
                    Color.Transparent
                ),
                center = androidx.compose.ui.geometry.Offset(0.4f, 0.4f),
                radius = 500f
            )
        )
    } else {
        listOf(
            Brush.radialGradient(
                colors = listOf(
                    Color(0x15B794F6),
                    Color.Transparent
                ),
                center = androidx.compose.ui.geometry.Offset(0.2f, 0.8f),
                radius = 500f
            ),
            Brush.radialGradient(
                colors = listOf(
                    Color(0x15F3B7BE),
                    Color.Transparent
                ),
                center = androidx.compose.ui.geometry.Offset(0.8f, 0.2f),
                radius = 500f
            ),
            Brush.radialGradient(
                colors = listOf(
                    Color(0x0DD0C1DA),
                    Color.Transparent
                ),
                center = androidx.compose.ui.geometry.Offset(0.4f, 0.4f),
                radius = 500f
            )
        )
    }
}

/**
 * Generate a consistent gradient from a name for avatar fallback
 */
fun generateGradientFromName(name: String): Brush {
    val hash = name.hashCode()
    val r = abs(hash % 256)
    val g = abs((hash / 256) % 256)
    val b = abs((hash / 65536) % 256)
    
    // Create two colors based on hash for gradient
    val color1 = Color(
        red = (r + 100).coerceIn(0, 255) / 255f,
        green = (g + 100).coerceIn(0, 255) / 255f,
        blue = (b + 100).coerceIn(0, 255) / 255f
    )
    val color2 = Color(
        red = (r + 50).coerceIn(0, 255) / 255f,
        green = (g + 50).coerceIn(0, 255) / 255f,
        blue = (b + 50).coerceIn(0, 255) / 255f
    )
    
    return Brush.linearGradient(
        colors = listOf(color1, color2),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(100f, 100f)
    )
}

/**
 * Get initials from display name (first 2 words, first letter of each)
 */
fun getInitials(displayName: String): String {
    val words = displayName.trim().split("\\s+".toRegex())
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> {
            val word = words[0]
            if (word.length >= 2) {
                word.take(2).uppercase()
            } else {
                word.uppercase() + "?"
            }
        }
        else -> {
            words.take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
        }
    }
}


