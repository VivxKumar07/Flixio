package com.nuvio.app.features.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.kmpalette.extensions.painter.rememberPainterDominantColorState
import com.nuvio.app.core.format.formatReleaseDateForDisplay
import com.nuvio.app.core.ui.ManropeFontFamily
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.core.ui.heroStretchHeight
import com.nuvio.app.core.ui.ScreenActivityEffect
import com.nuvio.app.core.ui.heroStretchZoom
import com.nuvio.app.features.home.HeroCardRibbon
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.home.heroCardRibbon
import com.nuvio.app.features.watchprogress.CurrentDateProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

private const val HERO_BACKGROUND_PARALLAX = 0.055f
private const val HERO_BACKGROUND_SCALE = 1.14f
private const val HERO_CONTENT_PARALLAX = 0.18f
private const val HERO_SCROLL_PARALLAX = 0.3f
private const val HERO_SCROLL_DOWN_SCALE_MULTIPLIER = 0.00006f
private const val HERO_SCROLL_UP_SCALE_MULTIPLIER = 0.0005f
// Keep the scroll parallax subtle so the hero card can never visibly bleed
// into the catalog rows below while scrolling vertically.
private const val HERO_SCROLL_MAX_SCALE = 1.06f
private const val HERO_SWIPE_THRESHOLD_FRACTION = 0.16f
private const val HERO_SWIPE_VELOCITY_THRESHOLD = 300f
private const val HERO_AUTO_SCROLL_INTERVAL_MS = 8_000L
private const val MOBILE_HERO_VIEWPORT_RATIO = 0.58f
private const val MOBILE_HERO_MIN_HEIGHT_DP = 320f
private const val MOBILE_HERO_MAX_HEIGHT_DP = 480f

internal data class HomeHeroLayout(
    val isTablet: Boolean,
    val heroHeight: Dp,
    val contentMaxWidth: Dp,
    val contentWidthFraction: Float,
    val contentHorizontalPadding: Dp,
    val contentVerticalPadding: Dp,
    val bottomFadeHeight: Dp,
    val logoWidthFraction: Float,
)

