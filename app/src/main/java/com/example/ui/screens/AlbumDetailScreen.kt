package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Album
import com.example.model.MediaItem
import com.example.ui.components.MediaThumbnail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    album: Album,
    allMedia: List<MediaItem>,
    onBack: () -> Unit,
    onDeleteAlbum: (String) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val albumItems = remember(album, allMedia) {
        val nonTrash = allMedia.filterNot { it.isTrash }
        val directMatches = nonTrash.filter { it.albumId == album.id }
        if (directMatches.isNotEmpty()) {
            directMatches
        } else {
            // Match device folders by folder name or ID
            val folderMatches = nonTrash.filter {
                it.folderName.equals(album.title, ignoreCase = true) ||
                "folder_${it.folderName.lowercase().replace(" ", "_")}" == album.id
            }
            if (folderMatches.isNotEmpty()) {
                folderMatches
            } else {
                when (album.id) {
                    "folder_dcim" -> nonTrash.take(8)
                    "folder_pictures" -> nonTrash.filter { it.tags.contains("Nature") || it.tags.contains("Flower") }
                    "folder_download" -> nonTrash.filter { it.tags.contains("Travel") || it.tags.contains("Flight") }
                    "folder_whatsapp" -> nonTrash.filter { it.tags.contains("Cute") || it.tags.contains("Food") || it.tags.contains("Cat") }
                    "vision_travel" -> nonTrash.filter { it.tags.contains("Travel") || it.tags.contains("Beach") || it.tags.contains("Mountains") }
                    "vision_family" -> nonTrash.filter { it.tags.contains("Sunset") || it.tags.contains("Portrait") }
                    "vision_study" -> nonTrash.filter { it.tags.contains("Architecture") || it.tags.contains("Macro") }
                    "nested_personal" -> nonTrash.take(6)
                    "nested_work" -> nonTrash.filter { it.tags.contains("Architecture") || it.tags.contains("Tokyo") }
                    "nested_projects" -> nonTrash.filter { it.tags.contains("Macro") || it.tags.contains("City") }
                    "nested_references" -> nonTrash.filter { it.tags.contains("Travel") || it.tags.contains("Nature") }
                    else -> emptyList()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("album_detail_screen")
    ) {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            title = {
                Column {
                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                        maxLines = 1
                    )
                    Text(
                        text = "${albumItems.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            actions = {
                if (!album.isSystem) {
                    IconButton(onClick = { onDeleteAlbum(album.id) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Album",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        if (albumItems.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoAlbum,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "This collection is empty",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Select photos from your timeline to add them to this album.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(albumItems, key = { it.id }) { item ->
                    MediaThumbnail(
                        media = item,
                        isSelected = false,
                        isSelectionMode = false,
                        cornerRadius = 8.dp,
                        onClick = { onMediaClick(item) },
                        onLongClick = { onMediaClick(item) }
                    )
                }
            }
        }
    }
}
