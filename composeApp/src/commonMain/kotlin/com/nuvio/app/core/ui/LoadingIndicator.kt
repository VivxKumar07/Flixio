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
import androidx.compose.material3.MaterialTheme
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

private const val DOT_COUNT = 5
private const val CYCLE_DURATION_MS = 2400
private const val DOT_STAGGER_FRACTION = 0.042f
private const val ACCELERATION_FACTOR = 0.132f

/**
 * Windows 11 inspired loading animation featuring orbiting glossy dots.
 *
 * Physics:
 * - 5 small glossy dots orbit along a circular track.
 * - Non-linear velocity curve creates acceleration (rushing around the arc where dots separate)
 *   and deceleration (where dots cluster tightly together).
 * - Seamless 60 FPS loop optimized with [drawWithCache] and zero runtime allocations.
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
        val progressState = rememberOrbitalProgress(active)

        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val dotRadius = (this.size.minDimension * 0.065f).coerceIn(1.8f.dp.toPx(), 4.2f.dp.toPx())
                    val orbitRadius = (this.size.minDimension - dotRadius * 3f) / 2f
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    val twoPi = (2.0 * PI).toFloat()
                    val halfPi = (PI / 2.0).toFloat()

                    val baseDotColor = if (color == Color.White) Color(0xFFF4F7FF) else color
                    val highlightColor = Color.White.copy(alpha = 0.75f)

                    onDrawBehind {
                        val t = progressState.value

                        for (i in 0 until DOT_COUNT) {
                            val dotProgress = (t - i * DOT_STAGGER_FRACTION + 1.0f) % 1.0f
                            val curvedProgress = dotProgress - (ACCELERATION_FACTOR / twoPi) * sin(twoPi * dotProgress)
                            val angleRad = curvedProgress * twoPi - halfPi

                            val dotX = center.x + orbitRadius * cos(angleRad)
                            val dotY = center.y + orbitRadius * sin(angleRad)
                            val dotCenter = Offset(dotX, dotY)

                            drawCircle(
                                color = baseDotColor,
                                radius = dotRadius,
                                center = dotCenter,
                            )

                            drawCircle(
                                color = highlightColor,
                                radius = dotRadius * 0.42f,
                                center = Offset(dotX - dotRadius * 0.28f, dotY - dotRadius * 0.28f),
                            )
                        }
                    }
                },
        )
    }
}

@Composable
private fun rememberOrbitalProgress(active: Boolean): State<Float> {
    if (!active) {
        return remember { mutableFloatStateOf(0f) }
    }
    val transition = rememberInfiniteTransition(label = "flixio_orbital_transition")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = CYCLE_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "flixio_orbital_progress",
    )
}

/**
 * Windows 11 / legacy compatibility progress loader delegating to [FlixioLoadingIndicator].
 */
@Composable
fun WindowsRingLoader(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    color: Color = MaterialTheme.nuvio.colors.accent,
    active: Boolean = LocalScreenActive.current,
) {
    FlixioLoadingIndicator(
        modifier = modifier,
        size = size,
        color = color,
        active = active,
    )
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

@Composable
fun rememberWindowsRingLoaderProgress(active: Boolean = true): State<Float> {
    return rememberOrbitalProgress(active)
}

@Composable
internal fun rememberLoadingIndicatorFrame(active: Boolean = true): State<Float> {
    return rememberOrbitalProgress(active)
}
