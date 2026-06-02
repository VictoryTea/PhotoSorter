package com.example.photosorter

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.photosorter.data.local.UserStatsDao
import com.example.photosorter.data.repository.LeaderboardRepository
import com.example.photosorter.data.repository.PhotoRepository
import com.example.photosorter.theme.AccentPurple
import com.example.photosorter.theme.DarkBackground
import com.example.photosorter.theme.DarkSurface
import com.example.photosorter.theme.GlassSurface
import com.example.photosorter.theme.TextMuted
import com.example.photosorter.ui.home.HomeScreen
import com.example.photosorter.ui.leaderboard.LeaderboardScreen
import com.example.photosorter.ui.leaderboard.LeaderboardViewModel
import com.example.photosorter.ui.settings.SettingsScreen
import com.example.photosorter.ui.stats.StatsScreen
import com.example.photosorter.ui.stats.StatsViewModel
import com.example.photosorter.ui.store.StoreScreen
import com.example.photosorter.ui.store.StoreViewModel
import com.example.photosorter.ui.swipe.SwipeScreen
import com.example.photosorter.ui.swipe.SwipeViewModel
import com.example.photosorter.ui.trash.TrashScreen
import com.example.photosorter.ui.trash.TrashViewModel
import com.example.photosorter.util.AudioPlayer
import com.example.photosorter.PhotoDeleter
import com.example.photosorter.updater.AppUpdater
import com.example.photosorter.updater.UpdateInfo
import kotlinx.serialization.Serializable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun MainNavigation(
    photoRepository: PhotoRepository,
    photoDeleter: PhotoDeleter,
    userStatsDao: UserStatsDao,
    leaderboardRepository: LeaderboardRepository,
    audioPlayer: AudioPlayer,
    backgroundPainter: Painter,
    appUpdater: AppUpdater
) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf<Any>(Home) }
    
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val info = appUpdater.checkForUpdate()
        if (info != null) {
            updateInfo = info
            showUpdateDialog = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Global Theme Background Image
        Image(
            painter = backgroundPainter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Dark Overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)
        ) {
            NavHost(
                navController = navController,
                startDestination = Home
            ) {
                composable<Home> {
                    val statsViewModel: StatsViewModel = viewModel { StatsViewModel(photoRepository) }
                    HomeScreen(
                        viewModel = statsViewModel,
                        onStartSorting = {
                            navController.navigate(Swipe) {
                                popUpTo(Home) { inclusive = false }
                            }
                            selectedTab = Swipe
                        },
                        onNavigateToStats = {
                            navController.navigate(Stats) {
                                popUpTo(Home) { inclusive = false }
                            }
                            selectedTab = Stats
                        },
                        onNavigateToTrash = {
                            navController.navigate(Trash) {
                                popUpTo(Home) { inclusive = false }
                            }
                            selectedTab = Trash
                        },
                        onNavigateToSettings = {
                            navController.navigate(Settings) {
                                popUpTo(Home) { inclusive = false }
                            }
                            selectedTab = Settings
                        }
                    )
                }
                composable<Swipe> {
                    val swipeViewModel: SwipeViewModel = viewModel { SwipeViewModel(photoRepository, audioPlayer) }
                    SwipeScreen(viewModel = swipeViewModel)
                }
                composable<Stats> {
                    val statsViewModel: StatsViewModel = viewModel { StatsViewModel(photoRepository) }
                    StatsScreen(viewModel = statsViewModel)
                }
                composable<Settings> {
                    SettingsScreen()
                }
                composable<Trash> {
                    val trashViewModel: TrashViewModel = viewModel { TrashViewModel(photoRepository, photoDeleter) }
                    TrashScreen(viewModel = trashViewModel)
                }
                composable<Store> {
                    val storeViewModel: StoreViewModel = viewModel { StoreViewModel(userStatsDao, audioPlayer) }
                    StoreScreen(viewModel = storeViewModel)
                }
                composable<Leaderboard> {
                    val leaderboardViewModel: LeaderboardViewModel = viewModel { LeaderboardViewModel(userStatsDao, leaderboardRepository) }
                    LeaderboardScreen(viewModel = leaderboardViewModel)
                }
            }
        }

        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = { key ->
                selectedTab = key
                navController.navigate(key) {
                    popUpTo(Home) { inclusive = false }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        if (showUpdateDialog && updateInfo != null) {
            AlertDialog(
                onDismissRequest = { showUpdateDialog = false },
                title = { Text("Update Available", color = Color.White) },
                text = { Text("Version ${updateInfo!!.versionName} is available! Would you like to download and install it now?", color = Color.LightGray) },
                confirmButton = {
                    TextButton(onClick = {
                        appUpdater.downloadAndInstallUpdate(updateInfo!!.apkUrl)
                        showUpdateDialog = false
                    }) {
                        Text("Update", color = AccentPurple)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUpdateDialog = false }) {
                        Text("Later", color = Color.Gray)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}

@Composable
private fun BottomNavBar(
    selectedTab: Any,
    onTabSelected: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.85f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(icon = Icons.Default.Home, label = "Home", selected = selectedTab == Home, onClick = { onTabSelected(Home) })
        BottomNavItem(icon = Icons.Default.SwipeRight, label = "Sort", selected = selectedTab == Swipe, onClick = { onTabSelected(Swipe) })
        BottomNavItem(icon = Icons.Default.ShoppingCart, label = "Store", selected = selectedTab == Store, onClick = { onTabSelected(Store) })
        BottomNavItem(icon = Icons.Default.Delete, label = "Trash", selected = selectedTab == Trash, onClick = { onTabSelected(Trash) })
        BottomNavItem(icon = Icons.Default.Star, label = "Top", selected = selectedTab == Leaderboard, onClick = { onTabSelected(Leaderboard) })
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(targetValue = if (selected) MaterialTheme.colorScheme.primary else TextMuted, animationSpec = tween(200), label = "")
    val bgColor by animateColorAsState(targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent, animationSpec = tween(200), label = "")

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(24.dp))
        Text(text = label, color = iconColor, fontSize = 10.sp, style = MaterialTheme.typography.labelSmall)
    }
}
