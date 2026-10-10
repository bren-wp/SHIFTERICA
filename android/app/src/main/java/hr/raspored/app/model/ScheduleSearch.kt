package hr.raspored.app.model

import java.text.Normalizer
import java.time.LocalDate
import java.util.Locale

/** Pure date/code/name search; upcoming entries come first, then recent history. */
internal object ScheduleSearch {
    // Android's ignoreCase comparison alone does not match Croatian names
    // such as "Noćna" when users type "nocna" without diacritics.
    private val marks = Regex("\\p{M}+")

    private fun searchable(value: String): String {
        val lower = value.lowercase(Locale.ROOT).replace('đ', 'd')
        return marks.replace(Normalizer.normalize(lower, Normalizer.Form.NFD), "")
    }

    fun find(
        entries: Map<LocalDate, String>,
        namesByCode: Map<String, String>,
        query: String,
        today: LocalDate,
        maxResults: Int = 50
    ): List<Pair<LocalDate, String>> {
        if (maxResults <= 0) return emptyList()
        val term = searchable(query.trim())
        return entries.asSequence()
            .filter { (date, code) ->
                term.isEmpty() ||
                    date.toString().contains(term) ||
                    searchable(code).contains(term) ||
                    namesByCode[code]?.let { searchable(it).contains(term) } == true
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
