package com.nuvio.app.features.streams

import com.nuvio.app.core.build.AppFeaturePolicy

object StreamAutoPlaySelector {

    fun orderAddonStreams(
        groups: List<AddonStreamGroup>,
        installedOrder: List<String>,
    ): List<AddonStreamGroup> {
        if (groups.isEmpty()) return groups

        val addonRankByName = HashMap<String, Int>(installedOrder.size)
        installedOrder.forEachIndexed { index, addonName ->
            if (addonName !in addonRankByName) {
                addonRankByName[addonName] = index
            }
        }

        val (directDebridEntries, remainingEntries) = groups.partition { group ->
            group.addonId.startsWith("debrid:") ||
                group.streams.any { stream -> stream.isAddonDebridCandidate && stream.isDirectDebridStream }
        }
        if (installedOrder.isEmpty()) return directDebridEntries + remainingEntries

        val (addonEntries, pluginEntries) = remainingEntries.partition { group ->
            group.addonName in addonRankByName
        }
        val orderedAddons = addonEntries.sortedBy { group ->
            addonRankByName.getValue(group.addonName)
        }
        return directDebridEntries + orderedAddons + pluginEntries
    }

    fun selectAutoPlayStream(
        streams: List<StreamItem>,
        mode: StreamAutoPlayMode,
        regexPattern: String,
        source: StreamAutoPlaySource,
        installedAddonNames: Set<String>,
        selectedAddons: Set<String>,
        selectedPlugins: Set<String>,
        preferredBingeGroup: String? = null,
        preferBingeGroupInSelection: Boolean = false,
        bingeGroupOnly: Boolean = false,
        debridEnabled: Boolean = true,
        activeResolverProviderId: String? = null,
        preferredQuality: StreamQualityOption = StreamQualityOption.AUTO,
        preferredAudioLanguage: String? = null,
        failedStreamKeys: Set<String> = emptySet(),
    ): StreamItem? =
        evaluateAutoPlayStream(
            streams = streams,
            mode = mode,
            regexPattern = regexPattern,
            source = source,
            installedAddonNames = installedAddonNames,
            selectedAddons = selectedAddons,
            selectedPlugins = selectedPlugins,
            preferredBingeGroup = preferredBingeGroup,
            preferBingeGroupInSelection = preferBingeGroupInSelection,
            bingeGroupOnly = bingeGroupOnly,
            debridEnabled = debridEnabled,
            activeResolverProviderId = activeResolverProviderId,
            preferredQuality = preferredQuality,
            preferredAudioLanguage = preferredAudioLanguage,
            failedStreamKeys = failedStreamKeys,
        ).stream

