package hr.raspored.app.model

import java.time.LocalDate

/** Prefer the next scheduled date, otherwise the most recent past occurrence. */
internal object ShiftUsageNavigator {
    fun closestDate(
        entries: Map<LocalDate, String>,
        code: String,
        today: LocalDate
    ): LocalDate? {
        val dates = entries.asSequence().filter { it.value == code }.map { it.key }.toList()
        return dates.filter { !it.isBefore(today) }.minOrNull() ?: dates.maxOrNull()
    }
}
