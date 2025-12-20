package ru.fromchat.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ru.fromchat.core.config.Config

@Composable
fun Avatar(
    profilePictureUrl: String?,
    displayName: String,
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    var imageLoadFailed by remember { mutableStateOf(false) }
    
    val gradient = remember(displayName) { generateGradientFromName(displayName) }
    val initials = remember(displayName) { getInitials(displayName) }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        if (profilePictureUrl != null && !imageLoadFailed) {
            val fullUrl = if (profilePictureUrl.startsWith("http")) {
                profilePictureUrl
            } else {
                "${Config.apiBaseUrl}$profilePictureUrl"
            }
            
            AsyncImage(
                model = fullUrl,
                contentDescription = displayName,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                onError = {
                    imageLoadFailed = true
                },
                onSuccess = {
                    imageLoadFailed = false
                }
            )
        }
        
        // Fallback initials
        if (imageLoadFailed || profilePictureUrl == null) {
            Text(
                text = initials,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier
            )
        }
    }
}


