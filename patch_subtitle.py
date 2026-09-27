import re

def patch(path, old, new, must=True):
    s = open(path, encoding="utf-8").read()
    if old not in s:
        if must:
            raise AssertionError(f"NOT FOUND in {path}: {old[:70]!r}")
        return
    s = s.replace(old, new, 1)
    open(path, "w", encoding="utf-8", newline="\n").write(s)

# 1. Model: shadowEnabled field
patch("composeApp/src/commonMain/kotlin/com/nuvio/app/features/player/SubtitleAudioModels.kt",
"    val outlineEnabled: Boolean = true,\n",
"    val outlineEnabled: Boolean = true,\n    val shadowEnabled: Boolean = false,\n")

# 2. Storage common expect
patch("composeApp/src/commonMain/kotlin/com/nuvio/app/features/player/PlayerSettingsStorage.kt",
"    fun loadSubtitleOutlineEnabled(): Boolean?\n    fun saveSubtitleOutlineEnabled(enabled: Boolean)\n",
"    fun loadSubtitleOutlineEnabled(): Boolean?\n    fun saveSubtitleOutlineEnabled(enabled: Boolean)\n    fun loadSubtitleShadowEnabled(): Boolean?\n    fun saveSubtitleShadowEnabled(enabled: Boolean)\n")

# 3. Android actual
p = "composeApp/src/androidMain/kotlin/com/nuvio/app/features/player/PlayerSettingsStorage.android.kt"
s = open(p, encoding="utf-8").read()
if "loadSubtitleShadowEnabled" not in s:
    s = s.replace('subtitleOutlineEnabledKey,\n', 'subtitleOutlineEnabledKey,\n        subtitleShadowEnabledKey,\n', 1)
    anchor = """    actual fun saveSubtitleOutlineEnabled(enabled: Boolean) {
        preferences
            ?.edit()
            ?.putBoolean(ProfileScopedKey.of(subtitleOutlineEnabledKey), enabled)
            ?.apply()
    }
"""
    add = """
    actual fun loadSubtitleShadowEnabled(): Boolean? =
        preferences?.let { sharedPreferences ->
            val key = ProfileScopedKey.of(subtitleShadowEnabledKey)
            if (sharedPreferences.contains(key)) {
                sharedPreferences.getBoolean(key, SubtitleStyleState.DEFAULT.shadowEnabled)
            } else {
                null
            }
        }

    actual fun saveSubtitleShadowEnabled(enabled: Boolean) {
        preferences
            ?.edit()
            ?.putBoolean(ProfileScopedKey.of(subtitleShadowEnabledKey), enabled)
            ?.apply()
    }
"""
    assert anchor in s, "android save outline"
    s = s.replace(anchor, anchor + add, 1)
    # key constant: find the outline key declaration
    m = re.search(r'private const val subtitleOutlineEnabledKey = "[^"]+"\n', s)
    assert m, "android key"
    s = s.replace(m.group(0), m.group(0) + '    private const val subtitleShadowEnabledKey = "subtitle_shadow_enabled"\n', 1)
    open(p, "w", encoding="utf-8", newline="\n").write(s)

# 4. iOS actual
p = "composeApp/src/iosMain/kotlin/com/nuvio/app/features/player/PlayerSettingsStorage.ios.kt"
s = open(p, encoding="utf-8").read()
if "loadSubtitleShadowEnabled" not in s:
    s = s.replace('    private const val subtitleOutlineEnabledKey = "subtitle_outline_enabled"\n',
                  '    private const val subtitleOutlineEnabledKey = "subtitle_outline_enabled"\n    private const val subtitleShadowEnabledKey = "subtitle_shadow_enabled"\n')
    s = s.replace("        subtitleOutlineEnabledKey,\n", "        subtitleOutlineEnabledKey,\n        subtitleShadowEnabledKey,\n", 1)
    anchor = """    actual fun saveSubtitleOutlineEnabled(enabled: Boolean) {
        NSUserDefaults.standardUserDefaults.setBool(enabled, forKey = ProfileScopedKey.of(subtitleOutlineEnabledKey))
    }
"""
    add = """
    actual fun loadSubtitleShadowEnabled(): Boolean? {
        val defaults = NSUserDefaults.standardUserDefaults
        val key = ProfileScopedKey.of(subtitleShadowEnabledKey)
        return if (defaults.objectForKey(key) != null) {
            defaults.boolForKey(key)
        } else {
            null
        }
    }

    actual fun saveSubtitleShadowEnabled(enabled: Boolean) {
        NSUserDefaults.standardUserDefaults.setBool(enabled, forKey = ProfileScopedKey.of(subtitleShadowEnabledKey))
    }
"""
    assert anchor in s, "ios save outline"
    s = s.replace(anchor, anchor + add, 1)
    open(p, "w", encoding="utf-8", newline="\n").write(s)

