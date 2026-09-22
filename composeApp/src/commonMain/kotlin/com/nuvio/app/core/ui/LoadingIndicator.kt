package com.nuvio.app.core.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Windows 11 Progressive / Rushing Loading Animation.
 *
 * Mathematical Structure:
 * - 5 small circular dots travelling around an implied circular orbit.
 * - Non-linear progressive velocity profile:
 *     1. Slow, separated movement at the start of the orbit.
 *     2. Smooth, rapid acceleration into high velocity ("rushing motion").
 *     3. Dots cluster tightly together during the rushing portion as leading dots decelerate.
 *     4. Progressive separation as dots decelerate and drift back into the slow section.
 * - Colors: Primarily pure crisp white with an extremely subtle light-blue tint on the lead dot.
 * - Zero allocations per frame with pure Compose Canvas [drawWithCache].
 */
@Composable
fun FlixioLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    trackColor: Color = Color.Transparent,
    size: Dp = 36.dp,
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
                    val minDim = this.size.minDimension
                    val baseDotRadius = (minDim * 0.058f).coerceIn(1.8f, 3.2f)
                    val orbitRadius = (minDim / 2f) - baseDotRadius - 2f
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)

                    val dotCount = 5
                    val dotPhaseOffset = 0.038f

                    onDrawBehind {
                        val progress = animationProgress.value

                        for (i in 0 until dotCount) {
                            val dotProgress = (progress - (i * dotPhaseOffset) + 1f) % 1f
                            val angleDeg = win11ProgressiveAngle(dotProgress) - 90f
                            val angleRad = angleDeg * (PI / 180.0)

                            val x = center.x + (orbitRadius * cos(angleRad)).toFloat()
                            val y = center.y + (orbitRadius * sin(angleRad)).toFloat()

                            val dotSize = baseDotRadius * (1f - (i * 0.045f))
                            val dotAlpha = (1f - (i * 0.07f)).coerceIn(0.65f, 1f)

                            val dotColor = if (i == 0) {
                                Color(0xFFE8F2FF).copy(alpha = dotAlpha)
                            } else {
                                color.copy(alpha = dotAlpha)
                            }

                            drawCircle(
                                color = dotColor,
                                radius = dotSize,
                                center = Offset(x, y),
                            )
                        }
                    }
                },
        )
    }
}

/**
 * Calculates progressive angle in degrees (0..360) for normalized time [t] in [0, 1).
 * Features:
 * - 0.00..0.22: slow drift / separated (0 -> 45 deg)
 * - 0.22..0.68: rapid rushing acceleration (45 -> 315 deg)
 * - 0.68..1.00: smooth deceleration / clustering & separation (315 -> 360 deg)
 */
private fun win11ProgressiveAngle(t: Float): Float {
    val progress = (t % 1f + 1f) % 1f
    return when {
        progress < 0.22f -> {
            val p = progress / 0.22f
            val eased = p * p * (3f - 2f * p)
            eased * 45f
        }
        progress < 0.68f -> {
            val p = (progress - 0.22f) / 0.46f
            val eased = p * p * (3f - 2f * p)
            45f + (eased * 270f)
        }
        else -> {
            val p = (progress - 0.68f) / 0.32f
            val eased = p * p * (3f - 2f * p)
            315f + (eased * 45f)
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
    size: Dp = 36.dp,
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
        return remember { mutableFloatStateOf(0.4f) }
    }

    val transition = rememberInfiniteTransition(label = "win11_progressive_loading")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "win11_progress",
    )
}
