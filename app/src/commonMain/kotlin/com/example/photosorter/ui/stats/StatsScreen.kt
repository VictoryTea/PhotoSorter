package com.example.photosorter.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.photosorter.data.local.UserStats
import com.example.photosorter.theme.AccentPurple
import com.example.photosorter.theme.AccentViolet
import com.example.photosorter.theme.AlbumBlue
import com.example.photosorter.theme.DarkCard
import com.example.photosorter.theme.GlassBorder
import com.example.photosorter.theme.GoldPoints
import com.example.photosorter.theme.KeepGreen
import com.example.photosorter.theme.LevelPurple
import com.example.photosorter.theme.SkipGray
import com.example.photosorter.theme.StreakOrange
import com.example.photosorter.theme.TextMuted
import com.example.photosorter.theme.TextPrimary
import com.example.photosorter.theme.TextSecondary
import com.example.photosorter.theme.TrashRed

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.stats.collectAsState()
    val currentStats = stats ?: UserStats()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Your Stats",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Level Progress
        LevelCard(
            level = currentStats.level,
            totalPoints = currentStats.totalPoints,
            pointsToNextLevel = ((currentStats.level) * 100) - currentStats.totalPoints
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Streak Card
        StreakCard(
            currentStreak = currentStats.currentStreak,
            longestStreak = currentStats.longestStreak
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Action Breakdown
        Text(
            text = "Action Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionStatCard(
                icon = Icons.Default.CheckCircle,
                count = currentStats.photosKept,
                label = "Kept",
                color = KeepGreen,
                modifier = Modifier.weight(1f)
            )
            ActionStatCard(
                icon = Icons.Default.Delete,
                count = currentStats.photosTrashed,
                label = "Trashed",
                color = TrashRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionStatCard(
                icon = Icons.Default.Folder,
                count = currentStats.photosAlbumed,
                label = "Albums",
                color = AlbumBlue,
                modifier = Modifier.weight(1f)
            )
            ActionStatCard(
                icon = Icons.Default.SkipNext,
                count = currentStats.photosSkipped,
                label = "Skipped",
                color = SkipGray,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Storage Freed
        StorageCard(bytesFreed = currentStats.totalStorageFreed)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LevelCard(
    level: Int,
    totalPoints: Int,
    pointsToNextLevel: Int
) {
    val levelProgress = if (level > 0) {
        val pointsInCurrentLevel = totalPoints - ((level - 1) * 100)
        (pointsInCurrentLevel.toFloat() / 100f).coerceIn(0f, 1f)
    } else 0f

    var animatedProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(levelProgress) {
        animatedProgress = levelProgress
    }
    val smoothProgress by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(1000),
        label = "level_progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCard)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(LevelPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$level",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LevelPurple
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Level $level",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "$totalPoints total points",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = GoldPoints,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { smoothProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = LevelPurple,
            trackColor = LevelPurple.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${pointsToNextLevel.coerceAtLeast(0)} points to next level",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}

@Composable
private fun StreakCard(
    currentStreak: Int,
    longestStreak: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCard)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = "Streak",
            tint = StreakOrange,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "$currentStreak day streak",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Best: $longestStreak days",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "🔥",
            fontSize = 32.sp
        )
    }
}

@Composable
private fun ActionStatCard(
    icon: ImageVector,
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun StorageCard(bytesFreed: Long) {
    val formatted = formatBytes(bytesFreed)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCard)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = "Storage freed",
            tint = KeepGreen,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "Storage Freed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = formatted,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = KeepGreen
            )
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val index = digitGroups.coerceIn(0, units.size - 1)
    return String.format("%.1f %s", bytes / Math.pow(1024.0, index.toDouble()), units[index])
}
