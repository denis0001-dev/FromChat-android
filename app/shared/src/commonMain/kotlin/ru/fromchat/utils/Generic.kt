package ru.fromchat.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.only

@Suppress("NOTHING_TO_INLINE")
inline fun WindowInsets.exclude(sides: WindowInsetsSides) = exclude(this.only(sides))