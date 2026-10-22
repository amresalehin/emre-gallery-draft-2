package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MediaRepository
import com.example.model.Album
import com.example.model.AlbumCategory
import com.example.model.MediaItem
import com.example.model.MemoryHighlight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TimelineFilter {
    ALL,
    PHOTOS,
    VIDEOS,
    FAVORITES
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application.applicationContext)

    val allMediaItems: StateFlow<List<MediaItem>> = repository.mediaItems
    val albums: StateFlow<List<Album>> = repository.albums
    val highlights: List<MemoryHighlight> = repository.highlights

    // Navigation and screen states
    private val _currentTab = MutableStateFlow(0) // 0=Photos, 1=Search, 2=Albums, 3=Stats
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _gridDensity = MutableStateFlow(3) // 2, 3, 4 columns
    val gridDensity: StateFlow<Int> = _gridDensity.asStateFlow()

    private val _timelineFilter = MutableStateFlow(TimelineFilter.ALL)
    val timelineFilter: StateFlow<TimelineFilter> = _timelineFilter.asStateFlow()

    // Multi-selection state
    private val _selectedMediaIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedMediaIds: StateFlow<Set<String>> = _selectedMediaIds.asStateFlow()

    val isSelectionMode: StateFlow<Boolean> = combine(_selectedMediaIds) { it.first().isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Fullscreen viewer state
    private val _viewingMedia = MutableStateFlow<MediaItem?>(null)
    val viewingMedia: StateFlow<MediaItem?> = _viewingMedia.asStateFlow()

    private val _viewingList = MutableStateFlow<List<MediaItem>>(emptyList())
    val viewingList: StateFlow<List<MediaItem>> = _viewingList.asStateFlow()

    // Story viewer state
    private val _activeStory = MutableStateFlow<MemoryHighlight?>(null)
    val activeStory: StateFlow<MemoryHighlight?> = _activeStory.asStateFlow()

    // Album detail state
    private val _viewingAlbumId = MutableStateFlow<String?>(null)
    val viewingAlbumId: StateFlow<String?> = _viewingAlbumId.asStateFlow()

    // Trash view state
    private val _isTrashOpen = MutableStateFlow(false)
    val isTrashOpen: StateFlow<Boolean> = _isTrashOpen.asStateFlow()

    // Search view state
    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    // Search & Filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterTag = MutableStateFlow<String?>(null)
    val filterTag: StateFlow<String?> = _filterTag.asStateFlow()

    private val _filterLocation = MutableStateFlow<String?>(null)
    val filterLocation: StateFlow<String?> = _filterLocation.asStateFlow()

    private val _filterCamera = MutableStateFlow<String?>(null)
    val filterCamera: StateFlow<String?> = _filterCamera.asStateFlow()

    // Filtered timeline media
    val timelineMedia: StateFlow<List<MediaItem>> = combine(
        allMediaItems,
        timelineFilter
    ) { items, filter ->
        val active = items.filterNot { it.isTrash }
        when (filter) {
            TimelineFilter.ALL -> active
            TimelineFilter.PHOTOS -> active.filter { !it.isVideo }
            TimelineFilter.VIDEOS -> active.filter { it.isVideo }
            TimelineFilter.FAVORITES -> active.filter { it.isFavorite }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Trash media
    val trashMedia: StateFlow<List<MediaItem>> = combine(allMediaItems) { items ->
        items.first().filter { it.isTrash }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search results media
    val searchResults: StateFlow<List<MediaItem>> = combine(
        allMediaItems,
        searchQuery,
        filterTag,
        filterLocation,
        filterCamera
    ) { items, query, tag, loc, cam ->
        val active = items.filterNot { it.isTrash }
        active.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.title.contains(query, ignoreCase = true) ||
                (item.locationName?.contains(query, ignoreCase = true) == true) ||
                item.tags.any { it.contains(query, ignoreCase = true) } ||
                item.cameraModel.contains(query, ignoreCase = true)

            val matchesTag = tag == null || item.tags.any { it.equals(tag, ignoreCase = true) }
            val matchesLoc = loc == null || item.locationName?.contains(loc, ignoreCase = true) == true
            val matchesCam = cam == null || item.cameraModel.contains(cam, ignoreCase = true)

            matchesQuery && matchesTag && matchesLoc && matchesCam
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Permission state
    private val _hasMediaPermission = MutableStateFlow(false)
    val hasMediaPermission: StateFlow<Boolean> = _hasMediaPermission.asStateFlow()

    init {
        val hasPerm = com.example.util.PermissionUtils.hasMediaPermission(application.applicationContext)
        _hasMediaPermission.value = hasPerm
        if (hasPerm) {
            loadDeviceMedia()
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _hasMediaPermission.value = isGranted
        if (isGranted) {
            loadDeviceMedia()
        }
    }

    fun loadDeviceMedia() {
        viewModelScope.launch {
            repository.loadDeviceMediaIfPermitted()
        }
    }

    fun setTab(index: Int) {
        _currentTab.value = index
        clearSelection()
    }

    fun cycleGridDensity() {
        _gridDensity.value = when (_gridDensity.value) {
            2 -> 3
            3 -> 4
            4 -> 2
            else -> 3
        }
    }

    fun setTimelineFilter(filter: TimelineFilter) {
        _timelineFilter.value = filter
    }

    fun toggleSelection(mediaId: String) {
        val current = _selectedMediaIds.value.toMutableSet()
        if (current.contains(mediaId)) {
            current.remove(mediaId)
        } else {
            current.add(mediaId)
        }
        _selectedMediaIds.value = current
    }

    fun selectAll(items: List<MediaItem>) {
        _selectedMediaIds.value = items.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedMediaIds.value = emptySet()
    }

    fun toggleFavorite(mediaId: String) {
        repository.toggleFavorite(mediaId)
    }

    fun batchToggleFavorite() {
        val selected = _selectedMediaIds.value
        selected.forEach { id ->
            repository.toggleFavorite(id)
        }
        clearSelection()
    }

    fun batchMoveToTrash() {
        repository.moveToTrash(_selectedMediaIds.value)
        clearSelection()
    }

    fun deleteSingleToTrash(mediaId: String) {
        repository.moveToTrash(setOf(mediaId))
        if (_viewingMedia.value?.id == mediaId) {
            closeViewer()
        }
    }

    fun restoreFromTrash(mediaIds: Set<String>) {
        repository.restoreFromTrash(mediaIds)
    }

    fun permanentlyDelete(mediaIds: Set<String>) {
        repository.permanentlyDelete(mediaIds)
    }

    fun emptyTrash() {
        repository.emptyTrash()
    }

    fun addTag(mediaId: String, tag: String) {
        repository.addTag(mediaId, tag)
    }

    fun removeTag(mediaId: String, tag: String) {
        repository.removeTag(mediaId, tag)
    }

    fun applyPhotoEdit(
        mediaId: String,
        filterPreset: String,
        rotation: Int,
        brightness: Float,
        contrast: Float,
        saturation: Float
    ) {
        repository.applyPhotoEdit(mediaId, filterPreset, rotation, brightness, contrast, saturation)
        // Refresh currently viewing media
        _viewingMedia.value = allMediaItems.value.find { it.id == mediaId }
    }

    fun createAlbum(
        title: String,
        category: AlbumCategory = AlbumCategory.MY_VISION,
        folderColorHex: Long = 0xFF4285F4
    ) {
        if (title.isNotBlank()) {
            val album = repository.createAlbum(title, category = category, folderColorHex = folderColorHex)
            if (_selectedMediaIds.value.isNotEmpty()) {
                repository.addItemsToAlbum(_selectedMediaIds.value, album.id)
                clearSelection()
            }
        }
    }

    fun deleteAlbum(albumId: String) {
        repository.deleteAlbum(albumId)
        if (_viewingAlbumId.value == albumId) {
            _viewingAlbumId.value = null
        }
    }

    fun addSelectedToAlbum(albumId: String) {
        repository.addItemsToAlbum(_selectedMediaIds.value, albumId)
        clearSelection()
    }

    fun importMediaUris(uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            repository.importMediaUris(uris)
        }
    }

    fun openViewer(media: MediaItem, list: List<MediaItem>) {
        _viewingMedia.value = media
        _viewingList.value = list
    }

    fun closeViewer() {
        _viewingMedia.value = null
        _viewingList.value = emptyList()
    }

    fun openStory(highlight: MemoryHighlight) {
        _activeStory.value = highlight
    }

    fun closeStory() {
        _activeStory.value = null
    }

    fun openAlbum(albumId: String) {
        _viewingAlbumId.value = albumId
    }

    fun closeAlbum() {
        _viewingAlbumId.value = null
    }

    fun openTrash() {
        _isTrashOpen.value = true
    }

    fun closeTrash() {
        _isTrashOpen.value = false
    }

    fun openSearch() {
        _isSearchOpen.value = true
    }

    fun closeSearch() {
        _isSearchOpen.value = false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterTag(tag: String?) {
        _filterTag.value = tag
    }

    fun setFilterLocation(loc: String?) {
        _filterLocation.value = loc
    }

    fun setFilterCamera(cam: String?) {
        _filterCamera.value = cam
    }

    fun clearAllSearchFilters() {
        _searchQuery.value = ""
        _filterTag.value = null
        _filterLocation.value = null
        _filterCamera.value = null
    }
}
