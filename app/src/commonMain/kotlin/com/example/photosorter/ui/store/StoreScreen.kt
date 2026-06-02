package com.example.photosorter.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ThemeItem(val id: String, val name: String, val cost: Int, val primaryColor: Color, val backgroundColor: Color)

val availableThemes = listOf(
    ThemeItem("default", "Classic Dark", 0, Color(0xFF9D4EDD), Color(0xFF121212)),
    ThemeItem("space", "Deep Space", 10, Color(0xFF00E5FF), Color(0xFF050510)),
    ThemeItem("cyberpunk", "Cyberpunk", 20, Color(0xFFFF007F), Color(0xFF0D0221)),
    ThemeItem("cowboy", "Wild West", 30, Color(0xFFFF8F00), Color(0xFF3E2723)),
    ThemeItem("piggie", "Piggies", 40, Color(0xFFEC407A), Color(0xFFFCE4EC)),
    ThemeItem("cow", "Highland Cow", 50, Color(0xFFD84315), Color(0xFF4E342E)),
    ThemeItem("raccoon", "Raccoon", 60, Color(0xFFFBC02D), Color(0xFF212121)),
    ThemeItem("zombie", "Zombie Survival", 70, Color(0xFFB71C1C), Color(0xFF1B5E20)),
    ThemeItem("military", "Military", 80, Color(0xFF8D6E63), Color(0xFF33691E)),
    ThemeItem("meep", "Meep", 90, Color(0xFFD7CCC8), Color(0xFF3E2723))
)

@Composable
fun StoreScreen(viewModel: StoreViewModel) {
    val stats by viewModel.stats.collectAsState()
    val points = stats?.totalPoints ?: 0
    val unlockedThemes = stats?.unlockedThemes?.split(",") ?: listOf("default")
    val activeThemeId = stats?.activeThemeId ?: "default"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 48.dp)
    ) {
        Text(
            text = "Theme Store",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Available Points", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("$points", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(availableThemes) { theme ->
                val isUnlocked = unlockedThemes.contains(theme.id)
                val isActive = activeThemeId == theme.id
                val canAfford = points >= theme.cost

                ThemeCard(
                    theme = theme,
                    isUnlocked = isUnlocked,
                    isActive = isActive,
                    canAfford = canAfford,
                    onClick = {
                        if (isUnlocked) {
                            viewModel.equipTheme(theme.id)
                        } else if (canAfford) {
                            viewModel.purchaseTheme(theme.id, theme.cost)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeCard(theme: ThemeItem, isUnlocked: Boolean, isActive: Boolean, canAfford: Boolean, onClick: () -> Unit) {
    val borderColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .border(3.dp, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = isUnlocked || canAfford) { onClick() },
        colors = CardDefaults.cardColors(containerColor = theme.backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(24.dp).clip(RoundedCornerShape(12.dp)).background(theme.primaryColor)
                )
                
                if (isActive) {
                    Icon(Icons.Default.Check, contentDescription = "Active", tint = theme.primaryColor, modifier = Modifier.size(20.dp))
                } else if (!isUnlocked) {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
            
            // Footer
            Column {
                Text(theme.name, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                if (!isUnlocked) {
                    Text(
                        text = "${theme.cost} pts", 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = if (canAfford) theme.primaryColor else Color.Gray
                    )
                } else {
                    Text(
                        text = if (isActive) "Equipped" else "Owned", 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}
