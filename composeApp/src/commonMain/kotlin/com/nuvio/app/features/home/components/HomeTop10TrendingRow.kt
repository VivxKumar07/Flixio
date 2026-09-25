package com.nuvio.app.features.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.nuvio.app.core.format.formatReleaseDateForDisplay
import com.nuvio.app.core.ui.ClashDisplayFontFamily
import com.nuvio.app.core.ui.ManropeFontFamily
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.home.stableKey

/**
 * Top 10 Trending row modeled after modern cinematic streaming platforms:
 * - Prominent section title with theme accent indicator bar and sub-label.
 * - #1 Trending item is an expanded landscape card with backdrop, synopsis, logo, and "See trailer" button.
 * - #2 to #10 items are portrait poster cards with distinct red "TOP X" corner badges.
 */
@Composable
fun HomeTop10TrendingRow(
    items: List<MetaPreview>,
    modifier: Modifier = Modifier,
    sectionPadding: Dp = 16.dp,
    onItemClick: ((MetaPreview) -> Unit)? = null,
) {
    if (items.isEmpty()) return
    val top10List = remember(items) { items.distinctBy { it.id }.take(10) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = sectionPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Theme accent vertical indicator bar
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "TOP 10 on Flixio",
                    fontFamily = ClashDisplayFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.3).sp,
                )
                Text(
                    text = "The most watched titles right now",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                )
            }
        }

        // Horizontal Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = sectionPadding),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            itemsIndexed(
                items = top10List,
                key = { _, item -> "top10_${item.stableKey()}" },
            ) { index, item ->
                if (index == 0) {
                    Top10FeaturedCard(
                        item = item,
                        onClick = { onItemClick?.invoke(item) },
                    )
                } else {
                    Top10StandardPosterCard(
                        item = item,
                        rank = index + 1,
                        onClick = { onItemClick?.invoke(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun Top10FeaturedCard(
    item: MetaPreview,
    onClick: () -> Unit,
) {
    val cardShape = RoundedCornerShape(14.dp)

    Column(
        modifier = Modifier
            .width(340.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.75f)
                .clip(cardShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), cardShape),
        ) {
            // Backdrop image
            AsyncImage(
                model = item.banner ?: item.poster,
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            // Dark cinematic gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.90f),
                                Color.Black.copy(alpha = 0.65f),
                                Color.Transparent,
                            ),
                            startX = 0f,
                            endX = 480f,
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f),
                            ),
                            startY = 60f,
                        ),
                    ),
            )

            // TOP 1 Corner Badge
            TopRankCornerBadge(
                rank = 1,
                modifier = Modifier.align(Alignment.TopStart),
            )

            // Content Block (Logo / Title, Overview, Button)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 34.dp, start = 14.dp, end = 120.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!item.logo.isNullOrBlank()) {
                        AsyncImage(
                            model = item.logo,
                            contentDescription = item.name,
                            modifier = Modifier
                                .width(120.dp)
                                .height(38.dp),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.CenterStart,
                        )
                    } else {
                        Text(
                            text = item.name,
                            fontFamily = ClashDisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Cast / Genres
                    val metaLine = item.genres.firstOrNull() ?: item.type.replaceFirstChar(Char::uppercase)
                    Text(
                        text = metaLine,
                        fontFamily = ManropeFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    // Synopsis
                    if (!item.description.isNullOrBlank()) {
                        Text(
                            text = item.description,
                            fontFamily = ManropeFontFamily,
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.80f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 14.sp,
                        )
                    }
                }

                // "See trailer" button
                Surface(
                    color = Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f)),
                    modifier = Modifier.clickable(onClick = onClick),
                ) {
                    Text(
                        text = "See trailer",
                        fontFamily = ManropeFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
            }
        }

        // Title and Rating info below the card
        ItemBelowInfo(item = item)
    }
}

@Composable
private fun Top10StandardPosterCard(
    item: MetaPreview,
    rank: Int,
    onClick: () -> Unit,
) {
    val cardShape = RoundedCornerShape(14.dp)

    Column(
        modifier = Modifier
            .width(136.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.68f)
                .clip(cardShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)), cardShape),
        ) {
            AsyncImage(
                model = item.poster ?: item.banner,
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            // TOP # Rank Corner Badge
            TopRankCornerBadge(
                rank = rank,
                modifier = Modifier.align(Alignment.TopStart),
            )
        }

        // Title and Rating info below the card
        ItemBelowInfo(item = item)
    }
}

@Composable
private fun TopRankCornerBadge(
    rank: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(topStart = 14.dp, bottomEnd = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "TOP",
                fontFamily = ManropeFontFamily,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.5.sp,
            )
            Text(
                text = "$rank",
                fontFamily = ClashDisplayFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                lineHeight = 11.sp,
            )
        }
    }
}

@Composable
private fun ItemBelowInfo(item: MetaPreview) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = item.name,
            fontFamily = ManropeFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item.imdbRating?.takeIf { it.isNotBlank() }?.let { rating ->
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(11.dp),
                )
                Text(
                    text = rating.take(3),
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                )
                Text(
                    text = "·",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                )
            }

            item.releaseInfo?.takeIf { it.isNotBlank() }?.let { release ->
                Text(
                    text = formatReleaseDateForDisplay(release),
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    maxLines = 1,
                )
                Text(
                    text = "·",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                )
            }

            Text(
                text = item.type.replaceFirstChar(Char::uppercase),
                fontFamily = ManropeFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                maxLines = 1,
            )
        }
    }
}
