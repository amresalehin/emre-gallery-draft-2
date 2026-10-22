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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.MediaItem
import com.example.ui.components.MediaThumbnail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashBinScreen(
    trashItems: List<MediaItem>,
    onBack: () -> Unit,
    onRestore: (Set<String>) -> Unit,
    onPermanentlyDelete: (Set<String>) -> Unit,
    onEmptyTrash: () -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var showEmptyConfirmDialog by remember { mutableStateOf(false) }
    var selectedItemForAction by remember { mutableStateOf<MediaItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("trash_bin_screen")
    ) {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            title = {
                Column {
                    Text(
                        text = "Trash Bin",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${trashItems.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            actions = {
                if (trashItems.isNotEmpty()) {
                    TextButton(
                        onClick = { showEmptyConfirmDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Empty Trash")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        // Info Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Items in Trash can be restored back to your library or permanently destroyed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (trashItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Trash is empty.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 96.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(trashItems, key = { it.id }) { item ->
                    MediaThumbnail(
                        media = item,
                        isSelected = false,
                        isSelectionMode = false,
                        onClick = { selectedItemForAction = item },
                        onLongClick = { selectedItemForAction = item }
                    )
                }
            }
        }
    }

    // Action dialog for single item: Restore or Delete Forever
    if (selectedItemForAction != null) {
        val item = selectedItemForAction!!
        AlertDialog(
            onDismissRequest = { selectedItemForAction = null },
            title = { Text(item.title) },
            text = { Text("What would you like to do with this item?") },
            confirmButton = {
                Button(
                    onClick = {
                        onRestore(setOf(item.id))
                        selectedItemForAction = null
                    }
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onPermanentlyDelete(setOf(item.id))
                        selectedItemForAction = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete Forever")
                }
            }
        )
    }

    // Confirmation dialog for emptying trash
    if (showEmptyConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirmDialog = false },
            title = { Text("Empty Trash?") },
            text = { Text("All ${trashItems.size} items in the trash will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        onEmptyTrash()
                        showEmptyConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Empty Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
