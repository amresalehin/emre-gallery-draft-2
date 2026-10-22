package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.Album
import com.example.model.AlbumCategory
import com.example.model.MediaItem

enum class AlbumSortOrder {
    NAME,
    COUNT_DESC,
    RECENT
}

@Composable
fun AlbumsScreen(
    albums: List<Album>,
    allMedia: List<MediaItem>,
    trashCount: Int,
    onAlbumClick: (String) -> Unit,
    onCreateAlbum: (String, AlbumCategory, Long) -> Unit = { _, _, _ -> },
    hasPermission: Boolean = true,
    onRequestPermission: () -> Unit = {},
    onOpenTrash: () -> Unit,
    onOpenFavorites: () -> Unit,
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(AlbumSortOrder.NAME) }
    var showSortMenu by remember { mutableStateOf(false) }

    var showCreateVisionDialog by remember { mutableStateOf(false) }
    var showCreateNestedDialog by remember { mutableStateOf(false) }
    var showHiddenDialog by remember { mutableStateOf(false) }

    val nonTrash = remember(allMedia) { allMedia.filterNot { it.isTrash } }
    val favoritesCount = remember(nonTrash) { nonTrash.count { it.isFavorite } }
    val videosCount = remember(nonTrash) { nonTrash.count { it.isVideo } }
    val hiddenCount = remember(nonTrash) { nonTrash.count { it.isArchived } }

    var showPeopleDialog by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }

    val peopleAndPetsMedia = remember(nonTrash) {
        val filtered = nonTrash.filter { item ->
            item.tags.any { tag ->
                tag.equals("Portrait", ignoreCase = true) ||
                tag.equals("People", ignoreCase = true) ||
                tag.equals("Cat", ignoreCase = true) ||
                tag.equals("Dog", ignoreCase = true) ||
                tag.equals("Pets", ignoreCase = true) ||
                tag.equals("Friends", ignoreCase = true) ||
                tag.equals("Family", ignoreCase = true)
            } || item.title.contains("Portrait", ignoreCase = true) ||
               item.title.contains("Cat", ignoreCase = true) ||
               item.title.contains("Dog", ignoreCase = true)
        }
        if (filtered.isNotEmpty()) filtered else nonTrash.take(4)
    }

    val geotaggedPlaces = remember(nonTrash) {
        val mapped = nonTrash
            .filter { it.locationName != null && it.locationName != "Studio" && it.locationName != "Local Storage" }
            .groupBy { it.locationName!! }
        if (mapped.isNotEmpty()) mapped else mapOf("Local Library" to nonTrash.take(4))
    }

    // 1. Device Folders derived dynamically from actual media items (NO fake counts!)
    val deviceFolders = remember(nonTrash, searchQuery, sortOrder) {
        val groups = nonTrash
            .groupBy { it.folderName }
            .map { (folder, items) ->
                Album(
                    id = "folder_${folder.lowercase().replace(" ", "_")}",
                    title = folder,
                    coverUri = items.firstOrNull()?.uri,
                    itemCount = items.size,
                    category = AlbumCategory.DEVICE_FOLDER
                )
            }
        val filtered = if (searchQuery.isBlank()) groups
        else groups.filter { it.title.contains(searchQuery, ignoreCase = true) }

        when (sortOrder) {
            AlbumSortOrder.NAME -> filtered.sortedBy { it.title.lowercase() }
            AlbumSortOrder.COUNT_DESC -> filtered.sortedByDescending { it.itemCount }
            AlbumSortOrder.RECENT -> filtered
        }
    }

    // 2. My Vision Smart Collections (User-created only, NO fake counts!)
    val visionAlbums = remember(albums, allMedia, searchQuery, sortOrder) {
        val list = albums.filter { it.category == AlbumCategory.MY_VISION }.map { album ->
            val count = allMedia.count { it.albumId == album.id && !it.isTrash }
            val cover = allMedia.firstOrNull { it.albumId == album.id && !it.isTrash }?.uri ?: album.coverUri
            album.copy(itemCount = count, coverUri = cover)
        }
        val filtered = if (searchQuery.isBlank()) list
        else list.filter { it.title.contains(searchQuery, ignoreCase = true) }

        when (sortOrder) {
            AlbumSortOrder.NAME -> filtered.sortedBy { it.title.lowercase() }
            AlbumSortOrder.COUNT_DESC -> filtered.sortedByDescending { it.itemCount }
            AlbumSortOrder.RECENT -> filtered
        }
    }

    // 3. Nested Albums with 2x2 collage (User-created only, NO fake counts!)
    val nestedAlbums = remember(albums, allMedia, searchQuery, sortOrder) {
        val list = albums.filter { it.category == AlbumCategory.NESTED_ALBUM }.map { album ->
            val matching = allMedia.filter { it.albumId == album.id && !it.isTrash }
            val collage = matching.take(4).map { it.uri }
            album.copy(
                itemCount = matching.size,
                coverUri = matching.firstOrNull()?.uri ?: album.coverUri,
                collageUris = collage
            )
        }
        val filtered = if (searchQuery.isBlank()) list
        else list.filter { it.title.contains(searchQuery, ignoreCase = true) }

        when (sortOrder) {
            AlbumSortOrder.NAME -> filtered.sortedBy { it.title.lowercase() }
            AlbumSortOrder.COUNT_DESC -> filtered.sortedByDescending { it.itemCount }
            AlbumSortOrder.RECENT -> filtered
        }
    }

    // Dynamic top padding for status bar + floating search pill (66dp)
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val contentTopPadding = statusBarHeight + 66.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scrollable content: Starts below the floating bar, scrolls underneath it and status bar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .testTag("albums_screen")
        ) {
            // Top spacer pushes content down initially, allowing full scroll underneath
            Spacer(modifier = Modifier.height(contentTopPadding))

            // ==========================================
            // 1. People and Pets (Dedicated Row - Our Style)
            // ==========================================
            PeopleAndPetsCard(
                count = peopleAndPetsMedia.size,
                mediaList = peopleAndPetsMedia,
                coverUris = peopleAndPetsMedia.map { it.uri },
                onClick = { showPeopleDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 2. Albums: My Vision (Outlined Section Card)
            // ==========================================
            SectionContainerCard(
                modifier = Modifier.testTag("my_vision_section_card")
            ) {
                SectionHeader(
                    icon = Icons.Default.AutoAwesome,
                    title = "My Vision",
                    countLabel = "${visionAlbums.size} collections",
                    onCreateClick = { showCreateVisionDialog = true },
                    onSeeAllClick = {
                        Toast.makeText(context, "${visionAlbums.size} Vision collections", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (visionAlbums.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(visionAlbums, key = { it.id }) { album ->
                            VisionAlbumCard(
                                album = album,
                                onClick = { onAlbumClick(album.id) }
                            )
                        }
                    }
                } else {
                    InlineSectionEmptyState(
                        message = "No Vision collections created yet"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 3. Nested albums (Outlined Section Card)
            // ==========================================
            SectionContainerCard(
                modifier = Modifier.testTag("nested_albums_section_card")
            ) {
                SectionHeader(
                    icon = Icons.Outlined.Folder,
                    title = "Nested albums",
                    countLabel = "${nestedAlbums.size} groups",
                    onCreateClick = { showCreateNestedDialog = true },
                    onSeeAllClick = {
                        Toast.makeText(context, "${nestedAlbums.size} Nested albums", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (nestedAlbums.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(nestedAlbums, key = { it.id }) { album ->
                            NestedAlbumFolderCard(
                                album = album,
                                onClick = { onAlbumClick(album.id) }
                            )
                        }
                    }
                } else {
                    InlineSectionEmptyState(
                        message = "No nested albums created yet"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 4. Map Section Card (Below Nested albums)
            // ==========================================
            MapPlacesCard(
                placesCount = geotaggedPlaces.size,
                places = geotaggedPlaces,
                coverUri = geotaggedPlaces.values.firstOrNull()?.firstOrNull()?.uri ?: nonTrash.firstOrNull()?.uri,
                onClick = { showMapDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 5. Device folders (Outlined Section Card)
            // ==========================================
            SectionContainerCard(
                modifier = Modifier.testTag("device_folders_section_card")
            ) {
                SectionHeader(
                    icon = Icons.Outlined.PhoneAndroid,
                    title = "Device folders",
                    countLabel = "${deviceFolders.size} folders",
                    onSeeAllClick = {
                        Toast.makeText(context, "${deviceFolders.size} Device folders", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (deviceFolders.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(deviceFolders, key = { it.id }) { album ->
                            DeviceFolderCard(
                                album = album,
                                onClick = { onAlbumClick(album.id) }
                            )
                        }
                    }
                } else {
                    InlineSectionEmptyState(
                        message = if (searchQuery.isNotBlank()) "No device folders matching \"$searchQuery\""
                                  else if (!hasPermission) "Storage permission needed to view folders"
                                  else "No device folders found on this device",
                        actionLabel = if (!hasPermission) "Grant Permission" else null,
                        onActionClick = if (!hasPermission) onRequestPermission else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 6. Other collections (Outlined Section Card)
            // ==========================================
            SectionContainerCard(
                modifier = Modifier.testTag("other_collections_section_card")
            ) {
                SectionHeader(
                    icon = Icons.Outlined.GridView,
                    title = "Other collections",
                    countLabel = null,
                    onSeeAllClick = {},
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OtherCollectionTile(
                        icon = Icons.Default.Favorite,
                        title = "Favourites",
                        count = "$favoritesCount items",
                        iconTint = Color(0xFFFA5252),
                        onClick = onOpenFavorites,
                        modifier = Modifier.weight(1f)
                    )

                    OtherCollectionTile(
                        icon = Icons.Default.PlayCircle,
                        title = "Videos",
                        count = "$videosCount items",
                        iconTint = MaterialTheme.colorScheme.onSurface,
                        onClick = onOpenFavorites,
                        modifier = Modifier.weight(1f)
                    )

                    OtherCollectionTile(
                        icon = Icons.Outlined.AccessTime,
                        title = "Recently added",
                        count = "${nonTrash.size} items",
                        iconTint = MaterialTheme.colorScheme.onSurface,
                        onClick = {
                            Toast.makeText(context, "Recently added: ${nonTrash.size} items", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    OtherCollectionTile(
                        icon = Icons.Outlined.Lock,
                        title = "Hidden",
                        count = "$hiddenCount items",
                        iconTint = MaterialTheme.colorScheme.onSurface,
                        onClick = { showHiddenDialog = true },
                        modifier = Modifier.weight(1f)
                    )

                    OtherCollectionTile(
                        icon = Icons.Outlined.Delete,
                        title = "Bin",
                        count = "$trashCount items",
                        iconTint = MaterialTheme.colorScheme.onSurface,
                        onClick = onOpenTrash,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Bottom spacer for floating bottom navigation bar
            Spacer(modifier = Modifier.height(110.dp))
        }

        // ==========================================
        // Floating Top Search Pill & Blur Backdrop
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

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.70f),
                            Color.Transparent
                        )
                    )
                )
                .statusBarsPadding()
                .padding(bottom = 8.dp)
        ) {
            AlbumSearchPill(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                onClearQuery = { searchQuery = "" },
                onSortClick = { showSortMenu = true }
            )

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                DropdownMenuItem(
                    text = { Text("Sort by Name") },
                    onClick = {
                        sortOrder = AlbumSortOrder.NAME
                        showSortMenu = false
                        Toast.makeText(context, "Sorted by Name", Toast.LENGTH_SHORT).show()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Sort by Item Count") },
                    onClick = {
                        sortOrder = AlbumSortOrder.COUNT_DESC
                        showSortMenu = false
                        Toast.makeText(context, "Sorted by Count", Toast.LENGTH_SHORT).show()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Sort by Recent") },
                    onClick = {
                        sortOrder = AlbumSortOrder.RECENT
                        showSortMenu = false
                        Toast.makeText(context, "Sorted by Recent", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Create Vision Collection Dialog
    if (showCreateVisionDialog) {
        CreateVisionDialog(
            onDismiss = { showCreateVisionDialog = false },
            onConfirm = { title ->
                onCreateAlbum(title, AlbumCategory.MY_VISION, 0xFF4285F4)
                showCreateVisionDialog = false
                Toast.makeText(context, "Collection \"$title\" created", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Create Nested Album Dialog
    if (showCreateNestedDialog) {
        CreateNestedDialog(
            onDismiss = { showCreateNestedDialog = false },
            onConfirm = { title, colorHex ->
                onCreateAlbum(title, AlbumCategory.NESTED_ALBUM, colorHex)
                showCreateNestedDialog = false
                Toast.makeText(context, "Nested album \"$title\" created", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Hidden Folder Vault Dialog
    if (showHiddenDialog) {
        AlertDialog(
            onDismissRequest = { showHiddenDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Hidden Vault")
                }
            },
            text = {
                Text(
                    text = if (hiddenCount == 0) {
                        "No hidden items yet. You can move photos to this secure folder to hide them from the main timeline."
                    } else {
                        "Your $hiddenCount hidden items are protected with on-device biometric security."
                    }
                )
            },
            confirmButton = {
                Button(onClick = { showHiddenDialog = false }) {
                    Text(if (hiddenCount > 0) "Unlock Vault" else "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHiddenDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // People & Pets Dialog
    if (showPeopleDialog) {
        PeopleAndPetsDialog(
            mediaList = peopleAndPetsMedia,
            onDismiss = { showPeopleDialog = false }
        )
    }

    // Photo Map Dialog
    if (showMapDialog) {
        PhotoMapDialog(
            places = geotaggedPlaces,
            onDismiss = { showMapDialog = false }
        )
    }
}

// -------------------------------------------------------------
// Floating Search Bar Pill (Matching Home page capsule design)
// -------------------------------------------------------------

@Composable
private fun AlbumSearchPill(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSortClick: () -> Unit,
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
            .testTag("album_search_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Search folders, albums...",
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
                        .testTag("album_search_input")
                )
            }

            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = onClearQuery,
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
                IconButton(
                    onClick = onSortClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Sort,
                        contentDescription = "Sort",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Component Cards & Sections
// -------------------------------------------------------------

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    countLabel: String?,
    onCreateClick: (() -> Unit)? = null,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            if (countLabel != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• $countLabel",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (onCreateClick != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onCreateClick)
                        .testTag("create_chip_${title.lowercase().replace(" ", "_")}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Create",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onSeeAllClick)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun DeviceFolderCard(
    album: Album,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(108.dp)
            .clickable(onClick = onClick)
            .testTag("device_folder_${album.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            if (album.coverUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(album.coverUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = album.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Small translucent folder badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open Folder") },
                        onClick = { showMenu = false; onClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Folder Info") },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "${album.title} • ${album.itemCount} items", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        Text(
            text = "${String.format("%,d", album.itemCount)} items",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VisionAlbumCard(
    album: Album,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .width(108.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            if (album.coverUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(album.coverUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = album.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }

        Text(
            text = "${String.format("%,d", album.itemCount)} items",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NestedAlbumFolderCard(
    album: Album,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val folderColor = Color(album.folderColorHex)

    Column(
        modifier = Modifier
            .width(108.dp)
            .clickable(onClick = onClick)
            .testTag("nested_album_${album.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(folderColor.copy(alpha = 0.35f))
                .padding(5.dp)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${album.nestedAlbumCount.coerceAtLeast(1)}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2x2 Image Collage inside folder window
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                val collage = album.collageUris.take(4)
                if (collage.isNotEmpty()) {
                    Row(modifier = Modifier.weight(1f)) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(collage.getOrNull(0) ?: album.coverUri).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .padding(1.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(collage.getOrNull(1) ?: album.coverUri).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .padding(1.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                    Row(modifier = Modifier.weight(1f)) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(collage.getOrNull(2) ?: album.coverUri).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .padding(1.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(collage.getOrNull(3) ?: album.coverUri).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .padding(1.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = folderColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }

        Text(
            text = "${String.format("%,d", album.itemCount)} items",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun OtherCollectionTile(
    icon: ImageVector,
    title: String,
    count: String,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .height(98.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = count,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EmptySectionCard(
    message: String,
    modifier: Modifier = Modifier
) {
    InlineSectionEmptyState(
        message = message,
        modifier = modifier
    )
}

@Composable
private fun SectionContainerCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.65f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            content = content
        )
    }
}

@Composable
private fun InlineSectionEmptyState(
    message: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun WowSuchEmptyCard(
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("wow_such_empty_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle ?: "No items yet",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDeviceFoldersCard(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (!hasPermission) "Media permission required" else "No device folders found",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (!hasPermission)
                    "Allow Lumina to access device media to view and organize your camera roll and device folders."
                else
                    "Photos and videos saved to your device will automatically appear organized into folders.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!hasPermission) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Outlined.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Grant Permission")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Dialogs
// -------------------------------------------------------------

@Composable
private fun CreateVisionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    val presetSuggestions = listOf("Travel Dreams", "Nature", "Architecture", "Portraits", "Sunset")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Vision Collection") },
        text = {
            Column {
                Text(
                    text = "Name your smart collection. Lumina will organize matching photos automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Collection Title") },
                    placeholder = { Text("e.g. Travel Dreams") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Suggestions:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presetSuggestions) { suggestion ->
                        FilterChip(
                            selected = title == suggestion,
                            onClick = { title = suggestion },
                            label = { Text(suggestion, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onConfirm(title.trim())
                },
                enabled = title.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CreateNestedDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedColor by remember { mutableLongStateOf(0xFFFFB74D) }

    val colorOptions = listOf(
        0xFFFFB74D to "Amber",
        0xFF42A5F5 to "Blue",
        0xFFBA68C8 to "Violet",
        0xFF66BB6A to "Mint",
        0xFFEF5350 to "Rose"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Nested Album") },
        text = {
            Column {
                Text(
                    text = "Combine multiple albums into a color-coded nested folder.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Folder Name") },
                    placeholder = { Text("e.g. Personal, Work, Projects") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Folder Color:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorOptions.forEach { (colorHex, _) ->
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onConfirm(title.trim(), selectedColor)
                },
                enabled = title.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// -------------------------------------------------------------
// People & Pets Card (Our Style - Dedicated Row)
// -------------------------------------------------------------

@Composable
fun PeopleAndPetsCard(
    count: Int,
    mediaList: List<MediaItem> = emptyList(),
    coverUris: List<String> = emptyList(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemsToShow = if (mediaList.isNotEmpty()) mediaList else emptyList()

    Card(
        modifier = modifier
            .testTag("people_and_pets_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
        ) {
            // Header with title, count, and See all
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "People & pets",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $count subjects",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onClick)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal row of recognized subjects
            if (itemsToShow.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(itemsToShow.take(8), key = { it.id }) { item ->
                        val isPet = item.tags.any {
                            it.equals("Cat", ignoreCase = true) ||
                            it.equals("Dog", ignoreCase = true) ||
                            it.equals("Pets", ignoreCase = true)
                        } || item.title.contains("Cat", ignoreCase = true)

                        val subjectLabel = when {
                            item.tags.contains("Cat") || item.title.contains("Cat", ignoreCase = true) -> "Tabby Cat"
                            item.tags.contains("Portrait") || item.title.contains("Portrait", ignoreCase = true) -> "Portrait"
                            else -> item.title.take(12)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onClick)
                                .padding(2.dp)
                        ) {
                            Box(modifier = Modifier.size(62.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    modifier = Modifier.fillMaxSize(),
                                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.surfaceVariant),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(item.uri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = if (isPet) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.BottomEnd)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isPet) Icons.Default.Pets else Icons.Default.Face,
                                            contentDescription = null,
                                            tint = if (isPet) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = subjectLabel,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                InlineSectionEmptyState(
                    message = "No people or pets recognized yet",
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Photo Map Card (Our Style - Below Nested Albums)
// -------------------------------------------------------------

@Composable
fun MapPlacesCard(
    placesCount: Int = 0,
    coverUri: String? = null,
    places: Map<String, List<MediaItem>> = emptyMap(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("map_places_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
        ) {
            // Header with title, count, and Explore
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your map",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    val count = if (places.isNotEmpty()) places.size else placesCount
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $count locations",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onClick)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Explore",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal preview of destinations
            if (places.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(places.entries.toList(), key = { it.key }) { (placeName, items) ->
                        val placeCover = items.firstOrNull()?.uri ?: coverUri
                        Box(
                            modifier = Modifier
                                .width(136.dp)
                                .height(94.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clickable(onClick = onClick)
                        ) {
                            if (!placeCover.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(placeCover)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = placeName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Clean gradient scrim
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.75f)
                                            ),
                                            startY = 30f
                                        )
                                    )
                            )

                            // Location label badge
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = placeName,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "${items.size} photos",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            } else {
                InlineSectionEmptyState(
                    message = "No geotagged locations available",
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// People & Pets Interactive Viewer Dialog
// -------------------------------------------------------------

@Composable
fun PeopleAndPetsDialog(
    mediaList: List<MediaItem>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredList = remember(mediaList, selectedFilter) {
        when (selectedFilter) {
            "People" -> mediaList.filter { item ->
                item.tags.any { tag ->
                    tag.equals("Portrait", ignoreCase = true) ||
                    tag.equals("People", ignoreCase = true) ||
                    tag.equals("Friends", ignoreCase = true) ||
                    tag.equals("Family", ignoreCase = true)
                } || item.title.contains("Portrait", ignoreCase = true)
            }
            "Pets" -> mediaList.filter { item ->
                item.tags.any { tag ->
                    tag.equals("Cat", ignoreCase = true) ||
                    tag.equals("Dog", ignoreCase = true) ||
                    tag.equals("Pets", ignoreCase = true)
                } || item.title.contains("Cat", ignoreCase = true) ||
                   item.title.contains("Dog", ignoreCase = true)
            }
            else -> mediaList
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.82f)
                .testTag("people_and_pets_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "People & pets",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${filteredList.size} recognized faces & pets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Filter chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("All", "People", "Pets").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid of people & pets
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recognized subjects found in this category",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredList, key = { it.id }) { item ->
                            val isPet = item.tags.any {
                                it.equals("Cat", ignoreCase = true) ||
                                it.equals("Dog", ignoreCase = true) ||
                                it.equals("Pets", ignoreCase = true)
                            } || item.title.contains("Cat", ignoreCase = true)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        Toast.makeText(context, "${item.title} selected", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(modifier = Modifier.size(76.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        modifier = Modifier.fillMaxSize(),
                                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(item.uri)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = item.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    // Badge icon for Pet vs Person
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isPet) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.BottomEnd)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isPet) Icons.Default.Pets else Icons.Default.Face,
                                                contentDescription = null,
                                                tint = if (isPet) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                val subjectLabel = when {
                                    item.tags.contains("Cat") || item.title.contains("Cat", ignoreCase = true) -> "Tabby Cat"
                                    item.tags.contains("Portrait") || item.title.contains("Portrait", ignoreCase = true) -> "Portrait"
                                    else -> item.title.take(12)
                                }

                                Text(
                                    text = subjectLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = if (isPet) "Pet" else "Person",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Photo Map Interactive Explorer Dialog
// -------------------------------------------------------------

@Composable
fun PhotoMapDialog(
    places: Map<String, List<MediaItem>>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedPlace by remember { mutableStateOf<String?>(null) }

    val activeItems = remember(places, selectedPlace) {
        if (selectedPlace == null) {
            places.values.flatten()
        } else {
            places[selectedPlace] ?: emptyList()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .testTag("photo_map_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEA4335).copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFEA4335),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Your Photo Map",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${places.size} destinations across the globe",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Location chips for quick jump
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedPlace == null,
                            onClick = { selectedPlace = null },
                            label = { Text("All (${places.values.flatten().size})") },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                    items(places.keys.toList()) { placeName ->
                        val count = places[placeName]?.size ?: 0
                        FilterChip(
                            selected = selectedPlace == placeName,
                            onClick = { selectedPlace = if (selectedPlace == placeName) null else placeName },
                            label = { Text("$placeName ($count)") },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Places destination cards / photo grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (selectedPlace == null) {
                        // Show place summary cards
                        items(places.entries.toList(), key = { it.key }) { (placeName, items) ->
                            val cover = items.firstOrNull()?.uri
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedPlace = placeName }
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (!cover.isNullOrBlank()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(cover)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = placeName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    // Gradient scrim
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.75f)
                                                    ),
                                                    startY = 40f
                                                )
                                            )
                                    )

                                    // Pin & Place name
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color(0xFFEA4335),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = placeName,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "${items.size} photos",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Show photos within selected place
                        items(activeItems, key = { it.id }) { item ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clickable {
                                        Toast.makeText(context, "${item.title} (${item.locationName})", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(item.uri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Location pin badge
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.Black.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color(0xFFEA4335),
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedPlace != null) {
                        OutlinedButton(
                            onClick = { selectedPlace = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("All Places")
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

