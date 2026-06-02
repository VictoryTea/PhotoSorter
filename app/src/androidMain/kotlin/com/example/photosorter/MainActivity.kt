package com.example.photosorter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
            ) { _ ->
                // Permissions handled
            }

            androidx.compose.runtime.LaunchedEffect(Unit) {
                val permissionsToRequest = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    )
                } else {
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                permissionLauncher.launch(permissionsToRequest)
            }

            val pendingDeleteCallback = androidx.compose.runtime.remember { 
                androidx.compose.runtime.mutableStateOf<((Boolean) -> Unit)?>(null) 
            }
            
            val deleteLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                pendingDeleteCallback.value?.invoke(result.resultCode == android.app.Activity.RESULT_OK)
                pendingDeleteCallback.value = null
            }

            val photoDeleter = androidx.compose.runtime.remember {
                object : PhotoDeleter {
                    override fun deletePhotos(uris: List<String>, onResult: (Boolean) -> Unit) {
                        val parsedUris = uris.map { android.net.Uri.parse(it) }
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            try {
                                val pendingIntent = android.provider.MediaStore.createTrashRequest(contentResolver, parsedUris, true)
                                pendingDeleteCallback.value = onResult
                                deleteLauncher.launch(
                                    androidx.activity.result.IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                                )
                            } catch (e: Exception) {
                                onResult(false)
                            }
                        } else {
                            try {
                                var allSuccess = true
                                parsedUris.forEach { uri ->
                                    val deleted = contentResolver.delete(uri, null, null)
                                    if (deleted == 0) allSuccess = false
                                }
                                onResult(allSuccess)
                            } catch (e: Exception) {
                                onResult(false)
                            }
                        }
                    }
                }
            }

            val stats by db.userStatsDao().getStats().collectAsState(initial = null)
            val themeId = stats?.activeThemeId ?: "default"

            val bgResId = when (themeId) {
                "space" -> R.drawable.bg_space
                "cyberpunk" -> R.drawable.bg_cyberpunk
                "cowboy" -> R.drawable.bg_cowboy
                "piggie" -> R.drawable.bg_piggie
                "cow" -> R.drawable.bg_cow
                "raccoon" -> R.drawable.bg_raccoon
                "zombie" -> R.drawable.bg_zombie
                "military" -> R.drawable.bg_military
                "meep" -> R.drawable.bg_meep
                else -> R.drawable.bg_default
            }
            val bgPainter = androidx.compose.ui.res.painterResource(id = bgResId)

            PhotoSorterTheme(themeId = themeId) {
                val leaderboardRepository = androidx.compose.runtime.remember { com.example.photosorter.data.repository.LeaderboardRepository() }
                val audioPlayer = androidx.compose.runtime.remember { com.example.photosorter.util.AndroidAudioPlayer(applicationContext) }
                val appUpdater = androidx.compose.runtime.remember { com.example.photosorter.updater.AppUpdater(applicationContext) }
                
                MainNavigation(
                    photoRepository = repository, 
                    photoDeleter = photoDeleter,
                    userStatsDao = db.userStatsDao(),
                    leaderboardRepository = leaderboardRepository,
                    audioPlayer = audioPlayer,
                    backgroundPainter = bgPainter,
                    appUpdater = appUpdater
                )
            }
        }
    }
}
