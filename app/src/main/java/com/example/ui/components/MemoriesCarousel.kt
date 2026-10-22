package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.MemoryHighlight

@Composable
fun MemoriesCarousel(
    highlights: List<MemoryHighlight>,
    onHighlightClick: (MemoryHighlight) -> Unit,
    modifier: Modifier = Modifier
) {
    if (highlights.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(highlights, key = { it.id }) { highlight ->
                MemoryCard(
                    highlight = highlight,
                    onClick = { onHighlightClick(highlight) }
                )
            }
        }
    }
}

@Composable
fun MemoryCard(
    highlight: MemoryHighlight,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .width(118.dp)
            .height(184.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("memory_card_${highlight.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(highlight.coverUri)
                    .crossfade(true)
                    .build(),
                contentDescription = highlight.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient scrim for portrait story look
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.20f),
                                Color.Black.copy(alpha = 0.88f)
                            )
                        )
                    )
            )

            // Story title and subtitle at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = highlight.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    lineHeight = 15.sp,
                    maxLines = 2
                )
                Text(
                    text = highlight.subtitle,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
