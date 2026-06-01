package com.example.photosorter.data.repository

import com.example.photosorter.data.local.AppDatabase
import com.example.photosorter.data.local.SortDecision
import com.example.photosorter.data.local.UserStats
import com.example.photosorter.data.model.PhotoItem
import com.example.photosorter.data.model.SwipeAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PhotoRepository(
    private val db: AppDatabase,
    private val photoProvider: PhotoProvider
) {
    private val sortDecisionDao = db.sortDecisionDao()
    private val userStatsDao = db.userStatsDao()

    /**
     * Query recent photos from the device,
     * excluding any that have already been sorted.
     */
    suspend fun getUnsortedPhotos(limit: Int = 50): List<PhotoItem> = withContext(Dispatchers.IO) {
        val sortedIds = sortDecisionDao.getSortedPhotoIds().toSet()
        val allPhotos = photoProvider.getRecentPhotos(limit + sortedIds.size)
        allPhotos.filter { it.id !in sortedIds }.take(limit)
    }

    /** Record a sort decision and update stats */
    suspend fun recordDecision(photo: PhotoItem, action: SwipeAction, albumName: String? = null) {
        val points = when (action) {
            SwipeAction.KEEP -> 10
            SwipeAction.TRASH -> 10
            SwipeAction.ALBUM -> 15
            SwipeAction.SKIP -> 0
        }

        sortDecisionDao.insert(
            SortDecision(
                photoId = photo.id,
                photoUri = photo.uri,
                decision = action,
                albumName = albumName,
                pointsEarned = points,
                photoSize = photo.size
            )
        )

        // Update user stats
        val currentStats = userStatsDao.getStatsOnce() ?: UserStats()
        val today = 0L // Temporarily stubbed for KMP
        val lastDay = 0L // Temporarily stubbed for KMP
        val newStreak = if (today - lastDay <= 1) currentStats.currentStreak + 1 else 1

        userStatsDao.upsert(
            currentStats.copy(
                totalPhotosSorted = currentStats.totalPhotosSorted + 1,
                totalStorageFreed = if (action == SwipeAction.TRASH)
                    currentStats.totalStorageFreed + photo.size else currentStats.totalStorageFreed,
                currentStreak = newStreak,
                longestStreak = maxOf(currentStats.longestStreak, newStreak),
                totalPoints = currentStats.totalPoints + points,
                level = (currentStats.totalPoints + points) / 100 + 1,
                lastSortDate = 0L, // Temporarily stubbed for KMP
                photosKept = currentStats.photosKept + if (action == SwipeAction.KEEP) 1 else 0,
                photosTrashed = currentStats.photosTrashed + if (action == SwipeAction.TRASH) 1 else 0,
                photosAlbumed = currentStats.photosAlbumed + if (action == SwipeAction.ALBUM) 1 else 0,
                photosSkipped = currentStats.photosSkipped + if (action == SwipeAction.SKIP) 1 else 0
            )
        )
    }

    /** Undo the last sort decision */
    suspend fun undoLastDecision(): SortDecision? {
        val recent = sortDecisionDao.getRecent(1).firstOrNull() ?: return null
        sortDecisionDao.delete(recent.photoId)

        // Adjust stats
        val stats = userStatsDao.getStatsOnce() ?: return recent
        userStatsDao.upsert(
            stats.copy(
                totalPhotosSorted = (stats.totalPhotosSorted - 1).coerceAtLeast(0),
                totalPoints = (stats.totalPoints - recent.pointsEarned).coerceAtLeast(0),
                totalStorageFreed = if (recent.decision == SwipeAction.TRASH)
                    (stats.totalStorageFreed - recent.photoSize).coerceAtLeast(0)
                else stats.totalStorageFreed
            )
        )
        return recent
    }

    /** Get user stats as Flow */
    fun getStats(): Flow<UserStats?> = userStatsDao.getStats()

    /** Get sorted photo count as Flow */
    fun getSortedCount(): Flow<Int> = sortDecisionDao.getCount()
}