# 5. Repository load + save
patch("composeApp/src/commonMain/kotlin/com/nuvio/app/features/player/PlayerSettingsRepository.kt",
"""            outlineEnabled = PlayerSettingsStorage.loadSubtitleOutlineEnabled()
                ?: SubtitleStyleState.DEFAULT.outlineEnabled,""",
"""            outlineEnabled = PlayerSettingsStorage.loadSubtitleOutlineEnabled()
                ?: SubtitleStyleState.DEFAULT.outlineEnabled,
            shadowEnabled = PlayerSettingsStorage.loadSubtitleShadowEnabled()
                ?: SubtitleStyleState.DEFAULT.shadowEnabled,""")
patch("composeApp/src/commonMain/kotlin/com/nuvio/app/features/player/PlayerSettingsRepository.kt",
"        PlayerSettingsStorage.saveSubtitleOutlineEnabled(normalized.outlineEnabled)\n",
"        PlayerSettingsStorage.saveSubtitleOutlineEnabled(normalized.outlineEnabled)\n        PlayerSettingsStorage.saveSubtitleShadowEnabled(normalized.shadowEnabled)\n")

# 6. Panel toggle (after the outline color picker section)
patch("composeApp/src/commonMain/kotlin/com/nuvio/app/features/player/SubtitleStylePanel.kt",
"""        SubtitleStyleSection(title = stringResource(Res.string.compose_player_outline)) {
            SubtitleToggleChip(
                enabled = style.outlineEnabled,
                onClick = { onStyleChanged(style.copy(outlineEnabled = !style.outlineEnabled)) },
            )
        }""",
"""        SubtitleStyleSection(title = stringResource(Res.string.compose_player_outline)) {
            SubtitleToggleChip(
                enabled = style.outlineEnabled,
                onClick = { onStyleChanged(style.copy(outlineEnabled = !style.outlineEnabled)) },
            )
        }

        SubtitleStyleSection(title = "Depth Shadow") {
            SubtitleToggleChip(
                enabled = style.shadowEnabled,
                onClick = { onStyleChanged(style.copy(shadowEnabled = !style.shadowEnabled)) },
            )
        }""")

# 7. Settings page toggle (after the outline block)
p = "composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/PlaybackSettingsPage.kt"
s = open(p, encoding="utf-8").read()
anchor = "                if (subtitleStyle.outlineEnabled) {"
idx = s.index(anchor)
insert = """                SettingsSwitchRow(
                    title = "Subtitle depth shadow",
                    description = "Adds a soft depth shadow behind subtitles.",
                    checked = subtitleStyle.shadowEnabled,
                    isTablet = isTablet,
                    onCheckedChange = { enabled ->
                        PlayerSettingsRepository.setSubtitleStyle(subtitleStyle.copy(shadowEnabled = enabled))
                    },
                )
"""
# find the end of the outline if-block heuristically: insert before the anchor line's preceding toggle? Simpler: insert right BEFORE anchor's containing section end — we insert after the outline switch row block by locating 'onCheckedChange = { enabled ->' near outline... Use direct insertion before anchor.
s = s[:idx] + insert + s[idx:]
open(p, "w", encoding="utf-8", newline="\n").write(s)

# 8. Android engine: apply shadow as edge type
patch("composeApp/src/androidMain/kotlin/com/nuvio/app/features/player/PlayerEngine.android.kt",
"""                if (style.outlineEnabled) CaptionStyleCompat.EDGE_TYPE_OUTLINE else CaptionStyleCompat.EDGE_TYPE_NONE,""",
"""                when {
                    style.outlineEnabled -> CaptionStyleCompat.EDGE_TYPE_OUTLINE
                    style.shadowEnabled -> CaptionStyleCompat.EDGE_TYPE_SHADOW
                    else -> CaptionStyleCompat.EDGE_TYPE_NONE
                },""")

print("subtitle shadow chain done")
