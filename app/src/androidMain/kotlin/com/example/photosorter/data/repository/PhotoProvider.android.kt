package com.example.photosorter.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.photosorter.data.model.PhotoItem
import com.example.photosorter.data.model.SortMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth

actual class PhotoProvider(private val context: Context) {
    actual suspend fun getRecentPhotos(limit: Int, sortMode: SortMode): List<PhotoItem> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<PhotoItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )

        var selection: String? = null
        var selectionArgs: Array<String>? = null
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        when (sortMode) {
            is SortMode.Recent -> {
                // No selection, just recent
            }
            is SortMode.Month -> {
                val yearMonth = YearMonth.of(sortMode.year, sortMode.month)
                val startOfMonthSecs = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
                val endOfMonthSecs = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond()
                selection = "${MediaStore.Images.Media.DATE_ADDED} >= ? AND ${MediaStore.Images.Media.DATE_ADDED} <= ?"
                selectionArgs = arrayOf(startOfMonthSecs.toString(), endOfMonthSecs.toString())
            }
            is SortMode.OnThisDay -> {
                // We'll filter in-memory, so no selection here
            }
        }

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

            val today = LocalDate.now()

            var count = 0
            while (cursor.moveToNext() && count < limit) {
                val dateAddedSecs = cursor.getLong(dateCol)
                
                if (sortMode is SortMode.OnThisDay) {
                    val photoDate = Instant.ofEpochSecond(dateAddedSecs).atZone(ZoneId.systemDefault()).toLocalDate()
                    // Must be same month and day, but we don't care about the year. 
                    // Actually, we probably want only previous years, or just any year.
                    if (photoDate.monthValue != today.monthValue || photoDate.dayOfMonth != today.dayOfMonth) {
                        continue
                    }
                }

                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                photos.add(
                    PhotoItem(
                        id = id,
                        uri = uri.toString(),
                        displayName = cursor.getString(nameCol) ?: "Unknown",
                        dateAdded = dateAddedSecs,
                        size = cursor.getLong(sizeCol),
                        width = cursor.getInt(widthCol),
                        height = cursor.getInt(heightCol)
                    )
                )
                count++
            }
        }
        photos
    }
}
