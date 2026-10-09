package hr.raspored.app.model

import java.time.LocalDate

/** Pure date/code/name search; upcoming entries come first, then recent history. */
internal object ScheduleSearch {
    fun find(
        entries: Map<LocalDate, String>,
        namesByCode: Map<String, String>,
        query: String,
        today: LocalDate,
        maxResults: Int = 50
    ): List<Pair<LocalDate, String>> {
        if (maxResults <= 0) return emptyList()
        val term = query.trim()
        return entries.asSequence()
            .filter { (date, code) ->
                term.isEmpty() ||
                    date.toString().contains(term, ignoreCase = true) ||
                    code.contains(term, ignoreCase = true) ||
                    namesByCode[code]?.contains(term, ignoreCase = true) == true
            }
            .sortedWith(compareBy<Map.Entry<LocalDate, String>>(
                { if (it.key.isBefore(today)) 1 else 0 },
                { if (it.key.isBefore(today)) -it.key.toEpochDay() else it.key.toEpochDay() }
            ))
            .take(maxResults)
            .map { it.key to it.value }
            .toList()
    }
}
