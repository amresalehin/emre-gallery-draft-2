package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import com.example.model.FilterOption
import com.example.model.FilterPresets
import com.example.model.MediaItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorDialog(
    media: MediaItem,
    onDismissRequest: () -> Unit,
    onSaveEdit: (filterPreset: String, rotation: Int, brightness: Float, contrast: Float, saturation: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf(media.filterPreset) }
    var rotation by remember { mutableIntStateOf(0) }
    var brightness by remember { mutableFloatStateOf(media.brightnessAdjustment) }
    var contrast by remember { mutableFloatStateOf(media.contrastAdjustment) }
    var saturation by remember { mutableFloatStateOf(media.saturationAdjustment) }

    var editorTab by remember { mutableIntStateOf(0) } // 0 = Filters, 1 = Adjust

    val previewMatrix = remember(selectedFilter, brightness, contrast, saturation) {
        FilterPresets.buildAdjustmentMatrix(selectedFilter, brightness, contrast, saturation)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("photo_editor_screen"),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top App Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                    }

                    Text(
                        text = "Edit Photo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Button(
                        onClick = {
                            onSaveEdit(selectedFilter, rotation, brightness, contrast, saturation)
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Save")
                    }
                }

                // Photo Canvas Preview
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(media.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Preview",
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.colorMatrix(previewMatrix),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .rotate((media.rotationDegrees + rotation).toFloat())
                    )
                }

                // Action Controls Bottom Area
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Quick rotation & reset buttons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { rotation = (rotation + 90) % 360 }
                                ) {
                                    Icon(
                                        Icons.Default.RotateRight,
                                        contentDescription = "Rotate 90°",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${(media.rotationDegrees + rotation) % 360}°",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            TextButton(
                                onClick = {
                                    selectedFilter = "Normal"
                                    rotation = 0
                                    brightness = 0f
                                    contrast = 0f
                                    saturation = 0f
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Reset")
                            }
                        }

                        // Tab selector: Filters vs Adjustments
                        TabRow(
                            selectedTabIndex = editorTab,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Tab(
                                selected = editorTab == 0,
                                onClick = { editorTab = 0 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Filters")
                                    }
                                }
                            )
                            Tab(
                                selected = editorTab == 1,
                                onClick = { editorTab = 1 },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Adjust")
                                    }
                                }
                            )
                        }

                        if (editorTab == 0) {
                            // Filters List
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(FilterPresets.all) { filter ->
                                    val isSelected = selectedFilter.equals(filter.id, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedFilter = filter.id },
                                        label = { Text(filter.name) },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        } else {
                            // Sliders for Brightness, Contrast, Saturation
                            Column(modifier = Modifier.fillMaxWidth()) {
                                SliderRow(
                                    label = "Brightness",
                                    value = brightness,
                                    onValueChange = { brightness = it },
                                    range = -0.6f..0.6f
                                )
                                SliderRow(
                                    label = "Contrast",
                                    value = contrast,
                                    onValueChange = { contrast = it },
                                    range = -0.5f..0.8f
                                )
                                SliderRow(
                                    label = "Saturation",
                                    value = saturation,
                                    onValueChange = { saturation = it },
                                    range = -1f..1f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(76.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = String.format("%+.0f", value * 100),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            modifier = Modifier.width(36.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
