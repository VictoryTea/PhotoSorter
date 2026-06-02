package com.example.photosorter.ui.swipe

import android.text.format.Formatter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.photosorter.data.model.PhotoItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/** Swipe-threshold as a fraction of the relevant screen dimension. */
private const val SWIPE_THRESHOLD_FRACTION = 0.30f

/** Multiplier to push the card off-screen during the exit animation. */
private const val EXIT_DISTANCE_MULTIPLIER = 1.5f

/** Maximum card rotation in degrees at full horizontal displacement. */
private const val MAX_ROTATION_DEG = 15f

/** Stamp fade-in threshold as a fraction of drag progress. */
private const val STAMP_VISIBLE_THRESHOLD = 0.15f

// ── Theme colours ────────────────────────────────────────────────────
private val KeepGreen = Color(0xFF4ECDC4)
private val TrashRed = Color(0xFFFF6B6B)
private val AlbumBlue = Color(0xFF45B7D1)
private val SkipGray = Color(0xFF636E72)

/**
 * A Tinder-style draggable photo card that supports 4-directional swiping.
 *
 * The card displays the photo via Coil's [AsyncImage], overlays directional
 * "stamp" labels that fade in during the drag, and animates either a spring-back
 * or an exit when the gesture ends.
 *
 * @param photoItem The photo to display.
 * @param onSwiped Called with the resolved [SwipeDirection] after the exit animation.
 * @param modifier Optional [Modifier] for layout.
 * @param enabled Whether drag gestures are processed (disable for stacked cards).
 */
