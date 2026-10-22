package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.BackHandler
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaItem
import com.example.model.MemoryHighlight
import com.example.model.TimelineGroupBy
import com.example.model.TimelineSortDirection
import com.example.model.TimelineSortField
import com.example.model.TimelineViewConfig
import com.example.model.TimelineViewMode
import com.example.ui.TimelineFilter
import com.example.ui.components.MediaThumbnail
import com.example.ui.components.MemoriesCarousel
import com.example.ui.components.TimelineHeader
import com.example.ui.components.TimelineViewControlBottomSheet
import com.example.ui.components.formatDateHeader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosTimelineScreen(
    mediaList: List<MediaItem>,
    highlights: List<MemoryHighlight>,
    selectedMediaIds: Set<String>,
    isSelectionMode: Boolean,
    gridDensity: Int,
    currentFilter: TimelineFilter,
    hasPermission: Boolean = true,
    onRequestPermission: () -> Unit = {},
    onMediaClick: (MediaItem) -> Unit,
    onMediaLongClick: (MediaItem) -> Unit,
    onToggleSelect: (String) -> Unit,
    onSelectAll: (List<MediaItem>) -> Unit,
    onClearSelection: () -> Unit,
    onBatchFavorite: () -> Unit,
    onBatchDelete: () -> Unit,
    onBatchAddToAlbum: (String) -> Unit,
    onCycleDensity: () -> Unit,
    onFilterChange: (TimelineFilter) -> Unit,
    onHighlightClick: (MemoryHighlight) -> Unit,
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showCreateAlbumDialog by remember { mutableStateOf(false) }
    var newAlbumTitle by remember { mutableStateOf("") }

    var showViewControlSheet by remember { mutableStateOf(false) }
    val viewControlSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var viewConfig by remember { mutableStateOf(TimelineViewConfig()) }

    BackHandler(enabled = showViewControlSheet) {
        showViewControlSheet = false
    }

    // 1. Sort media according to viewConfig
    val sortedMedia = remember(mediaList, viewConfig.sortField, viewConfig.sortDirection) {
        val comparator: Comparator<MediaItem> = when (viewConfig.sortField) {
            TimelineSortField.DATE_TAKEN -> compareBy { it.dateTaken }
            TimelineSortField.DATE_ADDED -> compareBy { if (it.dateModified > 0) it.dateModified else it.dateTaken }
            TimelineSortField.FILE_NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }
            TimelineSortField.FILE_SIZE -> compareBy { it.sizeBytes }
        }
        if (viewConfig.sortDirection == TimelineSortDirection.DESCENDING) {
            mediaList.sortedWith(comparator.reversed())
        } else {
            mediaList.sortedWith(comparator)
        }
    }

    // 2. Group media according to viewConfig
    val timelineGroups = remember(sortedMedia, viewConfig.groupBy) {
        when (viewConfig.groupBy) {
            TimelineGroupBy.NONE -> {
                listOf(TimelineMediaGroup(key = "all", title = null, items = sortedMedia))
            }
            TimelineGroupBy.DAY -> {
                sortedMedia.groupBy { item ->
                    val cal = Calendar.getInstance().apply { timeInMillis = item.dateTaken }
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }.map { (dayMillis, items) ->
                    TimelineMediaGroup(
                        key = "day_$dayMillis",
                        title = formatDateHeader(dayMillis),
                        items = items
                    )
                }
            }
            TimelineGroupBy.MONTH -> {
                val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                sortedMedia.groupBy { item ->
                    monthKeyFormat.format(Date(item.dateTaken))
                }.map { (_, items) ->
                    val displayMonth = monthFormat.format(Date(items.first().dateTaken))
                    TimelineMediaGroup(
                        key = "month_$displayMonth",
                        title = displayMonth,
                        items = items
                    )
                }
            }
            TimelineGroupBy.YEAR -> {
                val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
                sortedMedia.groupBy { item ->
                    yearFormat.format(Date(item.dateTaken))
                }.map { (yearStr, items) ->
                    TimelineMediaGroup(
                        key = "year_$yearStr",
                        title = yearStr,
                        items = items
                    )
                }
            }
            TimelineGroupBy.DEVICE_FOLDER -> {
                sortedMedia.groupBy { item ->
                    item.folderName.ifBlank { "Camera" }
                }.map { (folderName, items) ->
                    TimelineMediaGroup(
                        key = "folder_$folderName",
                        title = folderName,
                        items = items
                    )
                }
            }
            TimelineGroupBy.INITIAL -> {
                sortedMedia.groupBy { item ->
                    val firstChar = item.title.trim().take(1).uppercase()
                    if (firstChar.isNotEmpty() && firstChar[0].isLetter()) firstChar else "#"
                }.map { (initial, items) ->
                    TimelineMediaGroup(
                        key = "initial_$initial",
                        title = initial,
                        items = items
                    )
                }
            }
        }
    }

    // Aspect ratio patterns for 4-column organic mosaic look matching screenshot
    val aspectRatios = remember {
        listOf(
            0.75f, 1.0f, 0.70f, 1.0f,
            1.15f, 0.75f, 1.0f, 1.35f,
            0.85f, 1.0f, 0.85f, 0.75f,
            1.20f, 0.75f, 0.65f, 1.15f,
            1.35f, 1.15f, 1.40f, 0.85f
        )
    }

    // Calculate dynamic top padding: status bar height + floating search pill height + margin
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val contentTopPadding = statusBarHeight + 66.dp
    val columns = viewConfig.columnCount

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Page Content Grid: Starts below floating bar and scrolls all the way up underneath it and below status bar!
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(
                top = contentTopPadding,
                bottom = 110.dp,
                start = 12.dp,
                end = 12.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_timeline_grid")
        ) {
            // Permission prompt banner if device media permission not yet granted
            if (!hasPermission) {
                item(span = { GridItemSpan(columns) }) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Device photos permission",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Allow media access to view your device photos and videos.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Button(
                                onClick = onRequestPermission,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Allow")
                            }
                        }
                    }
                }
            }

            // Horizontal Memories Row right below search bar
            if (!isSelectionMode && highlights.isNotEmpty()) {
                item(span = { GridItemSpan(columns) }) {
                    MemoriesCarousel(
                        highlights = highlights,
                        onHighlightClick = onHighlightClick,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            timelineGroups.forEach { group ->
                // Section Header (only shown if not None)
                if (group.title != null) {
                    item(
                        key = "header_${group.key}",
                        span = { GridItemSpan(columns) }
                    ) {
                        TimelineHeader(
                            title = group.title,
                            items = group.items,
                            onSelectDateGroup = {
                                group.items.forEach { onToggleSelect(it.id) }
                            }
                        )
                    }
                }

                // Photo Grid items
                itemsIndexed(group.items, key = { _, item -> "${group.key}_${item.id}" }) { index, item ->
                    val isSelected = selectedMediaIds.contains(item.id)
                    val ratio = if (viewConfig.viewMode == TimelineViewMode.GRID) 1.0f else aspectRatios[index % aspectRatios.size]

                    MediaThumbnail(
                        media = item,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        aspectRatio = ratio,
                        cornerRadius = 10.dp,
                        onClick = {
                            if (isSelectionMode) {
                                onToggleSelect(item.id)
                            } else {
                                onMediaClick(item)
                            }
                        },
                        onLongClick = {
                            onMediaLongClick(item)
                        }
                    )
                }
            }

            // Extra space for floating bottom navigation bar
            item(span = { GridItemSpan(columns) }) {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Top Blur Backdrop Scrim
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(contentTopPadding + 14.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    )
                )
                .blur(20.dp)
        )

        // Floating Top Bar Overlay above scrolling page content
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.70f),
                            Color.Transparent
                        )
                    )
                )
                .statusBarsPadding()
                .padding(bottom = 8.dp)
        ) {
            if (isSelectionMode) {
                SelectionTopAppBar(
                    selectedCount = selectedMediaIds.size,
                    onClearSelection = onClearSelection,
                    onSelectAll = { onSelectAll(mediaList) },
                    onBatchFavorite = onBatchFavorite,
                    onBatchDelete = onBatchDelete,
                    onAddToAlbum = { showCreateAlbumDialog = true }
                )
            } else {
                HomeSearchPill(
                    onSearchClick = onSearchClick,
                    onVisualSearchClick = { showViewControlSheet = true }
                )
            }
        }
    }

    // Add To Album Dialog
    if (showCreateAlbumDialog) {
        AlertDialog(
            onDismissRequest = { showCreateAlbumDialog = false },
            title = { Text("Add to New Album") },
            text = {
                OutlinedTextField(
                    value = newAlbumTitle,
                    onValueChange = { newAlbumTitle = it },
                    label = { Text("Album Name") },
                    placeholder = { Text("e.g. Summer Memories") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAlbumTitle.isNotBlank()) {
                            onBatchAddToAlbum(newAlbumTitle)
                            newAlbumTitle = ""
                            showCreateAlbumDialog = false
                        }
                    }
                ) {
                    Text("Create & Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateAlbumDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // View Controls Drawer Sheet (opened from eye button on Home Search Bar)
    if (showViewControlSheet) {
        TimelineViewControlBottomSheet(
            currentConfig = viewConfig,
            sheetState = viewControlSheetState,
            onDismissRequest = { showViewControlSheet = false },
            onApplyConfig = { newConfig ->
                viewConfig = newConfig
                showViewControlSheet = false
            }
        )
    }
}

private data class TimelineMediaGroup(
    val key: String,
    val title: String?,
    val items: List<MediaItem>
)

@Composable
private fun HomeSearchPill(
    onSearchClick: () -> Unit,
    onVisualSearchClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(onClick = onSearchClick)
            .testTag("home_search_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Search photos, people, places...",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onVisualSearchClick,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("home_search_eye_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = "View Controls",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopAppBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onBatchFavorite: () -> Unit,
    onBatchDelete: () -> Unit,
    onAddToAlbum: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, contentDescription = "Close Selection")
            }
        },
        title = {
            Text(
                text = "$selectedCount selected",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Select All")
            }
            IconButton(onClick = onBatchFavorite) {
                Icon(Icons.Default.Favorite, contentDescription = "Favorite")
            }
            IconButton(onClick = onAddToAlbum) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = "Add to Album")
            }
            IconButton(onClick = onBatchDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Move to Trash")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    )
}
