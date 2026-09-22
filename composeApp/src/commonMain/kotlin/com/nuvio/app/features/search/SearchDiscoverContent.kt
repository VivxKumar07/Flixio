package com.nuvio.app.features.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.nuvio.app.core.network.NetworkCondition
import com.nuvio.app.core.ui.ClashDisplayFontFamily
import com.nuvio.app.core.ui.FlixioLoadingIndicator
import com.nuvio.app.core.ui.ManropeFontFamily
import com.nuvio.app.core.ui.NuvioDropdownChip
import com.nuvio.app.core.ui.NuvioDropdownOption
import com.nuvio.app.core.ui.NuvioNetworkOfflineCard
import com.nuvio.app.core.ui.nuvio
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.home.components.HomeEmptyStateCard
import com.nuvio.app.features.home.components.PosterGridRow
import com.nuvio.app.features.home.components.PosterGridSkeletonRow
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private val CuratedCategories = listOf(
    "Action",
    "Comedy",
    "Horror",
    "Romance",
    "Sci-Fi",
    "Animation",
    "Thriller",
    "Drama",
)

private val CategoryBackgrounds = mapOf(
    "Action" to "https://images.metahub.space/background/medium/tt0848228/img.jpg",
    "Comedy" to "https://images.metahub.space/background/medium/tt0468569/img.jpg",
    "Horror" to "https://images.metahub.space/background/medium/tt1457767/img.jpg",
    "Romance" to "https://images.metahub.space/background/medium/tt0120338/img.jpg",
    "Sci-Fi" to "https://images.metahub.space/background/medium/tt0816692/img.jpg",
    "Animation" to "https://images.metahub.space/background/medium/tt0114709/img.jpg",
    "Thriller" to "https://images.metahub.space/background/medium/tt0468569/img.jpg",
    "Drama" to "https://images.metahub.space/background/medium/tt0111161/img.jpg",
)

