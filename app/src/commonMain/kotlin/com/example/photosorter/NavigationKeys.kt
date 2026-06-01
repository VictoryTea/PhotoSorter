package com.example.photosorter

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Home : NavKey
@Serializable data object Swipe : NavKey
@Serializable data object Stats : NavKey
@Serializable data object Settings : NavKey
