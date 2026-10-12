package hr.raspored.kalendar

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class EntryRulesTest {
    private fun item(date: String = "2026-10-11") =
        CalendarEntry(id = "test", date = date, title = "Moja smjena")

    @Test fun acceptsFreeformNotesAndOvernightShifts() {
        assertTrue(EntryRules.valid(item().copy(title = "", note = "Bilo kakva napomena")))
        assertTrue(EntryRules.valid(item().copy(start = "22:00", end = "06:00")))
        assertTrue(EntryRules.valid(item().copy(start = "08:00", end = "08:00")))
    }

    @Test fun rejectsBrokenTimesDatesAndEmptyContent() {
        assertFalse(EntryRules.valid(item("2026-02-30")))
        assertFalse(EntryRules.valid(item().copy(title = " ", note = " ")))
        assertFalse(EntryRules.valid(item().copy(start = "27:00", end = "06:00")))
        assertFalse(EntryRules.valid(item().copy(start = "07:00", end = "")))
        assertFalse(EntryRules.valid(item().copy(color = 22)))
    }

    @Test fun searchMatchesCroatianTitlesAndNotesWithoutAccents() {
        val entries = listOf(
            item().copy(id = "one", title = "Noćna smjena", note = "Đurđica čeka"),
            item().copy(id = "two", title = "Jutarnja")
        )
        assertEquals(listOf("one"), CalendarSearch.find(entries, " NOCNA ").map { it.id })
        assertEquals(listOf("one"), CalendarSearch.find(entries, "durdica").map { it.id })
        assertTrue(CalendarSearch.find(entries, "nepoznato").isEmpty())
    }

    @Test fun multipleEntriesPerDaySortByStartAndKeepUntimedNotes() {
        val date = LocalDate.parse("2026-10-11")
        val entries = listOf(
            item().copy(id = "a", title = "Bilješka", start = ""),
            item().copy(id = "b", title = "Noćna", start = "22:00", end = "06:00"),
            item().copy(id = "c", title = "Jutro", start = "07:00", end = "15:00"),
            item("2026-10-12").copy(id = "d")
        )
        assertEquals(listOf("c", "b", "a"), EntryRules.forDay(entries, date).map { it.id })
    }
}
