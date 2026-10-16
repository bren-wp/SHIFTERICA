package hr.raspored.kalendar

import java.time.LocalDate

/** Generic suggestions open a draft editor; no record is saved until confirmation. */
data class CalendarEventPreset(val code: String, val title: String, val category: String, val color: Int) {
    fun draft(date: LocalDate): CalendarEntry = CalendarEntry(
        date = date.toString(), title = title, category = category, color = color
    )
}

object CalendarEventPresets {
    val all = listOf(
        CalendarEventPreset("OB", "Obveza", "Obveza", 0),
        CalendarEventPreset("TE", "Termin", "Termin", 1),
        CalendarEventPreset("SA", "Sastanak", "Termin", 1),
        CalendarEventPreset("RO", "Rođendan", "Rođendan", 2),
        CalendarEventPreset("GD", "Godišnjica", "Godišnjica", 2),
        CalendarEventPreset("LI", "Liječnički pregled", "Zdravlje", 0),
        CalendarEventPreset("TR", "Trening", "Aktivnost", 4),
        CalendarEventPreset("SK", "Školska obveza", "Škola", 3),
        CalendarEventPreset("IS", "Ispit", "Škola", 3),
        CalendarEventPreset("PU", "Putovanje", "Putovanje", 2),
        CalendarEventPreset("KU", "Kupnja", "Obveza", 3),
        CalendarEventPreset("DR", "Druženje", "Događaj", 5),
        CalendarEventPreset("NA", "Napomena", "Napomena", 4),
        CalendarEventPreset("OBI", "Obiteljski događaj", "Obitelj", 5),
        CalendarEventPreset("SD", "Slobodan dan", "Slobodno", 4)
    )
}