@Composable
fun HomeHeroSection(
    items: List<MetaPreview>,
    modifier: Modifier = Modifier,
    viewportHeight: Dp? = null,
    mobileBelowSectionHeightHint: Dp? = null,
    listState: LazyListState? = null,
    stretchPx: () -> Float = { 0f },
    onItemClick: ((MetaPreview) -> Unit)? = null,
    onPlayClick: ((MetaPreview) -> Unit)? = null,
) {
    if (items.isEmpty()) return

    val pagerState = key(items.size) {
        rememberPagerState(
            initialPage = if (items.size > 1) {
                Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2) % items.size
            } else {
                0
            },
            pageCount = { if (items.size > 1) Int.MAX_VALUE else 1 },
        )
    }
    val coroutineScope = rememberCoroutineScope()
    val autoScrollPage = pagerState.settledPage

    LaunchedEffect(pagerState) {
        pagerState.scrollToPage(pagerState.currentPage)
    }

    ScreenActivityEffect(pagerState) { active ->
        if (!active) {
            pagerState.stopScroll(MutatePriority.PreventUserInput)
            pagerState.scrollToPage(pagerState.currentPage)
        }
    }

    ScreenActivityEffect(autoScrollPage, items.size) { active ->
        if (!active || items.size <= 1) return@ScreenActivityEffect
        delay(HERO_AUTO_SCROLL_INTERVAL_MS)
        while (pagerState.isScrollInProgress) {
            delay(100L)
        }

        // Auto-advance towards the card on the right side: the deck slides so the
        // next card scrolls into view (never advances to the left-side card).
        val nextPage = pagerState.currentPage + 1
        pagerState.animateScrollToPage(nextPage)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .homeHeroPagerGesture(
                pagerState = pagerState,
                itemCount = items.size,
                coroutineScope = coroutineScope,
            ),
    ) {
        val layout = homeHeroLayout(
            maxWidthDp = maxWidth.value,
            viewportHeightDp = viewportHeight?.value,
            mobileBelowSectionHeightHintDp = mobileBelowSectionHeightHint?.value,
        )
        val heroWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val heroHeightPx = with(LocalDensity.current) { layout.heroHeight.toPx() }
        val scrollOffsetPx by remember(listState, heroHeightPx) {
            derivedStateOf {
                when {
                    listState == null -> 0f
                    listState.firstVisibleItemIndex > 0 -> heroHeightPx
                    else -> listState.firstVisibleItemScrollOffset.toFloat()
                }
            }
        }
        val currentPage = pagerState.currentPage
        // Centered 3D Cover Flow carousel matching Image 2:
        // Center card is elevated (depth 0), flanked by previous (-1, -2) and next (+1, +2) cards fanning out behind
        val stackOffsets = if (items.size >= 5) {
            listOf(-2, 2, -1, 1, 0)
        } else if (items.size >= 3) {
            listOf(-1, 1, 0)
        } else if (items.size == 2) {
            listOf(1, 0)
        } else {
            listOf(0)
        }
        val stackLayers = stackOffsets.map { offset ->
            val pageIndex = (currentPage + offset + items.size) % items.size
            val pageOffset = offset.toFloat() - pagerState.currentPageOffsetFraction
            HeroPageLayer(
                itemIndex = pageIndex,
                visibility = if (offset == 0) 1f else 0.85f,
                offset = pageOffset,
            )
        }.sortedByDescending { abs(it.offset) } // Background cards drawn first, center card drawn last on top

        val currentItem = items[currentPage % items.size]

        // Dominant color of the current center card — drives the feathered ambient
        // light spreading from behind the card cluster.
        val ambientColorState = rememberPainterDominantColorState(
            defaultColor = Color.Transparent,
            defaultOnColor = Color.White,
        )
        var heroArtwork by remember(currentPage, items.size) { mutableStateOf<androidx.compose.ui.graphics.painter.Painter?>(null) }
        LaunchedEffect(heroArtwork) {
            heroArtwork?.let { painter ->
                runCatching { ambientColorState.updateFrom(painter) }
            }
        }
        val ambientGlowColor = ambientColorState.color
        val fallbackColor = MaterialTheme.nuvio.colors.accent
        val effectiveGlowColor = if (ambientGlowColor != Color.Transparent) ambientGlowColor else fallbackColor
        val animatedGlowColor by animateColorAsState(
            targetValue = effectiveGlowColor,
            animationSpec = tween(400),
            label = "heroGlowColor",
        )

        val haptic = LocalHapticFeedback.current
        LaunchedEffect(currentPage) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heroStretchHeight(layout.heroHeight, stretchPx),
            ) {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.01f },
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(layout.heroHeight)
                            .heroStretchZoom(stretchPx),
                    ) {
                        stackLayers.forEach { layer ->
                            val offsetFromCenter = layer.offset
                            val isCenter = abs(offsetFromCenter) < 0.25f
                            val absOffset = abs(offsetFromCenter)
                            val deckScale = (1f - (absOffset * 0.13f)).coerceIn(0.72f, 1f)
                            val sign = if (offsetFromCenter >= 0) 1f else -1f
                            val translationXPx = sign * (absOffset.coerceAtMost(1f) * 0.28f + (absOffset - 1f).coerceAtLeast(0f) * 0.18f) * heroWidthPx

                            var imageLoaded by remember(layer.itemIndex, items.size) { mutableStateOf(false) }
                            val cardContentAlpha by animateFloatAsState(
                                targetValue = if (imageLoaded) 1f else 0f,
                                animationSpec = tween(280),
                                label = "heroCardContentLoaded",
                            )

                            val cardHeight = if (layout.isTablet) 300.dp else 265.dp

                            Box(
                                modifier = Modifier
                                    .height(cardHeight)
                                    .aspectRatio(0.67f)
                                    .align(Alignment.Center)
                                    .graphicsLayer {
                                        val offset = scrollOffsetPx
                                        val scrollScale = heroBackgroundScrollScale(offset)
                                        alpha = (1f - (absOffset * 0.15f)).coerceIn(0.68f, 1f)
                                        translationX = translationXPx
                                        translationY = absOffset * 6.dp.toPx()
                                        scaleX = scrollScale * deckScale
                                        scaleY = scrollScale * deckScale
                                    }
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (imageLoaded) 0.35f else 0.10f))
                                    .border(
                                        BorderStroke(
                                            width = if (isCenter) 1.5.dp else 1.dp,
                                            color = if (imageLoaded) {
                                                if (isCenter) {
                                                    Color.White.copy(alpha = 0.45f)
                                                } else {
                                                    Color.White.copy(alpha = 0.25f)
                                                }
                                            } else {
                                                Color.Transparent
                                            },
                                        ),
                                        RoundedCornerShape(22.dp),
                                    )
                                    .clickable(enabled = onItemClick != null && isCenter) {
                                        onItemClick?.invoke(items[layer.itemIndex])
                                    },
                            ) {
                                AsyncImage(
                                    model = items[layer.itemIndex].poster ?: items[layer.itemIndex].banner,
                                    contentDescription = items[layer.itemIndex].name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer { alpha = cardContentAlpha },
                                    alignment = Alignment.Center,
                                    contentScale = ContentScale.Crop,
                                    onState = { state ->
                                        when (state) {
                                            is AsyncImagePainter.State.Success,
                                            is AsyncImagePainter.State.Error,
                                            -> imageLoaded = true
                                            else -> Unit
                                        }
                                        if (isCenter && state is AsyncImagePainter.State.Success) {
                                            heroArtwork = state.painter
                                        }
                                    },
                                )
                                if (!isCenter && imageLoaded) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = (absOffset * 0.18f).coerceIn(0.12f, 0.35f))),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Below the card carousel: Indicator dots, Title, Metadata, Buttons (matching Image 2)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = layout.contentHorizontalPadding)
                    .padding(top = 10.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Pagination indicator dots
                if (items.size > 1) {
                    val indicatorCount = items.size.coerceAtMost(6)
                    Row(
                        modifier = Modifier.padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        items.take(indicatorCount).forEachIndexed { index, _ ->
                            val isSelected = (pagerState.currentPage % items.size) == index
                            val dotWidth by animateDpAsState(
                                targetValue = if (isSelected) 22.dp else 5.dp,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "indicatorDotWidth",
                            )
                            Box(
                                modifier = Modifier
                                    .width(dotWidth)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) Color(0xFF1E88E5) else Color.White.copy(alpha = 0.40f)),
                            )
                        }
                    }
                }

                // Centered Title
                Text(
                    text = currentItem.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = onItemClick != null) {
                            onItemClick?.invoke(currentItem)
                        },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Centered Metadata (e.g. 2026 | Drama / Funny)
                val metaParts = remember(currentItem.id, currentItem.releaseInfo, currentItem.genres) {
                    buildList {
                        currentItem.releaseInfo?.takeIf { it.isNotBlank() }?.let { info ->
                            add(formatReleaseDateForDisplay(info))
                        }
                        if (currentItem.genres.isNotEmpty()) {
                            add(currentItem.genres.take(2).joinToString(" / "))
                        } else if (currentItem.type.isNotBlank()) {
                            add(currentItem.type.replaceFirstChar(Char::uppercase))
                        }
                    }
                }
                if (metaParts.isNotEmpty()) {
                    Text(
                        text = metaParts.joinToString(" | "),
                        color = Color.White.copy(alpha = 0.70f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: "Play" (theme accent pill) & "Details" (dark pill)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val accentColor = MaterialTheme.nuvio.colors.accent
                    val playContentColor = if (accentColor.luminance() > 0.45f) Color(0xFF111111) else Color.White
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = onPlayClick != null || onItemClick != null) {
                                onPlayClick?.invoke(currentItem) ?: onItemClick?.invoke(currentItem)
                            },
                        color = accentColor,
                        contentColor = playContentColor,
                        shape = RoundedCornerShape(50),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(19.dp),
                                tint = playContentColor,
                            )
                            Text(
                                text = "Play",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = playContentColor,
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = onItemClick != null) {
                                onItemClick?.invoke(currentItem)
                            },
                        color = Color.White.copy(alpha = 0.12f),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class HeroPageLayer(
    val itemIndex: Int,
    val visibility: Float,
    val offset: Float,
)

private fun heroPageOffset(
    pagerState: PagerState,
    page: Int,
): Float = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

internal fun heroPageForItem(currentPage: Int, itemIndex: Int, itemCount: Int): Int {
    val page = currentPage.toLong() - currentPage % itemCount + itemIndex
    return listOf(page - itemCount, page, page + itemCount)
        .filter { it in 0L until Int.MAX_VALUE.toLong() }
        .minBy { abs(it - currentPage) }
        .toInt()
}

private val HeroCardRibbon.color: Color
    get() = when (this) {
        HeroCardRibbon.RECENTLY_ADDED -> Color(0xFF16A34A)
        HeroCardRibbon.COMING_SOON -> Color(0xFF2563EB)
    }

@Composable
private fun heroRibbonForItem(item: MetaPreview): HeroCardRibbon? {
    val todayIso = remember { CurrentDateProvider.todayIsoDate() }
    return remember(item.id, item.releaseInfo, item.rawReleaseDate, todayIso) {
        item.heroCardRibbon(todayIso)
    }
}

@Composable
fun HomeHeroReservedSpace(
    modifier: Modifier = Modifier,
    viewportHeight: Dp? = null,
    mobileBelowSectionHeightHint: Dp? = null,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
    ) {
        val layout = homeHeroLayout(
            maxWidthDp = maxWidth.value,
            viewportHeightDp = viewportHeight?.value,
            mobileBelowSectionHeightHintDp = mobileBelowSectionHeightHint?.value,
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(layout.heroHeight),
        )
    }
}

