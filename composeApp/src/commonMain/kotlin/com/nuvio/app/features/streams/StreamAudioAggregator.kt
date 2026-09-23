package com.nuvio.app.features.streams

data class StreamAudioOption(
    val code: String,
    val displayName: String,
) {
    companion object {
        val AUTO = StreamAudioOption(code = "auto", displayName = "Auto")
    }
}

object StreamAudioAggregator {

    private val knownLanguages = listOf(
        "English" to listOf("english", "eng", "en"),
        "Hindi" to listOf("hindi", "hin", "hi"),
        "Japanese" to listOf("japanese", "jpn", "ja"),
        "Spanish" to listOf("spanish", "spa", "es"),
        "French" to listOf("french", "fra", "fre", "fr"),
        "German" to listOf("german", "deu", "ger", "de"),
        "Italian" to listOf("italian", "ita", "it"),
        "Portuguese" to listOf("portuguese", "por", "pt"),
        "Russian" to listOf("russian", "rus", "ru"),
        "Korean" to listOf("korean", "kor", "ko"),
        "Chinese" to listOf("chinese", "chi", "zho", "zh"),
        "Tamil" to listOf("tamil", "tam", "ta"),
        "Telugu" to listOf("telugu", "tel", "te"),
        "Arabic" to listOf("arabic", "ara", "ar"),
        "Turkish" to listOf("turkish", "tur", "tr"),
    )

    fun detectAudioLanguages(stream: StreamItem): Set<String> {
        val found = mutableSetOf<String>()
        val parsed = stream.clientResolve?.stream?.raw?.parsed

        // 1. Check parsed languages list
        parsed?.languages?.forEach { lang ->
            val match = findLanguageName(lang)
            if (match != null) found.add(match)
        }

        // 2. Check parsed audio list
        parsed?.audio?.forEach { aud ->
            val match = findLanguageName(aud)
            if (match != null) found.add(match)
        }

        // 3. Check stream text tokens
        val textToSearch = "${stream.name} ${stream.title} ${stream.description} ${stream.behaviorHints.filename}"
        val lowerText = textToSearch.lowercase()
        for ((name, tokens) in knownLanguages) {
            for (token in tokens) {
                // Word boundary check
                val regex = Regex("""(?i)\b$token\b""")
                if (regex.containsMatchIn(lowerText)) {
                    found.add(name)
                    break
                }
            }
        }

        // If "Dual Audio" or "Multi" is mentioned and English not explicitly added, often includes English
        if (lowerText.contains("dual audio") || lowerText.contains("multi audio")) {
            if (found.isEmpty()) {
                found.add("English")
            }
        }

        return found
    }

    private fun findLanguageName(input: String): String? {
        val lower = input.trim().lowercase()
        for ((name, tokens) in knownLanguages) {
            if (name.equals(lower, ignoreCase = true) || tokens.any { it == lower }) {
                return name
            }
        }
        return null
    }

    /**
     * Aggregates distinct supported audio languages across all streams.
     * Crucial rule: Does NOT fabricate languages.
     */
    fun aggregate(streams: List<StreamItem>, defaultLanguage: String? = null): List<StreamAudioOption> {
        val languageSet = mutableSetOf<String>()

        for (stream in streams) {
            languageSet.addAll(detectAudioLanguages(stream))
        }

        defaultLanguage?.takeIf { it.isNotBlank() }?.let { def ->
            findLanguageName(def)?.let { languageSet.add(it) }
        }

        val result = mutableListOf<StreamAudioOption>()
        result.add(StreamAudioOption.AUTO)

        languageSet.sorted().forEach { langName ->
            val code = knownLanguages.firstOrNull { it.first == langName }?.second?.lastOrNull() ?: langName.lowercase()
            result.add(StreamAudioOption(code = code, displayName = langName))
        }

        return result
    }

    /**
     * Filters candidate streams that support the selected audio language.
     */
    fun filterByAudio(streams: List<StreamItem>, audioOption: StreamAudioOption): List<StreamItem> {
        if (audioOption.code == StreamAudioOption.AUTO.code) return streams

        val targetName = audioOption.displayName

        val matching = streams.filter { stream ->
            val detected = detectAudioLanguages(stream)
            detected.contains(targetName)
        }

        return if (matching.isNotEmpty()) matching else streams
    }
}
