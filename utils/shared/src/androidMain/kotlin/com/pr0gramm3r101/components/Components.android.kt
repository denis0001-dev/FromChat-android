@file:Suppress("NOTHING_TO_INLINE")

package com.pr0gramm3r101.components

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.asAndroidColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import com.pr0gramm3r101.utils.asAndroidScaleType

@Composable
inline fun Mipmap(
    @DrawableRes id: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    val update: (ImageView) -> Unit = {
        it.setImageResource(id)
        it.contentDescription = contentDescription
        it.alpha = alpha
        it.colorFilter = colorFilter?.asAndroidColorFilter()
        it.scaleType = contentScale.asAndroidScaleType()
    }

    AndroidView(
        modifier = modifier,
        factory = {
            ImageView(it).apply {
                update(this)
            }
        },
        update = update
    )
}