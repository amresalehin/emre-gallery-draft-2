package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MemoryStoryViewer
import com.example.ui.screens.AlbumDetailScreen
import com.example.ui.screens.AlbumsScreen
import com.example.ui.screens.CreateScreen
import com.example.ui.screens.FullscreenViewerScreen
import com.example.ui.screens.PhotosTimelineScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrashBinScreen
import com.example.util.PermissionUtils

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasMediaPermission by viewModel.hasMediaPermission.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it } || PermissionUtils.hasMediaPermission(context)
        viewModel.onPermissionResult(granted)
    }

    // Automatically ask for device media permissions when Lumina launches
    LaunchedEffect(Unit) {
        if (!PermissionUtils.hasMediaPermission(context)) {
            permissionLauncher.launch(PermissionUtils.getRequiredMediaPermissions())
        } else {
            viewModel.onPermissionResult(true)
        }
    }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val allMedia by viewModel.allMediaItems.collectAsStateWithLifecycle()
    val timelineMedia by viewModel.timelineMedia.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val highlights = viewModel.highlights

    val gridDensity by viewModel.gridDensity.collectAsStateWithLifecycle()
    val currentFilter by viewModel.timelineFilter.collectAsStateWithLifecycle()

    val selectedMediaIds by viewModel.selectedMediaIds.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()

    val viewingMedia by viewModel.viewingMedia.collectAsStateWithLifecycle()
    val viewingList by viewModel.viewingList.collectAsStateWithLifecycle()

    val activeStory by viewModel.activeStory.collectAsStateWithLifecycle()
    val viewingAlbumId by viewModel.viewingAlbumId.collectAsStateWithLifecycle()
    val isTrashOpen by viewModel.isTrashOpen.collectAsStateWithLifecycle()
    val isSearchOpen by viewModel.isSearchOpen.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterTag by viewModel.filterTag.collectAsStateWithLifecycle()
    val filterLocation by viewModel.filterLocation.collectAsStateWithLifecycle()
    val filterCamera by viewModel.filterCamera.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val trashMedia by viewModel.trashMedia.collectAsStateWithLifecycle()

    // Handle back button on root tabs
    BackHandler(enabled = isSelectionMode || isSearchOpen || currentTab != 0) {
        if (isSelectionMode) {
            viewModel.clearSelection()
        } else if (isSearchOpen) {
            viewModel.closeSearch()
        } else if (currentTab != 0) {
            viewModel.setTab(0)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentTab) {
                0 -> PhotosTimelineScreen(
                    mediaList = timelineMedia,
                    highlights = highlights,
                    selectedMediaIds = selectedMediaIds,
                    isSelectionMode = isSelectionMode,
                    gridDensity = 4,
                    currentFilter = currentFilter,
                    hasPermission = hasMediaPermission,
                    onRequestPermission = {
                        permissionLauncher.launch(PermissionUtils.getRequiredMediaPermissions())
                    },
                    onMediaClick = { media -> viewModel.openViewer(media, timelineMedia) },
                    onMediaLongClick = { media -> viewModel.toggleSelection(media.id) },
                    onToggleSelect = { id -> viewModel.toggleSelection(id) },
                    onSelectAll = { items -> viewModel.selectAll(items) },
                    onClearSelection = { viewModel.clearSelection() },
                    onBatchFavorite = { viewModel.batchToggleFavorite() },
                    onBatchDelete = { viewModel.batchMoveToTrash() },
                    onBatchAddToAlbum = { title -> viewModel.createAlbum(title) },
                    onCycleDensity = { viewModel.cycleGridDensity() },
                    onFilterChange = { filter -> viewModel.setTimelineFilter(filter) },
                    onHighlightClick = { hl -> viewModel.openStory(hl) },
                    onSearchClick = { viewModel.openSearch() },
                    modifier = Modifier.fillMaxSize()
                )

                1 -> AlbumsScreen(
                    albums = albums,
                    allMedia = allMedia,
                    trashCount = trashMedia.size,
                    hasPermission = hasMediaPermission,
                    onRequestPermission = {
                        permissionLauncher.launch(PermissionUtils.getRequiredMediaPermissions())
                    },
                    onAlbumClick = { albumId -> viewModel.openAlbum(albumId) },
                    onCreateAlbum = { title, category, colorHex ->
                        viewModel.createAlbum(title, category, colorHex)
                    },
                    onOpenTrash = { viewModel.openTrash() },
                    onOpenFavorites = {
                        viewModel.setTab(0)
                        viewModel.setTimelineFilter(TimelineFilter.FAVORITES)
                    },
                    onSearchClick = { viewModel.openSearch() },
                    modifier = Modifier.fillMaxSize()
                )

                2 -> CreateScreen(
                    onSearchClick = { viewModel.openSearch() },
                    modifier = Modifier.fillMaxSize()
                )

                3 -> SettingsScreen(
                    allMedia = allMedia,
                    onDeleteDuplicate = { id -> viewModel.deleteSingleToTrash(id) },
                    modifier = Modifier.fillMaxSize().statusBarsPadding()
                )
            }
        }

        // Bottom blur backdrop scrim behind floating navigation bar
        if (!isSelectionMode && viewingMedia == null && activeStory == null && viewingAlbumId == null && !isTrashOpen && !isSearchOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(118.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.50f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.88f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.98f)
                            )
                        )
                    )
                    .blur(20.dp)
            )

            // Floating Pill Navigation Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(36.dp),
                shadowElevation = 8.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 14.dp)
                    .navigationBarsPadding()
                    .height(68.dp)
                    .testTag("floating_navigation_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingNavItem(
                        label = "Home",
                        filledIcon = Icons.Filled.Home,
                        outlinedIcon = Icons.Outlined.Home,
                        isSelected = currentTab == 0,
                        onClick = { viewModel.setTab(0) },
                        testTag = "nav_tab_home"
                    )

                    FloatingNavItem(
                        label = "Album",
                        filledIcon = Icons.Filled.PhotoLibrary,
                        outlinedIcon = Icons.Outlined.PhotoLibrary,
                        isSelected = currentTab == 1,
                        onClick = { viewModel.setTab(1) },
                        testTag = "nav_tab_album"
                    )

                    FloatingNavItem(
                        label = "Create",
                        filledIcon = Icons.Filled.AddCircle,
                        outlinedIcon = Icons.Outlined.AddCircle,
                        isSelected = currentTab == 2,
                        onClick = { viewModel.setTab(2) },
                        testTag = "nav_tab_create"
                    )

                    FloatingNavItem(
                        label = "Settings",
                        filledIcon = Icons.Filled.Settings,
                        outlinedIcon = Icons.Outlined.Settings,
                        isSelected = currentTab == 3,
                        onClick = { viewModel.setTab(3) },
                        testTag = "nav_tab_settings"
                    )
                }
            }
        }

        // Sub-screen: Album Detail
        if (viewingAlbumId != null) {
            val selectedAlbum = albums.find { it.id == viewingAlbumId }
            if (selectedAlbum != null) {
                AlbumDetailScreen(
                    album = selectedAlbum,
                    allMedia = allMedia,
                    onBack = { viewModel.closeAlbum() },
                    onDeleteAlbum = { id -> viewModel.deleteAlbum(id) },
                    onMediaClick = { media ->
                        val albumItems = allMedia.filter { it.albumId == viewingAlbumId && !it.isTrash }
                        viewModel.openViewer(media, albumItems)
                    }
                )
            }
        }

        // Sub-screen: Trash Bin
        if (isTrashOpen) {
            TrashBinScreen(
                trashItems = trashMedia,
                onBack = { viewModel.closeTrash() },
                onRestore = { ids -> viewModel.restoreFromTrash(ids) },
                onPermanentlyDelete = { ids -> viewModel.permanentlyDelete(ids) },
                onEmptyTrash = { viewModel.emptyTrash() },
                onMediaClick = { media -> viewModel.openViewer(media, trashMedia) }
            )
        }

        // Sub-screen: Search
        if (isSearchOpen) {
            SearchScreen(
                searchQuery = searchQuery,
                filterTag = filterTag,
                filterLocation = filterLocation,
                filterCamera = filterCamera,
                allMedia = allMedia,
                searchResults = searchResults,
                onQueryChange = { q -> viewModel.setSearchQuery(q) },
                onTagSelect = { tag -> viewModel.setFilterTag(tag) },
                onLocationSelect = { loc -> viewModel.setFilterLocation(loc) },
                onCameraSelect = { cam -> viewModel.setFilterCamera(cam) },
                onClearAllFilters = { viewModel.clearAllSearchFilters() },
                onMediaClick = { media -> viewModel.openViewer(media, searchResults) },
                onBack = { viewModel.closeSearch() },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Fullscreen Photo/Video Viewer
        if (viewingMedia != null) {
            FullscreenViewerScreen(
                initialMedia = viewingMedia!!,
                mediaList = if (viewingList.isNotEmpty()) viewingList else listOf(viewingMedia!!),
                onClose = { viewModel.closeViewer() },
                onToggleFavorite = { id -> viewModel.toggleFavorite(id) },
                onDelete = { id -> viewModel.deleteSingleToTrash(id) },
                onAddTag = { id, tag -> viewModel.addTag(id, tag) },
                onRemoveTag = { id, tag -> viewModel.removeTag(id, tag) },
                onSaveEdit = { id, filter, rot, bright, cont, sat ->
                    viewModel.applyPhotoEdit(id, filter, rot, bright, cont, sat)
                }
            )
        }

        // Fullscreen Story Highlights Viewer
        if (activeStory != null) {
            MemoryStoryViewer(
                highlight = activeStory!!,
                allItems = allMedia,
                onDismissRequest = { viewModel.closeStory() }
            )
        }
    }
}

@Composable
private fun FloatingNavItem(
    label: String,
    filledIcon: ImageVector,
    outlinedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .width(52.dp)
                .height(28.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) filledIcon else outlinedIcon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
