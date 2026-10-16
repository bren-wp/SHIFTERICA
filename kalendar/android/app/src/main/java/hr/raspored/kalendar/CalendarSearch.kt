package hr.raspored.kalendar

import java.text.Normalizer
import java.util.Locale

/** Accent-insensitive full calendar search; never changes or filters saved entries. */
object CalendarSearch {
    private val marks = Regex("\\p{M}+")

    fun searchable(value: String): String {
        val lower = value.lowercase(Locale.ROOT).replace('đ', 'd')
        return marks.replace(Normalizer.normalize(lower, Normalizer.Form.NFD), "")
    }

    /** Shared filter used by day, global search and category chips; never alters storage. */
    fun displayed(
        entries: List<CalendarEntry>, day: java.time.LocalDate,
        query: String, searchAll: Boolean, category: String = "Sve"
    ): List<CalendarEntry> {
        val base = when {
            query.isNotBlank() -> find(entries, query)
            searchAll -> entries.sortedWith(compareBy({ it.date }, { it.start }, { it.id }))
            else -> EntryRules.forDay(entries, day)
        }
        return if (category == "Sve") base
            else base.filter { it.category == category }
    }

    fun find(entries: List<CalendarEntry>, query: String): List<CalendarEntry> {
        val term = searchable(query.trim())
        if (term.isEmpty()) return emptyList()
        return entries.asSequence()
            .filter { entry ->
                listOf(entry.date, entry.title, entry.note, entry.category,
                    entry.start, entry.end).any { searchable(it).contains(term) }
            }
            .sortedWith(compareBy({ it.date }, { it.start }, { it.id }))
            .toList()
    }
}
