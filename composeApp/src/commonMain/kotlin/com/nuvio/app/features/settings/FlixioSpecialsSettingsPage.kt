package com.nuvio.app.features.settings
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import com.nuvio.app.core.sync.ProfileSettingsSync
import com.nuvio.app.features.home.HomeCatalogSettingsRepository
import com.nuvio.app.core.ui.NuvioInputField
import com.nuvio.app.core.ui.NuvioPrimaryButton
import com.nuvio.app.core.ui.NuvioTokens
import com.nuvio.app.core.ui.NuvioToastController
import com.nuvio.app.core.ui.nuvio
import kotlinx.coroutines.launch

@Composable
internal fun rememberFlixioSpecialsState(): FlixioSpecialsSettings {
    FlixioSpecialsSettingsRepository.ensureLoaded()
    return FlixioSpecialsSettingsRepository.currentSettings.collectAsState().value
}

internal fun LazyListScope.flixioSpecialsSettingsContent(
    isTablet: Boolean,
    onOpenAppIconPicker: () -> Unit = {},
) {
    item {
        val settings = rememberFlixioSpecialsState()
        val clipboardManager = LocalClipboardManager.current
        var showPasteDialog by remember { mutableStateOf(false) }
        var showAppIconPicker by remember { mutableStateOf(false) }
        var pasteText by remember { mutableStateOf("") }
        var pasteError by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Section 1: Flixio Specials Exclusives
            SettingsSection(
                title = "Flixio Specials Exclusives",
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsSwitchRow(
                        title = "Disable Top-Left Corner Light Spreading",
                        description = "Turn off the ambient gradient glow spreading from the top-left corner on the home screen",
                        checked = settings.disableTopLeftLightSpreading,
                        isTablet = isTablet,
                        onCheckedChange = { FlixioSpecialsSettingsRepository.setDisableTopLeftLightSpreading(it) },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = "Disable Ambient Wallpaper",
                        description = "Turn off background poster wallpapers and use pure dark theme backdrop",
                        checked = settings.disableWallpaper,
                        isTablet = isTablet,
                        onCheckedChange = { FlixioSpecialsSettingsRepository.setDisableWallpaper(it) },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = "Flat Hero Style",
                        description = "Use the original flat Nuvio hero carousel layout instead of the 3D cover flow carousel",
                        checked = settings.disableHeroCarousel,
                        isTablet = isTablet,
                        onCheckedChange = { FlixioSpecialsSettingsRepository.setDisableHeroCarousel(it) },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = "OTT Platforms Row",
                        description = "Display streaming platform tiles (Netflix, Prime, Disney+, etc.) on the Home screen",
                        checked = settings.ottRowEnabled,
                        isTablet = isTablet,
                        onCheckedChange = { FlixioSpecialsSettingsRepository.setOttRowEnabled(it) },
                    )
                }
            }

            // Section 2: Enhanced App & Media Features
            SettingsSection(
                title = "Enhanced Media & App Features",
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsSwitchRow(
                        title = "Live TV Tab",
                        description = "Show Live TV channel guide and streaming tab in the navigation bar",
                        checked = settings.liveTvEnabled,
                        isTablet = isTablet,
                        onCheckedChange = { FlixioSpecialsSettingsRepository.setLiveTvEnabled(it) },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = "Pinned Stream Sources",
                        description = "Allow pinning preferred stream providers so their results appear first (long-press provider chip to pin)",
                        checked = settings.streamSourcePinningEnabled,
                        isTablet = isTablet,
                        onCheckedChange = { FlixioSpecialsSettingsRepository.setStreamSourcePinningEnabled(it) },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = "Background Stream Prefetch",
                        description = "Pre-fetch available stream sources in background when opening title details to accelerate playback launch",
                        checked = settings.backgroundStreamPrefetchEnabled,
                        isTablet = isTablet,
                        onCheckedChange = { FlixioSpecialsSettingsRepository.setBackgroundStreamPrefetchEnabled(it) },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = "Player Status Overlay",
                        description = "Show real-time device clock, battery percentage, Wi-Fi pill, and estimated finish time inside player controls",
                        checked = settings.playerStatusOverlayEnabled,
                        isTablet = isTablet,
                        onCheckedChange = { FlixioSpecialsSettingsRepository.setPlayerStatusOverlayEnabled(it) },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = "Status Bar Visible",
                        description = "Keep system status bar visible while navigating the app",
                        checked = settings.statusBarVisible,
                        isTablet = isTablet,
                        onCheckedChange = { FlixioSpecialsSettingsRepository.setStatusBarVisible(it) },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = "Memory-Safe ExoPlayer Buffer",
                        description = "Reduce video pre-buffer size (32MB instead of 100MB) to conserve RAM on lower-end Android devices",
                        checked = settings.memorySafeExoPlayerBufferEnabled,
                        isTablet = isTablet,
                        onCheckedChange = { FlixioSpecialsSettingsRepository.setMemorySafeExoPlayerBufferEnabled(it) },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsSwitchRow(
                        title = "Hide Unreleased Content",
                        description = "Filter out unreleased films and shows that lack air dates or streams",
                        checked = settings.hideUnreleasedContent,
                        isTablet = isTablet,
                        onCheckedChange = { FlixioSpecialsSettingsRepository.setHideUnreleasedContent(it) },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = "Custom App Icon",
                        description = "Customize Flixio app launcher icon on your home screen",
                        icon = Icons.Rounded.Palette,
                        isTablet = isTablet,
                        onClick = { showAppIconPicker = true },
                    )
                }
            }

            // Section 2.5: Catalog & Shelf Presentation
            val homeCatalogSettings by remember {
                HomeCatalogSettingsRepository.snapshot()
                HomeCatalogSettingsRepository.uiState
            }.collectAsStateWithLifecycle()

            SettingsSection(
                title = "Catalog & Shelf Presentation",
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    FlixioChoiceRow(
                        title = "Home Catalog Shelf Rows",
                        description = "Display 1, 2, 3, or 4 stacked poster rows per catalog section on Home",
                        selected = homeCatalogSettings.homeCatalogRowCount,
                        options = listOf(1, 2, 3, 4).map { count ->
                            FlixioChoiceOption(count, if (count == 1) "1 Row" else "$count Rows")
                        },
                        isTablet = isTablet,
                        onSelected = {
                            HomeCatalogSettingsRepository.setHomeCatalogRowCount(it)
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    FlixioChoiceRow(
                        title = "Catalog Grid Columns",
                        description = "Number of poster columns displayed in catalog views and folders",
                        selected = homeCatalogSettings.catalogColumnCount,
                        options = listOf(2, 3, 4, 5, 6).map { count ->
                            FlixioChoiceOption(count, "$count Cols")
                        },
                        isTablet = isTablet,
                        onSelected = {
                            HomeCatalogSettingsRepository.setCatalogColumnCount(it)
                        },
                    )
                }
            }

            // Section 3: Info-Rich Hero Suite
            SettingsSection(
                title = "Info-Rich Hero Suite",
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsSwitchRow(
                        title = "Info-Rich Hero Mode",
                        description = "Enable detailed information, richer tags, overview card, and interactive previews on the hero carousel",
                        checked = settings.infoRichHeroEnabled,
                        isTablet = isTablet,
                        onCheckedChange = { FlixioSpecialsSettingsRepository.setInfoRichHeroEnabled(it) },
                    )
                    if (settings.infoRichHeroEnabled) {
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsSwitchRow(
                            title = "Hero Video Preview",
                            description = "Play backdrop trailer video clips directly inside the hero showcase",
                            checked = settings.streamingShowcaseVideoPreviewEnabled,
                            isTablet = isTablet,
                            onCheckedChange = { FlixioSpecialsSettingsRepository.setStreamingShowcaseVideoPreviewEnabled(it) },
                        )
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsSwitchRow(
                            title = "Hero Preview Starts With Sound",
                            description = "Unmute hero trailer playback automatically on startup",
                            checked = settings.streamingShowcaseVideoPreviewSoundEnabled,
                            isTablet = isTablet,
                            onCheckedChange = { FlixioSpecialsSettingsRepository.setStreamingShowcaseVideoPreviewSoundEnabled(it) },
                        )
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsSwitchRow(
                            title = "Compact Hero Metadata",
                            description = "Keep genre, year and runtime tightly formatted for maximum artwork visibility",
                            checked = settings.compactHeroMetadata,
                            isTablet = isTablet,
                            onCheckedChange = { FlixioSpecialsSettingsRepository.setCompactHeroMetadata(it) },
                        )
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsSwitchRow(
                            title = "Hero Ratings Badge",
                            description = "Show IMDb and TMDB rating scores on hero cards",
                            checked = settings.showHeroRatings,
                            isTablet = isTablet,
                            onCheckedChange = { FlixioSpecialsSettingsRepository.setShowHeroRatings(it) },
                        )
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsSwitchRow(
                            title = "Hero Overview Glass Card",
                            description = "Show a glassmorphic synopsis card under the hero metadata",
                            checked = settings.showHeroOverview,
                            isTablet = isTablet,
                            onCheckedChange = { FlixioSpecialsSettingsRepository.setShowHeroOverview(it) },
                        )
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsSwitchRow(
                            title = "Hero Pull-to-Refresh Haptics",
                            description = "Trigger a tactile haptic pulse when pull-to-refresh snaps",
                            checked = settings.heroRefreshHapticsEnabled,
                            isTablet = isTablet,
                            onCheckedChange = { FlixioSpecialsSettingsRepository.setHeroRefreshHapticsEnabled(it) },
                        )
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsSwitchRow(
                            title = "Auto-Scroll Hero Carousel",
                            description = "Automatically cycle through featured hero titles every few seconds",
                            checked = settings.autoScrollHeroCarousel,
                            isTablet = isTablet,
                            onCheckedChange = { FlixioSpecialsSettingsRepository.setAutoScrollHeroCarousel(it) },
                        )
                        SettingsGroupDivider(isTablet = isTablet)
                        SettingsSwitchRow(
                            title = "Hero Motion Preview",
                            description = "Subtle cinematic motion parallax effect during carousel scrolling",
                            checked = settings.heroMotionPreview,
                            isTablet = isTablet,
                            onCheckedChange = { FlixioSpecialsSettingsRepository.setHeroMotionPreview(it) },
                        )
                    }
                }
            }

            // Section 4: Backup & Restore
            SettingsSection(
                title = "Backup & Restore",
                isTablet = isTablet,
            ) {
                SettingsGroup(isTablet = isTablet) {
                    SettingsNavigationRow(
                        title = "Download Backup File",
                        description = "Save your complete Flixio configuration, profile settings, and addons to a file",
                        icon = Icons.Rounded.Download,
                        isTablet = isTablet,
                        onClick = {
                            val backup = ProfileSettingsSync.exportBackupJson()
                            FlixioBackupFileBridge.exportBackup("flixio_backup.json", backup) { res ->
                                res.onSuccess { NuvioToastController.show("Backup file saved successfully") }
                                   .onFailure { NuvioToastController.show("Failed to save backup: ${it.message}") }
                            }
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = "Import Backup File",
                        description = "Select a saved Flixio backup JSON file to restore all your settings",
                        icon = Icons.Rounded.FileUpload,
                        isTablet = isTablet,
                        onClick = {
                            FlixioBackupFileBridge.importBackup { res ->
                                res.onSuccess { payload ->
                                    val result = ProfileSettingsSync.importBackupJson(payload)
                                    if (result.isSuccess) {
                                        NuvioToastController.show("Backup restored successfully!")
                                    } else {
                                        NuvioToastController.show("Invalid backup file format")
                                    }
                                }.onFailure { NuvioToastController.show("Failed to read backup: ${it.message}") }
                            }
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = "Copy Backup JSON",
                        description = "Copy complete JSON configuration directly to your clipboard",
                        icon = Icons.Rounded.ContentCopy,
                        isTablet = isTablet,
                        onClick = {
                            val json = ProfileSettingsSync.exportBackupJson()
                            clipboardManager.setText(AnnotatedString(json))
                            NuvioToastController.show("Backup JSON copied to clipboard!")
                        },
                    )
                    SettingsGroupDivider(isTablet = isTablet)
                    SettingsNavigationRow(
                        title = "Paste Backup JSON",
                        description = "Paste configuration JSON directly from your clipboard to restore",
                        icon = Icons.Rounded.ContentPaste,
                        isTablet = isTablet,
                        onClick = {
                            pasteText = ""
                            pasteError = null
                            showPasteDialog = true
                        },
                    )
                }
            }
        }

        // Paste Backup Dialog
        if (showPasteDialog) {
            PasteBackupDialog(
                value = pasteText,
                errorMessage = pasteError,
                onValueChange = {
                    pasteText = it
                    pasteError = null
                },
                onConfirm = {
                    val result = ProfileSettingsSync.importBackupJson(pasteText)
                    if (result.isSuccess) {
                        NuvioToastController.show("Settings restored from JSON!")
                        showPasteDialog = false
                    } else {
                        pasteError = "Invalid backup JSON payload. Please verify format."
                    }
                },
                onDismiss = { showPasteDialog = false },
            )
        }

        if (showAppIconPicker) {
            val appIconState by AppIconRepository.state.collectAsStateWithLifecycle()
            AppIconPicker(
                isTablet = isTablet,
                state = appIconState,
                onSelected = { icon ->
                    scope.launch { AppIconRepository.select(icon) }
                },
                onDismiss = { showAppIconPicker = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasteBackupDialog(
    value: String,
    errorMessage: String?,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(16.dp),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = tokens.colors.surfaceElevated,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Paste Backup JSON",
                    style = MaterialTheme.typography.titleLarge,
                    color = tokens.colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Paste your exported Flixio Specials JSON configuration below to restore all settings and pinned sources.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.colors.textSecondary,
                )
                androidx.compose.material3.OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            text = "{\n  \"liveTvEnabled\": true, ...\n}",
                            color = tokens.colors.textMuted,
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                )
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = tokens.colors.textSecondary,
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                    NuvioPrimaryButton(
                        text = "Restore",
                        onClick = onConfirm,
                        enabled = value.isNotBlank(),
                    )
                }
            }
        }
    }
}

data class FlixioChoiceOption<T>(
    val value: T,
    val label: String,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> FlixioChoiceRow(
    title: String,
    description: String,
    selected: T,
    options: List<FlixioChoiceOption<T>>,
    isTablet: Boolean,
    onSelected: (T) -> Unit,
) {
    val tokens = MaterialTheme.nuvio
    val horizontalPadding = if (isTablet) 20.dp else 16.dp
    val verticalPadding = if (isTablet) 16.dp else 14.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = tokens.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.colors.textMuted,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                val isSelected = option.value == selected
                Surface(
                    modifier = Modifier.clickable { onSelected(option.value) },
                    color = if (isSelected) {
                        tokens.colors.accent
                    } else {
                        tokens.colors.surfaceCard.copy(alpha = 0.72f)
                    },
                    contentColor = if (isSelected) {
                        tokens.colors.onAccent
                    } else {
                        tokens.colors.textPrimary
                    },
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(
                        tokens.borders.hairline,
                        if (isSelected) {
                            tokens.colors.accent.copy(alpha = 0.86f)
                        } else {
                            tokens.colors.borderSubtle
                        },
                    ),
                ) {
                    Text(
                        text = option.label,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) tokens.colors.onAccent else tokens.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
