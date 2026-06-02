package com.example.photosorter.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SortDecisionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(decision: SortDecision)

    @Query("SELECT * FROM sort_decisions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SortDecision>>

    @Query("SELECT photoId FROM sort_decisions")
    suspend fun getSortedPhotoIds(): List<Long>

    @Query("SELECT * FROM sort_decisions ORDER BY timestamp DESC LIMIT :count")
    suspend fun getRecent(count: Int): List<SortDecision>

    @Query("DELETE FROM sort_decisions WHERE photoId = :photoId")
    suspend fun delete(photoId: Long)

    @Query("SELECT COUNT(*) FROM sort_decisions")
    fun getCount(): Flow<Int>

    @Query("SELECT * FROM sort_decisions WHERE decision = 'TRASH' ORDER BY timestamp DESC")
    suspend fun getTrashedPhotos(): List<SortDecision>

    @Query("DELETE FROM sort_decisions WHERE decision = 'TRASH'")
    suspend fun clearTrashDecisions()

    @Query("DELETE FROM sort_decisions WHERE photoId IN (:photoIds)")
    suspend fun deleteByPhotoIds(photoIds: List<Long>)
}
