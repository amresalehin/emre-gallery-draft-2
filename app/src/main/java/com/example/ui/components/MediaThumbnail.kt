package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.FilterPresets
import com.example.model.MediaItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaThumbnail(
    media: MediaItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 1f,
    cornerRadius: Dp = 10.dp
) {
    val context = LocalContext.current
    val colorFilter = remember(media.filterPreset, media.brightnessAdjustment, media.contrastAdjustment, media.saturationAdjustment) {
        if (media.filterPreset != "Normal" || media.brightnessAdjustment != 0f || media.contrastAdjustment != 0f || media.saturationAdjustment != 0f) {
            ColorFilter.colorMatrix(
                FilterPresets.buildAdjustmentMatrix(
                    media.filterPreset,
                    media.brightnessAdjustment,
                    media.contrastAdjustment,
                    media.saturationAdjustment
                )
            )
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("media_thumbnail_${media.id}")
    ) {
        // Image
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(media.uri)
                .crossfade(true)
                .build(),
            contentDescription = media.title,
            contentScale = ContentScale.Crop,
            colorFilter = colorFilter,
            modifier = Modifier
                .fillMaxSize()
                .rotate(media.rotationDegrees.toFloat())
        )

        // Gradient shadow at bottom for badges
        if (media.isVideo || media.isFavorite) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .aspectRatio(3f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                        )
                    )
            )
        }

        // Video Badge
        if (media.isVideo) {
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Video",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                    if (media.formattedDuration.isNotEmpty()) {
                        Text(
                            text = media.formattedDuration,
                            color = Color.White,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }

        // Favorite Indicator
        if (media.isFavorite && !isSelectionMode) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite",
                tint = Color(0xFFFA5252),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .size(12.dp)
            )
        }

        // Selection Overlay & Checkbox
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        else Color.Transparent
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Black.copy(alpha = 0.4f)
                    )
                    .border(
                        width = 1.5.dp,
                        color = Color.White,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
