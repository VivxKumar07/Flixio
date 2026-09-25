package com.nuvio.app.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.build.AppVersionConfig
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.about_licenses_attributions_subtitle
import nuvio.composeapp.generated.resources.compose_about_made_with
import nuvio.composeapp.generated.resources.compose_about_version_format
import nuvio.composeapp.generated.resources.compose_settings_page_account
import nuvio.composeapp.generated.resources.compose_settings_page_advanced
import nuvio.composeapp.generated.resources.compose_settings_page_appearance
import nuvio.composeapp.generated.resources.compose_settings_page_content_discovery
import nuvio.composeapp.generated.resources.compose_settings_page_integrations
import nuvio.composeapp.generated.resources.compose_settings_page_licenses_attributions
import nuvio.composeapp.generated.resources.compose_settings_page_notifications
import nuvio.composeapp.generated.resources.compose_settings_page_playback
import nuvio.composeapp.generated.resources.compose_settings_root_account_description
import nuvio.composeapp.generated.resources.compose_settings_root_advanced_description
import nuvio.composeapp.generated.resources.compose_settings_root_appearance_description
import nuvio.composeapp.generated.resources.compose_settings_root_check_updates_description
import nuvio.composeapp.generated.resources.compose_settings_root_check_updates_title
import nuvio.composeapp.generated.resources.compose_settings_root_content_discovery_description
import nuvio.composeapp.generated.resources.compose_settings_root_integrations_description
import nuvio.composeapp.generated.resources.compose_settings_root_notifications_description
import nuvio.composeapp.generated.resources.compose_settings_root_switch_profile_description
import nuvio.composeapp.generated.resources.settings_playback_subtitle
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.settingsRootContent(
    isTablet: Boolean,
    onPlaybackClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onAdvancedClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onContentDiscoveryClick: () -> Unit,
    onIntegrationsClick: () -> Unit,
    onTrackingClick: () -> Unit,
    onSupportersContributorsClick: () -> Unit,
    onLicensesAttributionsClick: () -> Unit,
    onCheckForUpdatesClick: (() -> Unit)? = null,
    onTestUpdateBannerClick: (() -> Unit)? = null,
    onDownloadsClick: () -> Unit = {},
    onAccountClick: () -> Unit,
    onSwitchProfileClick: (() -> Unit)? = null,
    showAccountSection: Boolean = true,
    showGeneralSection: Boolean = true,
    showAboutSection: Boolean = true,
    showAdvancedSection: Boolean = true,
    showSupportersContributorsPage: Boolean = true,
) {
    item {
        SettingsGroup(isTablet = isTablet) {
            // 1. My Profile
            SettingsNavigationRow(
                title = "My Profile",
                description = stringResource(Res.string.compose_settings_root_switch_profile_description),
                icon = Icons.Rounded.Person,
                isTablet = isTablet,
                onClick = { onSwitchProfileClick?.invoke() ?: onAccountClick() },
            )
            SettingsGroupDivider(isTablet = isTablet)

            // 2. Account
            SettingsNavigationRow(
                title = stringResource(Res.string.compose_settings_page_account),
                description = stringResource(Res.string.compose_settings_root_account_description),
                icon = Icons.Rounded.AccountCircle,
                isTablet = isTablet,
                onClick = onAccountClick,
            )
            SettingsGroupDivider(isTablet = isTablet)

            // 3. Flixio Specials
            SettingsNavigationRow(
                title = "Flixio Specials",
                description = "Exclusive themes, features and experimental perks",
                icon = Icons.Rounded.AutoAwesome,
                isTablet = isTablet,
                onClick = onAppearanceClick,
            )
            SettingsGroupDivider(isTablet = isTablet)

            // 4. Layout
            SettingsNavigationRow(
                title = "Layout",
                description = stringResource(Res.string.compose_settings_root_appearance_description),
                icon = Icons.Rounded.Palette,
                isTablet = isTablet,
                onClick = onAppearanceClick,
            )
            SettingsGroupDivider(isTablet = isTablet)

            // 5. Content and Discovery
            SettingsNavigationRow(
                title = stringResource(Res.string.compose_settings_page_content_discovery),
                description = stringResource(Res.string.compose_settings_root_content_discovery_description),
                icon = Icons.Rounded.Extension,
                isTablet = isTablet,
                onClick = onContentDiscoveryClick,
            )
            SettingsGroupDivider(isTablet = isTablet)

            // 6. Playback
            SettingsNavigationRow(
                title = stringResource(Res.string.compose_settings_page_playback),
                description = stringResource(Res.string.settings_playback_subtitle),
                icon = Icons.Rounded.PlayArrow,
                isTablet = isTablet,
                onClick = onPlaybackClick,
            )
            SettingsGroupDivider(isTablet = isTablet)

            // 7. Integrations
            SettingsNavigationRow(
                title = stringResource(Res.string.compose_settings_page_integrations),
                description = stringResource(Res.string.compose_settings_root_integrations_description),
                icon = Icons.Rounded.Link,
                isTablet = isTablet,
                onClick = onIntegrationsClick,
            )
            SettingsGroupDivider(isTablet = isTablet)

            // 8. Notification
            SettingsNavigationRow(
                title = stringResource(Res.string.compose_settings_page_notifications),
                description = stringResource(Res.string.compose_settings_root_notifications_description),
                icon = Icons.Rounded.Notifications,
                isTablet = isTablet,
                onClick = onNotificationsClick,
            )
            SettingsGroupDivider(isTablet = isTablet)

            // 9. Supporters & Contribution
            SettingsNavigationRow(
                title = "Supporters & Contribution",
                description = "Coming soon in upcoming Flixio releases",
                icon = Icons.Rounded.Favorite,
                enabled = false,
                isTablet = isTablet,
                onClick = {},
            )
            SettingsGroupDivider(isTablet = isTablet)

            // 10. License and Attr
            SettingsNavigationRow(
                title = "License and Attr",
                description = stringResource(Res.string.about_licenses_attributions_subtitle),
                icon = Icons.Rounded.Info,
                isTablet = isTablet,
                onClick = onLicensesAttributionsClick,
            )

            // 11. Check for Updates
            if (onCheckForUpdatesClick != null) {
                SettingsGroupDivider(isTablet = isTablet)
                SettingsNavigationRow(
                    title = "Check for Updates",
                    description = stringResource(Res.string.compose_settings_root_check_updates_description),
                    icon = Icons.Rounded.CloudDownload,
                    isTablet = isTablet,
                    onClick = onCheckForUpdatesClick,
                )
            }

            // 12. Advanced
            SettingsGroupDivider(isTablet = isTablet)
            SettingsNavigationRow(
                title = stringResource(Res.string.compose_settings_page_advanced),
                description = stringResource(Res.string.compose_settings_root_advanced_description),
                icon = Icons.Rounded.Tune,
                isTablet = isTablet,
                onClick = onAdvancedClick,
            )
        }
    }

    item {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = if (isTablet) 20.dp else 16.dp),
        ) {
            MemberBrandWordmark(
                height = if (isTablet) 30.dp else 26.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(
                modifier = Modifier.height(if (isTablet) 10.dp else 8.dp),
            )
            Text(
                text = stringResource(Res.string.compose_about_made_with),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(
                    Res.string.compose_about_version_format,
                    AppVersionConfig.VERSION_NAME,
                    AppVersionConfig.VERSION_CODE,
                ),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
