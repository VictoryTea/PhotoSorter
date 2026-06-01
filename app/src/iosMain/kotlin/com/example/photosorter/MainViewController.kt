package com.example.photosorter

import androidx.compose.ui.window.ComposeUIViewController
import com.example.photosorter.data.local.getDatabase
import com.example.photosorter.data.repository.PhotoProvider
import com.example.photosorter.data.repository.PhotoRepository
import com.example.photosorter.theme.PhotoSorterTheme
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    val db = getDatabase()
    val provider = PhotoProvider()
    val repository = PhotoRepository(db, provider)
    
    PhotoSorterTheme {
        MainNavigation(photoRepository = repository)
    }
}
