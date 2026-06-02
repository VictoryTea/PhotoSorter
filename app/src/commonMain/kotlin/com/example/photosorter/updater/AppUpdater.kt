package com.example.photosorter.updater

data class UpdateInfo(val versionCode: Int, val versionName: String, val apkUrl: String)

expect class AppUpdater {
    suspend fun checkForUpdate(): UpdateInfo?
    fun downloadAndInstallUpdate(apkUrl: String)
}
