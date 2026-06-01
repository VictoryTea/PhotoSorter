package com.example.photosorter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.photosorter.data.model.SwipeAction

@Entity(tableName = "sort_decisions")
data class SortDecision(
    @PrimaryKey val photoId: Long,
    val photoUri: String,
    val decision: SwipeAction,
    val albumName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val pointsEarned: Int = 0,
    val photoSize: Long = 0
)
