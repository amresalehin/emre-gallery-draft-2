package com.example.model

data class MediaItem(
    val id: String,
    val uri: String,
    val title: String,
    val mimeType: String = "image/jpeg",
    val dateTaken: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 2_450_000L,
    val width: Int = 3840,
    val height: Int = 2160,
    val isVideo: Boolean = false,
    val durationMs: Long = 0L,
    val cameraModel: String = "Google Pixel 9 Pro",
    val lens: String = "24mm f/1.68 Main",
    val aperture: String = "f/1.7",
    val shutterSpeed: String = "1/500s",
    val iso: Int = 100,
    val focalLength: String = "24mm",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList(),
    val albumId: String? = null,
    val folderName: String = "Camera",
    val isTrash: Boolean = false,
    val isArchived: Boolean = false,
    val rotationDegrees: Int = 0,
    val filterPreset: String = "Normal",
    val brightnessAdjustment: Float = 0f,
    val contrastAdjustment: Float = 0f,
    val saturationAdjustment: Float = 0f
) {
    val megapixels: String
        get() {
            val mp = (width.toLong() * height.toLong()) / 1_000_000.0
            return String.format("%.1f MP", mp)
        }

    val formattedSize: String
        get() {
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            return if (mb >= 1.0) {
                String.format("%.1f MB", mb)
            } else {
                String.format("%.0f KB", kb)
            }
        }

    val formattedDuration: String
        get() {
            if (!isVideo || durationMs <= 0) return ""
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val aspectRatio: Float
        get() = if (height > 0) width.toFloat() / height.toFloat() else 1f
}
