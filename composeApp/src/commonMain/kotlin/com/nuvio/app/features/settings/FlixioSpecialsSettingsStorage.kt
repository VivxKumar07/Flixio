package com.nuvio.app.features.settings

internal expect object FlixioSpecialsSettingsStorage {
    fun loadPayload(): String?
    fun savePayload(payload: String)
}
