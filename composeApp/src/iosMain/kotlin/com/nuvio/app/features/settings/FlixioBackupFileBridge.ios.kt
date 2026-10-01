package com.nuvio.app.features.settings

internal actual object FlixioBackupFileBridge {
    actual fun exportBackup(
        fileName: String,
        payload: String,
        onResult: (Result<String>) -> Unit,
    ) {
        onResult(Result.failure(IllegalStateException("File backup is only supported on Android. Please use Copy/Paste JSON.")))
    }

    actual fun importBackup(
        onResult: (Result<String>) -> Unit,
    ) {
        onResult(Result.failure(IllegalStateException("File backup is only supported on Android. Please use Copy/Paste JSON.")))
    }
}
