package hr.raspored.kalendar

import java.time.LocalTime
import java.util.Locale

/**
 * Legacy-calendar helper functions retained for archive compatibility.
 * Screens from the retired standalone UI were removed; old events are
 * still read from their original private storage without conversion.
 */
internal object CalendarScheduledTime {
    fun minutes(entry: CalendarEntry): Int {
        if (entry.start.isBlank() || entry.end.isBlank()) return 0
        return runCatching {
            val start = LocalTime.parse(entry.start).toSecondOfDay() / 60
            val end = LocalTime.parse(entry.end).toSecondOfDay() / 60
            if (start == end) 1440 else (end - start + 1440) % 1440
        }.getOrDefault(0)
    }

    fun display(minutes: Int): String =
        "${minutes / 60} h ${minutes % 60} min"
}

/** Stable archive labels: retain the previous per-event categories/codes. */
internal fun calendarTileCode(item: CalendarEntry): String {
    CalendarShiftPresets.codeFor(item)?.let { return it }
    val title = item.title.trim()
    if (item.category != "Smjena") {
        return if (title.isBlank()) "•" else title.take(2).uppercase(Locale("hr", "HR"))
    }
    val lowered = title.lowercase(Locale("hr", "HR"))
    return when {
        lowered.startsWith("noć") || lowered.startsWith("noc") -> "N"
        lowered.startsWith("dnev") -> "D"
        lowered.startsWith("jut") -> "J"
        lowered.startsWith("pop") -> "P"
        lowered.startsWith("godiš") || lowered.startsWith("godis") -> "GO"
        lowered.startsWith("bolov") -> "BO"
        title.isNotBlank() -> title.take(2).uppercase(Locale("hr", "HR"))
        else -> "•"
    }
}