    fun evaluateAutoPlayStream(
        streams: List<StreamItem>,
        mode: StreamAutoPlayMode,
        regexPattern: String,
        source: StreamAutoPlaySource,
        installedAddonNames: Set<String>,
        selectedAddons: Set<String>,
        selectedPlugins: Set<String>,
        preferredBingeGroup: String? = null,
        preferBingeGroupInSelection: Boolean = false,
        bingeGroupOnly: Boolean = false,
        debridEnabled: Boolean = true,
        activeResolverProviderId: String? = null,
        preferredQuality: StreamQualityOption = StreamQualityOption.AUTO,
        preferredAudioLanguage: String? = null,
        failedStreamKeys: Set<String> = emptySet(),
    ): StreamAutoPlayEvaluation {
        if (streams.isEmpty()) return StreamAutoPlayEvaluation()

        val sourceScopedStreams = when (source) {
            StreamAutoPlaySource.ALL_SOURCES -> streams
            StreamAutoPlaySource.INSTALLED_ADDONS_ONLY -> streams.filter { it.addonName in installedAddonNames }
            StreamAutoPlaySource.ENABLED_PLUGINS_ONLY -> streams.filter { it.addonName !in installedAddonNames }
        }
        val candidateStreams = sourceScopedStreams.filter { stream ->
            val isAddonStream = stream.addonName in installedAddonNames
            if (isAddonStream) {
                selectedAddons.isEmpty() || stream.addonName in selectedAddons
            } else {
                selectedPlugins.isEmpty() || stream.addonName in selectedPlugins
            }
        }
        if (candidateStreams.isEmpty()) return StreamAutoPlayEvaluation()
        if (mode == StreamAutoPlayMode.MANUAL && !bingeGroupOnly) {
            return StreamAutoPlayEvaluation()
        }

        val targetBingeGroup = preferredBingeGroup?.trim().orEmpty()
        val bingeGroupCandidates = if (preferBingeGroupInSelection && targetBingeGroup.isNotEmpty()) {
            candidateStreams.filter { stream -> stream.behaviorHints.bingeGroup == targetBingeGroup }
        } else {
            emptyList()
        }
        val preferredReadyStream = bingeGroupCandidates.firstOrNull { stream ->
            stream.isAutoPlayable(debridEnabled, activeResolverProviderId)
        }
        if (bingeGroupOnly) {
            val readyStreams = preferredReadyStream?.let(::listOf).orEmpty()
            return StreamAutoPlayEvaluation(
                stream = preferredReadyStream,
                readyStreams = readyStreams,
                hasPendingDebridCandidate = preferredReadyStream == null &&
                    bingeGroupCandidates.any {
                        it.isPendingDebridAutoPlay(debridEnabled, activeResolverProviderId)
                    },
            )
        }
        if (mode == StreamAutoPlayMode.MANUAL) {
            return StreamAutoPlayEvaluation()
        }
        val preferredStream = if (preferBingeGroupInSelection && targetBingeGroup.isNotEmpty()) {
            candidateStreams.firstOrNull { stream ->
                stream.behaviorHints.bingeGroup == targetBingeGroup &&
                    stream.isAutoPlayable(debridEnabled, activeResolverProviderId)
            }
        } else {
            null
        }
        val matchingStreams = when (mode) {
            StreamAutoPlayMode.MANUAL -> emptyList()
            StreamAutoPlayMode.FIRST_STREAM -> candidateStreams
            StreamAutoPlayMode.UNIFIED_BEST -> rankStreams(
                streams = candidateStreams,
                preferredQuality = preferredQuality,
                preferredAudioLanguage = preferredAudioLanguage,
                failedStreamKeys = failedStreamKeys,
            )
            StreamAutoPlayMode.REGEX_MATCH -> {
                val pattern = regexPattern.trim()

                val userRegex = runCatching { Regex(pattern, RegexOption.IGNORE_CASE) }.getOrNull()
                    ?: return StreamAutoPlayEvaluation()

                val exclusionMatches = Regex("\\(\\?![^)]*?\\(([^)]+)\\)").findAll(pattern)

                val exclusionWords = exclusionMatches
                    .flatMap { match -> match.groupValues[1].split("|") }
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .toList()

                val excludeRegex = if (exclusionWords.isNotEmpty()) {
                    Regex(
                        "\\b(${exclusionWords.joinToString("|") { Regex.escape(it) }})\\b",
                        RegexOption.IGNORE_CASE,
                    )
                } else null

                candidateStreams.filter { stream ->
                    val url = stream.playableDirectUrl.orEmpty()

                    val searchableText = buildString {
                        append(stream.addonName).append(' ')
                        append(stream.name.orEmpty()).append(' ')
                        append(stream.streamLabel).append(' ')
                        append(stream.description.orEmpty()).append(' ')
                        append(url)
                    }

                    if (!userRegex.containsMatchIn(searchableText)) return@filter false

                    if (excludeRegex != null && excludeRegex.containsMatchIn(searchableText)) {
                        return@filter false
                    }

                    true
                }
            }
        }
        if (matchingStreams.isEmpty() && preferredStream == null) return StreamAutoPlayEvaluation()

        val readyStreams = buildList {
            preferredStream?.let(::add)
            matchingStreams
                .filter { it.isAutoPlayable(debridEnabled, activeResolverProviderId) }
                .filterNot { it == preferredStream }
                .forEach(::add)
        }
        val selected = readyStreams.firstOrNull()
        if (selected != null) {
            return StreamAutoPlayEvaluation(
                stream = selected,
                readyStreams = readyStreams,
            )
        }

        return StreamAutoPlayEvaluation(
            readyStreams = readyStreams,
            hasPendingDebridCandidate = matchingStreams.any {
                it.isPendingDebridAutoPlay(debridEnabled, activeResolverProviderId)
            },
        )
    }

    private fun StreamItem.isAutoPlayable(
        debridEnabled: Boolean,
        activeResolverProviderId: String?,
    ): Boolean =
        playableDirectUrl != null ||
            (
                AppFeaturePolicy.p2pEnabled &&
                    needsLocalDebridResolve &&
                    p2pInfoHash != null &&
                    !isPendingDebridAutoPlay(debridEnabled, activeResolverProviderId)
            ) ||
            (debridEnabled && isAddonDebridCandidate && isReadyDebridAutoPlay(activeResolverProviderId))

    private fun StreamItem.isReadyDebridAutoPlay(activeResolverProviderId: String?): Boolean =
        when {
            isDirectDebridStream -> clientResolve?.service.matchesResolver(activeResolverProviderId)
            isCachedDebridTorrentStream -> debridCacheStatus?.providerId.matchesResolver(activeResolverProviderId)
            else -> false
        }

    private fun StreamItem.isPendingDebridAutoPlay(
        debridEnabled: Boolean,
        activeResolverProviderId: String?,
    ): Boolean {
        if (!debridEnabled || !isInstalledAddonStream || !needsLocalDebridResolve) return false
        if (!debridCacheStatus?.providerId.matchesResolver(activeResolverProviderId)) return false
        val state = debridCacheStatus?.state
        return state == null || state == StreamDebridCacheState.CHECKING
    }

