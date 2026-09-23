package com.nuvio.app.features.cloudstream

import android.content.Context
import android.content.SharedPreferences
import java.io.File
import java.io.FileOutputStream

internal actual object CloudStreamPlatformStorage {
    private const val preferencesName = "nuvio_cloudstream"
    private const val stateKey = "state"
    private const val packagesDirectoryName = "cloudstream/packages"

    private var appContext: Context? = null
    private var preferences: SharedPreferences? = null
    private var activeProfileId: Int = 1

    actual fun initialize(context: Any?) {
        val androidContext = context as? Context ?: return
        appContext = androidContext.applicationContext
        preferences = androidContext.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        CloudStreamPlatformRuntime.initialize(androidContext)
    }

    private fun getOrResolveContext(): Context {
        appContext?.let { return it }
        val resolved = try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentApplicationMethod = activityThreadClass.getMethod("currentApplication")
            (currentApplicationMethod.invoke(null) as? Context)?.applicationContext
        } catch (_: Throwable) {
            null
        }
        if (resolved != null) {
            initialize(resolved)
            return resolved
        }
        return requireNotNull(appContext) { "CloudStream storage is not initialized" }
    }

    private fun getPreferences(): SharedPreferences? {
        if (preferences == null) {
            try {
                val ctx = getOrResolveContext()
                preferences = ctx.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
            } catch (_: Throwable) {}
        }
        return preferences
    }

    actual fun setActiveProfile(profileId: Int) {
        activeProfileId = profileId.coerceAtLeast(1)
    }

    actual fun loadState(profileId: Int): String? =
        getPreferences()?.getString("${stateKey}_$profileId", null)

    actual fun saveState(profileId: Int, payload: String) {
        getPreferences()?.edit()?.putString("${stateKey}_$profileId", payload)?.apply()
    }

    actual fun savePackageAtomically(storageKey: String, bytes: ByteArray) {
        val directory = packagesDirectory()
        val destination = File(directory, "$storageKey.cs3")
        val temporary = File.createTempFile(storageKey, ".tmp", directory)
        val backup = File(directory, "$storageKey.backup")
        try {
            FileOutputStream(temporary).use { output ->
                output.write(bytes)
                output.fd.sync()
            }
            if (backup.exists()) backup.delete()
            if (destination.exists() && !destination.renameTo(backup)) {
                error("Could not prepare existing CloudStream package for update")
            }
            if (!temporary.renameTo(destination)) {
                if (backup.exists()) backup.renameTo(destination)
                error("Could not commit CloudStream package")
            }
            backup.delete()
        } finally {
            temporary.delete()
        }
    }

    actual fun packageExists(storageKey: String): Boolean {
        val scoped = File(packagesDirectory(), "$storageKey.cs3")
        if (scoped.isFile) return true
        val legacy = legacyPackage(storageKey)
        return legacy.isFile && legacy.copyTo(scoped, overwrite = false).isFile
    }

    actual fun migratePackage(oldStorageKey: String, newStorageKey: String): Boolean {
        val directory = packagesDirectory()
        val source = File(directory, "$oldStorageKey.cs3")
        val destination = File(directory, "$newStorageKey.cs3")
        if (destination.isFile) return true
        if (source.isFile && source.renameTo(destination)) return true
        val legacy = legacyPackage(oldStorageKey)
        return legacy.isFile && legacy.copyTo(destination, overwrite = false).isFile
    }

    actual fun packagePath(storageKey: String): String? {
        if (!packageExists(storageKey)) return null
        return File(packagesDirectory(), "$storageKey.cs3").absolutePath
    }

    actual fun deletePackage(storageKey: String) {
        File(packagesDirectory(), "$storageKey.cs3").delete()
        File(packagesDirectory(), "$storageKey.backup").delete()
    }

    actual fun clearPackages() {
        packagesRootDirectory().deleteRecursively()
    }

    actual fun clearAllState() {
        getPreferences()?.edit()?.clear()?.apply()
    }

    private fun packagesDirectory(): File {
        return File(packagesRootDirectory(), "profile-$activeProfileId").apply { mkdirs() }
    }

    private fun legacyPackage(storageKey: String): File {
        return File(packagesRootDirectory(), "$storageKey.cs3")
    }

    private fun packagesRootDirectory(): File {
        val context = getOrResolveContext()
        return File(context.filesDir, packagesDirectoryName).apply { mkdirs() }
    }
}
