package ru.fromchat.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun getColorScheme(darkTheme: Boolean, dynamicColor: Boolean) =
    if (darkTheme) darkColorScheme()
    else lightColorScheme()