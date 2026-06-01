package com.example.photosorter.data.local

import androidx.room.TypeConverter
import com.example.photosorter.data.model.SwipeAction

class Converters {
    @TypeConverter
    fun fromSwipeAction(action: SwipeAction): String = action.name

    @TypeConverter
    fun toSwipeAction(name: String): SwipeAction = SwipeAction.valueOf(name)
}