internal fun LazyListScope.discoverContent(
    state: DiscoverUiState,
    isSourceLoading: Boolean,
    columns: Int,
    networkCondition: NetworkCondition,
    onTypeSelected: (String) -> Unit,
    onCatalogSelected: (String) -> Unit,
    onGenreSelected: (String?) -> Unit,
    onRetry: (() -> Unit)? = null,
    watchedKeys: Set<String> = emptySet(),
    fullyWatchedSeriesKeys: Set<String> = emptySet(),
    onPosterClick: ((MetaPreview) -> Unit)? = null,
    onPosterLongClick: ((MetaPreview) -> Unit)? = null,
) {
    item {
        DiscoverSectionHeader(modifier = Modifier.padding(horizontal = 16.dp))
    }
    item {
        DiscoverFilterRow(
            state = state,
            modifier = Modifier.padding(horizontal = 16.dp),
            onTypeSelected = onTypeSelected,
            onCatalogSelected = onCatalogSelected,
            onGenreSelected = onGenreSelected,
        )
    }

    if (state.selectedGenre == null) {
        // Show 8 curated category cards in 2 columns (4 rows)
        val categories = CuratedCategories
        val rows = (categories.size + 1) / 2
        items(count = rows, key = { "search_cat_row_$it" }) { rowIndex ->
            val firstIdx = rowIndex * 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val cat1 = categories[firstIdx]
                CategoryCard(
                    categoryName = cat1,
                    imageUrl = CategoryBackgrounds[cat1],
                    onClick = { onGenreSelected(cat1) },
                    modifier = Modifier.weight(1f),
                )
                if (firstIdx + 1 < categories.size) {
                    val cat2 = categories[firstIdx + 1]
                    CategoryCard(
                        categoryName = cat2,
                        imageUrl = CategoryBackgrounds[cat2],
                        onClick = { onGenreSelected(cat2) },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        if (state.items.isNotEmpty() || state.isLoading) {
            item(key = "discover_all_header") {
                val catalogTitle = state.selectedCatalog?.catalogName
                    ?: stringResource(Res.string.compose_search_discover_title)
                Text(
                    text = catalogTitle,
                    fontFamily = ClashDisplayFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
    } else {
        // Show active category bar and filtered content
        item(key = "active_genre_bar") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.nuvio.colors.surfaceCard)
                            .border(1.dp, MaterialTheme.nuvio.colors.borderSubtle, RoundedCornerShape(8.dp))
                            .clickable { onGenreSelected(null) }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = "← All Categories",
                            fontFamily = ManropeFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = state.selectedGenre.orEmpty(),
                        fontFamily = ClashDisplayFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }

    when {
        (state.isLoading || isSourceLoading) && state.items.isEmpty() -> {
            items(2) {
                PosterGridSkeletonRow(
                    columns = columns,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        state.items.isEmpty() -> {
            item {
                DiscoverEmptyStateCard(
                    reason = state.emptyStateReason,
                    errorMessage = state.errorMessage,
                    networkCondition = networkCondition,
                    onRetry = onRetry,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        else -> {
            items(count = (state.items.size + columns - 1) / columns) { rowIndex ->
                val firstIndex = rowIndex * columns
                PosterGridRow(
                    items = state.items.subList(firstIndex, minOf(firstIndex + columns, state.items.size)),
                    columns = columns,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    watchedKeys = watchedKeys,
                    fullyWatchedSeriesKeys = fullyWatchedSeriesKeys,
                    onPosterClick = onPosterClick,
                    onPosterLongClick = onPosterLongClick,
                )
            }
            if (state.isLoading) {
                item {
                    CatalogLoadingFooter(
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    categoryName: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(shape)
            .background(MaterialTheme.nuvio.colors.surfaceCard)
            .border(
                width = 1.dp,
                color = MaterialTheme.nuvio.colors.borderSubtle.copy(alpha = 0.35f),
                shape = shape,
            )
            .clickable(onClick = onClick),
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x66000000),
                            Color(0xCC000000),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = categoryName,
                fontFamily = ClashDisplayFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                maxLines = 1,
            )
            Text(
                text = "Explore",
                fontFamily = ManropeFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = MaterialTheme.nuvio.colors.accent,
            )
        }
    }
}

@Composable
private fun DiscoverSectionHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.compose_search_discover_title),
        modifier = modifier,
        fontFamily = ClashDisplayFontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun DiscoverFilterRow(
    state: DiscoverUiState,
    onTypeSelected: (String) -> Unit,
    onCatalogSelected: (String) -> Unit,
    onGenreSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NuvioDropdownChip(
            title = stringResource(Res.string.discover_select_type),
            label = state.selectedType?.displayTypeLabel() ?: stringResource(Res.string.discover_type),
            selectedKey = state.selectedType,
            options = state.typeOptions.map { NuvioDropdownOption(key = it, label = it.displayTypeLabel()) },
            enabled = state.typeOptions.isNotEmpty(),
            onSelected = { onTypeSelected(it.key) },
        )
        NuvioDropdownChip(
            title = stringResource(Res.string.discover_select_catalog),
            label = state.selectedCatalog?.catalogName ?: stringResource(Res.string.discover_catalog),
            selectedKey = state.selectedCatalogKey,
            options = state.catalogOptions.map { option -> NuvioDropdownOption(key = option.key, label = option.catalogName) },
            enabled = state.catalogOptions.isNotEmpty(),
            onSelected = { onCatalogSelected(it.key) },
        )

        val selectedCatalog = state.selectedCatalog
        val genreOptions = buildList {
            if (selectedCatalog?.genreRequired != true) {
                add(NuvioDropdownOption(key = "", label = stringResource(Res.string.discover_all_genres)))
            }
            addAll(state.genreOptions.map { genre -> NuvioDropdownOption(key = genre, label = genre) })
        }
        NuvioDropdownChip(
            title = stringResource(Res.string.discover_select_genre),
            label = state.selectedGenre ?: stringResource(Res.string.discover_all_genres),
            selectedKey = state.selectedGenre ?: "",
            options = genreOptions,
            enabled = genreOptions.size > 1 || selectedCatalog?.genreRequired == true,
            onSelected = { option ->
                onGenreSelected(option.key.ifBlank { null })
            },
        )
    }
}

@Composable
private fun CatalogLoadingFooter(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        FlixioLoadingIndicator(
            modifier = Modifier.size(22.dp),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun DiscoverEmptyStateCard(
    reason: DiscoverEmptyStateReason?,
    errorMessage: String?,
    networkCondition: NetworkCondition,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (
        reason == DiscoverEmptyStateReason.RequestFailed &&
        (networkCondition == NetworkCondition.NoInternet || networkCondition == NetworkCondition.ServersUnreachable)
    ) {
        NuvioNetworkOfflineCard(
            condition = networkCondition,
            modifier = modifier,
            onRetry = onRetry,
        )
        return
    }

    val title: String
    val message: String

    when (reason) {
        DiscoverEmptyStateReason.NoActiveAddons -> {
            title = stringResource(Res.string.compose_search_empty_no_active_addons_title)
            message = stringResource(Res.string.discover_empty_no_active_addons_message)
        }

        DiscoverEmptyStateReason.NoDiscoverCatalogs -> {
            title = stringResource(Res.string.discover_empty_no_catalogs_title)
            message = stringResource(Res.string.discover_empty_no_catalogs_message)
        }

        DiscoverEmptyStateReason.RequestFailed -> {
            title = stringResource(Res.string.discover_empty_load_failed_title)
            message = errorMessage ?: stringResource(Res.string.discover_empty_load_failed_message)
        }

        DiscoverEmptyStateReason.NoResults, null -> {
            title = stringResource(Res.string.discover_empty_no_results_title)
            message = stringResource(Res.string.discover_empty_no_results_message)
        }
    }

    HomeEmptyStateCard(
        modifier = modifier,
        title = title,
        message = message,
        actionLabel = if (reason == DiscoverEmptyStateReason.RequestFailed) {
            stringResource(Res.string.action_retry)
        } else {
            null
        },
        onActionClick = if (reason == DiscoverEmptyStateReason.RequestFailed) onRetry else null,
    )
}

@Composable
private fun String.displayTypeLabel(): String =
    when (lowercase()) {
        "movie" -> stringResource(Res.string.media_movies)
        "series" -> stringResource(Res.string.media_series)
        "anime" -> stringResource(Res.string.media_anime)
        "channel" -> stringResource(Res.string.media_channels)
        "tv" -> stringResource(Res.string.media_tv)
        else -> replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
