package hr.raspored.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import java.time.LocalDate
import java.time.YearMonth

class ScheduleStore(context: Context) {
    private val prefs = context.getSharedPreferences("raspored.schedule.premium", Context.MODE_PRIVATE)
    val entries = mutableStateMapOf<LocalDate, String>()

    init {
        load()
        if (entries.isEmpty()) seedReferenceOctober2026()
    }

    private fun load() {
        prefs.all.forEach { (key, value) ->
            if (!key.startsWith("date:")) return@forEach
            runCatching { LocalDate.parse(key.removePrefix("date:")) }.getOrNull()?.let { date ->
                (value as? String)?.let { entries[date] = it }
            }
        }
    }

    fun set(date: LocalDate, code: String?) {
        if (code == null) {
            entries.remove(date)
            prefs.edit().remove("date:$date").apply()
        } else {
            entries[date] = code
            prefs.edit().putString("date:$date", code).apply()
        }
    }

    fun code(date: LocalDate): String? = entries[date]

    fun monthEntries(month: YearMonth): Map<LocalDate, String> =
        entries.filterKeys { YearMonth.from(it) == month }

    fun count(month: YearMonth, code: String): Int = monthEntries(month).values.count { it == code }

    private fun seedReferenceOctober2026() {
        val month = YearMonth.of(2026, 10)
        val data = linkedMapOf(
            2 to "D", 3 to "N", 6 to "D", 7 to "N", 10 to "D", 11 to "N",
            14 to "D", 15 to "N", 18 to "D", 19 to "N", 22 to "D", 23 to "N",
            26 to "D", 27 to "N", 28 to "N", 30 to "D", 31 to "N"
        )
        data.forEach { (day, code) -> set(month.atDay(day), code) }
        set(LocalDate.of(2026, 9, 28), "D")
        set(LocalDate.of(2026, 9, 29), "N")
    }
}
