package com.example.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.model.Album
import com.example.model.AlbumCategory
import com.example.model.MediaItem
import com.example.model.MemoryHighlight
import com.example.model.SystemAlbumType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class MediaRepository(private val context: Context) {

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(SampleMediaCatalog.sampleMediaList)
    val mediaItems: StateFlow<List<MediaItem>> = _mediaItems.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    val highlights: List<MemoryHighlight> = SampleMediaCatalog.sampleHighlights

    suspend fun loadDeviceMediaIfPermitted() = withContext(Dispatchers.IO) {
        if (!com.example.util.PermissionUtils.hasMediaPermission(context)) {
            return@withContext
        }
        try {
            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.HEIGHT,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.DURATION
            )

            val queryUri = MediaStore.Files.getContentUri("external")
            val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"
            val selectionArgs = arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )
            val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

            val deviceItems = mutableListOf<MediaItem>()

            context.contentResolver.query(
                queryUri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                val dateCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
                val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val widthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                val heightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)
                val bucketCol = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                val durationCol = cursor.getColumnIndex(MediaStore.MediaColumns.DURATION)

                var count = 0
                while (cursor.moveToNext() && count < 500) {
                    if (idCol == -1) break
                    val id = cursor.getLong(idCol)
                    val name = if (nameCol != -1) cursor.getString(nameCol) ?: "Media $id" else "Media $id"
                    val mimeType = if (mimeCol != -1) cursor.getString(mimeCol) ?: "image/jpeg" else "image/jpeg"
                    val dateAddedSec = if (dateCol != -1) cursor.getLong(dateCol) else 0L
                    val sizeBytes = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                    val width = if (widthCol != -1) cursor.getInt(widthCol) else 1920
                    val height = if (heightCol != -1) cursor.getInt(heightCol) else 1080
                    val bucketName = if (bucketCol != -1) cursor.getString(bucketCol) ?: "Camera" else "Camera"
                    val durationMs = if (durationCol != -1) cursor.getLong(durationCol) else 0L

                    val isVideo = mimeType.startsWith("video")
                    val contentUri = if (isVideo) {
                        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    } else {
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    }

                    deviceItems.add(
                        MediaItem(
                            id = "device_$id",
                            uri = contentUri.toString(),
                            title = name,
                            mimeType = mimeType,
                            dateTaken = if (dateAddedSec > 0) dateAddedSec * 1000L else System.currentTimeMillis(),
                            sizeBytes = sizeBytes,
                            width = if (width > 0) width else 1920,
                            height = if (height > 0) height else 1080,
                            isVideo = isVideo,
                            durationMs = durationMs,
                            cameraModel = "Device Storage",
                            locationName = bucketName,
                            folderName = bucketName,
                            tags = listOf("Device", bucketName, if (isVideo) "Video" else "Photo")
                        )
                    )
                    count++
                }
            }

            if (deviceItems.isNotEmpty()) {
                val current = _mediaItems.value.toMutableList()
                val nonDevice = current.filterNot { it.id.startsWith("device_") }
                _mediaItems.value = (deviceItems + nonDevice).sortedByDescending { it.dateTaken }
            }
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    fun importMediaUris(uris: List<Uri>) {
        val newItems = uris.mapIndexed { index, uri ->
            MediaItem(
                id = "import_${UUID.randomUUID()}",
                uri = uri.toString(),
                title = "Imported Photo #${index + 1}",
                mimeType = "image/jpeg",
                dateTaken = System.currentTimeMillis() - (index * 60_000L),
                sizeBytes = 3_200_000L,
                width = 3840,
                height = 2160,
                cameraModel = "Imported",
                locationName = "Pictures",
                tags = listOf("Imported", "User Media")
            )
        }
        _mediaItems.value = (newItems + _mediaItems.value).sortedByDescending { it.dateTaken }
    }

    fun toggleFavorite(mediaId: String) {
        _mediaItems.value = _mediaItems.value.map { item ->
            if (item.id == mediaId) item.copy(isFavorite = !item.isFavorite) else item
        }
    }

    fun moveToTrash(mediaIds: Set<String>) {
        _mediaItems.value = _mediaItems.value.map { item ->
            if (item.id in mediaIds) item.copy(isTrash = true) else item
        }
    }

    fun restoreFromTrash(mediaIds: Set<String>) {
        _mediaItems.value = _mediaItems.value.map { item ->
            if (item.id in mediaIds) item.copy(isTrash = false) else item
        }
    }

    fun permanentlyDelete(mediaIds: Set<String>) {
        _mediaItems.value = _mediaItems.value.filterNot { it.id in mediaIds }
    }

    fun emptyTrash() {
        _mediaItems.value = _mediaItems.value.filterNot { it.isTrash }
    }

    fun addTag(mediaId: String, tag: String) {
        val cleanTag = tag.trim().capitalizeWord()
        if (cleanTag.isEmpty()) return
        _mediaItems.value = _mediaItems.value.map { item ->
            if (item.id == mediaId && !item.tags.contains(cleanTag)) {
                item.copy(tags = item.tags + cleanTag)
            } else {
                item
            }
        }
    }

    fun removeTag(mediaId: String, tag: String) {
        _mediaItems.value = _mediaItems.value.map { item ->
            if (item.id == mediaId) {
                item.copy(tags = item.tags.filterNot { it.equals(tag, ignoreCase = true) })
            } else {
                item
            }
        }
    }

    fun applyPhotoEdit(
        mediaId: String,
        filterPreset: String,
        rotationDegrees: Int,
        brightness: Float,
        contrast: Float,
        saturation: Float
    ) {
        _mediaItems.value = _mediaItems.value.map { item ->
            if (item.id == mediaId) {
                item.copy(
                    filterPreset = filterPreset,
                    rotationDegrees = (item.rotationDegrees + rotationDegrees) % 360,
                    brightnessAdjustment = brightness,
                    contrastAdjustment = contrast,
                    saturationAdjustment = saturation
                )
            } else {
                item
            }
        }
    }

    fun createAlbum(
        title: String,
        category: AlbumCategory = AlbumCategory.MY_VISION,
        coverUri: String? = null,
        folderColorHex: Long = 0xFF4285F4
    ): Album {
        val newAlbum = Album(
            id = "album_${UUID.randomUUID()}",
            title = title.trim(),
            coverUri = coverUri ?: _mediaItems.value.firstOrNull { !it.isTrash }?.uri,
            itemCount = 0,
            category = category,
            folderColorHex = folderColorHex
        )
        _albums.value = _albums.value + newAlbum
        return newAlbum
    }

    fun deleteAlbum(albumId: String) {
        _albums.value = _albums.value.filterNot { it.id == albumId }
        _mediaItems.value = _mediaItems.value.map { item ->
            if (item.albumId == albumId) item.copy(albumId = null) else item
        }
    }

    fun addItemsToAlbum(mediaIds: Set<String>, albumId: String) {
        _mediaItems.value = _mediaItems.value.map { item ->
            if (item.id in mediaIds) item.copy(albumId = albumId) else item
        }
    }

    private fun String.capitalizeWord(): String {
        return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
