package ru.fromchat.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFDBB9F9),
    onPrimary = Color(0xFF3E2458),
    primaryContainer = Color(0xFF563B71),
    onPrimaryContainer = Color(0xFFF0DBFF),
    secondary = Color(0xFFD0C1DA),
    onSecondary = Color(0xFF362C3F),
    secondaryContainer = Color(0xFF4D4356),
    onSecondaryContainer = Color(0xFFEDDDF6),
    tertiary = Color(0xFFF3B7BE),
    onTertiary = Color(0xFF4B252B),
    tertiaryContainer = Color(0xFF653A40),
    onTertiaryContainer = Color(0xFFFFD9DD),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF151218),
    onBackground = Color(0xFFE8E0E8),
    surface = Color(0xFF151218),
    onSurface = Color(0xFFE8E0E8),
    surfaceVariant = Color(0xFF4A454E),
    onSurfaceVariant = Color(0xFFCCC4CE),
    outline = Color(0xFF968E98),
    outlineVariant = Color(0xFF4A454E),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE8E0E8),
    inverseOnSurface = Color(0xFF332F35),
    inversePrimary = Color(0xFF6F528A),
    surfaceDim = Color(0xFF151218),
    surfaceBright = Color(0xFF3C383E),
    surfaceContainerLowest = Color(0xFF100D12),
    surfaceContainerLow = Color(0xFF1E1A20),
    surfaceContainer = Color(0xFF221E24),
    surfaceContainerHigh = Color(0xFF2C292E),
    surfaceContainerHighest = Color(0xFF373339),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1F6586),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC5E7FF),
    onPrimaryContainer = Color(0xFF004C6A),
    secondary = Color(0xFF4E616D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2E5F4),
    onSecondaryContainer = Color(0xFF374955),
    tertiary = Color(0xFF615A7C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE7DEFF),
    onTertiaryContainer = Color(0xFF494263),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFF6FAFE),
    onBackground = Color(0xFF181C1F),
    surface = Color(0xFFF6FAFE),
    onSurface = Color(0xFF181C1F),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41484D),
    outline = Color(0xFF71787E),
    outlineVariant = Color(0xFFC1C7CE),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2C3134),
    inverseOnSurface = Color(0xFFEDF1F5),
    inversePrimary = Color(0xFF91CEF4),
    surfaceDim = Color(0xFFD7DADF),
    surfaceBright = Color(0xFFF6FAFE),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F4F8),
    surfaceContainer = Color(0xFFEBEEF3),
    surfaceContainerHigh = Color(0xFFE5E8ED),
    surfaceContainerHighest = Color(0xFFDFE3E7),
)

@Composable
fun FromChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled for multiplatform compatibility
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}