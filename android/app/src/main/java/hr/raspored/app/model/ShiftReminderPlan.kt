package hr.raspored.app.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Pure, platform-independent reminder rules. No stored schedule is changed. */
object ShiftReminderPlan {
    enum class Kind { EVENING, DEPARTURE }

    data class Event(
        val shiftDate: LocalDate,
        val code: String,
        val kind: Kind,
        val at: LocalDateTime
    ) {
        val identifier: Int get() = (shiftDate.toEpochDay() * 4 + kind.ordinal).toInt()
    }

    fun upcoming(
        entries: Map<LocalDate, String>,
        now: LocalDateTime,
        eveningEnabled: Boolean = true,
        departureEnabled: Boolean = true,
        maxDays: Long = 60
    ): List<Event> {
        val lastDate = now.toLocalDate().plusDays(maxDays)
        return entries.asSequence()
            .filter { (date, code) ->
                !date.isAfter(lastDate) && !date.isBefore(now.toLocalDate()) &&
                    (code == "D" || code == "N")
            }
            .flatMap { (date, code) ->
                buildList {
                    if (eveningEnabled) add(Event(
                        date, code, Kind.EVENING,
                        LocalDateTime.of(date.minusDays(1), LocalTime.of(20, 0))
                    ))
                    if (departureEnabled) add(Event(
                        date, code, Kind.DEPARTURE,
                        LocalDateTime.of(date, if (code == "D") LocalTime.of(6, 0) else LocalTime.of(18, 0))
                    ))
                }.asSequence()
            }
            .filter { it.at.isAfter(now) }
            .sortedBy { it.at }
            .toList()
    }
}
