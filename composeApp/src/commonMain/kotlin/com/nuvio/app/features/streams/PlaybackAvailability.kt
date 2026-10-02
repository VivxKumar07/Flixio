package com.nuvio.app.features.streams

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.build.AppFeaturePolicy
import com.nuvio.app.features.addons.AddonManifest
import com.nuvio.app.features.addons.AddonRepository
import com.nuvio.app.features.addons.ManagedAddon
import com.nuvio.app.features.details.MetaDetailsRepository
import com.nuvio.app.features.downloads.DownloadsRepository
import com.nuvio.app.features.plugins.PluginRepository
import com.nuvio.app.features.plugins.PluginsUiState

import com.nuvio.app.features.cloudstream.CloudStreamRepository

internal fun AddonManifest.supportsStream(type: String, videoId: String): Boolean =
    resources.any { resource ->
        resource.name == "stream" &&
            resource.types.contains(type) &&
            (resource.idPrefixes.isEmpty() || resource.idPrefixes.any { videoId.startsWith(it) })
    }

internal fun hasCompatiblePlaybackSource(
    addons: List<ManagedAddon>,
    plugins: PluginsUiState,
    type: String,
    videoId: String,
): Boolean {
    val hasAddon = addons.any { it.enabled && it.manifest?.supportsStream(type, videoId) == true }
    val hasPluginScraper = plugins.pluginsEnabled && plugins.scrapers.any { it.enabled && it.supportsType(type) }
    val hasCloudStream = AppFeaturePolicy.pluginsEnabled && run {
        CloudStreamRepository.initialize()
        CloudStreamRepository.uiState.value.plugins.any { it.isRunnable }
    }
    return hasAddon || hasPluginScraper || hasCloudStream
}

internal class PlaybackAvailability(
    private val addons: List<ManagedAddon>,
    private val plugins: PluginsUiState,
    private val hasCloudStreamPlugins: Boolean = false,
) {
    fun canStream(type: String, videoId: String): Boolean =
        hasCompatiblePlaybackSource(addons, plugins, type, videoId) ||
            hasCloudStreamPlugins ||
            MetaDetailsRepository.findEmbeddedStreams(videoId).isNotEmpty()

    fun canPlay(
        type: String,
        videoId: String,
        parentMetaId: String,
        seasonNumber: Int? = null,
        episodeNumber: Int? = null,
    ): Boolean = canStream(type, videoId) || DownloadsRepository.findPlayableDownload(
        parentMetaId = parentMetaId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        videoId = videoId,
    ) != null

    companion object {
        fun current(): PlaybackAvailability = PlaybackAvailability(
            addons = AddonRepository.uiState.value.addons,
            plugins = if (AppFeaturePolicy.pluginsEnabled) {
                PluginRepository.uiState.value
            } else {
                PluginsUiState(pluginsEnabled = false)
            },
            hasCloudStreamPlugins = if (AppFeaturePolicy.pluginsEnabled) {
                CloudStreamRepository.initialize()
                CloudStreamRepository.uiState.value.plugins.any { it.isRunnable }
            } else false,
        )
    }
}

@Composable
internal fun rememberPlaybackAvailability(): PlaybackAvailability {
    val addons by remember {
        AddonRepository.initialize()
        AddonRepository.uiState
    }.collectAsStateWithLifecycle()
    val plugins = if (AppFeaturePolicy.pluginsEnabled) {
        val state by remember {
            PluginRepository.initialize()
            PluginRepository.uiState
        }.collectAsStateWithLifecycle()
        state
    } else {
        PluginsUiState(pluginsEnabled = false)
    }
    val cloudStreamPlugins = if (AppFeaturePolicy.pluginsEnabled) {
        val csState by remember {
            CloudStreamRepository.initialize()
            CloudStreamRepository.uiState
        }.collectAsStateWithLifecycle()
        csState.plugins.any { it.isRunnable }
    } else false
    val downloads by remember {
        DownloadsRepository.ensureLoaded()
        DownloadsRepository.uiState
    }.collectAsStateWithLifecycle()
    return remember(addons, plugins, cloudStreamPlugins, downloads) {
        PlaybackAvailability(addons.addons, plugins, cloudStreamPlugins)
    }
}
