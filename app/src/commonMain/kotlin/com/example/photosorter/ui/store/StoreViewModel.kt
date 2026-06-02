package com.example.photosorter.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photosorter.data.local.UserStats
import com.example.photosorter.data.local.UserStatsDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.photosorter.util.AudioPlayer

class StoreViewModel(
    private val userStatsDao: UserStatsDao,
    private val audioPlayer: AudioPlayer
) : ViewModel() {
    val stats: StateFlow<UserStats?> = userStatsDao.getStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun purchaseTheme(themeId: String, cost: Int) {
        viewModelScope.launch {
            val currentStats = userStatsDao.getStatsOnce() ?: return@launch
            
            // Check if already unlocked
            val unlockedList = currentStats.unlockedThemes.split(",")
            if (unlockedList.contains(themeId)) {
                // Just equip it
                userStatsDao.upsert(currentStats.copy(activeThemeId = themeId))
                return@launch
            }
            
            // Check points
            if (currentStats.totalPoints >= cost) {
                val newUnlocked = if (currentStats.unlockedThemes.isEmpty()) themeId else "${currentStats.unlockedThemes},$themeId"
                userStatsDao.upsert(
                    currentStats.copy(
                        totalPoints = currentStats.totalPoints - cost,
                        unlockedThemes = newUnlocked,
                        activeThemeId = themeId
                    )
                )
                audioPlayer.playThemeSelectSound(themeId)
            }
        }
    }
    
    fun equipTheme(themeId: String) {
        viewModelScope.launch {
            val currentStats = userStatsDao.getStatsOnce() ?: return@launch
            val unlockedList = currentStats.unlockedThemes.split(",")
            if (unlockedList.contains(themeId)) {
                userStatsDao.upsert(currentStats.copy(activeThemeId = themeId))
                audioPlayer.playThemeSelectSound(themeId)
            }
        }
    }
}
