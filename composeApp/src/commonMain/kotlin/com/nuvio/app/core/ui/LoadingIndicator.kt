package com.nuvio.app.core.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Windows 11 Fluent 2 inspired rotating "donut" loading animation (ProgressRing).
 *
 * Features:
 * - Smooth rotating donut ring.
 * - Subtle background track ring.
 * - Fluid expanding and contracting arc with rounded caps ([StrokeCap.Round]) using
 *   cubic-bezier easing that continuously revolves around the circle.
 * - Optimized with [drawWithCache] to eliminate object allocations during frame renders.
 */
@Composable
fun FlixioLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.nuvio.colors.accent,
    trackColor: Color = color.copy(alpha = 0.15f),
    size: Dp = NuvioTokens.Space.s40,
    strokeWidth: Dp? = null,
    active: Boolean = LocalScreenActive.current,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        val animationState = rememberFlixioLoadingAnimation(active)

        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val actualStrokeWidth = strokeWidth?.toPx()
                        ?: (this.size.minDimension * 0.1f).coerceIn(2.dp.toPx(), 4.5.dp.toPx())
                    val diameter = (this.size.minDimension - actualStrokeWidth).coerceAtLeast(1f)
                    val arcSize = Size(diameter, diameter)
                    val topLeft = Offset(
                        x = (this.size.width - diameter) / 2f,
                        y = (this.size.height - diameter) / 2f,
                    )
                    val stroke = Stroke(width = actualStrokeWidth, cap = StrokeCap.Round)

                    onDrawBehind {
                        // Draw background track ring
                        if (trackColor != Color.Transparent && trackColor.alpha > 0f) {
                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = stroke,
                            )
                        }

                        // Draw animated rotating arc (Windows 11 donut arc)
                        val rotation = animationState.rotation.value
                        val headOffset = animationState.headOffset.value
                        val tailOffset = animationState.tailOffset.value
                        val startAngle = (rotation + tailOffset) % 360f
                        val rawSweep = headOffset - tailOffset
                        val sweepAngle = if (rawSweep < 0f) rawSweep + 360f else rawSweep
                        val clampedSweep = sweepAngle.coerceIn(20f, 280f)

                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = clampedSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = stroke,
                        )
                    }
                },
        )
    }
}

/**
 * Backward-compatible delegating wrapper for [FlixioLoadingIndicator].
 */
@Composable
fun NuvioLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.nuvio.colors.accent,
    size: Dp = NuvioTokens.Space.s40,
    active: Boolean = LocalScreenActive.current,
) {
    FlixioLoadingIndicator(
        modifier = modifier,
        color = color,
        size = size,
        active = active,
    )
}

internal class FlixioLoadingAnimationState(
    val rotation: State<Float>,
    val headOffset: State<Float>,
    val tailOffset: State<Float>,
)

@Composable
internal fun rememberFlixioLoadingAnimation(active: Boolean): FlixioLoadingAnimationState {
    if (!active) {
        val staticRotation = remember { mutableFloatStateOf(0f) }
        val staticHead = remember { mutableFloatStateOf(100f) }
        val staticTail = remember { mutableFloatStateOf(0f) }
        return remember {
            FlixioLoadingAnimationState(
                rotation = staticRotation,
                headOffset = staticHead,
                tailOffset = staticTail,
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "flixio_donut_loader")
    val rotation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "flixio_donut_rotation",
    )
    val headOffset = transition.animateFloat(
        initialValue = 20f,
        targetValue = 360f + 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1600,
                easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f),
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "flixio_donut_head",
    )
    val tailOffset = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1600,
                delayMillis = 350,
                easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f),
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "flixio_donut_tail",
    )

    return remember(transition) {
        FlixioLoadingAnimationState(rotation, headOffset, tailOffset)
    }
}
