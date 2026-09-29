package com.nuvio.app.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.features.settings.ThemeSettingsRepository
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.flixio_background
import org.jetbrains.compose.resources.painterResource

private fun visualTierIsLowEnd(): Boolean {
    val cores = Runtime.getRuntime().availableProcessors()
    val maxHeapMb = Runtime.getRuntime().maxMemory() / (1024L * 1024L)
    return cores < 6 || maxHeapMb < 256
}

/**
 * Flixio ambient wallpaper: uses the Flixio background image with a theme-coloured
 * tint overlay so the wallpaper adapts to the selected accent colour in settings.
 * The background image provides the flowing abstract waves while the tint ensures
 * visual coherence with the active theme.
 */
@Composable
fun FlixioAmbientWallpaper(
    modifier: Modifier = Modifier,
    accent: Color,
) {
    val enabled by ThemeSettingsRepository.ambientWallpaperEnabled.collectAsStateWithLifecycle()
    if (!enabled) return

    val grayscaleMatrix = remember {
        androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(0f) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020404)),
    ) {
        // Desaturate the wave art to luminance grayscale, so it takes 100% of the active theme color
        Image(
            painter = painterResource(Res.drawable.flixio_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.85f },
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(grayscaleMatrix),
        )
        // Tint the luminous waves with the exact active theme color
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        color = accent.copy(alpha = 0.88f),
                        blendMode = BlendMode.Modulate,
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.25f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.65f),
                            ),
                        ),
                    )
                },
        )
    }
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
