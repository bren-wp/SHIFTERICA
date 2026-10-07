package hr.raspored.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import hr.raspored.app.model.ShiftCatalog
import hr.raspored.app.model.ShiftType
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/** Persistent user-created shift definitions. Built-in shifts always remain available. */
class ShiftLibraryStore(context: Context) {
    private val prefs = context.getSharedPreferences("raspored.shift.library", Context.MODE_PRIVATE)
    private val custom = mutableStateListOf<ShiftType>()

    init { load() }

    val all: List<ShiftType>
        get() = ShiftCatalog.all + custom

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

        val shift = ShiftType(
            code = normalizedCode,
            name = name.trim(),
            shortName = name.trim().take(14),
            start = start?.trim()?.ifBlank { null },
            end = end?.trim()?.ifBlank { null },
            secondaryStart = secondaryStart?.trim()?.ifBlank { null },
            secondaryEnd = secondaryEnd?.trim()?.ifBlank { null },
            color = background,
            textColor = textColor,
            fontSize = fontSize.coerceIn(8, 24),
            custom = true
        )
        val index = custom.indexOfFirst { it.code == normalizedCode }
        if (index >= 0) custom[index] = shift else custom.add(shift)
        persist()
        shift
    }

    fun importJson(raw: String): Result<Int> = runCatching {
        val source = raw.trim()
        val array = if (source.startsWith("[")) JSONArray(source) else JSONArray().put(JSONObject(source))
        var imported = 0
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val result = save(
                name = item.getString("name"),
                code = item.getString("code"),
                background = Color(item.optInt("background", Color(0xFF13B7F3).toArgb())),
                textColor = Color(item.optInt("foreground", Color(0xFF06131F).toArgb())),
                fontSize = item.optInt("fontSize", 12),
                start = item.optNullable("start"),
                end = item.optNullable("end"),
                secondaryStart = item.optNullable("secondaryStart"),
                secondaryEnd = item.optNullable("secondaryEnd")
            )
            result.getOrThrow()
            imported++
        }
        imported
    }

    fun delete(code: String) {
        if (custom.removeAll { it.code == normalizeCode(code) }) persist()
    }

    private fun load() {
        val raw = prefs.getString(KEY, null) ?: return
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

    private fun persist() {
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
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    private fun normalizeCode(value: String): String =
        value.trim().uppercase(Locale("hr", "HR")).filter { it.isLetterOrDigit() }.take(4)

    private fun JSONObject.optNullable(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private companion object { const val KEY = "customShifts" }
}
