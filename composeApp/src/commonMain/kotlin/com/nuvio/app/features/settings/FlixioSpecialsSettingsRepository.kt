package com.nuvio.app.features.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
internal data class FlixioSpecialsSettings(
    // Flixio-exclusive toggles
    val disableTopLeftLightSpreading: Boolean = false,
    val disableWallpaper: Boolean = false,
    val disableHeroCarousel: Boolean = false,
    val ottRowEnabled: Boolean = true,

    // Nuvio Mobile Enhanced features
    val liveTvEnabled: Boolean = true,
    val streamSourcePinningEnabled: Boolean = false,
    val backgroundStreamPrefetchEnabled: Boolean = false,
    val playerStatusOverlayEnabled: Boolean = false,
    val statusBarVisible: Boolean = true,
    val memorySafeExoPlayerBufferEnabled: Boolean = false,
    val hideUnreleasedContent: Boolean = false,

    // Info-Rich Hero Suite
    val infoRichHeroEnabled: Boolean = false,
    val streamingShowcaseVideoPreviewEnabled: Boolean = true,
    val streamingShowcaseVideoPreviewSoundEnabled: Boolean = false,
    val compactHeroMetadata: Boolean = true,
    val showHeroRatings: Boolean = true,
    val showHeroOverview: Boolean = false,
    val heroRefreshHapticsEnabled: Boolean = true,
    val autoScrollHeroCarousel: Boolean = false,
    val heroMotionPreview: Boolean = true,

    // App Icon
    val selectedAppIconId: String = "original",

    // Pinned stream sources
    val pinnedStreamSources: List<String> = emptyList(),
)

internal object FlixioSpecialsSettingsRepository {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private var stored = FlixioSpecialsSettings()
    private var loaded = false

    private val _settings = MutableStateFlow(stored)
    val settings: StateFlow<FlixioSpecialsSettings> = _settings.asStateFlow()
    val currentSettings: StateFlow<FlixioSpecialsSettings> get() = settings

    fun ensureLoaded() {
        if (loaded) return
        loaded = true
        val payload = FlixioSpecialsSettingsStorage.loadPayload()
        if (!payload.isNullOrBlank()) {
            runCatching {
                stored = json.decodeFromString<FlixioSpecialsSettings>(payload)
            }
        }
        _settings.value = stored
    }

    fun snapshot(): FlixioSpecialsSettings {
        ensureLoaded()
        return _settings.value
    }

    private fun update(transform: FlixioSpecialsSettings.() -> FlixioSpecialsSettings) {
        ensureLoaded()
        val next = stored.transform()
        if (next == stored) return
        stored = next
        _settings.value = next
        runCatching {
            FlixioSpecialsSettingsStorage.savePayload(json.encodeToString(next))
        }
    }

    fun setDisableTopLeftLightSpreading(disabled: Boolean) = update { copy(disableTopLeftLightSpreading = disabled) }
    fun setDisableWallpaper(disabled: Boolean) = update { copy(disableWallpaper = disabled) }
    fun setDisableHeroCarousel(disabled: Boolean) = update { copy(disableHeroCarousel = disabled) }
    fun setOttRowEnabled(enabled: Boolean) = update { copy(ottRowEnabled = enabled) }

    fun setLiveTvEnabled(enabled: Boolean) = update { copy(liveTvEnabled = enabled) }
    fun setStreamSourcePinningEnabled(enabled: Boolean) = update { copy(streamSourcePinningEnabled = enabled) }
    fun setBackgroundStreamPrefetchEnabled(enabled: Boolean) = update { copy(backgroundStreamPrefetchEnabled = enabled) }
    fun setPlayerStatusOverlayEnabled(enabled: Boolean) = update { copy(playerStatusOverlayEnabled = enabled) }
    fun setStatusBarVisible(visible: Boolean) = update { copy(statusBarVisible = visible) }
    fun setMemorySafeExoPlayerBufferEnabled(enabled: Boolean) = update { copy(memorySafeExoPlayerBufferEnabled = enabled) }
    fun setHideUnreleasedContent(enabled: Boolean) = update { copy(hideUnreleasedContent = enabled) }

    fun setInfoRichHeroEnabled(enabled: Boolean) = update { copy(infoRichHeroEnabled = enabled) }
    fun setStreamingShowcaseVideoPreviewEnabled(enabled: Boolean) = update { copy(streamingShowcaseVideoPreviewEnabled = enabled) }
    fun setStreamingShowcaseVideoPreviewSoundEnabled(enabled: Boolean) = update { copy(streamingShowcaseVideoPreviewSoundEnabled = enabled) }
    fun setCompactHeroMetadata(compact: Boolean) = update { copy(compactHeroMetadata = compact) }
    fun setShowHeroRatings(show: Boolean) = update { copy(showHeroRatings = show) }
    fun setShowHeroOverview(show: Boolean) = update { copy(showHeroOverview = show) }
    fun setHeroRefreshHapticsEnabled(enabled: Boolean) = update { copy(heroRefreshHapticsEnabled = enabled) }
    fun setAutoScrollHeroCarousel(enabled: Boolean) = update { copy(autoScrollHeroCarousel = enabled) }
    fun setHeroMotionPreview(enabled: Boolean) = update { copy(heroMotionPreview = enabled) }
    fun setSelectedAppIconId(iconId: String) = update { copy(selectedAppIconId = iconId) }

    fun pinSource(sourceId: String) {
        val current = snapshot()
        if (sourceId in current.pinnedStreamSources) return
        update { copy(pinnedStreamSources = pinnedStreamSources + sourceId) }
    }

    fun unpinSource(sourceId: String) {
        update { copy(pinnedStreamSources = pinnedStreamSources.filterNot { it == sourceId }) }
    }

    fun togglePinSource(sourceId: String) {
        val current = snapshot()
        if (sourceId in current.pinnedStreamSources) {
            unpinSource(sourceId)
        } else {
            pinSource(sourceId)
        }
    }

    fun exportBackupJson(): String {
        ensureLoaded()
        return json.encodeToString(stored)
    }

    fun importBackupJson(raw: String): Boolean {
        return runCatching {
            val parsed = json.decodeFromString<FlixioSpecialsSettings>(raw.trim())
            stored = parsed
            _settings.value = parsed
            FlixioSpecialsSettingsStorage.savePayload(json.encodeToString(parsed))
            true
        }.getOrDefault(false)
    }
}
