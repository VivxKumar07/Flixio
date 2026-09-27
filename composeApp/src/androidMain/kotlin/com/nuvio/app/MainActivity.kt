package com.nuvio.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import com.nuvio.app.core.auth.AuthStorage
import com.nuvio.app.core.network.ServerConfigurationStorage
import com.nuvio.app.core.network.SupabaseProvider
import io.github.jan.supabase.auth.handleDeeplinks
import com.nuvio.app.core.diagnostics.SentryInitializer
import com.nuvio.app.core.deeplink.handleAppUrl
import com.nuvio.app.core.storage.PlatformLocalAccountDataCleaner
import com.nuvio.app.core.sync.SyncClientIdentityStorage
import com.nuvio.app.features.addons.AddonHttpClientProvider
import com.nuvio.app.features.addons.AddonStorage
import com.nuvio.app.features.collection.CollectionMobileSettingsStorage
import com.nuvio.app.features.collection.CollectionStorage
import com.nuvio.app.features.debrid.DebridSettingsStorage
import com.nuvio.app.features.downloads.DownloadsLiveStatusPlatform
import com.nuvio.app.features.downloads.DownloadsPlatformDownloader
import com.nuvio.app.features.downloads.DownloadsStorage
import com.nuvio.app.features.library.LibraryDisplaySettingsStorage
import com.nuvio.app.features.membership.MemberAssetStorage
import com.nuvio.app.features.library.LibraryStorage
import com.nuvio.app.features.details.MetaScreenSettingsStorage
import com.nuvio.app.features.home.HomeCatalogSettingsStorage
import com.nuvio.app.features.mdblist.MdbListSettingsStorage
import com.nuvio.app.features.notifications.EpisodeReleaseNotificationPlatform
import com.nuvio.app.features.notifications.EpisodeReleaseNotificationsStorage
import com.nuvio.app.features.player.PlayerSettingsStorage
import com.nuvio.app.features.player.PlayerTrackPreferenceStorage
import com.nuvio.app.features.player.ExternalPlayerPlatform
import com.nuvio.app.features.player.SubtitleFileCache
import com.nuvio.app.features.player.PlayerPictureInPictureManager
import com.nuvio.app.features.player.PipRemoteActionReceiver
import com.nuvio.app.features.p2p.P2pSettingsStorage
import com.nuvio.app.features.p2p.P2pStreamingEngine
import com.nuvio.app.features.cloudstream.CloudStreamPlatformStorage
import com.nuvio.app.features.plugins.PluginStorage
import com.nuvio.app.features.profiles.AvatarStorage
import com.nuvio.app.features.profiles.ProfilePinCacheStorage
import com.nuvio.app.features.profiles.ProfileStorage
import com.nuvio.app.features.details.SeasonViewModeStorage
import com.nuvio.app.features.search.DiscoverSelectionStorage
import com.nuvio.app.features.search.SearchHistoryStorage
import com.nuvio.app.features.settings.SentrySettingsStorage
import com.nuvio.app.features.settings.AppIconPlatform
import com.nuvio.app.features.settings.ThemeSettingsStorage
import com.nuvio.app.features.trakt.TraktAuthStorage
import com.nuvio.app.features.trakt.TraktCommentsStorage
import com.nuvio.app.features.trakt.TraktLibraryStorage
import com.nuvio.app.features.trakt.TraktSettingsStorage
import com.nuvio.app.features.simkl.SimklAuthStorage
import com.nuvio.app.features.simkl.SimklSyncStorage
import com.nuvio.app.features.tmdb.TmdbSettingsStorage
import com.nuvio.app.features.updater.AndroidAppUpdaterPlatform
import com.nuvio.app.core.ui.CardDepthStyleStorage
import com.nuvio.app.core.ui.PosterCardStyleStorage
import com.nuvio.app.features.watched.WatchedStorage
import com.nuvio.app.features.streams.StreamLinkCacheStorage
import com.nuvio.app.features.streams.StreamBadgeSettingsStorage
import com.nuvio.app.features.streams.BingeGroupCacheStorage
import com.nuvio.app.features.watchprogress.ContinueWatchingEnrichmentStorage
import com.nuvio.app.features.watchprogress.ContinueWatchingPreferencesStorage
import com.nuvio.app.features.watchprogress.ResumePromptStorage
import com.nuvio.app.features.watchprogress.WatchProgressStorage

