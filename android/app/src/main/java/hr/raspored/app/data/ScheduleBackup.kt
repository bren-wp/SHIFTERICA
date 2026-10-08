package hr.raspored.app.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import hr.raspored.app.model.ShiftCatalog
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/**
 * Interoperable v1 JSON backup for Android/iOS. Nothing is uploaded automatically.
 * Restore is merge-only: no existing date is overwritten or deleted.
 */
object ScheduleBackup {
    private const val FORMAT = "raspored-schedule-backup"
    const val MAX_BYTES = 2_000_000
    private const val MAX_DATES = 25_000
    private const val MAX_CUSTOM = 100

    data class RestoreResult(val addedDates: Int, val importedCustom: Int)

    fun encode(schedule: ScheduleStore, library: ShiftLibraryStore): String {
        val dates = JSONObject()
        schedule.snapshot().toSortedMap().forEach { (date, code) ->
            dates.put(date.toString(), code)
        }
        val custom = JSONArray()
        library.all.filter { it.custom }.forEach { shift ->
            custom.put(JSONObject().apply {
                put("code", shift.code)
                put("name", shift.name)
                put("start", shift.start ?: JSONObject.NULL)
                put("end", shift.end ?: JSONObject.NULL)
                put("secondaryStart", shift.secondaryStart ?: JSONObject.NULL)
                put("secondaryEnd", shift.secondaryEnd ?: JSONObject.NULL)
                put("background", shift.color.toArgb())
                put("foreground", shift.textColor.toArgb())
                put("fontSize", shift.fontSize)
            })
        }
        val builtIns = JSONArray()
        library.all.filterNot { it.custom }.forEach { shift ->
            builtIns.put(JSONObject().apply {
                put("code", shift.code)
                put("background", shift.color.toArgb())
                put("foreground", shift.textColor.toArgb())
            })
        }
        return JSONObject().apply {
            put("format", FORMAT)
            put("version", 1)
            put("dates", dates)
            put("customShifts", custom)
            put("builtInColors", builtIns)
        }.toString(2)
    }

    fun restore(raw: String, schedule: ScheduleStore, library: ShiftLibraryStore): RestoreResult {
        require(raw.toByteArray(Charsets.UTF_8).size <= MAX_BYTES) { "Datoteka je prevelika." }
        val root = JSONObject(raw)
        require(root.optString("format") == FORMAT && root.optInt("version") == 1) {
            "Nepodržan format sigurnosne kopije."
        }
        val dates = root.getJSONObject("dates")
        require(dates.length() <= MAX_DATES) { "Previše datuma u sigurnosnoj kopiji." }
        val customs = root.optJSONArray("customShifts") ?: JSONArray()
        require(customs.length() <= MAX_CUSTOM) { "Previše vlastitih smjena." }

        val valid = linkedMapOf<LocalDate, String>()
        val importedCodes = mutableSetOf<String>()
        val novelShifts = JSONArray()
        val timePattern = Regex("""^(?:[01]\d|2[0-3]):[0-5]\d$""")
        for (i in 0 until customs.length()) {
            val item = customs.getJSONObject(i)
            val code = item.getString("code")
            require(code.matches(Regex("^[A-Z0-9]{1,4}$"))) { "Neispravna oznaka smjene." }
            require(ShiftCatalog.byCode(code) == null && importedCodes.add(code)) {
                "Neispravna ili ponovljena vlastita smjena."
            }
            require(item.getString("name").length in 1..100) { "Neispravan naziv smjene." }
            listOf("start", "end", "secondaryStart", "secondaryEnd").forEach { key ->
                if (!item.isNull(key)) {
                    require(timePattern.matches(item.getString(key))) {
                        "Neispravan vremenski interval."
                    }
                }
            }
            // An already installed custom code wins over imported definitions.
            if (library.byCode(code) == null) novelShifts.put(item)
        }
        val known = (library.all.map { it.code } + importedCodes).toSet()
        val keys = dates.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val date = LocalDate.parse(key)
            require(date.year in 1900..2200) { "Datum izvan dopuštenog raspona." }
            val code = dates.getString(key)
            require(code in known) { "Sigurnosna kopija sadrži nepoznatu smjenu." }
            valid[date] = code
        }

        // Definitions are validated before the schedule is changed.
        var countCustom = 0
        if (novelShifts.length() > 0) {
            countCustom = library.importJson(novelShifts.toString()).getOrThrow()
        }
        val colors = root.optJSONArray("builtInColors") ?: JSONArray()
        if (colors.length() <= ShiftCatalog.all.size) {
            for (i in 0 until colors.length()) {
                val item = colors.getJSONObject(i)
                val code = item.getString("code")
                if (ShiftCatalog.byCode(code) != null) {
                    library.updateBuiltIn(
                        code,
                        Color(item.getInt("background")),
                        Color(item.getInt("foreground"))
                    ).getOrThrow()
                }
            }
        }
        return RestoreResult(schedule.mergeMissing(valid), countCustom)
    }
}
