package com.example.photosorter.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.photosorter.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

actual class AppUpdater(private val context: Context) {
    
    // Configurable update URL - pointing to VictoryTea/PhotoSorter master branch
    private val UPDATE_URL = "https://raw.githubusercontent.com/VictoryTea/PhotoSorter/master/version.json"

    actual suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(UPDATE_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            
            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.use { it.readText() }
                val json = JSONObject(response)
                
                val serverVersionCode = json.getInt("versionCode")
                if (serverVersionCode > BuildConfig.VERSION_CODE) {
                    return@withContext UpdateInfo(
                        versionCode = serverVersionCode,
                        versionName = json.getString("versionName"),
                        apkUrl = json.getString("apkUrl")
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    actual fun downloadAndInstallUpdate(apkUrl: String) {
        val fileName = "PhotoSorter-update.apk"
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadDir, fileName)
        if (file.exists()) {
            file.delete() // Clean up previous downloads
        }

        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("Downloading Photo Sorter Update")
            .setDescription("Downloading latest version")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        
        // Register receiver for when download finishes
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    installApk(file)
                    context.unregisterReceiver(this)
                }
            }
        }
        
        // Use correct receiver flag for Android 14+
        if (android.os.Build.VERSION.SDK_INT >= 33) { // TIRAMISU
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }
    
    private fun installApk(file: File) {
        if (!file.exists()) return
        
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }
}
