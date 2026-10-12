package hr.raspored.kalendar

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject

/** Independent, private device-only storage. Commit first; update UI only on success. */
class CalendarStore(context: Context) {
    private val prefs = context.getSharedPreferences("raspored.universal.v1", Context.MODE_PRIVATE)
    val entries = mutableStateListOf<CalendarEntry>()

    init {
        val raw = prefs.getString("items", null)
        if (raw != null) runCatching {
            val array = JSONArray(raw)
            val seen = HashSet<String>()
            repeat(array.length().coerceAtMost(100_000)) { index ->
                val obj = array.optJSONObject(index) ?: return@repeat
                val entry = CalendarEntry(
                    id = obj.optString("id"),
                    date = obj.optString("date"),
                    title = obj.optString("title"),
                    note = obj.optString("note"),
                    category = obj.optString("category", "Smjena"),
                    start = obj.optString("start"),
                    end = obj.optString("end"),
                    color = obj.optInt("color", 0)
                )
                if (EntryRules.valid(entry) && seen.add(entry.id)) entries.add(entry)
            }
        }
    }

    private fun persist(next: List<CalendarEntry>): Boolean {
        val array = JSONArray()
        next.forEach { entry ->
            array.put(JSONObject().apply {
                put("id", entry.id)
                put("date", entry.date)
                put("title", entry.title)
                put("note", entry.note)
                put("category", entry.category)
                put("start", entry.start)
                put("end", entry.end)
                put("color", entry.color)
            })
        }
        return prefs.edit().putString("items", array.toString()).commit()
    }

    fun save(entry: CalendarEntry): Boolean {
        if (!EntryRules.valid(entry)) return false
        val next = entries.toMutableList()
        val index = next.indexOfFirst { it.id == entry.id }
        if (index >= 0) next[index] = entry else next.add(entry)
        if (!persist(next)) return false
        entries.clear()
        entries.addAll(next)
        return true
    }

    /**
     * Import is merge-only. Validate the entire backup before one persistent
     * transaction; user-created local entries always win on identical IDs.
     */
    fun mergeMissing(imported: List<CalendarEntry>): Int {
        require(imported.size <= CalendarBackup.MAX_ENTRIES &&
            imported.all(EntryRules::valid) &&
            imported.map { it.id }.toSet().size == imported.size) {
            "Sigurnosna kopija nije valjana."
        }
        val currentIds = entries.mapTo(HashSet()) { it.id }
        val fresh = imported.filter { it.id !in currentIds }
        if (fresh.isEmpty()) return 0
        val next = entries.toList() + fresh
        require(next.size <= CalendarBackup.MAX_ENTRIES) { "Kalendar ima previše zapisa." }
        check(persist(next)) { "Nije moguće spremiti uvezene zapise." }
        entries.clear()
        entries.addAll(next)
        return fresh.size
    }

    fun delete(id: String): Boolean {
        val next = entries.filterNot { it.id == id }
        if (next.size == entries.size || !persist(next)) return false
        entries.clear()
        entries.addAll(next)
        return true
    }
}
