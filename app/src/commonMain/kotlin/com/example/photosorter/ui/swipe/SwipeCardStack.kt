package com.example.photosorter.ui.swipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.photosorter.data.model.PhotoItem
import kotlinx.coroutines.delay

/** Maximum number of visible cards in the stack. */
private const val MAX_VISIBLE_CARDS = 3

/**
 * Manages and renders a stack of [SwipeableCard]s.
 *
 * Up to [MAX_VISIBLE_CARDS] cards are drawn in a [Box]. The top card (index 0)
 * is interactive; cards behind it are progressively scaled down and shifted
 * downward to create a visual depth effect.
 *
 * When no photos remain an animated "All caught up!" empty state is shown.
 *
 * @param photos Ordered list of photos to sort (first = top of stack).
 * @param onCardSwiped Called after the top card exits with the photo and its direction.
 * @param onUndo Callback wired to the undo action (handled upstream).
 * @param modifier Optional [Modifier] for layout.
 */
@Composable
fun SwipeCardStack(
    photos: List<PhotoItem>,
    onCardSwiped: (PhotoItem, SwipeDirection) -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (photos.isEmpty()) {
            EmptyState()
        } else {
            val visibleCards = photos.take(MAX_VISIBLE_CARDS)

            // Draw back-to-front so the top card is rendered last (on top).
            visibleCards.asReversed().forEachIndexed { reversedIndex, photo ->
                val index = visibleCards.lastIndex - reversedIndex
                val scale = 1f - (index * 0.05f)       // 1.0, 0.95, 0.90
                val yOffset = (index * 8).dp            // 0, 8, 16

                key(photo.id) {
                    SwipeableCard(
                        photoItem = photo,
                        onSwiped = { direction -> onCardSwiped(photo, direction) },
                        enabled = index == 0, // only top card is interactive
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f) // portrait photo ratio
                            .offset(y = yOffset)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                }
            }
        }
    }
}

// ── Empty state ──────────────────────────────────────────────────────

/**
 * "All caught up 🎉" message with a subtle scale-in + fade entrance animation.
 */
@Composable
private fun EmptyState() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(150) // brief pause before animating in
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(500)
        ) + fadeIn(animationSpec = tween(500))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎉",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "All caught up!",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "You've sorted all your photos",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
