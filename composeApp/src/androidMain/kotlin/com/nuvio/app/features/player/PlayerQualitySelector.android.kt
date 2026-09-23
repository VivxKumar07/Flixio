package com.nuvio.app.features.player

import java.io.File

internal actual fun writeTemporaryHlsPlaylist(playlistText: String): String? {
    return try {
        val tempFile = File.createTempFile("hls_variant_", ".m3u8")
        tempFile.deleteOnExit()
        tempFile.writeText(playlistText, Charsets.UTF_8)
        tempFile.toURI().toString()
    } catch (_: Throwable) {
        null
    }
}
