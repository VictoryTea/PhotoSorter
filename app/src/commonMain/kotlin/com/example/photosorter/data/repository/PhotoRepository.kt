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
    suspend fun getUnsortedPhotos(limit: Int = 50, sortMode: com.example.photosorter.data.model.SortMode = com.example.photosorter.data.model.SortMode.Recent): List<PhotoItem> = withContext(Dispatchers.IO) {
        val sortedIds = sortDecisionDao.getSortedPhotoIds().toSet()
        // If we are filtering, we might need to pull more items to satisfy the limit, 
        // since we might skip many already sorted. The provider limit bounds the fetch.
        // For OnThisDay, finding 50 might require scanning thousands of photos, 
        // but our provider does it in-memory. We should pass a much larger limit to the provider 
        // so it scans enough to find the matches.
        val providerLimit = if (sortMode is com.example.photosorter.data.model.SortMode.OnThisDay) 10000 else limit + sortedIds.size
        val allPhotos = photoProvider.getRecentPhotos(providerLimit, sortMode)
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

    /** Get current stats once */
    suspend fun getStatsOnce(): UserStats? = userStatsDao.getStatsOnce()

    /** Set the preferred sort mode */
    suspend fun setSortMode(mode: String) {
        val stats = getStatsOnce()
        if (stats != null) {
            userStatsDao.upsert(stats.copy(sortMode = mode))
        }
    }

    /** Get sorted photo count as Flow */
    fun getSortedCount(): Flow<Int> = sortDecisionDao.getCount()

    /** Get all photos marked as TRASH */
    suspend fun getTrashedPhotos(): List<PhotoItem> = withContext(Dispatchers.IO) {
        sortDecisionDao.getTrashedPhotos().map { decision ->
            PhotoItem(
                id = decision.photoId,
                uri = decision.photoUri,
                displayName = "Photo",
                dateAdded = decision.timestamp,
                size = decision.photoSize,
                width = 0,
                height = 0
            )
        }
    }

    /** Clear all trash decisions from the database */
    suspend fun clearTrashDecisions() = withContext(Dispatchers.IO) {
        sortDecisionDao.clearTrashDecisions()
    }

    /** Restore photos from trash (undo the trash decision) */
    suspend fun restorePhotos(photoIds: List<Long>) = withContext(Dispatchers.IO) {
        val trashed = sortDecisionDao.getTrashedPhotos().filter { it.photoId in photoIds }
        val totalSizeToRestore = trashed.sumOf { it.photoSize }
        
        sortDecisionDao.deleteByPhotoIds(photoIds)

        // Adjust stats: remove points and storage freed
        val currentStats = userStatsDao.getStatsOnce() ?: return@withContext
        userStatsDao.upsert(
            currentStats.copy(
                totalPhotosSorted = (currentStats.totalPhotosSorted - photoIds.size).coerceAtLeast(0),
                photosTrashed = (currentStats.photosTrashed - photoIds.size).coerceAtLeast(0),
                totalStorageFreed = (currentStats.totalStorageFreed - totalSizeToRestore).coerceAtLeast(0),
                totalPoints = (currentStats.totalPoints - (10 * photoIds.size)).coerceAtLeast(0)
            )
        )
    }
}