open class MainActivity : AppCompatActivity() {
    private var pipRemoteActionReceiver: PipRemoteActionReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash first so the themed starting window (logo) is shown from the
        // very first moment, before any other work runs.
        val firstFrameReady = AtomicBoolean(false)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !firstFrameReady.get() }

        // Essential storages needed before the first composition: theme/locale plus the
        // auth/profile caches. ServerConfigurationStorage must be ready here because
        // SupabaseProvider.client (created by AuthRepository.initialize() during
        // composition) reads ServerConfigurationRepository on first access.
        ThemeSettingsStorage.initialize(applicationContext)
        AuthStorage.initialize(applicationContext)
        ProfileStorage.initialize(applicationContext)
        AvatarStorage.initialize(applicationContext)
        ServerConfigurationStorage.initialize(applicationContext)

        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(
                scrim = 0xFF020404.toInt(),
            ),
        )
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(R.color.nuvio_background)

        pipRemoteActionReceiver = PipRemoteActionReceiver.register(this)
        handleIncomingAppIntent(intent)

        // Render Compose splash UI immediately on the very first frame!
        setContent {
            App()
            // Signal that the first frame has actually been drawn — splash can now dismiss
            androidx.compose.runtime.LaunchedEffect(Unit) {
                androidx.compose.runtime.withFrameNanos { }
                firstFrameReady.set(true)
            }
        }

        // Asynchronously initialize secondary storages, background tasks and SDKs on Dispatchers.IO
        // after the initial frame is rendered, ensuring the UI thread remains completely fluid.
        // Split into parallel lanes so no single feature's storage waits on the whole chain.
        lifecycleScope.launch(Dispatchers.IO) {
            while (!firstFrameReady.get()) {
                kotlinx.coroutines.delay(40)
            }
            fun safeInit(name: String, block: () -> Unit) {
                runCatching(block).onFailure { error ->
                    android.util.Log.w("AppInit", "init failed: $name", error)
                }
            }
            kotlinx.coroutines.coroutineScope {
                // Lane 1: diagnostics + addons/plugins
                launch {
                    safeInit("SyncClientIdentityStorage") { SyncClientIdentityStorage.initialize(applicationContext) }
                    safeInit("SentrySettingsStorage") { SentrySettingsStorage.initialize(applicationContext) }
                    safeInit("SentryInitializer") { SentryInitializer.start(application) }
                    safeInit("AddonHttpClientProvider") { AddonHttpClientProvider.initialize(applicationContext) }
                    safeInit("AddonStorage") { AddonStorage.initialize(applicationContext) }
                    safeInit("PluginStorage") { PluginStorage.initialize(applicationContext) }
                    safeInit("CloudStreamPlatformStorage") { CloudStreamPlatformStorage.initialize(applicationContext) }
                }
                // Lane 2: library / watch history / collections / downloads
                launch {
                    safeInit("LibraryStorage") { LibraryStorage.initialize(applicationContext) }
                    safeInit("WatchedStorage") { WatchedStorage.initialize(applicationContext) }
                    safeInit("LibraryDisplaySettingsStorage") { LibraryDisplaySettingsStorage.initialize(applicationContext) }
                    safeInit("WatchProgressStorage") { WatchProgressStorage.initialize(applicationContext) }
                    safeInit("ContinueWatchingPreferencesStorage") { ContinueWatchingPreferencesStorage.initialize(applicationContext) }
                    safeInit("ResumePromptStorage") { ResumePromptStorage.initialize(applicationContext) }
                    safeInit("ContinueWatchingEnrichmentStorage") { ContinueWatchingEnrichmentStorage.initialize(applicationContext) }
                    safeInit("CollectionMobileSettingsStorage") { CollectionMobileSettingsStorage.initialize(applicationContext) }
                    safeInit("CollectionStorage") { CollectionStorage.initialize(applicationContext) }
                    safeInit("DownloadsStorage") { DownloadsStorage.initialize(applicationContext) }
                    safeInit("DownloadsPlatformDownloader") { DownloadsPlatformDownloader.initialize(applicationContext) }
                    safeInit("DownloadsLiveStatusPlatform") { DownloadsLiveStatusPlatform.initialize(applicationContext) }
                    safeInit("MemberAssetStorage") { MemberAssetStorage.initialize(applicationContext) }
                }
                // Lane 3: player / streaming / notifications
                launch {
                    safeInit("PlayerSettingsStorage") { PlayerSettingsStorage.initialize(applicationContext) }
                    safeInit("PlayerTrackPreferenceStorage") { PlayerTrackPreferenceStorage.initialize(applicationContext) }
                    safeInit("P2pSettingsStorage") { P2pSettingsStorage.initialize(applicationContext) }
                    safeInit("P2pStreamingEngine") { P2pStreamingEngine.initialize(applicationContext) }
                    safeInit("ExternalPlayerPlatform") { ExternalPlayerPlatform.initialize(applicationContext) }
                    safeInit("SubtitleFileCache") { SubtitleFileCache.initialize(applicationContext) }
                    safeInit("StreamLinkCacheStorage") { StreamLinkCacheStorage.initialize(applicationContext) }
                    safeInit("StreamBadgeSettingsStorage") { StreamBadgeSettingsStorage.initialize(applicationContext) }
                    safeInit("BingeGroupCacheStorage") { BingeGroupCacheStorage.initialize(applicationContext) }
                    safeInit("EpisodeReleaseNotificationsStorage") { EpisodeReleaseNotificationsStorage.initialize(applicationContext) }
                    safeInit("EpisodeReleaseNotificationPlatform") { EpisodeReleaseNotificationPlatform.initialize(applicationContext) }
                    lifecycleScope.launch(Dispatchers.Main) {
                        EpisodeReleaseNotificationPlatform.bindActivity(this@MainActivity)
                    }
                }
                // Lane 4: metadata / personalization / integrations / updater
                launch {
                    safeInit("AppIconPlatform") { AppIconPlatform.initialize(applicationContext) }
                    safeInit("MetaScreenSettingsStorage") { MetaScreenSettingsStorage.initialize(applicationContext) }
                    safeInit("HomeCatalogSettingsStorage") { HomeCatalogSettingsStorage.initialize(applicationContext) }
                    safeInit("ProfilePinCacheStorage") { ProfilePinCacheStorage.initialize(applicationContext) }
                    safeInit("DiscoverSelectionStorage") { DiscoverSelectionStorage.initialize(applicationContext) }
                    safeInit("SearchHistoryStorage") { SearchHistoryStorage.initialize(applicationContext) }
                    safeInit("SeasonViewModeStorage") { SeasonViewModeStorage.initialize(applicationContext) }
                    safeInit("PosterCardStyleStorage") { PosterCardStyleStorage.initialize(applicationContext) }
                    safeInit("CardDepthStyleStorage") { CardDepthStyleStorage.initialize(applicationContext) }
                    safeInit("DebridSettingsStorage") { DebridSettingsStorage.initialize(applicationContext) }
                    safeInit("TmdbSettingsStorage") { TmdbSettingsStorage.initialize(applicationContext) }
                    safeInit("MdbListSettingsStorage") { MdbListSettingsStorage.initialize(applicationContext) }
                    safeInit("TraktAuthStorage") { TraktAuthStorage.initialize(applicationContext) }
                    safeInit("TraktCommentsStorage") { TraktCommentsStorage.initialize(applicationContext) }
                    safeInit("TraktLibraryStorage") { TraktLibraryStorage.initialize(applicationContext) }
                    safeInit("TraktSettingsStorage") { TraktSettingsStorage.initialize(applicationContext) }
                    safeInit("SimklAuthStorage") { SimklAuthStorage.initialize(applicationContext) }
                    safeInit("SimklSyncStorage") { SimklSyncStorage.initialize(applicationContext) }
                    safeInit("AndroidAppUpdaterPlatform") { AndroidAppUpdaterPlatform.initialize(applicationContext) }
                    safeInit("PlatformLocalAccountDataCleaner") { PlatformLocalAccountDataCleaner.initialize(applicationContext) }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingAppIntent(intent)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        PlayerPictureInPictureManager.onUserLeaveHint(this)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        PlayerPictureInPictureManager.onPictureInPictureModeChanged(this, isInPictureInPictureMode)
    }

    override fun onDestroy() {
        EpisodeReleaseNotificationPlatform.unbindActivity(this)
        val receiver = pipRemoteActionReceiver
        if (receiver != null) {
            runCatching { unregisterReceiver(receiver) }
            pipRemoteActionReceiver = null
        }
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        if (EpisodeReleaseNotificationPlatform.handlePermissionRequestResult(requestCode, grantResults)) {
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun handleIncomingAppIntent(intent: Intent?) {
        if (intent == null) return
        val appUrl = intent.dataString?.trim().orEmpty()
        if (appUrl.isBlank()) return
        SupabaseProvider.client.handleDeeplinks(intent)
        handleAppUrl(appUrl)
    }
}
