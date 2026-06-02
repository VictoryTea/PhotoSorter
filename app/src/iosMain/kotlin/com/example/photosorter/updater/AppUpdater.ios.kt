package com.example.photosorter.updater

actual class AppUpdater {
    actual suspend fun checkForUpdate(): UpdateInfo? = null

    actual fun downloadAndInstallUpdate(apkUrl: String) {
        // No-op on iOS. Updates must be managed through TestFlight or the App Store.
    }
}
