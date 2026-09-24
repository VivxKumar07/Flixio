package com.nuvio.app.features.player

import androidx.compose.runtime.Composable

@Composable
actual fun rememberSubtitleFontPicker(
    onFontImported: (fontName: String, fontPath: String) -> Unit,
): () -> Unit = {}
