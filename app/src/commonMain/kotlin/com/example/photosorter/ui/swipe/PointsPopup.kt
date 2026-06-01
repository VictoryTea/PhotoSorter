package com.example.photosorter.ui.swipe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Animated popup that shows points earned from a swipe.
 *
 * Displays a "+N" label in gold that floats upward and fades out over ~1 second,
 * then calls [onDismiss] when the animation completes.
 *
 * @param points The number of points earned (displayed as "+N").
 * @param onDismiss Called when the float-up / fade-out animation finishes.
 * @param modifier Optional [Modifier] applied to the root element.
 */
@Composable
fun PointsPopup(
    points: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Animate upward movement and fade-out concurrently
        launch {
            offsetY.animateTo(
                targetValue = -120f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1000)
        )
        onDismiss()
    }

    Text(
        text = "+$points",
        color = Color(0xFFFFD700).copy(alpha = alpha.value),
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.offset { IntOffset(0, offsetY.value.toInt()) }
    )
}
