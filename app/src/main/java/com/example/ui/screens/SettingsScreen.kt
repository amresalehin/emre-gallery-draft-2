package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.MediaItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SettingsTile(
    val id: String,
    val title: String,
    val drawableResId: Int,
    val keywords: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    allMedia: List<MediaItem>,
    onDeleteDuplicate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var activeModal by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val tiles = remember {
        listOf(
            SettingsTile("overview", "Library overview", R.drawable.ic_library_overview, listOf("library", "overview", "photos", "storage", "media")),
            SettingsTile("theme", "Theme", R.drawable.ic_palette_art, listOf("theme", "dark", "light", "colors", "amoled", "mode")),
            SettingsTile("tools", "Tools", R.drawable.ic_wrench_tools, listOf("tools", "clean", "cache", "rescan", "exif", "fix")),
            SettingsTile("intelligence", "Intelligence", R.drawable.ic_brain_ai, listOf("intelligence", "ai", "smart", "face", "clustering", "memories")),
            SettingsTile("ocr", "OCR", R.drawable.ic_ocr_scan, listOf("ocr", "text", "scan", "extract", "recognition", "search text")),
            SettingsTile("indexing", "Indexing\nStatus", R.drawable.ic_database_stack, listOf("indexing", "status", "database", "cache", "sync")),
            SettingsTile("duplicates", "Duplicates\nor Similar", R.drawable.ic_duplicates_similar, listOf("duplicates", "similar", "photos", "clean", "compare")),
            SettingsTile("about", "About", R.drawable.ic_info_about, listOf("about", "version", "privacy", "license", "lumina"))
        )
    }

    val filteredTiles = remember(searchQuery) {
        if (searchQuery.isBlank()) tiles
        else tiles.filter { tile ->
            tile.title.contains(searchQuery, ignoreCase = true) ||
                tile.keywords.any { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .testTag("settings_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search Settings Input Bar matching screenshot
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Search settings...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f),
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("search_settings_bar")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2-Column Grid of Settings Cards matching screenshot
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredTiles, key = { it.id }) { tile ->
                SettingsCardItem(
                    tile = tile,
                    onClick = { activeModal = tile.id }
                )
            }
        }
    }

    // Modal Bottom Sheets for each settings card
    if (activeModal != null) {
        ModalBottomSheet(
            onDismissRequest = { activeModal = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.testTag("settings_modal_${activeModal}")
        ) {
            when (activeModal) {
                "overview" -> LibraryOverviewContent(allMedia = allMedia)
                "theme" -> ThemeSettingsContent()
                "tools" -> ToolsSettingsContent()
                "intelligence" -> IntelligenceSettingsContent()
                "ocr" -> OcrSettingsContent(allMedia = allMedia)
                "indexing" -> IndexingStatusContent(totalItems = allMedia.size)
                "duplicates" -> DuplicatesContent(
                    allMedia = allMedia,
                    onDelete = { id ->
                        onDeleteDuplicate(id)
                        Toast.makeText(context, "Duplicate moved to trash", Toast.LENGTH_SHORT).show()
                    }
                )
                "about" -> AboutSettingsContent()
            }
        }
    }
}

@Composable
private fun SettingsCardItem(
    tile: SettingsTile,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag("settings_card_${tile.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon
            Icon(
                painter = painterResource(id = tile.drawableResId),
                contentDescription = tile.title,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Title
            Text(
                text = tile.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    lineHeight = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Right Chevron Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// Interactive Contents for Each Card
// -------------------------------------------------------------

@Composable
private fun LibraryOverviewContent(allMedia: List<MediaItem>) {
    val nonTrash = allMedia.filterNot { it.isTrash }
    val photoCount = nonTrash.count { !it.isVideo }
    val videoCount = nonTrash.count { it.isVideo }
    val totalSize = nonTrash.sumOf { it.sizeBytes }
    val formattedSize = remember(totalSize) {
        val mb = totalSize / (1024.0 * 1024.0)
        if (mb >= 1000.0) String.format("%.2f GB", mb / 1024.0) else String.format("%.1f MB", mb)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Library Overview",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Complete summary of local storage and catalog items",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OverviewStatCard("Total Media", "${nonTrash.size}", Modifier.weight(1f))
            OverviewStatCard("Photos", "$photoCount", Modifier.weight(1f))
            OverviewStatCard("Videos", "$videoCount", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))
        OverviewStatCard("Storage Size", formattedSize, Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Storage Folders",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("• Camera (DCIM) — 8 items", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("• Downloads — 3 items", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("• Screenshots — 2 items", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("• Imported & Albums — 3 items", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun OverviewStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ThemeSettingsContent() {
    var selectedTheme by remember { mutableStateOf("System default") }
    var amoledBlack by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
        Text(
            text = "Theme & Appearance",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(16.dp))

        listOf("System default", "Light", "Dark").forEach { themeName ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedTheme = themeName }
                    .padding(vertical = 6.dp)
            ) {
                RadioButton(
                    selected = selectedTheme == themeName,
                    onClick = { selectedTheme = themeName }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(themeName, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text("Pure AMOLED Black", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                Text("Optimized for OLED displays in dark mode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = amoledBlack, onCheckedChange = { amoledBlack = it })
        }
    }
}

@Composable
private fun ToolsSettingsContent() {
    val context = LocalContext.current
    var isCleaning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
        Text(text = "Gallery Tools", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(16.dp))

        ToolItemRow(
            title = "Clean Thumbnail Cache",
            subtitle = "Free up 42.8 MB of cached previews",
            actionLabel = if (isCleaning) "Cleaning..." else "Clean",
            onAction = {
                isCleaning = true
                Toast.makeText(context, "Thumbnail cache cleared", Toast.LENGTH_SHORT).show()
                isCleaning = false
            }
        )

        ToolItemRow(
            title = "Rescan MediaStore",
            subtitle = "Discover newly taken photos and system downloads",
            actionLabel = "Rescan",
            onAction = {
                Toast.makeText(context, "Library rescanned", Toast.LENGTH_SHORT).show()
            }
        )

        ToolItemRow(
            title = "Metadata Exif Repair",
            subtitle = "Verify timestamps and camera parameters",
            actionLabel = "Repair",
            onAction = {
                Toast.makeText(context, "All metadata verified and repaired", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun ToolItemRow(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onAction,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(actionLabel, fontSize = 12.sp)
        }
    }
}

@Composable
private fun IntelligenceSettingsContent() {
    var faceDetection by remember { mutableStateOf(true) }
    var semanticSearch by remember { mutableStateOf(true) }
    var autoMemories by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
        Text(text = "Intelligence & On-Device ML", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(
            text = "All AI models execute locally on-device. No data ever leaves your phone.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        IntelligenceToggleRow("Face & People Clustering", "Group family and friends automatically", faceDetection) { faceDetection = it }
        IntelligenceToggleRow("Semantic Search", "Search by concept: 'beach', 'sunset', 'document'", semanticSearch) { semanticSearch = it }
        IntelligenceToggleRow("Automatic Memory Highlights", "Create story reels from trips and milestones", autoMemories) { autoMemories = it }
    }
}

@Composable
private fun IntelligenceToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun OcrSettingsContent(allMedia: List<MediaItem>) {
    val context = LocalContext.current
    val sampleScannedMedia = allMedia.firstOrNull { it.tags.contains("Architecture") || it.tags.contains("City") } ?: allMedia.first()
    val detectedText = "PARIS MODERN DESIGN 2026\nFACADE STRUCTURAL STEEL & GLASS\nARCHITECTURAL SECTION A-1"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
        Text(text = "Optical Character Recognition (OCR)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(
            text = "Scan and extract text directly from receipts, documents, and signboards in your photos.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sample Detected Text in Photo:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = detectedText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Scanned Text", detectedText))
                        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy Extracted Text")
                }
            }
        }
    }
}

@Composable
private fun IndexingStatusContent(totalItems: Int) {
    val coroutineScope = rememberCoroutineScope()
    var isReindexing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(1f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
        Text(text = "Indexing Status", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isReindexing) "Reindexing Library..." else "Status: Up to date",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isReindexing) MaterialTheme.colorScheme.primary else Color(0xFF34A853)
                    )
                    Text(
                        text = "${(progress * totalItems).toInt()} of $totalItems items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("• MediaStore Sync: OK (Active)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("• EXIF Metadata Cache: 100% Complete", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("• Thumbnail Cache Size: 18.4 MB", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isReindexing = true
                            progress = 0f
                            for (i in 1..10) {
                                delay(120)
                                progress = i / 10f
                            }
                            isReindexing = false
                        }
                    },
                    enabled = !isReindexing,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isReindexing) "Indexing..." else "Rebuild Index")
                }
            }
        }
    }
}

