package com.example.model

enum class SystemAlbumType {
    CAMERA,
    SCREENSHOTS,
    DOWNLOADS,
    FAVORITES,
    VIDEOS,
    ARCHIVE,
    TRASH
}

enum class AlbumCategory {
    DEVICE_FOLDER,
    MY_VISION,
    NESTED_ALBUM,
    OTHER_COLLECTION
}

data class Album(
    val id: String,
    val title: String,
    val coverUri: String? = null,
    val itemCount: Int = 0,
    val isSystem: Boolean = false,
    val systemType: SystemAlbumType? = null,
    val category: AlbumCategory = AlbumCategory.DEVICE_FOLDER,
    val collageUris: List<String> = emptyList(),
    val nestedAlbumCount: Int = 0,
    val folderColorHex: Long = 0xFF4285F4,
    val subtitle: String? = null
)
