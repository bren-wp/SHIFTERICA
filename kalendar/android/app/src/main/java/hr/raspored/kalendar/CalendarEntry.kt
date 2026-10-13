package hr.raspored.kalendar

import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.util.UUID

/** Independent universal calendar schema. Never stores payroll or user identity. */
data class CalendarEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val title: String,
    val note: String = "",
    val category: String = "Smjena",
    val start: String = "",
    val end: String = "",
    val color: Int = 0
)

object EntryRules {
    val categories = listOf("Smjena", "Događaj", "Napomena", "Slobodno", "Ostalo")

    fun valid(item: CalendarEntry): Boolean {
        if (item.id.isBlank() || item.id.length > 128) return false
        if (runCatching { LocalDate.parse(item.date).toString() }.getOrNull() != item.date) return false
        if (item.title.isBlank() && item.note.isBlank()) return false
        if (item.title.length > 120 || item.note.length > 10_000) return false
        if (item.category !in categories || item.color !in 0..5) return false
        if (item.start.isEmpty() && item.end.isEmpty()) return true
        if (item.start.isEmpty() || item.end.isEmpty()) return false
        // Start and end may span midnight. Equal times represent a 24-hour shift.
        return listOf(item.start, item.end).all {
            it.length == 5 && runCatching { LocalTime.parse(it) }.getOrNull()
                ?.toString()?.let { normalized ->
                    normalized == it || normalized + ":00" == it
                } == true
        }
    }

    fun forDay(items: List<CalendarEntry>, day: LocalDate): List<CalendarEntry> =
        items.filter { it.date == day.toString() }
            .sortedWith(compareBy<CalendarEntry>({ it.start.isEmpty() }, { it.start }, { it.title }))
}

/** Monday-first seven-day window, including weeks spanning months or years. */
object CalendarWeeks {
    /** Render only the number of week rows actually needed by this month. */
    fun monthCells(month: YearMonth): List<LocalDate> {
        val first = month.atDay(1)
        val offset = first.dayOfWeek.value - 1
        val cellCount = ((offset + month.lengthOfMonth() + 6) / 7) * 7
        val monday = first.minusDays(offset.toLong())
        return List(cellCount) { monday.plusDays(it.toLong()) }
    }

    fun containing(date: LocalDate): List<LocalDate> {
        val monday = date.minusDays((date.dayOfWeek.value - 1).toLong())
        return List(7) { monday.plusDays(it.toLong()) }
    }
}
