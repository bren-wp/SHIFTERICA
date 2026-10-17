package hr.raspored.kalendar

/** Search and ordering for historical calendar events; never mutates storage. */
object LegacyArchiveSearch {
    fun chronological(entries: List<CalendarEntry>): List<CalendarEntry> =
        entries.sortedWith(compareBy({ it.date }, { it.start }, { it.title }, { it.id }))

    fun matching(chronological: List<CalendarEntry>, rawQuery: String): List<CalendarEntry> {
        val query = rawQuery.trim()
        if (query.isEmpty()) return chronological
        return chronological.filter { item ->
            item.date.contains(query, ignoreCase = true) ||
                item.title.contains(query, ignoreCase = true) ||
                item.note.contains(query, ignoreCase = true) ||
                item.category.contains(query, ignoreCase = true) ||
                item.start.contains(query, ignoreCase = true) ||
                item.end.contains(query, ignoreCase = true)
        }
    }
}
