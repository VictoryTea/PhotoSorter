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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.photosorter.data.repository.PhotoRepository
import com.example.photosorter.theme.AccentPurple
import com.example.photosorter.theme.AccentViolet
import com.example.photosorter.theme.DarkBackground
import com.example.photosorter.theme.DarkSurface
import com.example.photosorter.theme.GlassBorder
import com.example.photosorter.theme.GlassSurface
import com.example.photosorter.theme.TextMuted
import com.example.photosorter.theme.TextPrimary
import com.example.photosorter.ui.home.HomeScreen
import com.example.photosorter.ui.settings.SettingsScreen
import com.example.photosorter.ui.stats.StatsScreen
import com.example.photosorter.ui.stats.StatsViewModel
import com.example.photosorter.ui.swipe.SwipeScreen
import com.example.photosorter.ui.swipe.SwipeViewModel

@Composable
fun MainNavigation(photoRepository: PhotoRepository) {
    val backStack = rememberNavBackStack(Home)
    var selectedTab by remember { mutableStateOf<NavKey>(Home as NavKey) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, DarkSurface)
                )
            )
    ) {
        // Content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp) // Space for bottom nav
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = {
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                        selectedTab = backStack.lastOrNull() ?: Home
                    }
                },
                entryProvider = entryProvider {
                    entry<Home> {
                        HomeScreen(
                            onStartSorting = {
                                backStack.clear()
                                backStack.add(Swipe)
                                selectedTab = Swipe
                            },
                            onNavigateToStats = {
                                backStack.clear()
                                backStack.add(Stats)
                                selectedTab = Stats
                            }
                        )
                    }
                    entry<Swipe> {
                        val swipeViewModel: SwipeViewModel = viewModel { SwipeViewModel(photoRepository) }
                        SwipeScreen(viewModel = swipeViewModel)
                    }
                    entry<Stats> {
                        val statsViewModel: StatsViewModel = viewModel { StatsViewModel(photoRepository) }
                        StatsScreen(viewModel = statsViewModel)
                    }
                    entry<Settings> {
                        SettingsScreen()
                    }
                },
            )
        }

        // Bottom Navigation Bar
        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = { key ->
                selectedTab = key
                backStack.clear()
                backStack.add(key)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun BottomNavBar(
    selectedTab: NavKey,
    onTabSelected: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GlassSurface)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = Icons.Default.Home,
            label = "Home",
            selected = selectedTab == Home,
            onClick = { onTabSelected(Home) }
        )
        BottomNavItem(
            icon = Icons.Default.SwipeRight,
            label = "Sort",
            selected = selectedTab == Swipe,
            onClick = { onTabSelected(Swipe) }
        )
        BottomNavItem(
            icon = Icons.Default.BarChart,
            label = "Stats",
            selected = selectedTab == Stats,
            onClick = { onTabSelected(Stats) }
        )
        BottomNavItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            selected = selectedTab == Settings,
            onClick = { onTabSelected(Settings) }
        )
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) AccentPurple else TextMuted,
        animationSpec = tween(200),
        label = "nav_icon_color"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) AccentPurple.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(200),
        label = "nav_bg_color"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = iconColor,
            fontSize = 10.sp,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