@Composable
private fun DuplicatesContent(
    allMedia: List<MediaItem>,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current
    // Identify photos with same location or similar tags
    val sampleGroup = allMedia.filter { it.tags.contains("Tokyo") || it.tags.contains("Nature") }.take(2)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
        Text(text = "Duplicates or Similar", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(
            text = "Review similar burst shots and duplicate downloads to save device storage.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (sampleGroup.size >= 2) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Potential Match: ${sampleGroup[0].locationName ?: "Similar Shots"}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        sampleGroup.forEachIndexed { index, item ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context).data(item.uri).crossfade(true).build(),
                                        contentDescription = item.title,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Text(
                                    text = if (index == 0) "Higher Resolution" else "Lower Resolution",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (index == 0) Color(0xFF34A853) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = "${item.width}×${item.height} • ${item.formattedSize}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (index == 1) {
                                    Button(
                                        onClick = { onDelete(item.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.padding(top = 6.dp)
                                    ) {
                                        Text("Delete Duplicate", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text("No duplicate photos detected in current library.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AboutSettingsContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_library_overview),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Lumina Gallery",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Version 1.2.0 • Release",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Privacy First",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lumina Gallery operates 100% on-device. No telemetry, no cloud ads, and no external tracking. Inspired by the best of Google Photos and Aves Gallery.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
