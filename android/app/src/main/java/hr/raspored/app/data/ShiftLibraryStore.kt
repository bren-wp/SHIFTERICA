package hr.raspored.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import hr.raspored.app.model.ShiftCatalog
import hr.raspored.app.model.ShiftType
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class BuiltInOverride(
    val background: Color,
    val textColor: Color,
    val start: String? = null,
    val end: String? = null
)

private data class ShiftIntervals(
    val start: String?,
    val end: String?,
    val secondaryStart: String?,
    val secondaryEnd: String?
)

/** Persistent shift definitions. Default built-in times can be restored safely. */
class ShiftLibraryStore(context: Context) {
    private val prefs = context.getSharedPreferences("raspored.shift.library", Context.MODE_PRIVATE)
    private val custom = mutableStateListOf<ShiftType>()
    private val builtInOverrides = mutableStateMapOf<String, BuiltInOverride>()

    init {
        loadCustom()
        loadBuiltInOverrides()
    }

    val all: List<ShiftType>
        get() = ShiftCatalog.all.map { base ->
            val override = builtInOverrides[base.code] ?: return@map base
            base.copy(
                color = override.background,
                textColor = override.textColor,
                start = override.start ?: base.start,
                end = override.end ?: base.end
            )
        } + custom

    fun byCode(code: String?): ShiftType? = all.firstOrNull { it.code == code }

    fun save(
        name: String,
        code: String,
        background: Color,
        textColor: Color,
        fontSize: Int,
        start: String? = null,
        end: String? = null,
        secondaryStart: String? = null,
        secondaryEnd: String? = null
    ): Result<ShiftType> = runCatching {
        val normalizedCode = normalizeCode(code)
        require(normalizedCode.length in 1..4) { "Skraćenica mora imati od 1 do 4 znaka." }
        require(name.trim().isNotEmpty()) { "Naziv smjene ne može biti prazan." }
        require(ShiftCatalog.byCode(normalizedCode) == null) { "Ta je skraćenica rezervirana za ugrađenu smjenu." }

        val intervals = normalizeIntervals(start, end, secondaryStart, secondaryEnd)
        val cleanName = name.trim()
        val shift = ShiftType(
            code = normalizedCode,
            name = cleanName,
            shortName = cleanName.take(14),
            start = intervals.start,
            end = intervals.end,
            secondaryStart = intervals.secondaryStart,
            secondaryEnd = intervals.secondaryEnd,
            color = background,
            textColor = textColor,
            fontSize = fontSize.coerceIn(8, 24),
            custom = true
        )

        val index = custom.indexOfFirst { it.code == normalizedCode }
        if (index >= 0) custom[index] = shift else custom.add(shift)
        persistCustom()
        shift
    }

    fun updateBuiltIn(
        code: String,
        background: Color,
        textColor: Color,
        start: String? = null,
        end: String? = null
    ): Result<ShiftType> = runCatching {
        val normalized = normalizeCode(code)
        val base = requireNotNull(ShiftCatalog.byCode(normalized)) {
            "Nepoznata ugrađena smjena."
        }

        val existing = builtInOverrides[normalized]
        val requestedStart = start ?: existing?.start ?: base.start
        val requestedEnd = end ?: existing?.end ?: base.end
        if (base.start != null) {
            require(requestedStart != null && requestedEnd != null &&
                TIME_PATTERN.matches(requestedStart) && TIME_PATTERN.matches(requestedEnd) &&
                requestedStart != requestedEnd) {
                "Unesite različita vremena u formatu HH:mm."
            }
        }
        builtInOverrides[normalized] = BuiltInOverride(
            background = background,
            textColor = textColor,
            start = if (base.start != null) requestedStart else null,
            end = if (base.end != null) requestedEnd else null
        )
        persistBuiltInOverrides()

        base.copy(
            color = background,
            textColor = textColor,
            start = if (base.start != null) requestedStart else null,
            end = if (base.end != null) requestedEnd else null
        )
    }

    fun resetBuiltIn(code: String) {
        builtInOverrides.remove(normalizeCode(code))
        persistBuiltInOverrides()
    }

