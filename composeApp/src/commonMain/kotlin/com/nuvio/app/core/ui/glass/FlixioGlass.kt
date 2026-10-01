package com.nuvio.app.core.ui.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.ManropeFontFamily
import com.nuvio.app.core.ui.NuvioTokens
import com.nuvio.app.core.ui.nuvio
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * Reusable, unified iOS-inspired Frosted Glass Material System for Flixio.
 *
 * Characteristics:
 * - Refined translucent fills with background diffusion where Haze is present.
 * - Subtle, crisp 0.75dp-1dp top-to-bottom specular borders.
 * - Theme-aware accent adaptation with zero neon/cyberpunk glows.
 * - Extremely lightweight execution with graceful degradation on Android 11.
 */
object FlixioGlassDefaults {
    val CardShape = RoundedCornerShape(18.dp)
    val PillShape = CircleShape
    val ButtonShape = RoundedCornerShape(14.dp)
    val SheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val ChipShape = RoundedCornerShape(12.dp)

    val CardPadding = 16.dp
    val ButtonPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    val ChipPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)

    @Composable
    fun glassFill(elevated: Boolean = false, hasHaze: Boolean = false): Color {
        val baseDark = if (elevated) Color(0xFF141722) else Color(0xFF0C0F17)
        return if (hasHaze) {
            baseDark.copy(alpha = if (elevated) 0.68f else 0.58f)
        } else {
            baseDark.copy(alpha = if (elevated) 0.90f else 0.82f)
        }
    }

    @Composable
    fun glassBorderBrush(accentTint: Boolean = false): Brush {
        val accent = MaterialTheme.nuvio.colors.accent
        return Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.18f),
                if (accentTint) accent.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.05f),
            ),
        )
    }

    fun hazeModifier(hazeState: HazeState?, blurRadius: Dp = 20.dp): Modifier {
        if (hazeState == null) return Modifier
        return Modifier.hazeEffect(state = hazeState) {
            this.blurRadius = blurRadius
            backgroundColor = Color(0xFF090B12)
            tints = listOf(
                HazeTint(Color(0xFF0C0F17).copy(alpha = 0.60f)),
            )
            noiseFactor = 0.03f
        }
    }
}

/**
 * Core translucent glass surface container.
 */
@Composable
fun FlixioGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = FlixioGlassDefaults.CardShape,
    elevated: Boolean = false,
    hazeState: HazeState? = null,
    borderBrush: Brush? = FlixioGlassDefaults.glassBorderBrush(),
    borderWidth: Dp = 0.85.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val fill = FlixioGlassDefaults.glassFill(elevated = elevated, hasHaze = hazeState != null)

    Box(
        modifier = modifier
            .clip(shape)
            .then(FlixioGlassDefaults.hazeModifier(hazeState))
            .background(fill, shape)
            .then(
                if (borderBrush != null) {
                    Modifier.border(BorderStroke(borderWidth, borderBrush), shape)
                } else Modifier,
            ),
        content = content,
    )
}

/**
 * Translucent iOS-inspired glass card for groupings and content.
 */
@Composable
fun FlixioGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = FlixioGlassDefaults.CardShape,
    elevated: Boolean = false,
    hazeState: HazeState? = null,
    padding: PaddingValues = PaddingValues(FlixioGlassDefaults.CardPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    FlixioGlassSurface(
        modifier = modifier,
        shape = shape,
        elevated = elevated,
        hazeState = hazeState,
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content,
        )
    }
}

/**
 * Compact frosted glass action button.
 */
@Composable
fun FlixioGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = FlixioGlassDefaults.ButtonShape,
    accentHighlighted: Boolean = false,
    hazeState: HazeState? = null,
    contentPadding: PaddingValues = FlixioGlassDefaults.ButtonPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val alpha by animateFloatAsState(
        targetValue = if (!enabled) 0.45f else if (isPressed) 0.72f else 1f,
        animationSpec = tween(120),
        label = "glass_btn_alpha",
    )
    val accent = MaterialTheme.nuvio.colors.accent
    val baseFill = if (accentHighlighted) {
        accent.copy(alpha = if (isPressed) 0.32f else 0.22f)
    } else {
        Color.White.copy(alpha = if (isPressed) 0.16f else 0.08f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .then(FlixioGlassDefaults.hazeModifier(hazeState, blurRadius = 14.dp))
            .background(baseFill, shape)
            .border(
                BorderStroke(
                    0.85.dp,
                    if (accentHighlighted) accent.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.18f),
                ),
                shape,
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/**
 * Sleek translucent chip for category filters and genre tags.
 */
@Composable
fun FlixioGlassChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
) {
    val accent = MaterialTheme.nuvio.colors.accent
    val fill = if (selected) accent.copy(alpha = 0.26f) else Color(0xFF131722).copy(alpha = 0.75f)
    val borderColor = if (selected) accent.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.12f)
    val textColor = if (selected) Color.White else Color(0xFFD4D8E2)

    Box(
        modifier = modifier
            .clip(FlixioGlassDefaults.ChipShape)
            .clickable(onClick = onClick)
            .then(FlixioGlassDefaults.hazeModifier(hazeState, blurRadius = 10.dp))
            .background(fill, FlixioGlassDefaults.ChipShape)
            .border(BorderStroke(0.85.dp, borderColor), FlixioGlassDefaults.ChipShape)
            .padding(FlixioGlassDefaults.ChipPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontFamily = ManropeFontFamily,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor,
        )
    }
}
