package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TimelineHeader(
    dateMillis: Long,
    items: List<MediaItem>,
    onSelectDateGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    TimelineHeader(
        title = formatDateHeader(dateMillis),
        items = items,
        onSelectDateGroup = onSelectDateGroup,
        modifier = modifier
    )
}

@Composable
fun TimelineHeader(
    title: String,
    items: List<MediaItem>,
    onSelectDateGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemCountText = "${items.size} items"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = itemCountText,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Selection checkbox on the right matching screenshot
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable(onClick = onSelectDateGroup)
        )
    }
}

fun formatDateHeader(timestamp: Long): String {
    val itemCal = Calendar.getInstance().apply { timeInMillis = timestamp }
    val nowCal = Calendar.getInstance()

    val itemYear = itemCal.get(Calendar.YEAR)
    val itemDay = itemCal.get(Calendar.DAY_OF_YEAR)

    val nowYear = nowCal.get(Calendar.YEAR)
    val nowDay = nowCal.get(Calendar.DAY_OF_YEAR)

    return when {
        itemYear == nowYear && itemDay == nowDay -> "Today"
        itemYear == nowYear && itemDay == nowDay - 1 -> "Yesterday"
        itemYear == nowYear && (nowDay - itemDay) in 2..6 -> {
            SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(timestamp))
        }
        itemYear == nowYear -> {
            SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date(timestamp))
        }
        else -> {
            SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