    fun importJson(raw: String): Result<Int> = runCatching {
        val source = raw.trim()
        require(source.isNotEmpty()) { "JSON za uvoz je prazan." }

        val array = if (source.startsWith("[")) {
            JSONArray(source)
        } else {
            JSONArray().put(JSONObject(source))
        }

        var imported = 0
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            save(
                name = item.getString("name"),
                code = item.getString("code"),
                background = Color(item.optInt("background", Color(0xFF13B7F3).toArgb())),
                textColor = Color(item.optInt("foreground", Color(0xFF06131F).toArgb())),
                fontSize = item.optInt("fontSize", 12),
                start = item.optNullable("start"),
                end = item.optNullable("end"),
                secondaryStart = item.optNullable("secondaryStart"),
                secondaryEnd = item.optNullable("secondaryEnd")
            ).getOrThrow()
            imported++
        }
        imported
    }

    /** Refuse deletion while a calendar date still refers to this custom shift. */
    fun delete(code: String, assignedCodes: Collection<String>): Boolean {
        val normalized = normalizeCode(code)
        if (!hr.raspored.app.model.ShiftDeletionPolicy.canDelete(assignedCodes, normalized)) {
            return false
        }
        val removed = custom.removeAll { it.code == normalized }
        if (removed) persistCustom()
        return removed
    }

    private fun normalizeIntervals(
        start: String?,
        end: String?,
        secondaryStart: String?,
        secondaryEnd: String?
    ): ShiftIntervals {
        val primaryStart = normalizeTime(start)
        val primaryEnd = normalizeTime(end)
        val secondStart = normalizeTime(secondaryStart)
        val secondEnd = normalizeTime(secondaryEnd)

        require((primaryStart == null) == (primaryEnd == null)) {
            "Početak i završetak smjene moraju biti uneseni zajedno."
        }
        require((secondStart == null) == (secondEnd == null)) {
            "Početak i završetak drugog intervala moraju biti uneseni zajedno."
        }

        return ShiftIntervals(primaryStart, primaryEnd, secondStart, secondEnd)
    }

    private fun normalizeTime(value: String?): String? {
        val trimmed = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        require(TIME_PATTERN.matches(trimmed)) { "Vrijeme mora biti u formatu HH:mm." }
        runCatching { LocalTime.parse(trimmed, TIME_FORMATTER) }
            .getOrElse { throw IllegalArgumentException("Vrijeme mora biti u formatu HH:mm.") }
        return trimmed
    }

    private fun loadCustom() {
        val raw = prefs.getString(CUSTOM_KEY, null) ?: return
        runCatching {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                custom += ShiftType(
                    code = item.getString("code"),
                    name = item.getString("name"),
                    shortName = item.optString("shortName", item.getString("name").take(14)),
                    start = item.optNullable("start"),
                    end = item.optNullable("end"),
                    secondaryStart = item.optNullable("secondaryStart"),
                    secondaryEnd = item.optNullable("secondaryEnd"),
                    color = Color(item.getInt("background")),
                    textColor = Color(item.getInt("foreground")),
                    fontSize = item.optInt("fontSize", 12),
                    custom = true
                )
            }
        }
    }

    private fun loadBuiltInOverrides() {
        val current = prefs.getString(BUILTIN_OVERRIDES_KEY, null)
        if (current != null) {
            runCatching {
                val root = JSONObject(current)
                root.keys().forEach { code ->
                    val base = ShiftCatalog.byCode(code) ?: return@forEach
                    val item = root.getJSONObject(code)
                    builtInOverrides[code] = BuiltInOverride(
                        background = Color(item.optInt("background", base.color.toArgb())),
                        textColor = Color(item.optInt("foreground", base.textColor.toArgb())),
                        start = item.optNullable("start")?.takeIf { TIME_PATTERN.matches(it) },
                        end = item.optNullable("end")?.takeIf { TIME_PATTERN.matches(it) }
                    )
                }
            }
            return
        }

        migrateLegacyBuiltInColors()
    }

    private fun migrateLegacyBuiltInColors() {
        val legacy = prefs.getString(LEGACY_BUILTIN_COLORS_KEY, null) ?: return
        runCatching {
            val root = JSONObject(legacy)
            root.keys().forEach { code ->
                val base = ShiftCatalog.byCode(code) ?: return@forEach
                val item = root.getJSONObject(code)
                builtInOverrides[code] = BuiltInOverride(
                    background = Color(item.optInt("background", base.color.toArgb())),
                    textColor = Color(item.optInt("foreground", base.textColor.toArgb()))
                )
            }
            persistBuiltInOverrides()
            prefs.edit().remove(LEGACY_BUILTIN_COLORS_KEY).apply()
        }
    }

    private fun persistBuiltInOverrides() {
        val root = JSONObject()
        builtInOverrides.forEach { (code, override) ->
            root.put(code, JSONObject().apply {
                put("background", override.background.toArgb())
                put("foreground", override.textColor.toArgb())
                put("start", override.start ?: JSONObject.NULL)
                put("end", override.end ?: JSONObject.NULL)
            })
        }
        prefs.edit().putString(BUILTIN_OVERRIDES_KEY, root.toString()).apply()
    }

    private fun persistCustom() {
        val array = JSONArray()
        custom.forEach { shift ->
            array.put(JSONObject().apply {
                put("code", shift.code)
                put("name", shift.name)
                put("shortName", shift.shortName)
                put("start", shift.start ?: JSONObject.NULL)
                put("end", shift.end ?: JSONObject.NULL)
                put("secondaryStart", shift.secondaryStart ?: JSONObject.NULL)
                put("secondaryEnd", shift.secondaryEnd ?: JSONObject.NULL)
                put("background", shift.color.toArgb())
                put("foreground", shift.textColor.toArgb())
                put("fontSize", shift.fontSize)
            })
        }
        prefs.edit().putString(CUSTOM_KEY, array.toString()).apply()
    }

    private fun normalizeCode(value: String): String =
        value.trim().uppercase(Locale("hr", "HR")).filter { it.isLetterOrDigit() }.take(4)

    private fun JSONObject.optNullable(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private companion object {
        const val CUSTOM_KEY = "customShifts"
        const val BUILTIN_OVERRIDES_KEY = "builtInOverrides.v2"
        const val LEGACY_BUILTIN_COLORS_KEY = "builtInColors"
        val TIME_PATTERN = Regex("""^(?:[01]\d|2[0-3]):[0-5]\d$""")
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
