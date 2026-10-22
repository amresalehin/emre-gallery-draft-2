package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.MediaItem
import com.example.model.MemoryHighlight

@Composable
fun MemoryStoryViewer(
    highlight: MemoryHighlight,
    allItems: List<MediaItem>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val storyItems = remember(highlight, allItems) {
        val mapped = highlight.mediaIds.mapNotNull { id -> allItems.find { it.id == id } }
        if (mapped.isNotEmpty()) mapped else allItems.take(4)
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }

    val currentMedia = storyItems.getOrNull(currentIndex) ?: return

    // Auto-advance timer
    LaunchedEffect(currentIndex, isPaused) {
        if (!isPaused) {
            val progress = Animatable(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 4000, easing = LinearEasing)
            )
            if (currentIndex < storyItems.lastIndex) {
                currentIndex++
            } else {
                onDismissRequest()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("memory_story_viewer"),
            color = Color.Black
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPaused = true
                                tryAwaitRelease()
                                isPaused = false
                            },
                            onTap = { offset ->
                                val screenWidth = size.width
                                if (offset.x < screenWidth * 0.35f) {
                                    if (currentIndex > 0) currentIndex--
                                } else {
                                    if (currentIndex < storyItems.lastIndex) currentIndex++ else onDismissRequest()
                                }
                            }
                        )
                    }
            ) {
                // Background Photo
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(currentMedia.uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = currentMedia.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Vignette Scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                )

                // Bottom Vignette Scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                // Top Controls: Story Segments & Title
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Progress Bars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        storyItems.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        when {
                                            index < currentIndex -> Color.White
                                            index == currentIndex -> Color.White.copy(alpha = 0.9f)
                                            else -> Color.White.copy(alpha = 0.3f)
                                        }
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = highlight.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = highlight.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Bottom Details (Media Title & Location)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Text(
                        text = currentMedia.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    if (currentMedia.locationName != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = currentMedia.locationName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "${currentMedia.cameraModel} • ${currentMedia.formattedSize}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
