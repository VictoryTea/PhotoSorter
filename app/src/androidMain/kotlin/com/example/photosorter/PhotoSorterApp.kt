package com.example.photosorter

import android.app.Application

/**
 * Application class for Photo Sorter.
 * Initializes app-wide singletons like Coil image loader.
 */
class PhotoSorterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Coil 3 auto-initializes via its content provider,
        // so no explicit setup needed here.
    }
}
