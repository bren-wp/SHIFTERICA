package hr.raspored.kalendar

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream

class CalendarBackupTest {
    private val a = CalendarEntry(
        id = "e1", date = "2026-10-12", title = "Smjena",
        note = "Važno: novi raspored", start = "22:00", end = "06:00"
    )
    private val b = CalendarEntry(
        id = "e2", date = "2026-10-12", title = "",
        note = "Samo napomena", category = "Napomena", color = 3
    )

    @Test fun roundTripKeepsNotesAndMultipleItemsOnSameDate() {
        val bytes = CalendarBackup.encode(listOf(b, a))
        assertEquals(listOf(a, b), CalendarBackup.decode(bytes))
        assertArrayEquals(bytes, CalendarBackup.readLimited(ByteArrayInputStream(bytes)))
    }

    @Test fun invalidDataAndDuplicateIdsNeverRestore() {
        val root = JSONObject(String(CalendarBackup.encode(listOf(a))))
        root.getJSONArray("entries").put(root.getJSONArray("entries").getJSONObject(0))
        assertThrows(IllegalArgumentException::class.java) {
            CalendarBackup.decode(root.toString().toByteArray())
        }
        root.put("version", 999)
        assertThrows(IllegalArgumentException::class.java) {
            CalendarBackup.decode(root.toString().toByteArray())
        }
    }

    @Test fun tooLargeFileRejectedBeforeParsing() {
        assertThrows(IllegalArgumentException::class.java) {
            CalendarBackup.readLimited(ByteArrayInputStream(ByteArray(CalendarBackup.MAX_BYTES + 1)))
        }
    }
}
