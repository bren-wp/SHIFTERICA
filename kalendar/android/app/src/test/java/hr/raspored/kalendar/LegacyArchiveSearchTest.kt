package hr.raspored.kalendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyArchiveSearchTest {
    @Test fun allEventsRemainAccessibleBeyondPreviousTwoHundredLimit() {
        val records = (1..501).map { i ->
            CalendarEntry(
                id = "event-$i",
                date = "2026-10-17",
                title = "Termin $i",
                category = "Termin",
                note = if (i == 501) "Važna bilješka" else ""
            )
        }
        val ordered = LegacyArchiveSearch.chronological(records)
        assertEquals(501, LegacyArchiveSearch.matching(ordered, "").size)
        assertEquals("event-501", LegacyArchiveSearch.matching(ordered, "VAŽNA").single().id)
        assertEquals(501, LegacyArchiveSearch.matching(ordered, "  termin  ").size)
        assertTrue(LegacyArchiveSearch.matching(ordered, "2026-10-18").isEmpty())
    }

    @Test fun originalEventsAndOrderingAreUnchangedBySearch() {
        val first = CalendarEntry(id = "b", date = "2026-10-18",
            title = "Noćna smjena", category = "Smjena", start = "22:00", end = "06:00")
        val second = CalendarEntry(id = "a", date = "2026-10-17",
            title = "Rođendan", category = "Rođendan", note = "Obiteljsko slavlje")
        val unsorted = listOf(first, second)
        val ordered = LegacyArchiveSearch.chronological(unsorted)
        assertEquals(listOf("a", "b"), ordered.map { it.id })
        assertEquals(listOf("b"), LegacyArchiveSearch.matching(ordered, "22:00").map { it.id })
        assertEquals(listOf("a"), LegacyArchiveSearch.matching(ordered, "OBITELJSKO").map { it.id })
        assertEquals(unsorted, listOf(first, second))
    }
}
