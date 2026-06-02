package com.example.photosorter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1, // singleton row
    val totalPhotosSorted: Int = 0,
    val totalStorageFreed: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalPoints: Int = 0, // Lifetime points (for leaderboard)
    val currentPoints: Int = 0, // Spendable points
    val level: Int = 1,
    val lastSortDate: Long = 0,
    val photosKept: Int = 0,
    val photosTrashed: Int = 0,
    val photosAlbumed: Int = 0,
    val photosSkipped: Int = 0,
    val unlockedThemes: String = "default", // Comma separated theme IDs
    val activeThemeId: String = "default",
    val username: String? = null, // For leaderboard
    val sortMode: String = "recent" // "recent", "month_2023_05", "on_this_day"
)
