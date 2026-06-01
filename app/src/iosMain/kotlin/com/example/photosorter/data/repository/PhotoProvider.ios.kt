package com.example.photosorter.data.repository

import com.example.photosorter.data.model.PhotoItem
import platform.Photos.PHAsset
import platform.Photos.PHFetchOptions
import platform.Photos.PHAssetMediaTypeImage
import platform.Foundation.NSSortDescriptor
import kotlinx.cinterop.ExperimentalForeignApi

actual class PhotoProvider {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getRecentPhotos(limit: Int): List<PhotoItem> {
        val photos = mutableListOf<PhotoItem>()
        val options = PHFetchOptions().apply {
            sortDescriptors = listOf(NSSortDescriptor("creationDate", ascending = false))
            fetchLimit = limit.toULong()
        }
        
        val fetchResult = PHAsset.fetchAssetsWithMediaType(PHAssetMediaTypeImage, options)
        for (i in 0 until fetchResult.count.toInt()) {
            val asset = fetchResult.objectAtIndex(i.toULong()) as? PHAsset ?: continue
            photos.add(
                PhotoItem(
                    id = asset.localIdentifier.hashCode().toLong(),
                    uri = asset.localIdentifier, // Use localIdentifier for image loading
                    displayName = "iOS_Photo_$i",
                    dateAdded = (asset.creationDate?.timeIntervalSince1970 ?: 0.0).toLong(),
                    size = 0L, // Size requires async request in PhotoKit
                    width = asset.pixelWidth.toInt(),
                    height = asset.pixelHeight.toInt()
                )
            )
        }
        return photos
    }
}
