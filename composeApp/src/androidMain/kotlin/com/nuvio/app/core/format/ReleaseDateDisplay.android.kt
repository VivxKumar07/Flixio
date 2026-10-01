package com.nuvio.app.core.format

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

internal actual fun formatCalendarDate(isoDate: String, localeTag: String, includeYear: Boolean): String {
    return runCatching {
        val date = LocalDate.parse(isoDate)
        val locale = Locale.forLanguageTag(localeTag)
        val formatter = if (includeYear) {
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        } else {
            DateTimeFormatter.ofPattern("d MMM", locale)
        }
        date.format(formatter)
    }.getOrElse { isoDate }
}
