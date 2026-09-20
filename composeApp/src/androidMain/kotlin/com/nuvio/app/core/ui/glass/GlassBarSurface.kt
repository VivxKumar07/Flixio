package com.nuvio.app.core.ui.glass

import androidx.compose.foundation.layout.Box
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

private val GlassSurfaceColor = Color(0xFF111525)
private val GlassAmethystTint = Color(0xFF6257A8)

@Composable
internal fun GlassBarSurface(hazeState: HazeState?, modifier: Modifier = Modifier, glowStrength: Float = 1f) {
    Box(
        modifier
            // The Haze effect owns the surface. Applying another RenderEffect over it
            // replaced the blurred backdrop on some devices, leaving only transparency.
            // Keeping blur at this layer guarantees that content from hazeSource is sampled
            // and diffused before the subtle tint and highlight are drawn.
            .then(if (hazeState != null) Modifier.barBackdrop(hazeState) else Modifier)
            .drawWithCache {
                val fill = GlassSurfaceColor.copy(alpha = if (hazeState != null) 0.28f else 0.82f)
                val edge = Brush.verticalGradient(
                    listOf(
                        Color(0xFFD8DEFF).copy(alpha = 0.38f),
                        GlassAmethystTint.copy(alpha = 0.12f),
                    ),
                )
                val width = 0.8.dp.toPx()
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

private fun Modifier.barBackdrop(hazeState: HazeState): Modifier = hazeEffect(state = hazeState) {
    blurRadius = 32.dp
    backgroundColor = GlassSurfaceColor
    tints = listOf(HazeTint(GlassAmethystTint.copy(alpha = 0.28f)))
    noiseFactor = 0f
}
