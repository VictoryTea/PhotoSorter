package com.example.photosorter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.photosorter.theme.PhotoSorterTheme
import com.example.photosorter.data.local.getDatabase
import com.example.photosorter.data.repository.PhotoProvider
import com.example.photosorter.data.repository.PhotoRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val db = getDatabase(applicationContext)
        val provider = PhotoProvider(applicationContext)
        val repository = PhotoRepository(db, provider)
        
        setContent {
            PhotoSorterTheme {
                MainNavigation(photoRepository = repository)
            }
        }
    }
}
