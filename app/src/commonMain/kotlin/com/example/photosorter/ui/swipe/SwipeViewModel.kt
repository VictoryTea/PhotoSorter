package com.example.photosorter.ui.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photosorter.data.local.UserStats
import com.example.photosorter.data.model.PhotoItem
import com.example.photosorter.data.model.SwipeAction
import com.example.photosorter.data.repository.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the swipe-to-sort screen.
 *
 * Manages the unsorted photo queue, maps swipe directions to [SwipeAction]s,
 * records decisions through [PhotoRepository], and exposes stats / point
 * feedback for the UI layer.
 *
 * @param repository Data layer for photo queries, sort decisions, and stats.
 */
class SwipeViewModel(private val repository: PhotoRepository) : ViewModel() {

    // ── Photos ───────────────────────────────────────────────────────
    private val _photos = MutableStateFlow<List<PhotoItem>>(emptyList())

    /** Ordered list of photos waiting to be sorted (first = top of stack). */
    val photos: StateFlow<List<PhotoItem>> = _photos.asStateFlow()

    // ── Loading ──────────────────────────────────────────────────────
    private val _isLoading = MutableStateFlow(true)

    /** `true` while the initial photo list is being fetched. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ── Points feedback ──────────────────────────────────────────────
    private val _lastPoints = MutableStateFlow(0)

    /** Points earned on the most recent swipe (drives the popup animation). */
    val lastPoints: StateFlow<Int> = _lastPoints.asStateFlow()

    // ── User stats ───────────────────────────────────────────────────
    /** Live user statistics (streak, level, totals, etc.). */
    val stats: StateFlow<UserStats?> = repository.getStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Undo stack ───────────────────────────────────────────────────
    private val undoStack = ArrayDeque<PhotoItem>()

    init {
        loadPhotos()
    }

    // ── Public API ───────────────────────────────────────────────────

    /**
     * Load (or reload) unsorted photos from the device's MediaStore.
     */
    fun loadPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _photos.value = repository.getUnsortedPhotos()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Handle a completed swipe gesture.
     *
     * Maps the [direction] to a [SwipeAction], records the decision, removes
     * the photo from the queue, and emits point feedback.
     */
    fun onCardSwiped(photo: PhotoItem, direction: SwipeDirection) {
        val action = when (direction) {
            SwipeDirection.RIGHT -> SwipeAction.KEEP
            SwipeDirection.LEFT -> SwipeAction.TRASH
            SwipeDirection.UP -> SwipeAction.ALBUM
            SwipeDirection.DOWN -> SwipeAction.SKIP
            SwipeDirection.NONE -> return
        }

        val points = when (action) {
            SwipeAction.KEEP -> 10
            SwipeAction.TRASH -> 10
            SwipeAction.ALBUM -> 15
            SwipeAction.SKIP -> 0
        }

        viewModelScope.launch {
            repository.recordDecision(photo, action)
            undoStack.addLast(photo)
            _photos.value = _photos.value.filter { it.id != photo.id }
            _lastPoints.value = points
        }
    }

    /**
     * Undo the last sort decision: removes the decision from the database,
     * adjusts stats, and re-inserts the photo at the front of the queue.
     */
    fun onUndo() {
        viewModelScope.launch {
            val decision = repository.undoLastDecision() ?: return@launch
            val photo = undoStack.removeLastOrNull() ?: return@launch
            _photos.value = listOf(photo) + _photos.value
            _lastPoints.value = 0
        }
    }

    /**
     * Clear the one-shot points value after the popup animation has played.
     */
    fun clearLastPoints() {
        _lastPoints.value = 0
    }


}
