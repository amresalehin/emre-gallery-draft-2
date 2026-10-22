package com.example

import com.example.data.SampleMediaCatalog
import com.example.model.FilterPresets
import com.example.model.MediaItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LuminaGalleryUnitTest {

    @Test
    fun mediaItem_megapixelsCalculation_isAccurate() {
        val media = MediaItem(
            id = "test_1",
            uri = "https://example.com/test.jpg",
            title = "Test Photo",
            width = 4000,
            height = 3000
        )
        // 4000 * 3000 = 12,000,000 pixels -> 12.0 MP
        assertEquals("12.0 MP", media.megapixels)
    }

    @Test
    fun mediaItem_formattedSize_isAccurate() {
        val media = MediaItem(
            id = "test_2",
            uri = "https://example.com/test.jpg",
            title = "Test Photo",
            sizeBytes = 4_194_304L // 4 MB exactly
        )
        assertEquals("4.0 MB", media.formattedSize)
    }

    @Test
    fun mediaItem_formattedDuration_forVideo_isCorrect() {
        val video = MediaItem(
            id = "test_video",
            uri = "https://example.com/test.mp4",
            title = "Sample Video",
            isVideo = true,
            durationMs = 85_000L // 1 min 25 sec
        )
        assertEquals("01:25", video.formattedDuration)
    }

    @Test
    fun filterPresets_matrices_areConfigured() {
        val vivid = FilterPresets.getFilterMatrix("Vivid")
        assertNotNull(vivid)
        val matrix = FilterPresets.buildAdjustmentMatrix("Normal", 0.1f, 0.2f, 0f)
        assertNotNull(matrix)
    }

    @Test
    fun sampleMediaCatalog_hasValidRichItems() {
        val items = SampleMediaCatalog.sampleMediaList
        assertTrue("Catalog should contain items", items.isNotEmpty())
        assertTrue("Catalog should have photos", items.any { !it.isVideo })
        assertTrue("Catalog should have videos", items.any { it.isVideo })
        assertTrue("Catalog should have favorites", items.any { it.isFavorite })
        assertTrue("Catalog should have items with EXIF camera model", items.all { it.cameraModel.isNotBlank() })
    }
}
