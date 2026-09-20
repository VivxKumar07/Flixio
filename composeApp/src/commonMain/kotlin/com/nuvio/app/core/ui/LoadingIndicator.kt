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
 * Windows 11 Boot / Fluent Progress Ring loading animation.
 *
 * Characteristics:
 * - Minimal, elegant thin circular arc travelling around a centered ring.
 * - Non-linear acceleration curve: the leading edge expands the arc as it sweeps forward,
 *   followed by the trailing edge accelerating and catching up to contract the arc.
 * - Perfectly seamless, continuous periodic motion without stutters or sudden jumps.
 * - Pure mathematical Canvas drawing with [drawWithCache] for zero garbage-collection overhead.
 */
@Composable
fun FlixioLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    trackColor: Color = Color.White.copy(alpha = 0.08f),
    size: Dp = 32.dp,
    strokeWidth: Dp? = null,
    active: Boolean = LocalScreenActive.current,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        val animationProgress = rememberFlixioLoadingAnimation(active)

        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val strokePx = strokeWidth?.toPx()
                        ?: (this.size.minDimension * 0.065f).coerceIn(1.75.dp.toPx(), 2.5.dp.toPx())
                    val halfStroke = strokePx / 2f
                    val arcSize = Size(
                        width = this.size.width - strokePx,
                        height = this.size.height - strokePx,
                    )
                    val arcTopLeft = Offset(halfStroke, halfStroke)
                    val ringRadius = (this.size.minDimension - strokePx) / 2f
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)

                    val strokeStyle = Stroke(
                        width = strokePx,
                        cap = StrokeCap.Round,
                    )
                    val trackStyle = Stroke(
                        width = strokePx * 0.75f,
                    )

                    onDrawBehind {
                        // Subtle track ring
                        if (trackColor != Color.Transparent && trackColor.alpha > 0f) {
                            drawCircle(
                                color = trackColor,
                                radius = ringRadius,
                                center = center,
                                style = trackStyle,
                            )
                        }

                        val progress = animationProgress.value
                        val baseRotation = progress * 720f

                        val (headOffset, tailOffset) = if (progress < 0.5f) {
                            val t = progress / 0.5f
                            val head = HeadEasing.transform(t) * 260f
                            val tail = TailEasing.transform(t) * 100f
                            head to tail
                        } else {
                            val t = (progress - 0.5f) / 0.5f
                            val head = 260f + TailEasing.transform(t) * 100f
                            val tail = 100f + HeadEasing.transform(t) * 260f
                            head to tail
                        }

                        val startAngle = (baseRotation + tailOffset - 90f) % 360f
                        val sweepAngle = (headOffset - tailOffset + 12f).coerceIn(12f, 172f)

                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcSize,
                            style = strokeStyle,
                        )
                    }
                },
        )
    }
}

private val HeadEasing = CubicBezierEasing(0.2f, 0f, 0.1f, 1f)
private val TailEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

/**
 * Backward-compatible delegating wrapper for [FlixioLoadingIndicator].
 */
@Composable
fun NuvioLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    size: Dp = 32.dp,
    active: Boolean = LocalScreenActive.current,
) {
    FlixioLoadingIndicator(
        modifier = modifier,
        color = color,
        size = size,
        active = active,
    )
}

@Composable
internal fun rememberFlixioLoadingAnimation(active: Boolean): State<Float> {
    if (!active) {
        return remember { mutableFloatStateOf(0.5f) }
    }

    val transition = rememberInfiniteTransition(label = "win11_progress_ring")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "win11_progress",
    )
}
