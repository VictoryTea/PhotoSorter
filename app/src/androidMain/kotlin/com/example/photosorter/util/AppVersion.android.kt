package com.example.photosorter.util

import com.example.photosorter.BuildConfig

actual fun getAppVersion(): String {
    return BuildConfig.VERSION_NAME
}
