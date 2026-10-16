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



/** Preset groups deliberately stay outside the six Raspored quick choices. */
data class CalendarShiftPresetGroup(val name: String, val presets: List<CalendarShiftPreset>)


object CalendarShiftPresets {
    /** Idempotent quick choices never modify an existing user's own notes or colors. */
    fun alreadyScheduled(
        existing: List<CalendarEntry>, preset: CalendarShiftPreset, date: LocalDate
    ): Boolean = existing.any { item ->
        item.date == date.toString() && item.category == "Smjena" &&
            item.title == preset.title && item.start == preset.start &&
            item.end == preset.end
    }

    val builtIn: List<CalendarShiftPreset> = listOf(
        CalendarShiftPreset("N", "Noćna smjena", "19:00", "07:00", 5),
        CalendarShiftPreset("D", "Dnevna smjena", "07:00", "19:00", 1),
        CalendarShiftPreset("J", "Jutarnja smjena", "07:00", "15:00", 0),
        CalendarShiftPreset("P", "Popodnevna smjena", "14:00", "22:00", 3),
        CalendarShiftPreset("GO", "Godišnji odmor", color = 4),
        CalendarShiftPreset("BO", "Bolovanje", color = 2)
    )

    /** Additional calendar-only patterns; no pay rates, wages or accounting fields. */
    val additionalGroups: List<CalendarShiftPresetGroup> = listOf(
        CalendarShiftPresetGroup("Rane i rotacijske", listOf(
            CalendarShiftPreset("R", "Rana smjena", "05:00", "13:00", 0),
            CalendarShiftPreset("S1", "Prva smjena", "06:00", "14:00", 1),
            CalendarShiftPreset("S2", "Druga smjena", "14:00", "22:00", 3),
            CalendarShiftPreset("S3", "Treća smjena", "22:00", "06:00", 5),
            CalendarShiftPreset("V", "Večernja smjena", "16:00", "00:00", 3),
            CalendarShiftPreset("U", "Standardna smjena", "08:00", "16:00", 0)
        )),
        CalendarShiftPresetGroup("Kraće smjene", listOf(
            CalendarShiftPreset("4J", "Četverosatna jutarnja", "08:00", "12:00", 0),
            CalendarShiftPreset("4P", "Četverosatna popodnevna", "12:00", "16:00", 3),
            CalendarShiftPreset("6J", "Šestosatna jutarnja", "06:00", "12:00", 0),
            CalendarShiftPreset("6P", "Šestosatna popodnevna", "12:00", "18:00", 3),
            CalendarShiftPreset("6N", "Šestosatna noćna", "00:00", "06:00", 5),
            CalendarShiftPreset("6V", "Šestosatna večernja", "16:00", "22:00", 3)
        )),
        CalendarShiftPresetGroup("Produžene i 24-satne", listOf(
            CalendarShiftPreset("10D", "Desetosatna dnevna", "07:00", "17:00", 1),
            CalendarShiftPreset("10N", "Desetosatna noćna", "19:00", "05:00", 5),
            CalendarShiftPreset("12D", "Dvanaestosatna dnevna", "06:00", "18:00", 1),
            CalendarShiftPreset("12N", "Dvanaestosatna noćna", "18:00", "06:00", 5),
            CalendarShiftPreset("24", "Dvadesetčetverosatna smjena", "08:00", "08:00", 2)
        )),
        CalendarShiftPresetGroup("Dežurstva i posebne", listOf(
            CalendarShiftPreset("DE", "Dežurstvo", "08:00", "08:00", 2),
            CalendarShiftPreset("ND", "Noćno dežurstvo", "19:00", "07:00", 5),
            CalendarShiftPreset("PR", "Pripravnost", "", "", 2),
            CalendarShiftPreset("IN", "Intervencija", "", "", 3),
            CalendarShiftPreset("TE", "Terenski rad", "", "", 0),
            CalendarShiftPreset("RK", "Rad od kuće", "", "", 4),
            CalendarShiftPreset("DV", "Dvokratna smjena", "", "", 1),
            CalendarShiftPreset("KL", "Klizno radno vrijeme", "", "", 0)
        )),
        CalendarShiftPresetGroup("Vikendi i odsutnosti", listOf(
            CalendarShiftPreset("VI", "Vikend smjena", "08:00", "16:00", 3),
            CalendarShiftPreset("BL", "Blagdanska smjena", "08:00", "16:00", 3),
            CalendarShiftPreset("SO", "Slobodan dan", "", "", 4),
            CalendarShiftPreset("DP", "Dopust", "", "", 4),
            CalendarShiftPreset("RD", "Roditeljski dopust", "", "", 4),
            CalendarShiftPreset("ED", "Edukacija", "", "", 2)
        ))
    )

    val additional: List<CalendarShiftPreset> = additionalGroups.flatMap { it.presets }
    val all: List<CalendarShiftPreset> = builtIn + additional

    /** Preserve codes in both month and year tiles without extending the backup schema. */
    fun codeFor(item: CalendarEntry): String? = if (item.category != "Smjena") null else
        all.firstOrNull {
            it.title == item.title && it.start == item.start && it.end == item.end
        }?.code

    fun matches(preset: CalendarShiftPreset, query: String): Boolean {
        fun normalize(text: String) = java.text.Normalizer.normalize(
            text.lowercase(java.util.Locale("hr", "HR")).replace("đ", "d"),
            java.text.Normalizer.Form.NFD
        ).replace(Regex("\\p{Mn}+"), "")
        val needle = normalize(query.trim())
        return needle.isBlank() || listOf(preset.code, preset.title, preset.start,
            preset.end).any { normalize(it).contains(needle) }
    }
}
