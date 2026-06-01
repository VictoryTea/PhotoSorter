package com.example.photosorter.data.repository

import com.example.photosorter.data.model.PhotoItem

expect class PhotoProvider {
    suspend fun getRecentPhotos(limit: Int): List<PhotoItem>
}
