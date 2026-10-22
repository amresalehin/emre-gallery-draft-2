package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.absoluteValue
import com.example.model.FilterPresets
import com.example.model.MediaItem
import com.example.ui.components.ExifBottomSheet
import com.example.ui.components.PhotoEditorDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FullscreenViewerScreen(
    initialMedia: MediaItem,
    mediaList: List<MediaItem>,
    onClose: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDelete: (String) -> Unit,
    onAddTag: (String, String) -> Unit,
    onRemoveTag: (String, String) -> Unit,
    onSaveEdit: (String, String, Int, Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val initialIndex = remember(initialMedia, mediaList) {
        val found = mediaList.indexOfFirst { it.id == initialMedia.id }
        if (found >= 0) found else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { mediaList.size }
    )

    val currentMedia = mediaList.getOrNull(pagerState.currentPage) ?: initialMedia

    var showControls by remember { mutableStateOf(true) }
    var showExifSheet by remember { mutableStateOf(false) }
    var showEditor by remember { mutableStateOf(false) }

    // Video playback simulation
    var isPlayingVideo by remember { mutableStateOf(false) }
    var videoProgress by remember { mutableFloatStateOf(0f) }

    val exifSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    BackHandler {
        if (showExifSheet) {
            coroutineScope.launch { exifSheetState.hide() }
            showExifSheet = false
        } else {
            onClose()
        }
    }

    // Video auto playback simulation
    LaunchedEffect(currentMedia.id, isPlayingVideo) {
        if (currentMedia.isVideo && isPlayingVideo) {
            while (isPlayingVideo && videoProgress < 1f) {
                delay(100)
                videoProgress += 0.01f
            }
            if (videoProgress >= 1f) {
                isPlayingVideo = false
                videoProgress = 0f
            }
        }
    }

    // Reset video progress on page change
    LaunchedEffect(pagerState.currentPage) {
        isPlayingVideo = false
        videoProgress = 0f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("fullscreen_viewer_screen")
    ) {
        // Horizontal Pager for media browsing with smooth carousel transition
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pageMedia = mediaList.getOrNull(page) ?: return@HorizontalPager

            // Smooth carousel scale and alpha transition across pages
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
            val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val scale = 1f - (absOffset * 0.12f)
                        scaleX = scale
                        scaleY = scale
                        alpha = 1f - (absOffset * 0.35f)
                    }
            ) {
                ZoomableMediaView(
                    media = pageMedia,
                    isCurrentPage = (page == pagerState.currentPage),
                    onTap = { showControls = !showControls },
                    onSwipeUp = {
                        coroutineScope.launch {
                            showExifSheet = true
                        }
                    },
                    onSwipeDown = {
                        onClose()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Top App Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("viewer_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = currentMedia.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = "${pagerState.currentPage + 1} of ${mediaList.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    IconButton(
                        onClick = { showExifSheet = true },
                        modifier = Modifier.testTag("viewer_info_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info & EXIF",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Video Player Scrub Bar Overlay (if video)
        if (currentMedia.isVideo) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .navigationBarsPadding()
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        IconButton(
                            onClick = { isPlayingVideo = !isPlayingVideo },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlayingVideo) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlayingVideo) "Pause" else "Play",
                                tint = Color.White
                            )
                        }

                        Slider(
                            value = videoProgress,
                            onValueChange = {
                                videoProgress = it
                                isPlayingVideo = false
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        Text(
                            text = currentMedia.formattedDuration,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Bottom Action Bar (Share, Favorite, Edit, Info, Delete)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .navigationBarsPadding()
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ViewerActionButton(
                        icon = Icons.Default.Share,
                        label = "Share",
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = currentMedia.mimeType
                                putExtra(Intent.EXTRA_SUBJECT, currentMedia.title)
                                putExtra(Intent.EXTRA_TEXT, "Photo: ${currentMedia.title} - ${currentMedia.uri}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Media"))
                        }
                    )

                    ViewerActionButton(
                        icon = if (currentMedia.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        label = if (currentMedia.isFavorite) "Favorited" else "Favorite",
                        tint = if (currentMedia.isFavorite) Color(0xFFFA5252) else Color.White,
                        onClick = { onToggleFavorite(currentMedia.id) }
                    )

                    if (!currentMedia.isVideo) {
                        ViewerActionButton(
                            icon = Icons.Default.Edit,
                            label = "Edit",
                            onClick = { showEditor = true }
                        )
                    }

                    ViewerActionButton(
                        icon = Icons.Default.Info,
                        label = "Details",
                        onClick = { showExifSheet = true }
                    )

                    ViewerActionButton(
                        icon = Icons.Default.Delete,
                        label = "Trash",
                        onClick = { onDelete(currentMedia.id) }
                    )
                }
            }
        }
    }

    // EXIF Bottom Sheet
    if (showExifSheet) {
        ExifBottomSheet(
            media = currentMedia,
            sheetState = exifSheetState,
            onDismissRequest = { showExifSheet = false },
            onAddTag = { tag -> onAddTag(currentMedia.id, tag) },
            onRemoveTag = { tag -> onRemoveTag(currentMedia.id, tag) }
        )
    }

    // Photo Editor Dialog
    if (showEditor) {
        PhotoEditorDialog(
            media = currentMedia,
            onDismissRequest = { showEditor = false },
            onSaveEdit = { filter, rot, bright, cont, sat ->
                onSaveEdit(currentMedia.id, filter, rot, bright, cont, sat)
                showEditor = false
            }
        )
    }
}

@Composable
private fun ZoomableMediaView(
    media: MediaItem,
    isCurrentPage: Boolean,
    onTap: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var targetScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Smooth physics-based spring animation for zoom
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "viewer_zoom_scale"
    )

    // Vertical drag offset for gesture-driven swipe up (details) and swipe down (dismiss)
    val verticalOffsetY = remember { Animatable(0f) }
    val swipeUpThresholdPx = remember(density) { with(density) { 65.dp.toPx() } }
    val swipeDownThresholdPx = remember(density) { with(density) { 95.dp.toPx() } }

    // Reset zoom and offsets when navigating across pages
    LaunchedEffect(isCurrentPage) {
        if (!isCurrentPage) {
            targetScale = 1f
            panOffset = Offset.Zero
            verticalOffsetY.snapTo(0f)
        }
    }

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

    val isZoomed = targetScale > 1.05f

    Box(
        modifier = modifier
            // 1. Double tap to smoothly zoom in/out with spring, single tap to toggle controls
            .pointerInput(isZoomed) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        targetScale = if (targetScale > 1.2f) 1f else 2.5f
                        panOffset = Offset.Zero
                    }
                )
            }
            // 2. When zoomed in, allow smooth pan and pinch-zoom
            .then(
                if (isZoomed) {
                    Modifier.pointerInput(targetScale) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (targetScale * zoom).coerceIn(1f, 4.5f)
                            targetScale = newScale
                            if (newScale > 1f) {
                                val maxOffset = 600f * (newScale - 1f)
                                panOffset = Offset(
                                    x = (panOffset.x + pan.x).coerceIn(-maxOffset, maxOffset),
                                    y = (panOffset.y + pan.y).coerceIn(-maxOffset, maxOffset)
                                )
                            } else {
                                panOffset = Offset.Zero
                            }
                        }
                    }
                } else {
                    // 3. When at 1x, handle smooth vertical drag: swipe up for details, swipe down to close
                    Modifier.pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    val currentY = verticalOffsetY.value
                                    if (currentY < -swipeUpThresholdPx) {
                                        // Swiped up -> smoothly trigger details
                                        verticalOffsetY.animateTo(
                                            0f,
                                            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium)
                                        )
                                        onSwipeUp()
                                    } else if (currentY > swipeDownThresholdPx) {
                                        // Swiped down -> smoothly trigger close
                                        verticalOffsetY.animateTo(
                                            0f,
                                            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium)
                                        )
                                        onSwipeDown()
                                    } else {
                                        // Released before threshold -> spring back smoothly
                                        verticalOffsetY.animateTo(
                                            0f,
                                            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium)
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    verticalOffsetY.animateTo(
                                        0f,
                                        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium)
                                    )
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    val currentY = verticalOffsetY.value
                                    val resistance = if (currentY < 0f) 0.65f else 0.80f
                                    verticalOffsetY.snapTo(currentY + dragAmount * resistance)
                                }
                            }
                        )
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(media.uri)
                .crossfade(true)
                .build(),
            contentDescription = media.title,
            contentScale = ContentScale.Fit,
            colorFilter = colorFilter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                    translationX = if (isZoomed) panOffset.x else 0f
                    translationY = if (isZoomed) panOffset.y else verticalOffsetY.value

                    // Visual feedback during swipe down to dismiss
                    if (!isZoomed && verticalOffsetY.value > 0f) {
                        val pullFraction = (verticalOffsetY.value / 400f).coerceIn(0f, 0.45f)
                        scaleX = (1f - (pullFraction * 0.45f)).coerceAtLeast(0.7f)
                        scaleY = (1f - (pullFraction * 0.45f)).coerceAtLeast(0.7f)
                        alpha = (1f - pullFraction).coerceIn(0.2f, 1f)
                    }
                }
                .rotate(media.rotationDegrees.toFloat())
        )
    }
}

@Composable
private fun ViewerActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color.White
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("action_$label")
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = tint
        )
    }
}
