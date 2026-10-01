package com.nuvio.app.core.format

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

actual fun formatLocalDateTime(epochMs: Long): String {
    return runCatching {
        val instant = Instant.ofEpochMilli(epochMs)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
        zonedDateTime.format(formatter)
    }.getOrElse { "" }
}
