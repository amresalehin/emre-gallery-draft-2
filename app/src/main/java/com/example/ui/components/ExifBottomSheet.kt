package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoSizeSelectActual
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExifBottomSheet(
    media: MediaItem,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isAddingTag by remember { mutableStateOf(false) }
    var newTagText by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy • h:mm a", Locale.getDefault()) }
    val dateTakenStr = remember(media.dateTaken) { dateFormat.format(Date(media.dateTaken)) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.testTag("exif_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Title & Type
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (media.isVideo) Icons.Default.Videocam else Icons.Default.PhotoSizeSelectActual,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateTakenStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Row (Dimensions, Megapixels, File Size)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    label = "Resolution",
                    value = "${media.width}×${media.height}",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = if (media.isVideo) "Duration" else "Megapixels",
                    value = if (media.isVideo) media.formattedDuration else media.megapixels,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "Size",
                    value = media.formattedSize,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Camera & EXIF Optics Section
            MetadataSection(
                title = "Camera & Optics",
                icon = Icons.Default.CameraAlt
            ) {
                InfoRow(label = "Camera", value = media.cameraModel)
                InfoRow(label = "Lens", value = media.lens)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        InfoRow(label = "Aperture", value = media.aperture)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        InfoRow(label = "Shutter", value = media.shutterSpeed)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        InfoRow(label = "ISO", value = "${media.iso}")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        InfoRow(label = "Focal Length", value = media.focalLength)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location Section
            if (media.locationName != null || (media.latitude != null && media.longitude != null)) {
                MetadataSection(
                    title = "Location",
                    icon = Icons.Default.LocationOn
                ) {
                    if (media.locationName != null) {
                        InfoRow(label = "Place", value = media.locationName)
                    }
                    if (media.latitude != null && media.longitude != null) {
                        val coords = String.format(Locale.US, "%.5f, %.5f", media.latitude, media.longitude)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Coordinates",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = coords,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Coordinates", coords))
                                    Toast.makeText(context, "Coordinates copied!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Coordinates",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Tags & Categorization Section
            MetadataSection(
                title = "Tags & Organization",
                icon = Icons.Default.Sell
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    media.tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = { },
                            label = { Text(tag, fontSize = 12.sp) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Tag",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    // Add Tag Button
                    if (!isAddingTag) {
                        FilledTonalButton(
                            onClick = { isAddingTag = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Tag", fontSize = 12.sp)
                        }
                    }
                }

                if (isAddingTag) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = newTagText,
                            onValueChange = { newTagText = it },
                            placeholder = { Text("e.g. Travel, Sunset, Favorite") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                if (newTagText.isNotBlank()) {
                                    onAddTag(newTagText)
                                    newTagText = ""
                                    isAddingTag = false
                                }
                            }
                        ) {
                            Text("Save")
                        }
                        IconButton(onClick = { isAddingTag = false; newTagText = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // File Path & System Details
            MetadataSection(
                title = "File Details",
                icon = Icons.Default.Info
            ) {
                InfoRow(label = "Format", value = media.mimeType)
                InfoRow(label = "URI / Path", value = media.uri)
                if (media.rotationDegrees != 0) {
                    InfoRow(label = "Rotation", value = "${media.rotationDegrees}°")
                }
                if (media.filterPreset != "Normal") {
                    InfoRow(label = "Filter Applied", value = media.filterPreset)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MetadataSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
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
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
