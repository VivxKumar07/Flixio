package com.nuvio.app.features.mdblist

import android.content.Context
import android.content.SharedPreferences
import com.nuvio.app.core.storage.ProfileScopedKey

internal actual object PlatformMdbListSyncStorage : MdbListSyncStorage {
    private const val PREFERENCES_NAME = "nuvio_mdblist_sync"
    private const val PAYLOAD_KEY = "mdblist_sync_snapshot"

    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    actual override suspend fun load(profileId: Int): String? =
        preferences?.getString(ProfileScopedKey.of(PAYLOAD_KEY, profileId), null)

    actual override suspend fun save(profileId: Int, payload: String, checkScope: () -> Unit) {
        checkScope()
        preferences?.edit()?.putString(ProfileScopedKey.of(PAYLOAD_KEY, profileId), payload)?.apply()
    }

    actual override suspend fun remove(profileId: Int, checkScope: () -> Unit) {
        checkScope()
        preferences?.edit()?.remove(ProfileScopedKey.of(PAYLOAD_KEY, profileId))?.apply()
    }

    actual fun clearAll() {
        preferences?.edit()?.clear()?.apply()
    }
}
