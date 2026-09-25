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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.core.ui.ClashDisplayFontFamily
import com.nuvio.app.core.ui.ManropeFontFamily

data class GenreCardItem(
    val name: String,
    val genreId: String,
    val gradientColors: List<Color>,
    val imageUrl: String,
)

private val POPULAR_GENRES = listOf(
    GenreCardItem(
        name = "Action",
        genreId = "action",
        gradientColors = listOf(Color(0xFFD32F2F).copy(alpha = 0.82f), Color(0xFF5C0000).copy(alpha = 0.95f)),
        imageUrl = "https://image.tmdb.org/t/p/w500/mDfGiamn2gL8q52ZcM92Tq7Kx6w.jpg",
    ),
    GenreCardItem(
        name = "Comedy",
        genreId = "comedy",
        gradientColors = listOf(Color(0xFF0288D1).copy(alpha = 0.82f), Color(0xFF013A63).copy(alpha = 0.95f)),
        imageUrl = "https://image.tmdb.org/t/p/w500/9BBTo63ANSmAgaxRaVgiYR68Jvh.jpg",
    ),
    GenreCardItem(
        name = "Drama",
        genreId = "drama",
        gradientColors = listOf(Color(0xFF00897B).copy(alpha = 0.82f), Color(0xFF00332C).copy(alpha = 0.95f)),
        imageUrl = "https://image.tmdb.org/t/p/w500/kXfqcdQKsToO0OUXHcrrNCHDBzO.jpg",
    ),
    GenreCardItem(
        name = "Horror",
        genreId = "horror",
        gradientColors = listOf(Color(0xFF6A1B9A).copy(alpha = 0.82f), Color(0xFF26004B).copy(alpha = 0.95f)),
        imageUrl = "https://image.tmdb.org/t/p/w500/7c9UVPPiTPltouxRVY69929xBtK.jpg",
    ),
    GenreCardItem(
        name = "Sci-Fi",
        genreId = "sci-fi",
        gradientColors = listOf(Color(0xFF1E88E5).copy(alpha = 0.82f), Color(0xFF0A2E68).copy(alpha = 0.95f)),
        imageUrl = "https://image.tmdb.org/t/p/w500/rAiYTsqJJR0KP8qi8urRNDwZ1a5.jpg",
    ),
    GenreCardItem(
        name = "Romance",
        genreId = "romance",
        gradientColors = listOf(Color(0xFFC2185B).copy(alpha = 0.82f), Color(0xFF560027).copy(alpha = 0.95f)),
        imageUrl = "https://image.tmdb.org/t/p/w500/zfbjgQE1uSd9wiPTX4VzsLi0rGG.jpg",
    ),
    GenreCardItem(
        name = "Animation",
        genreId = "animation",
        gradientColors = listOf(Color(0xFFF57C00).copy(alpha = 0.82f), Color(0xFF872500).copy(alpha = 0.95f)),
        imageUrl = "https://image.tmdb.org/t/p/w500/14QbnygCuTO0vl7CAFmPf1fgZfV.jpg",
    ),
    GenreCardItem(
        name = "Thriller",
        genreId = "thriller",
        gradientColors = listOf(Color(0xFF37474F).copy(alpha = 0.82f), Color(0xFF101416).copy(alpha = 0.95f)),
        imageUrl = "https://image.tmdb.org/t/p/w500/gg4Z2ZgT0B4kO06lR2e6W3zSg1K.jpg",
    ),
)

/**
 * Category / Popular Genres row:
 * - Theme accent indicator + "Popular Genres >" + "Find something by mood".
 * - Landscape cards with rich backdrop photography merged into colored mood gradients.
 */
@Composable
fun HomePopularGenresRow(
    modifier: Modifier = Modifier,
    sectionPadding: Dp = 16.dp,
    onGenreClick: ((genreName: String) -> Unit)? = null,
) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = sectionPadding)
                .clickable(enabled = onGenreClick != null) { onGenreClick?.invoke("all") },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Theme accent indicator bar
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Popular Genres",
                        fontFamily = ClashDisplayFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.3).sp,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = "Find something by mood",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                )
            }
        }

        // Horizontal Row of Genre Cards
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = sectionPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = POPULAR_GENRES,
                key = { it.genreId },
            ) { genre ->
                Box(
                    modifier = Modifier
                        .width(170.dp)
                        .height(95.dp)
                        .clip(cardShape)
                        .border(
                            BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            cardShape,
                        )
                        .clickable { onGenreClick?.invoke(genre.name) },
                ) {
                    // Movie/Show backdrop image
                    coil3.compose.AsyncImage(
                        model = genre.imageUrl,
                        contentDescription = genre.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    )

                    // Rich mood gradient merged over image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = genre.gradientColors,
                                ),
                            ),
                    )

                    // Subtle top sheen overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.18f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.45f),
                                    ),
                                ),
                            ),
                    )

                    // Bold Genre Title at bottom-left
                    Text(
                        text = genre.name,
                        fontFamily = ClashDisplayFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}
