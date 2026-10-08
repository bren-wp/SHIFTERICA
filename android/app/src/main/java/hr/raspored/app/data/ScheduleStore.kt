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

    fun count(month: YearMonth, code: String): Int =
        monthEntries(month).values.count { it == code }

    /**
     * Merge-only restoration: existing dates always win. A single committed
     * SharedPreferences transaction protects against partial imports.
     */
    fun mergeMissing(imported: Map<LocalDate, String>): Int {
        val missing = imported.filterKeys { it !in entries }
        if (missing.isEmpty()) return 0
        val editor = prefs.edit()
        missing.forEach { (date, code) -> editor.putString("date:$date", code) }
        if (!editor.commit()) return 0
        entries.putAll(missing)
        return missing.size
    }

    fun snapshot(): Map<LocalDate, String> = entries.toMap()

}
