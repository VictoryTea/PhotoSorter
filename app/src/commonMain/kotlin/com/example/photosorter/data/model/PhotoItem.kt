package com.example.photosorter.data.model

data class PhotoItem(
    val id: Long,
    val uri: String,
    val displayName: String,
    val dateAdded: Long,
    val size: Long,
    val width: Int,
    val height: Int
)
