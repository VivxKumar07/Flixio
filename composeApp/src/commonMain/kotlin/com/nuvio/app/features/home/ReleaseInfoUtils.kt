package com.nuvio.app.features.home

import com.nuvio.app.core.time.EpisodeReleaseDatePlatform
import com.nuvio.app.core.time.isEpisodeReleaseAired
import com.nuvio.app.core.time.parseEpisodeReleaseEpochMs

private val yearRegex = Regex("""\b(19|20)\d{2}\b""")

private const val MILLIS_PER_DAY = 86_400_000L
private const val RECENT_RELEASE_WINDOW_MS = 30L * MILLIS_PER_DAY

internal enum class HeroCardRibbon(val label: String) {
    RECENTLY_ADDED("Recently Added"),
    COMING_SOON("Coming Soon"),
}

/**
 * Real release-date based ribbon for hero cards: "Coming Soon" for titles that have
 * not aired yet, "Recently Added" for titles released within the last 30 days,
 * null otherwise.
 */
internal fun MetaPreview.heroCardRibbon(
    todayIsoDate: String,
    nowEpochMs: Long = EpisodeReleaseDatePlatform.nowEpochMs(),
): HeroCardRibbon? {
    if (isUnreleased(todayIsoDate, nowEpochMs)) return HeroCardRibbon.COMING_SOON

    val raw = rawReleaseDate?.trim()?.takeIf { it.isNotEmpty() }
        ?: releaseInfo?.trim()?.takeIf { it.isNotEmpty() }
        ?: return null
    val releasedEpochMs = parseEpisodeReleaseEpochMs(raw) ?: return null
    return if (releasedEpochMs > nowEpochMs - RECENT_RELEASE_WINDOW_MS) {
        HeroCardRibbon.RECENTLY_ADDED
    } else {
        null
    }
}

internal fun MetaPreview.isUnreleased(
    todayIsoDate: String,
    nowEpochMs: Long = EpisodeReleaseDatePlatform.nowEpochMs(),
): Boolean {
    rawReleaseDate
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let { rawReleased ->
            isEpisodeReleaseAired(rawReleased, nowEpochMs)?.let { hasAired ->
                return !hasAired
            }
        }

    val info = releaseInfo ?: return false
    isEpisodeReleaseAired(info.trim(), nowEpochMs)?.let { hasAired ->
        return !hasAired
    }

    val releaseYear = yearRegex.find(info)?.value?.toIntOrNull() ?: return false
    val currentYear = todayIsoDate.take(4).toIntOrNull() ?: return false
    return releaseYear > currentYear
}

internal fun HomeCatalogSection.filterReleasedItems(
    todayIsoDate: String,
    nowEpochMs: Long = EpisodeReleaseDatePlatform.nowEpochMs(),
): HomeCatalogSection {
    val filteredItems = items.filterReleasedItems(todayIsoDate, nowEpochMs)
    return if (filteredItems.size == items.size) this else copy(items = filteredItems)
}

internal fun List<MetaPreview>.filterReleasedItems(
    todayIsoDate: String,
    nowEpochMs: Long = EpisodeReleaseDatePlatform.nowEpochMs(),
): List<MetaPreview> = filterNot { item -> item.isUnreleased(todayIsoDate, nowEpochMs) }
