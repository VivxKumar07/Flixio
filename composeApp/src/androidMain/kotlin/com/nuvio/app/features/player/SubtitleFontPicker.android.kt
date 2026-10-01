package com.nuvio.app.features.player

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private val log = Logger.withTag("SubtitleFontPicker")

@Composable
actual fun rememberSubtitleFontPicker(
    onFontImported: (fontName: String, fontPath: String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            importFontFile(context, uri)?.let { (name, path) ->
                withContext(Dispatchers.Main) {
                    onFontImported(name, path)
                }
            }
        }
    }

    return remember(launcher) {
        {
            launcher.launch(
                arrayOf(
                    "font/*",
                    "font/ttf",
                    "font/otf",
                    "application/x-font-ttf",
                    "application/x-font-opentype",
                    "application/x-font-truetype",
                    "application/octet-stream",
                    "*/*",
                ),
            )
        }
    }
}

private fun importFontFile(context: Context, uri: Uri): Pair<String, String>? {
    return try {
        val contentResolver = context.contentResolver
        var displayName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIdx >= 0) {
                    displayName = cursor.getString(nameIdx)
                }
            }
        }

        val rawName = displayName?.takeIf { it.isNotBlank() } ?: "custom_font_${System.currentTimeMillis()}.ttf"
        val cleanName = rawName.substringBeforeLast('.')
        val extension = rawName.substringAfterLast('.', "ttf").lowercase()

        val fontsDir = File(context.filesDir, "fonts")
        if (!fontsDir.exists()) fontsDir.mkdirs()

        val targetFile = File(fontsDir, "font_${System.currentTimeMillis()}.$extension")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        } ?: return null

        // Validate that this is actually a valid readable font file by attempting to create Typeface
        val testTypeface = runCatching { Typeface.createFromFile(targetFile) }.getOrNull()
        if (testTypeface == null) {
            log.w { "Imported file is not a valid font: ${targetFile.absolutePath}" }
            targetFile.delete()
            return null
        }

        val friendlyName = cleanName.replace('_', ' ').replace('-', ' ').trim()
        Pair(friendlyName, targetFile.absolutePath)
    } catch (e: Throwable) {
        log.e(e) { "Failed to import font file from $uri" }
        null
    }
}