    fun rankStreams(
        streams: List<StreamItem>,
        preferredQuality: StreamQualityOption = StreamQualityOption.AUTO,
        preferredAudioLanguage: String? = null,
        failedStreamKeys: Set<String> = emptySet(),
    ): List<StreamItem> {
        if (streams.isEmpty()) return emptyList()
        return streams.sortedWith(
            compareByDescending<StreamItem> { scoreStream(it, preferredQuality, preferredAudioLanguage, failedStreamKeys) }
                .thenByDescending { it.fileSizeBytes ?: 0L }
                .thenByDescending { it.behaviorHints.bingeGroup != null }
        )
    }

    fun scoreStream(
        stream: StreamItem,
        preferredQuality: StreamQualityOption = StreamQualityOption.AUTO,
        preferredAudioLanguage: String? = null,
        failedStreamKeys: Set<String> = emptySet(),
    ): Long {
        val key = stream.streamKey()
        if (failedStreamKeys.contains(key) || (stream.playableDirectUrl != null && failedStreamKeys.contains(stream.playableDirectUrl))) {
            return -1_000_000L
        }

        var score = 10_000L

        // 1. Audio Language Match (Highest priority if user explicitly requested one)
        val prefLang = preferredAudioLanguage?.trim()
        if (!prefLang.isNullOrBlank() && !prefLang.equals("auto", ignoreCase = true)) {
            val detected = StreamAudioAggregator.detectAudioLanguages(stream)
            if (detected.any { it.equals(prefLang, ignoreCase = true) }) {
                score += 100_000L
            }
        }

        // 2. Quality Match
        val streamRes = StreamQualityAggregator.detectResolutionHeight(stream) ?: 1080
        val isDv = StreamQualityAggregator.isDolbyVision(stream)
        val isHdr = StreamQualityAggregator.isHdr(stream)

        if (preferredQuality.id != StreamQualityOption.AUTO.id) {
            when (preferredQuality.id) {
                StreamQualityOption.DOLBY_VISION_4K.id -> {
                    if (streamRes >= 2160 && isDv) score += 50_000L
                    else if (streamRes >= 2160 && isHdr) score += 30_000L
                    else if (streamRes >= 2160) score += 20_000L
                }
                StreamQualityOption.HDR_4K.id -> {
                    if (streamRes >= 2160 && isHdr) score += 50_000L
                    else if (streamRes >= 2160 && isDv) score += 40_000L
                    else if (streamRes >= 2160) score += 20_000L
                }
                else -> {
                    val targetRes = preferredQuality.targetResolution ?: 1080
                    if (streamRes == targetRes) {
                        score += 50_000L
                    } else {
                        val diff = kotlin.math.abs(streamRes - targetRes)
                        score += (30_000L - diff * 10L).coerceAtLeast(0L)
                    }
                }
            }
        } else {
            // Auto quality: Prefer 4K / 1080p best visual fidelity
            when {
                streamRes >= 2160 -> score += 35_000L
                streamRes >= 1080 -> score += 25_000L
                streamRes >= 720 -> score += 15_000L
                else -> score += 5_000L
            }
            if (isDv) score += 4_000L
            if (isHdr) score += 2_000L
        }

        // 3. Source Reliability & Protocol
        when {
            stream.isDirectDebridStream || stream.isCachedDebridTorrentStream -> score += 25_000L
            stream.playableDirectUrl != null -> score += 20_000L
            stream.isTorrentStream -> score += 5_000L
        }

        // 4. Codec Preference
        val text = "${stream.name} ${stream.title} ${stream.description} ${stream.behaviorHints.filename}".lowercase()
        when {
            "hevc" in text || "x265" in text || "h.265" in text || "h265" in text -> score += 3_000L
            "av1" in text -> score += 3_000L
            "x264" in text || "h.264" in text || "avc" in text -> score += 1_500L
        }

        // 5. File Size sanity check
        val size = stream.fileSizeBytes
        if (size != null && size > 0L) {
            val gigabytes = size / (1024.0 * 1024.0 * 1024.0)
            when {
                gigabytes in 1.0..35.0 -> score += 2_000L
                gigabytes > 70.0 -> score -= 3_000L
            }
        }

        return score
    }

    private fun String?.matchesResolver(activeResolverProviderId: String?): Boolean {
        val active = activeResolverProviderId?.trim().orEmpty()
        return active.isBlank() || this == null || equals(active, ignoreCase = true)
    }
}

val StreamItem.fileSizeBytes: Long?
    get() = clientResolve?.stream?.raw?.size
        ?: debridCacheStatus?.cachedSize
        ?: clientResolve?.stream?.raw?.folderSize

fun StreamItem.streamKey(): String =
    playableDirectUrl ?: url ?: infoHash ?: "${addonId}:${name.orEmpty()}:${description.orEmpty()}"

data class StreamAutoPlayEvaluation(
    val stream: StreamItem? = null,
    val readyStreams: List<StreamItem> = emptyList(),
    val hasPendingDebridCandidate: Boolean = false,
)