@Composable
fun SwipeableCard(
    photoItem: PhotoItem,
    onSwiped: (SwipeDirection) -> Unit,
    onInfoClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Card pixel dimensions – updated on first layout.
    var cardWidth by remember { mutableFloatStateOf(1f) }
    var cardHeight by remember { mutableFloatStateOf(1f) }

    // Animated drag offset.
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    // Normalised progress in each axis (−1 … +1).
    val progressX = if (cardWidth > 0f) offsetX.value / cardWidth else 0f
    val progressY = if (cardHeight > 0f) offsetY.value / cardHeight else 0f

    // ── Stamp alpha (fades in once past threshold) ──────────────────
    val keepAlpha by animateFloatAsState(
        targetValue = if (progressX > STAMP_VISIBLE_THRESHOLD) (progressX - STAMP_VISIBLE_THRESHOLD) / (1f - STAMP_VISIBLE_THRESHOLD) else 0f,
        label = "keepAlpha"
    )
    val trashAlpha by animateFloatAsState(
        targetValue = if (progressX < -STAMP_VISIBLE_THRESHOLD) (abs(progressX) - STAMP_VISIBLE_THRESHOLD) / (1f - STAMP_VISIBLE_THRESHOLD) else 0f,
        label = "trashAlpha"
    )
    val albumAlpha by animateFloatAsState(
        targetValue = if (progressY < -STAMP_VISIBLE_THRESHOLD) (abs(progressY) - STAMP_VISIBLE_THRESHOLD) / (1f - STAMP_VISIBLE_THRESHOLD) else 0f,
        label = "albumAlpha"
    )
    val skipAlpha by animateFloatAsState(
        targetValue = if (progressY > STAMP_VISIBLE_THRESHOLD) (progressY - STAMP_VISIBLE_THRESHOLD) / (1f - STAMP_VISIBLE_THRESHOLD) else 0f,
        label = "skipAlpha"
    )

    Box(
        modifier = modifier
            .onSizeChanged { size ->
                cardWidth = size.width.toFloat()
                cardHeight = size.height.toFloat()
            }
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                rotationZ = (offsetX.value / cardWidth.coerceAtLeast(1f)) * MAX_ROTATION_DEG
                alpha = 1f - (maxOf(abs(progressX), abs(progressY)) * 0.3f)
            }
            .then(
                if (enabled) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                    offsetY.snapTo(offsetY.value + dragAmount.y)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    val totalX = offsetX.value
                                    val totalY = offsetY.value
                                    val absX = abs(totalX)
                                    val absY = abs(totalY)

                                    val direction: SwipeDirection? = when {
                                        absX > absY && absX > cardWidth * SWIPE_THRESHOLD_FRACTION ->
                                            if (totalX > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
                                        absY > absX && absY > cardHeight * SWIPE_THRESHOLD_FRACTION ->
                                            if (totalY > 0) SwipeDirection.DOWN else SwipeDirection.UP
                                        else -> null
                                    }

                                    if (direction != null) {
                                        // Exit animation – fling off screen.
                                        val targetX = when (direction) {
                                            SwipeDirection.RIGHT -> cardWidth * EXIT_DISTANCE_MULTIPLIER
                                            SwipeDirection.LEFT -> -cardWidth * EXIT_DISTANCE_MULTIPLIER
                                            else -> 0f
                                        }
                                        val targetY = when (direction) {
                                            SwipeDirection.DOWN -> cardHeight * EXIT_DISTANCE_MULTIPLIER
                                            SwipeDirection.UP -> -cardHeight * EXIT_DISTANCE_MULTIPLIER
                                            else -> 0f
                                        }
                                        launch { offsetX.animateTo(targetX, tween(300)) }
                                        offsetY.animateTo(targetY, tween(300))
                                        onSwiped(direction)
                                    } else {
                                        // Spring back to centre.
                                        launch {
                                            offsetX.animateTo(
                                                0f,
                                                spring(dampingRatio = 0.7f, stiffness = 300f)
                                            )
                                        }
                                        offsetY.animateTo(
                                            0f,
                                            spring(dampingRatio = 0.7f, stiffness = 300f)
                                        )
                                    }
                                }
                            }
                        )
                    }
                } else Modifier
            )
            .clip(RoundedCornerShape(16.dp))
    ) {
        // ── Photo ────────────────────────────────────────────────────
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(photoItem.uri)
                .crossfade(true)
                .build(),
            contentDescription = photoItem.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // ── Stamp overlays ───────────────────────────────────────────
        // KEEP – top-left (opposite of right-drag)
        StampLabel(
            text = "KEEP",
            color = KeepGreen,
            rotation = -15f,
            alpha = keepAlpha,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
        )
        // TRASH – top-right (opposite of left-drag)
        StampLabel(
            text = "TRASH",
            color = TrashRed,
            rotation = 15f,
            alpha = trashAlpha,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        )
        // ALBUM – bottom-center (opposite of up-drag)
        StampLabel(
            text = "ALBUM",
            color = AlbumBlue,
            rotation = -15f,
            alpha = albumAlpha,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
        // SKIP – top-center (opposite of down-drag)
        StampLabel(
            text = "SKIP",
            color = SkipGray,
            rotation = 15f,
            alpha = skipAlpha,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        )

        // ── Metadata overlay ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = photoItem.displayName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onInfoClick() }
                )
            }
            Text(
                text = buildString {
                    val date = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        .format(Date(photoItem.dateAdded * 1000))
                    append(date)
                    append("  •  ")
                    append(Formatter.formatShortFileSize(context, photoItem.size))
                },
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

// ── Reusable stamp label ─────────────────────────────────────────────

/**
 * Bold, bordered stamp text that appears during a drag gesture.
 *
 * @param text The stamp label (e.g. "KEEP").
 * @param color Border + text colour.
 * @param rotation Slight tilt in degrees for a "rubber stamp" look.
 * @param alpha Current opacity driven by drag progress.
 */
@Composable
private fun StampLabel(
    text: String,
    color: Color,
    rotation: Float,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    if (alpha > 0f) {
        Text(
            text = text,
            color = color.copy(alpha = alpha),
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = modifier
                .graphicsLayer { rotationZ = rotation }
                .border(
                    width = 3.dp,
                    color = color.copy(alpha = alpha),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
