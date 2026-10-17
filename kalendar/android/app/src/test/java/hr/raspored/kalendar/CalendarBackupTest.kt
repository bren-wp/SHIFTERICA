package hr.raspored.kalendar

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.charset.MalformedInputException

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
        assertEquals(listOf(b, a), CalendarBackup.decode(bytes))
        assertArrayEquals(bytes, CalendarBackup.readLimited(ByteArrayInputStream(bytes)))
    }

    @Test fun legacyExportRetainsAllEventKindsAndRejectsShiftBackupAsLegacy() {
        val events = listOf(
            a.copy(id = "shift", date = "2026-10-17", category = "Smjena"),
            b.copy(id = "note", date = "2026-10-17", note = "Osobna napomena"),
            CalendarEntry(id = "appointment", date = "2026-10-17",
                title = "Liječnik", category = "Zdravlje", start = "08:00", end = "09:00"),
            CalendarEntry(id = "birthday", date = "2026-10-17",
                title = "Rođendan", category = "Rođendan")
        )
        val exported = CalendarBackup.encode(events)
        val restored = CalendarBackup.decode(exported)
        assertEquals(4, restored.size)
        assertEquals(events.toSet(), restored.toSet())
        assertEquals(1, restored.map { it.date }.distinct().size)
        val wrongFormat = JSONObject(String(exported))
            .put("format", "raspored-schedule-backup")
        assertThrows(IllegalArgumentException::class.java) {
            CalendarBackup.decode(wrongFormat.toString().toByteArray(Charsets.UTF_8))
        }
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

    @Test fun exportRejectsDuplicateIdsBeforeWritingAnyFile() {
        assertThrows(IllegalArgumentException::class.java) {
            CalendarBackup.encode(listOf(a, a.copy(title = "Duplicate")))
        }
    }

    @Test fun distinctIdCopyKeepsBothEventsAcrossBackupRoundTrip() {
        val copied = a.copy(id = "e3", date = "2026-10-13")
        assertEquals(listOf(a, copied), CalendarBackup.decode(CalendarBackup.encode(listOf(a, copied))))
    }

    @Test fun malformedUtf8BackupIsRejectedWithoutReplacement() {
        val invalid = CalendarBackup.encode(listOf(a)) +
            byteArrayOf(0xC3.toByte(), 0x28)
        assertThrows(MalformedInputException::class.java) {
            CalendarBackup.decode(invalid)
        }
    }

    @Test fun rejectsCoercedJsonTypesToMatchIosAndProtectUserData() {
        val original = String(CalendarBackup.encode(listOf(a)))
        for ((field, invalid) in listOf(
            "title" to 42,
            "note" to true,
            "id" to JSONObject.NULL,
            "color" to "0",
            "category" to 9
        )) {
            val root = JSONObject(original)
            root.getJSONArray("entries").getJSONObject(0).put(field, invalid)
            assertThrows("Invalid type for $field must not be coerced",
                IllegalArgumentException::class.java) {
                CalendarBackup.decode(root.toString().toByteArray(Charsets.UTF_8))
            }
        }
        val invalidVersion = JSONObject(original).put("version", "1")
        assertThrows(IllegalArgumentException::class.java) {
            CalendarBackup.decode(invalidVersion.toString().toByteArray(Charsets.UTF_8))
        }
    }

    @Test fun tooLargeFileRejectedBeforeParsing() {
        assertThrows(IllegalArgumentException::class.java) {
            CalendarBackup.readLimited(ByteArrayInputStream(ByteArray(CalendarBackup.MAX_BYTES + 1)))
        }
    }
}
