package hr.raspored.kalendar

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream

/** Exportable JSON v1 shared with the iOS calendar; import never overwrites saved items. */
object CalendarBackup {
    private const val FORMAT = "raspored-universal-calendar"
    const val MAX_BYTES = 5_000_000
    const val MAX_ENTRIES = 15_000

    fun encode(entries: List<CalendarEntry>): ByteArray {
        require(entries.size <= MAX_ENTRIES && entries.all(EntryRules::valid) &&
            entries.map { it.id }.toSet().size == entries.size) {
            "Neispravan kalendar ili previše zapisa."
        }
        val sorted = entries.sortedWith(compareBy({ it.date }, { it.start }, { it.title }, { it.id }))
        val items = JSONArray()
        sorted.forEach { entry ->
            items.put(JSONObject().apply {
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
        val bytes = JSONObject().apply {
            put("format", FORMAT)
            put("version", 1)
            put("entries", items)
        }.toString(2).toByteArray(Charsets.UTF_8)
        require(bytes.size <= MAX_BYTES) { "Sigurnosna kopija je prevelika." }
        return bytes
    }

    fun decode(bytes: ByteArray): List<CalendarEntry> {
        require(bytes.size in 1..MAX_BYTES) { "Neispravna ili prevelika datoteka." }
        val root = JSONObject(bytes.toString(Charsets.UTF_8))
        require(root.getString("format") == FORMAT && root.getInt("version") == 1) {
            "Nepodržan format sigurnosne kopije."
        }
        val items = root.getJSONArray("entries")
        require(items.length() <= MAX_ENTRIES) { "Previše zapisa." }
        val seen = HashSet<String>()
        return List(items.length()) { i ->
            val obj = items.getJSONObject(i)
            val entry = CalendarEntry(
                id = obj.getString("id"),
                date = obj.getString("date"),
                title = obj.getString("title"),
                note = obj.getString("note"),
                category = obj.getString("category"),
                start = obj.getString("start"),
                end = obj.getString("end"),
                color = obj.getInt("color")
            )
            require(EntryRules.valid(entry) && seen.add(entry.id)) {
                "Neispravan ili ponovljen zapis u kopiji."
            }
            entry
        }
    }

    /** Bound reading before parsing any untrusted user-selected file. */
    fun readLimited(stream: InputStream): ByteArray {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var count = 0
        while (true) {
            val read = stream.read(buffer)
            if (read == -1) break
            count += read
            require(count <= MAX_BYTES) { "Sigurnosna kopija je prevelika." }
            out.write(buffer, 0, read)
        }
        return out.toByteArray()
    }
}
