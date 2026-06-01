package com.example.photosorter.ui.swipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ── Theme colours ────────────────────────────────────────────────────
private val AccentStart = Color(0xFF667EEA)
private val AccentEnd = Color(0xFF764BA2)
private val KeepGreen = Color(0xFF4ECDC4)
private val TrashRed = Color(0xFFFF6B6B)
private val AlbumBlue = Color(0xFF45B7D1)
private val SkipGray = Color(0xFF636E72)
private val BgTop = Color(0xFF0D0D0D)
private val BgBottom = Color(0xFF1A0A2E)

/**
 * Full-screen swipe-to-sort interface.
 *
 * Composes a top stats bar (streak, points, level), the [SwipeCardStack] in
 * the centre, bottom action buttons for tap-based sorting, a floating undo
 * button, and an animated points popup.
 *
 * @param viewModel [SwipeViewModel] driving all state.
 * @param modifier Optional [Modifier] for the root container.
 */
@Composable
fun SwipeScreen(
    viewModel: SwipeViewModel,
    modifier: Modifier = Modifier
) {
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val lastPoints by viewModel.lastPoints.collectAsStateWithLifecycle()

    var showPointsPopup by remember { mutableStateOf(false) }

    // Trigger popup when points change to a non-zero value.
    if (lastPoints > 0 && !showPointsPopup) {
        showPointsPopup = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(BgTop, BgBottom),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top stats bar ────────────────────────────────────────
            StatsBar(
                streak = stats?.currentStreak ?: 0,
                points = stats?.totalPoints ?: 0,
                level = stats?.level ?: 1,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Card stack ───────────────────────────────────────────
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentStart)
                }
            } else {
                SwipeCardStack(
                    photos = photos,
                    onCardSwiped = { photo, direction ->
                        viewModel.onCardSwiped(photo, direction)
                    },
                    onUndo = { viewModel.onUndo() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Bottom action buttons ────────────────────────────────
            ActionButtonRow(
                onTrash = {
                    photos.firstOrNull()?.let {
                        viewModel.onCardSwiped(it, SwipeDirection.LEFT)
                    }
                },
                onSkip = {
                    photos.firstOrNull()?.let {
                        viewModel.onCardSwiped(it, SwipeDirection.DOWN)
                    }
                },
                onAlbum = {
                    photos.firstOrNull()?.let {
                        viewModel.onCardSwiped(it, SwipeDirection.UP)
                    }
                },
                onKeep = {
                    photos.firstOrNull()?.let {
                        viewModel.onCardSwiped(it, SwipeDirection.RIGHT)
                    }
                },
                enabled = photos.isNotEmpty() && !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ── Floating undo button ─────────────────────────────────────
        FloatingActionButton(
            onClick = { viewModel.onUndo() },
            containerColor = Color.White.copy(alpha = 0.15f),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 96.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo last swipe"
            )
        }

        // ── Points popup ─────────────────────────────────────────────
        AnimatedVisibility(
            visible = showPointsPopup,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            PointsPopup(
                points = lastPoints,
                onDismiss = {
                    showPointsPopup = false
                    viewModel.clearLastPoints()
                }
            )
        }
    }
}

// ── Top stats bar ────────────────────────────────────────────────────

/**
 * Horizontal row displaying streak 🔥, total points, and level badge.
 */
@Composable
private fun StatsBar(
    streak: Int,
    points: Int,
    level: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Streak
        Text(
            text = "🔥 $streak",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        // Points
        Text(
            text = "⭐ $points pts",
            color = Color(0xFFFFD700),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        // Level badge
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(listOf(AccentStart, AccentEnd)),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Lv.$level",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── Bottom action buttons ────────────────────────────────────────────

/**
 * Row of four tap-based action buttons: Trash, Skip, Album, Keep.
 */
@Composable
private fun ActionButtonRow(
    onTrash: () -> Unit,
    onSkip: () -> Unit,
    onAlbum: () -> Unit,
    onKeep: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(emoji = "❌", label = "Trash", color = TrashRed, onClick = onTrash, enabled = enabled)
        ActionButton(emoji = "⏭️", label = "Skip", color = SkipGray, onClick = onSkip, enabled = enabled)
        ActionButton(emoji = "📁", label = "Album", color = AlbumBlue, onClick = onAlbum, enabled = enabled)
        ActionButton(emoji = "✅", label = "Keep", color = KeepGreen, onClick = onKeep, enabled = enabled)
    }
}

/**
 * Circular icon-button with an emoji and a tiny label beneath.
 */
@Composable
private fun ActionButton(
    emoji: String,
    label: String,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = color.copy(alpha = 0.15f),
                contentColor = color,
                disabledContainerColor = Color.Gray.copy(alpha = 0.08f),
                disabledContentColor = Color.Gray.copy(alpha = 0.3f)
            ),
            modifier = Modifier.size(56.dp)
        ) {
            Text(text = emoji, fontSize = 24.sp, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (enabled) color else Color.Gray.copy(alpha = 0.3f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
