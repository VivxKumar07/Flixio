package com.nuvio.app.core.ui.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.nuvio
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

private val GlassSurfaceBase = Color(0xFF0C0F17)

@Composable
internal fun GlassBarSurface(
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
    glowStrength: Float = 1f,
) {
    val themeAccent = MaterialTheme.nuvio.colors.accent

    Box(
        modifier
            .then(if (hazeState != null) Modifier.barBackdrop(hazeState, themeAccent) else Modifier)
            .drawWithCache {
                val fill = if (hazeState != null) {
                    GlassSurfaceBase.copy(alpha = 0.65f)
                } else {
                    GlassSurfaceBase.copy(alpha = 0.90f)
                }
                val edge = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.22f),
                        themeAccent.copy(alpha = 0.12f),
                    ),
                )
                val width = 0.85.dp.toPx()
                onDrawBehind {
                    drawRect(fill)
                    drawRoundRect(
                        brush = edge,
                        topLeft = Offset(width / 2, width / 2),
                        size = Size(size.width - width, size.height - width),
                        cornerRadius = CornerRadius((size.height - width) / 2),
                        style = Stroke(width),
                        alpha = glowStrength,
                    )
                }
            },
    )
}

private fun Modifier.barBackdrop(hazeState: HazeState, accentColor: Color): Modifier = hazeEffect(state = hazeState) {
    blurRadius = 24.dp
    backgroundColor = Color(0xFF080A10)
    tints = listOf(
        HazeTint(Color(0xFF0B0E17).copy(alpha = 0.62f)),
        HazeTint(accentColor.copy(alpha = 0.10f)),
    )
    noiseFactor = 0.02f
}

