package hr.raspored.kalendar

import java.time.LocalDate

/** Same built-in shift labels, colors and clock intervals as Raspored.
 * Entries are calendar-only: no payroll, paid hours or tax logic.
 */
data class CalendarShiftPreset(
    val code: String,
    val title: String,
    val start: String = "",
    val end: String = "",
    val color: Int
) {
    fun forDate(date: LocalDate): CalendarEntry = CalendarEntry(
        date = date.toString(), title = title, category = "Smjena",
        start = start, end = end, color = color
    )
}

object CalendarShiftPresets {
    val builtIn: List<CalendarShiftPreset> = listOf(
        CalendarShiftPreset("N", "Noćna smjena", "19:00", "07:00", 5),
        CalendarShiftPreset("D", "Dnevna smjena", "07:00", "19:00", 1),
        CalendarShiftPreset("J", "Jutarnja smjena", "07:00", "15:00", 0),
        CalendarShiftPreset("P", "Popodnevna smjena", "14:00", "22:00", 3),
        CalendarShiftPreset("GO", "Godišnji odmor", color = 4),
        CalendarShiftPreset("BO", "Bolovanje", color = 2)
    )
}
