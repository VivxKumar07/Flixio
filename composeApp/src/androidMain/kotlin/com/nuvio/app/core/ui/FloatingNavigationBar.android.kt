package com.nuvio.app.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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

    val barHeight = if (compactSize) 42.dp else 46.dp
    val dockShape = RoundedCornerShape(23.dp)

    val scrollFraction = scrollState?.labelVisibility ?: 1f
    val navBarScale by animateFloatAsState(
        targetValue = 0.96f + (0.04f * scrollFraction),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "nav_bar_scale",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        contentAlignment = Alignment.BottomCenter,
    ) {
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
                        Modifier.hazeEffect(state = hazeState) {
                            blurRadius = 20.dp
                        }
                    } else {
                        Modifier
                    },
                )
                .shadow(
                    elevation = 8.dp,
                    shape = dockShape,
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black.copy(alpha = 0.5f),
                ),
            shape = dockShape,
            color = Color(0xDD0D0F14),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            tonalElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items.forEach { item ->
                    FloatingNavItem(
                        item = item,
                        barHeight = barHeight,
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
) {
    val isSelected = item.selected
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }

    fun handleItemClick() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        coroutineScope.launch {
            scaleAnim.animateTo(0.95f, tween(60, easing = FastOutSlowInEasing))
            scaleAnim.animateTo(1.0f, tween(120, easing = FastOutSlowInEasing))
        }
        item.onClick()
    }

    val itemShape = RoundedCornerShape(12.dp)
    val itemHeight = barHeight - 8.dp

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scaleAnim.value
            scaleY = scaleAnim.value
        },
    ) {
        if (isSelected) {
            Surface(
                modifier = Modifier
                    .size(width = 44.dp, height = itemHeight)
                    .clip(itemShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = ::handleItemClick,
                    ),
                shape = itemShape,
                color = Color.White.copy(alpha = 0.12f),
                contentColor = Color.White,
                border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.14f)),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White,
                                )
                            }
                            item.drawable != null -> {
                                Icon(
                                    painter = painterResource(item.drawable),
                                    contentDescription = item.label,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = itemHeight)
                    .clip(itemShape)
                    .background(Color.Transparent)
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
                                modifier = Modifier.size(20.dp),
                                tint = Color.White.copy(alpha = 0.44f),
                            )
                        }
                        item.drawable != null -> {
                            Icon(
                                painter = painterResource(item.drawable),
                                contentDescription = item.label,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White.copy(alpha = 0.44f),
                            )
                        }
                    }
                }
            }
        }
    }
}
