package com.example.photosorter.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSHomeDirectory

fun getDatabase(): AppDatabase {
    val dbFile = NSHomeDirectory() + "/photo_sorter_db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile
    )
        .fallbackToDestructiveMigration(true)
        .setDriver(BundledSQLiteDriver())
        .build()
}
