@file:Suppress("UnusedReceiverParameter", "unused")

package com.pr0gramm3r101.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.Language
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.vectorResource
import com.pr0gramm3r101.utils.Res
import com.pr0gramm3r101.utils.license
import com.pr0gramm3r101.utils.siren_filled
import com.pr0gramm3r101.utils.siren_outlined

inline val Icons.Outlined.Internet get() = Icons.Outlined.Language
inline val Icons.Filled.Internet get() = Icons.Filled.Language

inline val Icons.Outlined.Website get() = Icons.Outlined.Language
inline val Icons.Filled.Website get() = Icons.Filled.Language

inline val Icons.Outlined.License
    @Composable get() = vectorResource(Res.drawable.license)
inline val Icons.Filled.License
    @Composable get() = Icons.Outlined.License

inline val Icons.Outlined.Siren
    @Composable get() = vectorResource(Res.drawable.siren_outlined)
inline val Icons.Filled.Siren
    @Composable get() = vectorResource(Res.drawable.siren_filled)