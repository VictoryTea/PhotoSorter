package com.example.photosorter.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photosorter.data.local.UserStats
import com.example.photosorter.data.repository.PhotoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(private val repository: PhotoRepository) : ViewModel() {

    val stats: StateFlow<UserStats?> = repository.getStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val sortedCount: StateFlow<Int> = repository.getSortedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
