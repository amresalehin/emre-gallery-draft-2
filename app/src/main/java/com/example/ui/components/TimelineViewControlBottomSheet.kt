package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TimelineGroupBy
import com.example.model.TimelineSortDirection
import com.example.model.TimelineSortField
import com.example.model.TimelineViewConfig
import com.example.model.TimelineViewMode
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineViewControlBottomSheet(
    currentConfig: TimelineViewConfig,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onApplyConfig: (TimelineViewConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    // Local draft states inside drawer sheet
    var draftViewMode by remember(currentConfig) { mutableStateOf(currentConfig.viewMode) }
    var draftColumns by remember(currentConfig) { mutableIntStateOf(currentConfig.columnCount) }
    var draftSortField by remember(currentConfig) { mutableStateOf(currentConfig.sortField) }
    var draftSortDirection by remember(currentConfig) { mutableStateOf(currentConfig.sortDirection) }
    var draftGroupBy by remember(currentConfig) { mutableStateOf(currentConfig.groupBy) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier.testTag("timeline_view_control_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            // ==========================================
            // 1. View (Grid vs Masonry)
            // ==========================================
            Text(
                text = "View",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ViewModeSegmentButton(
                    title = "Grid",
                    icon = Icons.Default.GridView,
                    isSelected = draftViewMode == TimelineViewMode.GRID,
                    onClick = { draftViewMode = TimelineViewMode.GRID },
                    modifier = Modifier.weight(1f)
                )

                ViewModeSegmentButton(
                    title = "Masonry",
                    icon = Icons.Outlined.GridView,
                    isSelected = draftViewMode == TimelineViewMode.MASONRY,
                    onClick = { draftViewMode = TimelineViewMode.MASONRY },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // Columns Slider Row
            // ==========================================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Columns",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(68.dp)
                )

                Icon(
                    imageVector = Icons.Outlined.GridView,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )

                Slider(
                    value = draftColumns.toFloat(),
                    onValueChange = { draftColumns = it.roundToInt().coerceIn(2, 6) },
                    valueRange = 2f..6f,
                    steps = 3, // ticks at 3, 4, 5
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onSurface,
                        activeTrackColor = MaterialTheme.colorScheme.onSurface,
                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                        activeTickColor = MaterialTheme.colorScheme.surface,
                        inactiveTickColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                )

                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "$draftColumns",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(18.dp),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==========================================
            // 2. Sort by Section
            // ==========================================
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            val sortScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(sortScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortByCard(
                    title = "Date taken",
                    icon = Icons.Outlined.CalendarMonth,
                    isSelected = draftSortField == TimelineSortField.DATE_TAKEN,
                    sortDirection = if (draftSortField == TimelineSortField.DATE_TAKEN) draftSortDirection else null,
                    onSelect = {
                        if (draftSortField == TimelineSortField.DATE_TAKEN) {
                            draftSortDirection = if (draftSortDirection == TimelineSortDirection.DESCENDING) TimelineSortDirection.ASCENDING else TimelineSortDirection.DESCENDING
                        } else {
                            draftSortField = TimelineSortField.DATE_TAKEN
                            draftSortDirection = TimelineSortDirection.DESCENDING
                        }
                    },
                    onDirectionClick = { dir ->
                        draftSortField = TimelineSortField.DATE_TAKEN
                        draftSortDirection = dir
                    }
                )

                SortByCard(
                    title = "Date added",
                    icon = Icons.Outlined.EventNote,
                    isSelected = draftSortField == TimelineSortField.DATE_ADDED,
                    sortDirection = if (draftSortField == TimelineSortField.DATE_ADDED) draftSortDirection else null,
                    onSelect = {
                        if (draftSortField == TimelineSortField.DATE_ADDED) {
                            draftSortDirection = if (draftSortDirection == TimelineSortDirection.DESCENDING) TimelineSortDirection.ASCENDING else TimelineSortDirection.DESCENDING
                        } else {
                            draftSortField = TimelineSortField.DATE_ADDED
                            draftSortDirection = TimelineSortDirection.DESCENDING
                        }
                    },
                    onDirectionClick = { dir ->
                        draftSortField = TimelineSortField.DATE_ADDED
                        draftSortDirection = dir
                    }
                )

                SortByCard(
                    title = "File name",
                    icon = Icons.Outlined.SortByAlpha,
                    isSelected = draftSortField == TimelineSortField.FILE_NAME,
                    sortDirection = if (draftSortField == TimelineSortField.FILE_NAME) draftSortDirection else null,
                    onSelect = {
                        if (draftSortField == TimelineSortField.FILE_NAME) {
                            draftSortDirection = if (draftSortDirection == TimelineSortDirection.ASCENDING) TimelineSortDirection.DESCENDING else TimelineSortDirection.ASCENDING
                        } else {
                            draftSortField = TimelineSortField.FILE_NAME
                            draftSortDirection = TimelineSortDirection.ASCENDING
                        }
                    },
                    onDirectionClick = { dir ->
                        draftSortField = TimelineSortField.FILE_NAME
                        draftSortDirection = dir
                    }
                )

                SortByCard(
                    title = "File size",
                    icon = Icons.Outlined.Description,
                    isSelected = draftSortField == TimelineSortField.FILE_SIZE,
                    sortDirection = if (draftSortField == TimelineSortField.FILE_SIZE) draftSortDirection else null,
                    onSelect = {
                        if (draftSortField == TimelineSortField.FILE_SIZE) {
                            draftSortDirection = if (draftSortDirection == TimelineSortDirection.DESCENDING) TimelineSortDirection.ASCENDING else TimelineSortDirection.DESCENDING
                        } else {
                            draftSortField = TimelineSortField.FILE_SIZE
                            draftSortDirection = TimelineSortDirection.DESCENDING
                        }
                    },
                    onDirectionClick = { dir ->
                        draftSortField = TimelineSortField.FILE_SIZE
                        draftSortDirection = dir
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 3. Group by Section
            // ==========================================
            Text(
                text = "Group by",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            val groupScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(groupScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GroupByCard(
                    title = "None",
                    icon = Icons.Outlined.Block,
                    isSelected = draftGroupBy == TimelineGroupBy.NONE,
                    onClick = { draftGroupBy = TimelineGroupBy.NONE }
                )

                GroupByCard(
                    title = "Day",
                    icon = Icons.Outlined.Today,
                    isSelected = draftGroupBy == TimelineGroupBy.DAY,
                    onClick = { draftGroupBy = TimelineGroupBy.DAY }
                )

                GroupByCard(
                    title = "Month",
                    icon = Icons.Outlined.CalendarMonth,
                    isSelected = draftGroupBy == TimelineGroupBy.MONTH,
                    onClick = { draftGroupBy = TimelineGroupBy.MONTH }
                )

                GroupByCard(
                    title = "Year",
                    icon = Icons.Outlined.DateRange,
                    isSelected = draftGroupBy == TimelineGroupBy.YEAR,
                    onClick = { draftGroupBy = TimelineGroupBy.YEAR }
                )

                GroupByCard(
                    title = "Device folder",
                    icon = Icons.Outlined.Folder,
                    isSelected = draftGroupBy == TimelineGroupBy.DEVICE_FOLDER,
                    onClick = { draftGroupBy = TimelineGroupBy.DEVICE_FOLDER }
                )

                GroupByCard(
                    title = "Initial",
                    icon = null,
                    customInitialText = "A|",
                    isSelected = draftGroupBy == TimelineGroupBy.INITIAL,
                    onClick = { draftGroupBy = TimelineGroupBy.INITIAL }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 4. Bottom Action Buttons: Reset to default & Apply
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        val def = TimelineViewConfig()
                        draftViewMode = def.viewMode
                        draftColumns = def.columnCount
                        draftSortField = def.sortField
                        draftSortDirection = def.sortDirection
                        draftGroupBy = def.groupBy
                    },
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reset to default",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    )
                }

                Button(
                    onClick = {
                        val updated = TimelineViewConfig(
                            viewMode = draftViewMode,
                            columnCount = draftColumns,
                            sortField = draftSortField,
                            sortDirection = draftSortDirection,
                            groupBy = draftGroupBy
                        )
                        onApplyConfig(updated)
                        onDismissRequest()
                    },
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .width(130.dp)
                ) {
                    Text(
                        text = "Apply",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewModeSegmentButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val border = if (isSelected) {
        null
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = border,
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.5.sp
                ),
                color = contentColor
            )
        }
    }
}

@Composable
private fun SortByCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    sortDirection: TimelineSortDirection?,
    onSelect: () -> Unit,
    onDirectionClick: (TimelineSortDirection) -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    }

    val borderWidth = if (isSelected) 1.5.dp else 1.dp

    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(borderWidth, borderColor),
        modifier = Modifier
            .width(106.dp)
            .height(74.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        lineHeight = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }

            // Direction arrows column (Up & Down)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isUpActive = isSelected && sortDirection == TimelineSortDirection.ASCENDING
                val isDownActive = isSelected && sortDirection == TimelineSortDirection.DESCENDING

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUpActive) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        .clickable { onDirectionClick(TimelineSortDirection.ASCENDING) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Ascending",
                        tint = if (isUpActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(13.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDownActive) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        .clickable { onDirectionClick(TimelineSortDirection.DESCENDING) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Descending",
                        tint = if (isDownActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupByCard(
    title: String,
    icon: ImageVector?,
    customInitialText: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    }

    val borderWidth = if (isSelected) 1.5.dp else 1.dp

    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(borderWidth, borderColor),
        modifier = Modifier
            .width(82.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            } else if (customInitialText != null) {
                Text(
                    text = customInitialText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}
