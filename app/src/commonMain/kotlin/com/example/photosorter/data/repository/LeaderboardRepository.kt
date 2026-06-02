package com.example.photosorter.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val username: String,
    val score: Int
)

class LeaderboardRepository {
    private val firestore = Firebase.firestore
    private val leaderboardCollection = firestore.collection("leaderboard")

    fun getTopPlayers(limitCount: Long = 100): Flow<List<LeaderboardEntry>> {
        return leaderboardCollection
            .orderBy("score", Direction.DESCENDING)
            .limit(limitCount)
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents.map { doc ->
                    doc.data()
                }
            }
    }

    suspend fun updateScore(username: String, score: Int) {
        leaderboardCollection.document(username).set(LeaderboardEntry(username, score))
    }
}
