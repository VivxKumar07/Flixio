package com.nuvio.app.features.mdblist

import android.content.Context
import android.content.SharedPreferences
import com.nuvio.app.core.storage.ProfileScopedKey
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

internal actual object PlatformMdbListAuthPersistence : MdbListAuthPersistence {
    private const val PREFERENCES_NAME = "nuvio_mdblist_auth"
    private const val AUTH_PAYLOAD_KEY = "mdblist_auth_payload"

    private var preferences: SharedPreferences? = null

    fun initialize(context: Context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    actual override fun read(profileId: Int): String? =
        preferences?.getString(ProfileScopedKey.of(AUTH_PAYLOAD_KEY, profileId), null)

    actual override fun write(profileId: Int, value: String?) {
        val key = ProfileScopedKey.of(AUTH_PAYLOAD_KEY, profileId)
        val editor = preferences?.edit() ?: return
        if (value.isNullOrBlank()) {
            editor.remove(key).apply()
        } else {
            editor.putString(key, value).apply()
        }
    }

    actual override fun clear() {
        preferences?.edit()?.clear()?.apply()
    }
}

internal actual fun createMdbListHttpClient(): HttpClient = HttpClient(OkHttp)
