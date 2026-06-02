package com.example.photosorter.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PhotoSorterColorScheme = darkColorScheme(
    primary = AccentPurple,
    onPrimary = Color.White,
    primaryContainer = AccentViolet,
    onPrimaryContainer = Color.White,
    secondary = KeepGreen,
    onSecondary = Color.Black,
    secondaryContainer = AlbumBlue,
    onSecondaryContainer = Color.White,
    tertiary = GoldPoints,
    onTertiary = Color.Black,
    error = TrashRed,
    onError = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = TextMuted,
)

private val SpaceColorScheme = darkColorScheme(
    primary = SpaceAccent,
    onPrimary = Color.Black,
    primaryContainer = SpaceAccent.copy(alpha = 0.5f),
    secondary = SpaceGreen,
    onSecondary = Color.Black,
    tertiary = SpaceGold,
    error = SpaceRed,
    background = SpaceBackground,
    onBackground = Color.White,
    surface = SpaceSurface,
    onSurface = Color.White,
    surfaceVariant = SpaceBackground,
    onSurfaceVariant = Color.LightGray,
    outline = SpaceAccent.copy(alpha = 0.5f)
)

private val CyberColorScheme = darkColorScheme(
    primary = CyberAccent,
    onPrimary = Color.White,
    primaryContainer = CyberAccent.copy(alpha = 0.5f),
    secondary = CyberGreen,
    onSecondary = Color.Black,
    tertiary = CyberGold,
    error = CyberRed,
    background = CyberBackground,
    onBackground = Color.White,
    surface = CyberSurface,
    onSurface = Color.White,
    surfaceVariant = CyberBackground,
    onSurfaceVariant = Color.LightGray,
    outline = CyberAccent.copy(alpha = 0.5f)
)

private val CowboyColorScheme = darkColorScheme(
    primary = CowboyAccent,
    onPrimary = Color.Black,
    primaryContainer = CowboyAccent.copy(alpha = 0.5f),
    secondary = CowboyGreen,
    onSecondary = Color.Black,
    tertiary = CowboyGold,
    error = CowboyRed,
    background = CowboyBackground,
    onBackground = Color.White,
    surface = CowboySurface,
    onSurface = Color.White,
    surfaceVariant = CowboyBackground,
    onSurfaceVariant = Color.LightGray,
    outline = CowboyAccent.copy(alpha = 0.5f)
)

private val PiggieColorScheme = darkColorScheme(
    primary = PiggieAccent,
    onPrimary = Color.White,
    primaryContainer = PiggieAccent.copy(alpha = 0.5f),
    secondary = PiggieGreen,
    onSecondary = Color.Black,
    tertiary = PiggieGold,
    error = PiggieRed,
    background = PiggieBackground,
    onBackground = PiggieText,
    surface = PiggieSurface,
    onSurface = PiggieText,
    surfaceVariant = PiggieBackground,
    onSurfaceVariant = Color.DarkGray,
    outline = PiggieAccent.copy(alpha = 0.5f)
)

private val CowColorScheme = darkColorScheme(
    primary = CowAccent,
    onPrimary = Color.White,
    primaryContainer = CowAccent.copy(alpha = 0.5f),
    secondary = CowGreen,
    onSecondary = Color.Black,
    tertiary = CowGold,
    error = CowRed,
    background = CowBackground,
    onBackground = Color.White,
    surface = CowSurface,
    onSurface = Color.White,
    surfaceVariant = CowBackground,
    onSurfaceVariant = Color.LightGray,
    outline = CowAccent.copy(alpha = 0.5f)
)

private val RaccoonColorScheme = darkColorScheme(
    primary = RaccoonAccent,
    onPrimary = Color.Black,
    primaryContainer = RaccoonAccent.copy(alpha = 0.5f),
    secondary = RaccoonGreen,
    onSecondary = Color.Black,
    tertiary = RaccoonGold,
    error = RaccoonRed,
    background = RaccoonBackground,
    onBackground = Color.White,
    surface = RaccoonSurface,
    onSurface = Color.White,
    surfaceVariant = RaccoonBackground,
    onSurfaceVariant = Color.LightGray,
    outline = RaccoonAccent.copy(alpha = 0.5f)
)

private val ZombieColorScheme = darkColorScheme(
    primary = ZombieAccent,
    onPrimary = Color.White,
    primaryContainer = ZombieAccent.copy(alpha = 0.5f),
    secondary = ZombieGreen,
    onSecondary = Color.Black,
    tertiary = ZombieGold,
    error = ZombieRed,
    background = ZombieBackground,
    onBackground = Color.White,
    surface = ZombieSurface,
    onSurface = Color.White,
    surfaceVariant = ZombieBackground,
    onSurfaceVariant = Color.LightGray,
    outline = ZombieAccent.copy(alpha = 0.5f)
)

private val MilitaryColorScheme = darkColorScheme(
    primary = MilitaryAccent,
    onPrimary = Color.Black,
    primaryContainer = MilitaryAccent.copy(alpha = 0.5f),
    secondary = MilitaryGreen,
    onSecondary = Color.Black,
    tertiary = MilitaryGold,
    error = MilitaryRed,
    background = MilitaryBackground,
    onBackground = Color.White,
    surface = MilitarySurface,
    onSurface = Color.White,
    surfaceVariant = MilitaryBackground,
    onSurfaceVariant = Color.LightGray,
    outline = MilitaryAccent.copy(alpha = 0.5f)
)

private val MeepColorScheme = darkColorScheme(
    primary = MeepAccent,
    onPrimary = Color.Black,
    primaryContainer = MeepAccent.copy(alpha = 0.5f),
    secondary = MeepGreen,
    onSecondary = Color.Black,
    tertiary = MeepGold,
    error = MeepRed,
    background = MeepBackground,
    onBackground = MeepText,
    surface = MeepSurface,
    onSurface = MeepText,
    surfaceVariant = MeepBackground,
    onSurfaceVariant = Color.LightGray,
    outline = MeepAccent.copy(alpha = 0.5f)
)

@Composable
fun PhotoSorterTheme(
    themeId: String = "default",
    content: @Composable () -> Unit,
) {
    val colorScheme = when(themeId) {
        "space" -> SpaceColorScheme
        "cyberpunk" -> CyberColorScheme
        "cowboy" -> CowboyColorScheme
        "piggie" -> PiggieColorScheme
        "cow" -> CowColorScheme
        "raccoon" -> RaccoonColorScheme
        "zombie" -> ZombieColorScheme
        "military" -> MilitaryColorScheme
        "meep" -> MeepColorScheme
        else -> PhotoSorterColorScheme
    }

    // Set status bar to transparent with light icons
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
