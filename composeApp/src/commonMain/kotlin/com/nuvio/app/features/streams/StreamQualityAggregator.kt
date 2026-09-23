package com.nuvio.app.features.streams

/**
 * Represents a user-selectable stream quality option in Flixio.
 */
data class StreamQualityOption(
    val id: String,
    val label: String,
    val targetResolution: Int? = null,
    val isDolbyVision: Boolean = false,
    val isHdr: Boolean = false,
) {
    companion object {
        val AUTO = StreamQualityOption(id = "auto", label = "Auto")
        val DOLBY_VISION_4K = StreamQualityOption(
            id = "4k_dv",
            label = "4K Dolby Vision",
            targetResolution = 2160,
            isDolbyVision = true,
        )
        val HDR_4K = StreamQualityOption(
            id = "4k_hdr",
            label = "4K HDR",
            targetResolution = 2160,
            isHdr = true,
        )
        val UHD_4K = StreamQualityOption(
            id = "4k",
            label = "4K",
            targetResolution = 2160,
        )
        val FHD_1080P = StreamQualityOption(
            id = "1080p",
            label = "1080p",
            targetResolution = 1080,
        )
        val HD_720P = StreamQualityOption(
            id = "720p",
            label = "720p",
            targetResolution = 720,
        )
        val SD_480P = StreamQualityOption(
            id = "480p",
            label = "480p",
            targetResolution = 480,
        )

        fun fromId(id: String?): StreamQualityOption {
            val key = id?.trim()?.lowercase() ?: return AUTO
            return when (key) {
                "4k_dv" -> DOLBY_VISION_4K
                "4k_hdr" -> HDR_4K
                "4k" -> UHD_4K
                "1080p" -> FHD_1080P
                "720p" -> HD_720P
                "480p" -> SD_480P
                else -> AUTO
            }
        }
    }
}

/**
 * Extracts and aggregates available qualities across all resolved streams.
 * Crucial rule: Only returns qualities that are actually backed by at least one compatible stream.
 */
object StreamQualityAggregator {

    private val dolbyVisionRegex = Regex("""(?i)\b(dv|dovi|dolby[\s._-]?vision)\b""")
    private val hdrRegex = Regex("""(?i)\b(hdr|hdr10\+?)\b""")
    private val resolution4kRegex = Regex("""(?i)\b(4k|2160p?|uhd)\b""")
    private val resolution1080pRegex = Regex("""(?i)\b(1080p?|fhd)\b""")
    private val resolution720pRegex = Regex("""(?i)\b(720p?|hd)\b""")
    private val resolution480pRegex = Regex("""(?i)\b(480p?|sd)\b""")

    fun isDolbyVision(stream: StreamItem): Boolean {
        val parsed = stream.clientResolve?.stream?.raw?.parsed
        if (parsed?.hdr?.any { it.contains("dv", ignoreCase = true) || it.contains("dolby", ignoreCase = true) } == true) {
            return true
        }
        val textToSearch = "${stream.name} ${stream.title} ${stream.description} ${stream.behaviorHints.filename}"
        return dolbyVisionRegex.containsMatchIn(textToSearch)
    }

    fun isHdr(stream: StreamItem): Boolean {
        val parsed = stream.clientResolve?.stream?.raw?.parsed
        if (parsed?.hdr?.any { it.contains("hdr", ignoreCase = true) } == true) {
            return true
        }
        val textToSearch = "${stream.name} ${stream.title} ${stream.description} ${stream.behaviorHints.filename}"
        return hdrRegex.containsMatchIn(textToSearch)
    }

    fun detectResolutionHeight(stream: StreamItem): Int? {
        val parsed = stream.clientResolve?.stream?.raw?.parsed
        val parsedRes = parsed?.resolution?.trim()?.lowercase()
        if (parsedRes != null) {
            when {
                "2160" in parsedRes || "4k" in parsedRes -> return 2160
                "1080" in parsedRes -> return 1080
                "720" in parsedRes -> return 720
                "480" in parsedRes -> return 480
            }
        }
        val textToSearch = "${stream.name} ${stream.title} ${stream.description} ${stream.behaviorHints.filename}"
        return when {
            resolution4kRegex.containsMatchIn(textToSearch) -> 2160
            resolution1080pRegex.containsMatchIn(textToSearch) -> 1080
            resolution720pRegex.containsMatchIn(textToSearch) -> 720
            resolution480pRegex.containsMatchIn(textToSearch) -> 480
            else -> null
        }
    }

    /**
     * Inspects all streams and returns the distinct available qualities, always starting with Auto.
     */
    fun aggregate(streams: List<StreamItem>): List<StreamQualityOption> {
        if (streams.isEmpty()) return listOf(StreamQualityOption.AUTO)

        val result = mutableListOf<StreamQualityOption>()
        result.add(StreamQualityOption.AUTO)

        var has4kDv = false
        var has4kHdr = false
        var has4k = false
        var has1080p = false
        var has720p = false
        var has480p = false

        for (stream in streams) {
            val res = detectResolutionHeight(stream)
            val dv = isDolbyVision(stream)
            val hdr = isHdr(stream)

            if (res == 2160) {
                has4k = true
                if (dv) has4kDv = true
                if (hdr) has4kHdr = true
            } else if (res == 1080) {
                has1080p = true
            } else if (res == 720) {
                has720p = true
            } else if (res == 480) {
                has480p = true
            }
        }

        if (has4kDv) result.add(StreamQualityOption.DOLBY_VISION_4K)
        if (has4kHdr) result.add(StreamQualityOption.HDR_4K)
        if (has4k && !has4kDv && !has4kHdr) result.add(StreamQualityOption.UHD_4K)
        if (has1080p) result.add(StreamQualityOption.FHD_1080P)
        if (has720p) result.add(StreamQualityOption.HD_720P)
        if (has480p) result.add(StreamQualityOption.SD_480P)

        return result
    }

    /**
     * Filters candidate streams that match the requested quality option.
     */
    fun filterByQuality(streams: List<StreamItem>, option: StreamQualityOption): List<StreamItem> {
        if (option.id == StreamQualityOption.AUTO.id) return streams

        return streams.filter { stream ->
            val res = detectResolutionHeight(stream)
            val dv = isDolbyVision(stream)
            val hdr = isHdr(stream)

            when (option.id) {
                StreamQualityOption.DOLBY_VISION_4K.id -> res == 2160 && dv
                StreamQualityOption.HDR_4K.id -> res == 2160 && hdr
                StreamQualityOption.UHD_4K.id -> res == 2160
                StreamQualityOption.FHD_1080P.id -> res == 1080
                StreamQualityOption.HD_720P.id -> res == 720
                StreamQualityOption.SD_480P.id -> res == 480
                else -> true
            }
        }
    }
}
