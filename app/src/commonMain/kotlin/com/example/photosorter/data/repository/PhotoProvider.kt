package com.example.photosorter.data.repository

import com.example.photosorter.data.model.PhotoItem

import com.example.photosorter.data.model.SortMode

expect class PhotoProvider {
    suspend fun getRecentPhotos(limit: Int, sortMode: SortMode = SortMode.Recent): List<PhotoItem>
}
