package com.example.photosorter.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photosorter.PhotoDeleter
import com.example.photosorter.data.model.PhotoItem
import com.example.photosorter.data.repository.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrashViewModel(
    private val repository: PhotoRepository,
    private val photoDeleter: PhotoDeleter
) : ViewModel() {

    private val _trashedPhotos = MutableStateFlow<List<PhotoItem>>(emptyList())
    val trashedPhotos: StateFlow<List<PhotoItem>> = _trashedPhotos.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _selectedPhotoIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedPhotoIds: StateFlow<Set<Long>> = _selectedPhotoIds.asStateFlow()

    init {
        loadTrashedPhotos()
    }

    private fun loadTrashedPhotos() {
        viewModelScope.launch {
            _trashedPhotos.value = repository.getTrashedPhotos()
        }
    }

    fun toggleSelection(photoId: Long) {
        val current = _selectedPhotoIds.value.toMutableSet()
        if (current.contains(photoId)) {
            current.remove(photoId)
        } else {
            current.add(photoId)
        }
        _selectedPhotoIds.value = current
    }

    fun clearSelection() {
        _selectedPhotoIds.value = emptySet()
    }

    fun restoreSelected() {
        val selected = _selectedPhotoIds.value.toList()
        if (selected.isEmpty()) return
        
        viewModelScope.launch {
            repository.restorePhotos(selected)
            _selectedPhotoIds.value = emptySet()
            loadTrashedPhotos()
        }
    }

    fun emptyTrash() {
        val photos = _trashedPhotos.value
        if (photos.isEmpty()) return

        _isDeleting.value = true
        val uris = photos.map { it.uri }
        
        photoDeleter.deletePhotos(uris) { success ->
            if (success) {
                viewModelScope.launch {
                    repository.clearTrashDecisions()
                    _trashedPhotos.value = emptyList()
                    _selectedPhotoIds.value = emptySet()
                    _isDeleting.value = false
                }
            } else {
                _isDeleting.value = false
            }
        }
    }
}
