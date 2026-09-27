package com.nuvio.app.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.features.settings.ThemeSettingsRepository
import kotlin.math.abs

private fun visualTierIsLowEnd(): Boolean {
    val cores = Runtime.getRuntime().availableProcessors()
    val maxHeapMb = Runtime.getRuntime().maxMemory() / (1024L * 1024L)
    return cores < 6 || maxHeapMb < 256
}

/**
 * Flixio ambient wallpaper: a motion-graphic style backdrop in the visual language of
 * the Flixio logo — deep near-black base with soft violet/accent light blooms that
 * drift extremely slowly on capable devices (static on low-end). Theme aware: the
 * accent of the selected Flixio theme drives the blooms. Rendered via drawBehind —
 * no bitmap, no blur, near-zero cost.
 */
@Composable
fun FlixioAmbientWallpaper(
    modifier: Modifier = Modifier,
    accent: Color,
) {
    val enabled by ThemeSettingsRepository.ambientWallpaperEnabled.collectAsStateWithLifecycle()
    if (!enabled) return

    val transition = rememberInfiniteTransition(label = "ambient_wallpaper")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ambient_wallpaper_phase",
    )
    val drift = if (visualTierIsLowEnd()) 0f else 1f

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = drift * phase * 12f
                translationY = drift * (phase - 0.5f) * -8f
            }
            .drawBehind {
                val w = size.width
                val h = size.height
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.10f),
                            accent.copy(alpha = 0.03f),
                            Color.Transparent,
                        ),
                        center = Offset(w * 0.85f, h * 0.12f),
                        radius = w * 1.35f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7C4DFF).copy(alpha = 0.14f),
                            Color(0xFF7C4DFF).copy(alpha = 0.05f),
                            Color.Transparent,
                        ),
                        center = Offset(w * (0.12f + 0.04f * abs(phase)), h * 0.22f),
                        radius = w * 1.05f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        center = Offset(w * 0.82f, h * 0.88f),
                        radius = w * 1.15f,
                    ),
                )
            },
    )
}

/**
 * Depth shadow for content placed "on top of" the ambient wallpaper.
 */
fun Modifier.flixioDepthShadow(
    elevation: Dp = 10.dp,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
): Modifier = this
    .shadow(elevation = elevation, shape = shape, ambientColor = Color.Black, spotColor = Color.Black)
    .background(Color.White.copy(alpha = 0.02f))

/**
 * Text depth shadow used for subtitles floating over the wallpaper.
 */
fun subtitleDepthShadow(blurRadius: Float = 10f): Shadow =
    Shadow(color = Color.Black.copy(alpha = 0.85f), offset = Offset(0f, 2f), blurRadius = blurRadius)
