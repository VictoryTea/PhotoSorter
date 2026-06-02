package com.example.photosorter.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE user_stats ADD COLUMN sortMode TEXT NOT NULL DEFAULT 'recent'")
    }
}

fun getDatabase(context: Context): AppDatabase {
    val dbFile = context.getDatabasePath("photo_sorter_db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
        .addMigrations(MIGRATION_2_3)
        .setDriver(BundledSQLiteDriver())
        .build()
}
