package com.nuvio.app.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

internal actual val floatingNavigationGlowSupported: Boolean
    get() = true

internal fun visualNavIndex(logicalIndex: Int, count: Int, isRtl: Boolean): Int =
    if (logicalIndex in 0 until count && isRtl) count - 1 - logicalIndex else logicalIndex

internal fun logicalNavIndex(visualIndex: Int, count: Int, isRtl: Boolean): Int =
    if (visualIndex in 0 until count && isRtl) count - 1 - visualIndex else visualIndex

@Composable
internal actual fun FloatingNavigationBar(
    items: List<FloatingNavigationItem>,
    modifier: Modifier,
    scrollState: NuvioNavBarScrollState?,
    hazeState: HazeState?,
    contentPadding: PaddingValues,
    compactSize: Boolean,
    glowEnabled: Boolean,
) {
    if (items.isEmpty()) return

    val barHeight = if (compactSize) 48.dp else 54.dp
    val pillShape = RoundedCornerShape(50)

    val scrollFraction = scrollState?.labelVisibility ?: 1f
    val navBarScale by animateFloatAsState(
        targetValue = 0.90f + (0.15f * scrollFraction),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_bar_zoom_scale",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Floating pill navigation bar matching the target design (Image 5)
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .height(barHeight)
                .graphicsLayer {
                    scaleX = navBarScale
                    scaleY = navBarScale
                }
                .then(
                    if (hazeState != null) {
                        Modifier.hazeEffect(state = hazeState)
                    } else {
                        Modifier
                    }
                )
                .shadow(elevation = 16.dp, shape = pillShape, ambientColor = Color.Black, spotColor = Color.Black),
            shape = pillShape,
            color = Color(0xEE1E231F),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
            tonalElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items.forEach { item ->
                    FloatingNavItem(
                        item = item,
                        barHeight = barHeight,
                        pillShape = pillShape,
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    item: FloatingNavigationItem,
    barHeight: Dp,
    pillShape: RoundedCornerShape,
) {
    val isSelected = item.selected
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }
    val rotationAnim = remember { Animatable(0f) }

    fun handleItemClick() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        coroutineScope.launch {
            launch {
                scaleAnim.animateTo(0.93f, tween(75, easing = FastOutSlowInEasing))
                scaleAnim.animateTo(1.05f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow))
                scaleAnim.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium))
            }
            launch {
                rotationAnim.animateTo(-3.5f, tween(60, easing = FastOutSlowInEasing))
                rotationAnim.animateTo(3.0f, tween(75, easing = FastOutSlowInEasing))
                rotationAnim.animateTo(-1.0f, tween(60, easing = FastOutSlowInEasing))
                rotationAnim.animateTo(0f, spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium))
            }
        }
        item.onClick()
    }

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scaleAnim.value
            scaleY = scaleAnim.value
            rotationZ = rotationAnim.value
        },
    ) {
        if (isSelected) {
            // Active item: Elongated white pill with black icon + black label text
            Surface(
                modifier = Modifier
                    .height(barHeight - 10.dp)
                    .clip(pillShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = ::handleItemClick,
                    ),
                shape = pillShape,
                color = Color.White,
                contentColor = Color.Black,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (item.content != null) {
                        item.content(::handleItemClick)
                    } else {
                        when {
                            item.icon != null -> {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Black,
                                )
                            }
                            item.drawable != null -> {
                                Icon(
                                    painter = painterResource(item.drawable),
                                    contentDescription = item.label,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Black,
                                )
                            }
                        }
                    }
                    Text(
                        text = item.label,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                    )
                }
            }
        } else {
            // Inactive item: Circular button with icon only (no label)
            Box(
                modifier = Modifier
                    .size(barHeight - 10.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.07f))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = ::handleItemClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (item.content != null) {
                    item.content(::handleItemClick)
                } else {
                    when {
                        item.icon != null -> {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(19.dp),
                                tint = Color.White.copy(alpha = 0.85f),
                            )
                        }
                        item.drawable != null -> {
                            Icon(
                                painter = painterResource(item.drawable),
                                contentDescription = item.label,
                                modifier = Modifier.size(19.dp),
                                tint = Color.White.copy(alpha = 0.85f),
                            )
                        }
                    }
                }
            }
        }
    }
}
