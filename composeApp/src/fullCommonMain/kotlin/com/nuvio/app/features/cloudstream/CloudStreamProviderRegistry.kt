package com.nuvio.app.features.cloudstream

internal object CloudStreamProviderRegistry {
    private val providers: List<CloudStreamProvider> = listOf(
        KickTrCloudStreamProvider,
    )

    fun find(id: String): CloudStreamProvider? =
        providers.firstOrNull { it.id == id }

    fun all(): List<CloudStreamProvider> = providers
}
