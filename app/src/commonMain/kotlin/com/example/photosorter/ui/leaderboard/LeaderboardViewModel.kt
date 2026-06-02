package com.example.photosorter.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photosorter.data.local.UserStatsDao
import com.example.photosorter.data.repository.LeaderboardEntry
import com.example.photosorter.data.repository.LeaderboardRepository
import com.example.photosorter.util.ProfanityFilter
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LeaderboardViewModel(
    private val userStatsDao: UserStatsDao,
    private val leaderboardRepository: LeaderboardRepository
) : ViewModel() {

    val topPlayers: StateFlow<List<LeaderboardEntry>> = leaderboardRepository.getTopPlayers(100)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentUsername = MutableStateFlow<String?>(null)
    val currentUsername = _currentUsername.asStateFlow()

    private val _joinError = MutableStateFlow<String?>(null)
    val joinError = _joinError.asStateFlow()

    private val _isJoining = MutableStateFlow(false)
    val isJoining = _isJoining.asStateFlow()

    init {
        viewModelScope.launch {
            userStatsDao.getStats().collect { stats ->
                _currentUsername.value = stats?.username
                // Also update their score on the leaderboard if they have a username
                if (stats?.username != null) {
                    try {
                        leaderboardRepository.updateScore(stats.username, stats.totalPoints)
                    } catch (e: Exception) {
                        // Ignore firestore errors locally
                    }
                }
            }
        }
    }

    fun clearError() {
        _joinError.value = null
    }

    fun joinLeaderboard(username: String) {
        val trimmed = username.trim()
        if (trimmed.length < 3) {
            _joinError.value = "Username must be at least 3 characters."
            return
        }
        if (trimmed.length > 15) {
            _joinError.value = "Username must be at most 15 characters."
            return
        }
        if (!ProfanityFilter.isClean(trimmed)) {
            _joinError.value = "Please choose a more appropriate username."
            return
        }

        viewModelScope.launch {
            _isJoining.value = true
            _joinError.value = null
            try {
                // Check if username exists to allow resuming
                val doc = Firebase.firestore.collection("leaderboard").document(trimmed).get()
                val stats = userStatsDao.getStats().first()
                
                if (doc.exists) {
                    // Resume existing profile: take the max of their local points and server points
                    val existingEntry = doc.data<LeaderboardEntry>()
                    val localPoints = stats?.totalPoints ?: 0
                    val mergedPoints = maxOf(localPoints, existingEntry.score)
                    
                    if (stats != null) {
                        userStatsDao.upsert(stats.copy(username = trimmed, totalPoints = mergedPoints))
                    } else {
                        userStatsDao.upsert(com.example.photosorter.data.local.UserStats(username = trimmed, totalPoints = mergedPoints))
                    }
                } else {
                    // New profile
                    if (stats != null) {
                        userStatsDao.upsert(stats.copy(username = trimmed))
                    } else {
                        userStatsDao.upsert(com.example.photosorter.data.local.UserStats(username = trimmed))
                    }
                }
            } catch (e: Exception) {
                _joinError.value = "Failed to connect to the server."
            } finally {
                _isJoining.value = false
            }
        }
    }
}
