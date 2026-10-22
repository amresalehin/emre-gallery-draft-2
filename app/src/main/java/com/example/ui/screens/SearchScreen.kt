package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.InsertPhoto
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.MediaItem
import com.example.ui.components.MediaThumbnail

@Composable
fun SearchScreen(
    searchQuery: String,
    filterTag: String?,
    filterLocation: String?,
    filterCamera: String?,
    allMedia: List<MediaItem>,
    searchResults: List<MediaItem>,
    onQueryChange: (String) -> Unit,
    onTagSelect: (String?) -> Unit,
    onLocationSelect: (String?) -> Unit,
    onCameraSelect: (String?) -> Unit,
    onClearAllFilters: () -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler(onBack = onBack)
    }

    val context = LocalContext.current
    val hasActiveFilter = searchQuery.isNotBlank() || filterTag != null || filterLocation != null || filterCamera != null

    // Extract unique tags, locations, cameras from library
    val availableTags = remember(allMedia) {
        allMedia.flatMap { it.tags }.distinct().sorted()
    }
    val availableLocations = remember(allMedia) {
        allMedia.mapNotNull { it.locationName }.filter { it != "Studio" && it != "Local Storage" }.distinct().sorted()
    }
    val availableCameras = remember(allMedia) {
        allMedia.map { it.cameraModel }.distinct().sorted()
    }

    // Dynamic top padding for status bar + floating search pill (66dp)
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val contentTopPadding = statusBarHeight + 66.dp

    // Stats calculations for Explore hub
    val activeMedia = remember(allMedia) { allMedia.filterNot { it.isTrash } }
    val totalCount = activeMedia.size
    val photoCount = remember(activeMedia) { activeMedia.count { !it.isVideo } }
    val videoCount = remember(activeMedia) { activeMedia.count { it.isVideo } }
    val totalSizeBytes = remember(activeMedia) { activeMedia.sumOf { it.sizeBytes } }
    val formattedTotalSize = remember(totalSizeBytes) {
        val mb = totalSizeBytes / (1024.0 * 1024.0)
        if (mb >= 1000.0) String.format("%.2f GB", mb / 1024.0) else String.format("%.1f MB", mb)
    }
    val cameraStats = remember(activeMedia) {
        activeMedia.groupBy { it.cameraModel }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }
    val mimeStats = remember(activeMedia) {
        activeMedia.groupBy { it.mimeType }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (hasActiveFilter) {
            // Search Results Mode
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("search_results_container")
            ) {
                // Top spacer for floating search bar
                Spacer(modifier = Modifier.height(contentTopPadding))

                // Active Filter Badges
                if (filterTag != null || filterLocation != null || filterCamera != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (filterTag != null) {
                            FilterChip(
                                selected = true,
                                onClick = { onTagSelect(null) },
                                label = { Text("Tag: $filterTag") },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                        if (filterLocation != null) {
                            FilterChip(
                                selected = true,
                                onClick = { onLocationSelect(null) },
                                label = { Text("Place: $filterLocation") },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                        if (filterCamera != null) {
                            FilterChip(
                                selected = true,
                                onClick = { onCameraSelect(null) },
                                label = { Text("Gear: $filterCamera") },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                }

                // Results Count Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${searchResults.size} results found",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onClearAllFilters) {
                        Text("Clear filters")
                    }
                }

                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matching photos found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(bottom = 110.dp, start = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchResults, key = { it.id }) { item ->
                            MediaThumbnail(
                                media = item,
                                isSelected = false,
                                isSelectionMode = false,
                                aspectRatio = 1f,
                                cornerRadius = 8.dp,
                                onClick = { onMediaClick(item) },
                                onLongClick = { onMediaClick(item) }
                            )
                        }
                    }
                }
            }
        } else {
            // Explore Hub Mode (Tags, Places, Cameras, Storage & Insights)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .testTag("explore_hub_container")
            ) {
                // Top spacer pushes content down initially, allowing full scroll underneath
                Spacer(modifier = Modifier.height(contentTopPadding))

                // Section: Tags & Topics
                SearchCategoryHeader(title = "Tags & Topics", icon = Icons.Default.Sell)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableTags) { tag ->
                        FilterChip(
                            selected = false,
                            onClick = { onTagSelect(tag) },
                            label = { Text(tag) },
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section: Places
                SearchCategoryHeader(title = "Places", icon = Icons.Default.LocationOn)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(availableLocations) { location ->
                        val coverItem = allMedia.firstOrNull { it.locationName == location }
                        PlaceCard(
                            location = location,
                            count = allMedia.count { it.locationName == location },
                            coverUri = coverItem?.uri,
                            onClick = { onLocationSelect(location) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section: Cameras & Gear
                SearchCategoryHeader(title = "Camera Gear", icon = Icons.Default.CameraAlt)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableCameras) { cam ->
                        FilterChip(
                            selected = false,
                            onClick = { onCameraSelect(cam) },
                            label = { Text(cam) },
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: Storage & Library Insights
                SearchCategoryHeader(title = "Storage & Library Insights", icon = Icons.Default.DataUsage)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricBox(label = "Total Media", value = "$totalCount", icon = Icons.Default.InsertPhoto)
                            MetricBox(label = "Photos", value = "$photoCount", icon = Icons.Default.InsertPhoto)
                            MetricBox(label = "Videos", value = "$videoCount", icon = Icons.Default.Videocam)
                            MetricBox(label = "Storage", value = formattedTotalSize, icon = Icons.Default.PieChart)
                        }

                        if (cameraStats.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Camera Breakdown",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            cameraStats.take(3).forEach { (cam, count) ->
                                val pct = if (totalCount > 0) count.toFloat() / totalCount.toFloat() else 0f
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = cam, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                        Text(text = "$count (${(pct * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    LinearProgressIndicator(
                                        progress = { pct },
                                        strokeCap = StrokeCap.Round,
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.fillMaxWidth().height(5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom spacer for floating bottom navigation bar
                Spacer(modifier = Modifier.height(110.dp))
            }
        }

        // ==========================================
        // Top Blur Backdrop Scrim
        // ==========================================
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

        // ==========================================
        // Floating Top Search Pill (Exact match to Home & Album)
        // ==========================================
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(bottom = 8.dp)
        ) {
            SearchFloatingPill(
                searchQuery = searchQuery,
                hasActiveFilters = hasActiveFilter,
                onQueryChange = onQueryChange,
                onClear = {
                    onQueryChange("")
                    onClearAllFilters()
                },
                onBack = onBack
            )
        }
    }
}

@Composable
private fun SearchFloatingPill(
    searchQuery: String,
    hasActiveFilters: Boolean,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
            .testTag("explore_search_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Search photos, people, places, gear...",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input_field")
                )
            }

            if (searchQuery.isNotEmpty() || hasActiveFilters) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = "Explore",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchCategoryHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PlaceCard(
    location: String,
    count: Int,
    coverUri: String?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (coverUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(coverUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = location,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$count items",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricBox(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
