package com.nuvio.app.core.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Windows 11 dynamic donut / arc loading animation:
 * - Dynamic white rotating arc (half-circle sweep) with rounded stroke caps.
 * - Dynamic expansion & contraction as it accelerates and decelerates along its circular path.
 * - NO full path in grey being shown (zero background track).
 * - Stable 60 FPS Compose Canvas rendering.
 */
@Composable
fun FlixioLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    trackColor: Color = Color.Transparent,
    size: Dp = NuvioTokens.Space.s40,
    strokeWidth: Dp? = null,
    active: Boolean = LocalScreenActive.current,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        if (!active) return@Box

        val transition = rememberInfiniteTransition(label = "win11_donut_transition")

        // Continuous full rotation
        val rotation by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "win11_donut_rotation",
        )

        // Dynamic arc sweep breathing smoothly between ~50° and ~250°
        val sweepAngle by transition.animateFloat(
            initialValue = 50f,
            targetValue = 250f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 750,
                    easing = CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f),
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "win11_donut_sweep",
        )

        Canvas(modifier = Modifier.size(size)) {
            val strokePx = (strokeWidth?.toPx() ?: (this.size.minDimension * 0.14f)).coerceIn(3.5.dp.toPx(), 6.5.dp.toPx())
            val diameter = this.size.minDimension - strokePx
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset((this.size.width - diameter) / 2f, (this.size.height - diameter) / 2f)

            // Dynamic white arc rotating smoothly with zero grey background track
            drawArc(
                color = color,
                startAngle = rotation,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(
                    width = strokePx,
                    cap = StrokeCap.Round,
                ),
            )
        }
    }
}

/**
 * Backward-compatible delegating wrapper for [FlixioLoadingIndicator].
 */
@Composable
fun NuvioLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
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

/**
 * Windows 11 / legacy compatibility progress loader delegating to [FlixioLoadingIndicator].
 */
@Composable
fun WindowsRingLoader(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    color: Color = Color.White,
    active: Boolean = LocalScreenActive.current,
) {
    FlixioLoadingIndicator(
        modifier = modifier,
        size = size,
        color = color,
        active = active,
    )
}

