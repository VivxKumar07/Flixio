package com.nuvio.app.features.cloudstream

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.ui.NuvioIconActionButton
import com.nuvio.app.core.ui.NuvioInfoBadge
import com.nuvio.app.core.ui.NuvioInputField
import com.nuvio.app.core.ui.NuvioPrimaryButton
import com.nuvio.app.core.ui.NuvioScreen
import com.nuvio.app.core.ui.NuvioScreenHeader
import com.nuvio.app.core.ui.NuvioSectionLabel
import com.nuvio.app.core.ui.NuvioSurfaceCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.compose_settings_page_cloudstream
import org.jetbrains.compose.resources.stringResource

@Composable
fun CloudStreamSettingsScreen(
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) {
        CloudStreamRepository.initialize()
    }

    NuvioScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        stickyHeader {
            NuvioScreenHeader(
                title = stringResource(Res.string.compose_settings_page_cloudstream),
                onBack = onBack,
            )
        }
        item {
            CloudStreamSettingsPageContent()
        }
    }
}

@Composable
fun CloudStreamSettingsPageContent(
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        CloudStreamRepository.initialize()
    }

    val uiState by CloudStreamRepository.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var csRepoUrl by rememberSaveable { mutableStateOf("") }
    var csMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var csMessageIsError by rememberSaveable { mutableStateOf(false) }
    var isAddingCsRepo by remember { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val installingPluginIds = remember { mutableStateMapOf<String, Boolean>() }

    // Repositories whose extensions list is currently expanded
    var expandedRepoUrls by rememberSaveable { mutableStateOf(setOf<String>()) }

    val sortedRepos = remember(uiState.repositories) {
        uiState.repositories.sortedBy { it.manifest.name.lowercase() }
    }

    val pluginsByRepo = remember(uiState.plugins) {
        uiState.plugins.groupBy { it.metadata.repositoryManifestUrl }
    }

    val orphanedPlugins = remember(uiState.plugins, uiState.repositories) {
        val knownRepoUrls = uiState.repositories.map { it.manifest.sourceUrl }.toSet()
        uiState.plugins.filter { it.metadata.repositoryManifestUrl !in knownRepoUrls }
    }

    val isSearching = searchQuery.trim().isNotBlank()
    val normalizedSearch = searchQuery.trim().lowercase()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. Overview Section
        NuvioSectionLabel("Overview")
        NuvioSurfaceCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                NuvioInfoBadge(text = "${sortedRepos.size} Repositories")
                NuvioInfoBadge(text = "${uiState.plugins.count { it.isInstalled }} Installed")
                NuvioInfoBadge(text = "${uiState.plugins.count { it.isRunnable }} Active")
            }

            if (!uiState.securityWarningAccepted) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Third-Party Extensions Authorization",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CloudStream extensions (.cs3 packages) run community code locally on your device to query streaming sources and scrapers. Only install repositories from sources you trust.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                NuvioPrimaryButton(
                    text = "Authorize & Enable Extensions",
                    onClick = { CloudStreamRepository.acceptSecurityWarning() },
                )
            }
        }

        // 2. Add Repository Section
        NuvioSectionLabel("Add Repository")
        NuvioSurfaceCard {
            NuvioInputField(
                value = csRepoUrl,
                onValueChange = {
                    csRepoUrl = it
                    csMessage = null
                },
                placeholder = "Repository URL or raw repo.json URL",
            )
            Spacer(modifier = Modifier.height(16.dp))
            NuvioPrimaryButton(
                text = if (isAddingCsRepo) "Installing Repository..." else "Install CloudStream Repository",
                enabled = csRepoUrl.isNotBlank() && !isAddingCsRepo,
                onClick = {
                    val requested = csRepoUrl.trim()
                    if (requested.isBlank()) {
                        csMessage = "Please enter a repository URL"
                        csMessageIsError = true
                        return@NuvioPrimaryButton
                    }
                    isAddingCsRepo = true
                    csMessage = null
                    coroutineScope.launch {
                        when (val result = CloudStreamRepository.addRepository(requested)) {
                            is AddCloudStreamRepositoryResult.Success -> {
                                csRepoUrl = ""
                                csMessage = "Installed repository: ${result.repository.name}"
                                csMessageIsError = false
                                // Auto-expand newly installed repo
                                expandedRepoUrls = expandedRepoUrls + result.repository.sourceUrl
                            }
                            is AddCloudStreamRepositoryResult.Error -> {
                                csMessage = result.message
                                csMessageIsError = true
                            }
                        }
                        isAddingCsRepo = false
                    }
                },
            )
            csMessage?.let { text ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (csMessageIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
        }

        // 3. Search Filter (if multiple plugins exist)
        if (uiState.plugins.size > 5) {
            NuvioInputField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search extensions by name or author...",
            )
        }

        // 4. Installed Repositories with Collapsible Extension Dropdowns
        NuvioSectionLabel("Installed Repositories & Extensions")
        if (sortedRepos.isEmpty() && orphanedPlugins.isEmpty()) {
            NuvioSurfaceCard {
                Text(
                    text = "No CloudStream repositories installed",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add a repository URL above (such as a GitHub raw repo.json URL) to discover and install streaming extensions.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            sortedRepos.forEach { repo ->
                val repoSourceUrl = repo.manifest.sourceUrl
                val allRepoPlugins = pluginsByRepo[repoSourceUrl].orEmpty()
                val visiblePlugins = if (isSearching) {
                    allRepoPlugins.filter {
                        it.metadata.name.lowercase().contains(normalizedSearch) ||
                            it.metadata.description?.lowercase()?.contains(normalizedSearch) == true ||
                            it.metadata.authors.any { a -> a.lowercase().contains(normalizedSearch) }
                    }
                } else {
                    allRepoPlugins
                }

                val isExpanded = isSearching || (repoSourceUrl in expandedRepoUrls)
                val installedCount = allRepoPlugins.count { it.isInstalled }
                val activeCount = allRepoPlugins.count { it.isRunnable }

                NuvioSurfaceCard {
                    // Repository Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = repo.manifest.name,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            repo.manifest.description?.let { desc ->
                                if (desc.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = repo.manifest.sourceUrl,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            NuvioIconActionButton(
                                icon = Icons.Rounded.Refresh,
                                contentDescription = "Refresh repository",
                                tint = MaterialTheme.colorScheme.primary,
                                onClick = { CloudStreamRepository.refreshRepository(repo.manifest.sourceUrl) },
                            )
                            NuvioIconActionButton(
                                icon = Icons.Rounded.Delete,
                                contentDescription = "Remove repository",
                                tint = MaterialTheme.colorScheme.error,
                                onClick = { CloudStreamRepository.removeRepository(repo.manifest.sourceUrl) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        NuvioInfoBadge(text = "${allRepoPlugins.size} Available")
                        if (installedCount > 0) {
                            NuvioInfoBadge(text = "$installedCount Installed")
                        }
                        if (activeCount > 0) {
                            NuvioInfoBadge(text = "$activeCount Active")
                        }
                        if (repo.isRefreshing) {
                            NuvioInfoBadge(text = "Refreshing...")
                        }
                    }

                    repo.errorMessage?.let { errorMessage ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Collapsible Dropdown Toggle Row
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                expandedRepoUrls = if (repoSourceUrl in expandedRepoUrls) {
                                    expandedRepoUrls - repoSourceUrl
                                } else {
                                    expandedRepoUrls + repoSourceUrl
                                }
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (isExpanded) {
                                    "Hide Extensions (${visiblePlugins.size})"
                                } else {
                                    "View Extensions (${allRepoPlugins.size})"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    // Only render extensions when this repository is expanded
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (visiblePlugins.isEmpty()) {
                                Text(
                                    text = if (isSearching) "No matching extensions in this repository." else "No extensions available in this repository.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            } else {
                                visiblePlugins.forEach { plugin ->
                                    val isInstalling = installingPluginIds[plugin.metadata.id.value] == true
                                    ExtensionItemCard(
                                        plugin = plugin,
                                        isInstalling = isInstalling,
                                        onInstall = {
                                            installingPluginIds[plugin.metadata.id.value] = true
                                            coroutineScope.launch {
                                                when (val result = CloudStreamRepository.installPlugin(plugin.metadata.id.value)) {
                                                    is CloudStreamInstallResult.Success -> {
                                                        CloudStreamRepository.setPluginEnabled(plugin.metadata.id.value, true)
                                                    }
                                                    is CloudStreamInstallResult.Error -> {
                                                        csMessage = result.message
                                                        csMessageIsError = true
                                                    }
                                                }
                                                installingPluginIds[plugin.metadata.id.value] = false
                                            }
                                        },
                                        onUpdate = {
                                            installingPluginIds[plugin.metadata.id.value] = true
                                            coroutineScope.launch {
                                                CloudStreamRepository.updatePlugin(plugin.metadata.id.value)
                                                installingPluginIds[plugin.metadata.id.value] = false
                                            }
                                        },
                                        onToggleEnabled = { enabled ->
                                            CloudStreamRepository.setPluginEnabled(plugin.metadata.id.value, enabled)
                                        },
                                        onUninstall = {
                                            CloudStreamRepository.removePlugin(plugin.metadata.id.value)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Orphaned / Standalone installed extensions
            if (orphanedPlugins.isNotEmpty()) {
                val isExpanded = isSearching || ("orphaned" in expandedRepoUrls)
                NuvioSurfaceCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Other Installed Extensions",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${orphanedPlugins.size} extensions from unlisted sources",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                expandedRepoUrls = if ("orphaned" in expandedRepoUrls) {
                                    expandedRepoUrls - "orphaned"
                                } else {
                                    expandedRepoUrls + "orphaned"
                                }
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (isExpanded) "Hide Extensions (${orphanedPlugins.size})" else "View Extensions (${orphanedPlugins.size})",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            orphanedPlugins.forEach { plugin ->
                                ExtensionItemCard(
                                    plugin = plugin,
                                    isInstalling = false,
                                    onInstall = {},
                                    onUpdate = {},
                                    onToggleEnabled = { enabled ->
                                        CloudStreamRepository.setPluginEnabled(plugin.metadata.id.value, enabled)
                                    },
                                    onUninstall = {
                                        CloudStreamRepository.removePlugin(plugin.metadata.id.value)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtensionItemCard(
    plugin: CloudStreamPluginItem,
    isInstalling: Boolean,
    onInstall: () -> Unit,
    onUpdate: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onUninstall: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Extension,
                        contentDescription = null,
                        tint = if (plugin.isRunnable) Color(0xFF68B76A) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (plugin.metadata.authors.isNotEmpty()) {
                                plugin.metadata.authors.joinToString(", ")
                            } else {
                                "CloudStream Extension"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = plugin.metadata.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        plugin.metadata.description?.let { desc ->
                            if (desc.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                if (plugin.isInstalled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = plugin.enabled,
                            onCheckedChange = onToggleEnabled,
                            enabled = plugin.compatibility.isRunnable && plugin.metadata.status.canInstall,
                        )
                        NuvioIconActionButton(
                            icon = Icons.Rounded.Delete,
                            contentDescription = "Uninstall extension",
                            tint = MaterialTheme.colorScheme.error,
                            onClick = onUninstall,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NuvioInfoBadge(text = "v${plugin.metadata.version}")
                if (plugin.metadata.tvTypes.isNotEmpty()) {
                    NuvioInfoBadge(text = plugin.metadata.tvTypes.take(3).joinToString(" | ") { it.name })
                }
                plugin.metadata.language?.let { lang ->
                    if (lang.isNotBlank()) {
                        NuvioInfoBadge(text = lang.uppercase())
                    }
                }
                if (plugin.isRunnable) {
                    NuvioInfoBadge(text = "Active")
                } else if (plugin.isInstalled) {
                    NuvioInfoBadge(text = "Installed (Disabled)")
                }
                if (!plugin.compatibility.isRunnable) {
                    NuvioInfoBadge(text = "Incompatible")
                }
            }

            if (!plugin.isInstalled) {
                Spacer(modifier = Modifier.height(10.dp))
                NuvioPrimaryButton(
                    text = if (isInstalling) "Installing..." else "Install Extension",
                    enabled = !isInstalling && plugin.compatibility.isRunnable && plugin.metadata.status.canInstall,
                    onClick = onInstall,
                )
            } else if (plugin.hasUpdate) {
                Spacer(modifier = Modifier.height(10.dp))
                NuvioPrimaryButton(
                    text = if (isInstalling) "Updating..." else "Update to v${plugin.metadata.version}",
                    enabled = !isInstalling,
                    onClick = onUpdate,
                )
            }

            plugin.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