@Composable
private fun HeroContentBlock(
    item: MetaPreview,
    layout: HomeHeroLayout,
    onItemClick: ((MetaPreview) -> Unit)?,
) {
    var logoLoadError by remember(item.type, item.id, item.logo) {
        mutableStateOf(false)
    }
    val logoUrl = item.logo?.takeIf { it.isNotBlank() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        if (logoUrl != null && !logoLoadError) {
            AsyncImage(
                model = logoUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth(layout.logoWidthFraction)
                    .aspectRatio(2.6f)
                    .clickable(enabled = onItemClick != null) {
                        onItemClick?.invoke(item)
                    },
                alignment = if (layout.isTablet) Alignment.CenterStart else Alignment.Center,
                contentScale = ContentScale.Fit,
                onError = { logoLoadError = true },
            )
        } else {
            Text(
                text = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = onItemClick != null) {
                        onItemClick?.invoke(item)
                    },
                style = if (layout.isTablet) {
                    MaterialTheme.typography.displaySmall
                } else {
                    MaterialTheme.typography.displaySmall
                },
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                textAlign = if (layout.isTablet) TextAlign.Start else TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (layout.isTablet) {
                Arrangement.spacedBy(8.dp, Alignment.Start)
            } else {
                Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeroMetaText(text = item.type.replaceFirstChar(Char::uppercase))
            item.genres.firstOrNull()?.let { genre ->
                HeroMetaDot()
                HeroMetaText(text = genre)
            }
            item.releaseInfo?.takeIf { it.isNotBlank() }?.let { info ->
                HeroMetaDot()
                HeroMetaText(text = formatReleaseDateForDisplay(info))
            }
        }
    }
}

@Composable
private fun HeroMetaText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

internal fun homeHeroLayout(
    maxWidthDp: Float,
    viewportHeightDp: Float? = null,
    mobileBelowSectionHeightHintDp: Float? = null,
): HomeHeroLayout =
    when {
        maxWidthDp >= 1200f -> HomeHeroLayout(
            isTablet = true,
            heroHeight = 350.dp,
            contentMaxWidth = 640.dp,
            contentWidthFraction = 0.56f,
            contentHorizontalPadding = 56.dp,
            contentVerticalPadding = 22.dp,
            bottomFadeHeight = 190.dp,
            logoWidthFraction = 0.58f,
        )
        maxWidthDp >= 840f -> HomeHeroLayout(
            isTablet = true,
            heroHeight = 340.dp,
            contentMaxWidth = 560.dp,
            contentWidthFraction = 0.62f,
            contentHorizontalPadding = 40.dp,
            contentVerticalPadding = 20.dp,
            bottomFadeHeight = 180.dp,
            logoWidthFraction = 0.56f,
        )
        maxWidthDp >= 600f -> HomeHeroLayout(
            isTablet = true,
            heroHeight = 320.dp,
            contentMaxWidth = 520.dp,
            contentWidthFraction = 0.72f,
            contentHorizontalPadding = 32.dp,
            contentVerticalPadding = 18.dp,
            bottomFadeHeight = 170.dp,
            logoWidthFraction = 0.54f,
        )
        else -> HomeHeroLayout(
            isTablet = false,
            heroHeight = mobileHeroHeight(
                maxWidthDp = maxWidthDp,
                viewportHeightDp = viewportHeightDp,
                mobileBelowSectionHeightHintDp = mobileBelowSectionHeightHintDp,
            ),
            contentMaxWidth = 480.dp,
            contentWidthFraction = 1f,
            contentHorizontalPadding = 20.dp,
            contentVerticalPadding = 20.dp,
            bottomFadeHeight = 140.dp,
            logoWidthFraction = 0.65f,
        )
    }

private fun mobileHeroHeight(
    maxWidthDp: Float,
    viewportHeightDp: Float?,
    mobileBelowSectionHeightHintDp: Float?,
): Dp {
    return 295.dp
}

@Composable
private fun HeroMetaDot() {
    Box(
        modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)),
    )
}

private fun heroBackgroundScrollScale(scrollOffsetPx: Float): Float {
    val scaleIncrease = if (scrollOffsetPx < 0f) {
        abs(scrollOffsetPx) * HERO_SCROLL_UP_SCALE_MULTIPLIER
    } else {
        scrollOffsetPx * HERO_SCROLL_DOWN_SCALE_MULTIPLIER
    }
    return (1f + scaleIncrease).coerceAtMost(HERO_SCROLL_MAX_SCALE)
}

private fun heroBackgroundScrollTranslationY(scrollOffsetPx: Float): Float {
    return 0f
}

private fun Modifier.homeHeroPagerGesture(
    pagerState: PagerState,
    itemCount: Int,
    coroutineScope: CoroutineScope,
): Modifier {
    if (itemCount <= 1) return this

    return pointerInput(pagerState, itemCount) {
        awaitEachGesture {
            val down = awaitFirstDown(pass = PointerEventPass.Initial)
            val widthPx = size.width.toFloat().takeIf { it > 0f } ?: return@awaitEachGesture
            val velocityTracker = VelocityTracker().apply {
                addPosition(down.uptimeMillis, down.position)
            }
            val startPage = pagerState.currentPage
            var totalDx = 0f
            var totalDy = 0f
            var dragging = false

            while (true) {
                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                velocityTracker.addPosition(change.uptimeMillis, change.position)

                if (!change.pressed) {
                    if (dragging) {
                        val targetPage = resolveHeroTargetPage(
                            startPage = startPage,
                            pageCount = pagerState.pageCount,
                            totalDx = totalDx,
                            velocityX = velocityTracker.calculateVelocity().x,
                            widthPx = widthPx,
                        )
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(targetPage)
                        }
                    }
                    break
                }

                val delta = change.position - change.previousPosition
                totalDx += delta.x
                totalDy += delta.y

                if (!dragging) {
                    val horizontalDrag =
                        abs(totalDx) > viewConfiguration.touchSlop && abs(totalDx) > abs(totalDy)
                    val verticalDrag =
                        abs(totalDy) > viewConfiguration.touchSlop && abs(totalDy) > abs(totalDx)

                    when {
                        verticalDrag -> break
                        horizontalDrag -> dragging = true
                        else -> continue
                    }
                }

                pagerState.dispatchRawDelta(-delta.x)
                change.consume()
            }
        }
    }
}

private fun resolveHeroTargetPage(
    startPage: Int,
    pageCount: Int,
    totalDx: Float,
    velocityX: Float,
    widthPx: Float,
): Int {
    val thresholdPassed = abs(totalDx) > widthPx * HERO_SWIPE_THRESHOLD_FRACTION ||
        abs(velocityX) > HERO_SWIPE_VELOCITY_THRESHOLD
    if (!thresholdPassed) return startPage

    val currentPage = startPage.coerceIn(0, pageCount - 1)
    return when {
        totalDx > 0f -> (currentPage - 1).coerceAtLeast(0)
        totalDx < 0f -> (currentPage + 1).coerceAtMost(pageCount - 1)
        else -> currentPage
    }
}
