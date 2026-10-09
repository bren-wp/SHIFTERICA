package hr.raspored.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ScheduleSearchTest {
    private fun d(value: String) = LocalDate.parse(value)

    @Test fun nextDatesFirstAndMostRecentHistoryAfterwards() {
        val entries = mapOf(
            d("2024-01-01") to "N",
            d("2026-10-08") to "D",
            d("2026-10-13") to "N",
            d("2026-10-10") to "D",
            d("2026-10-06") to "N"
        )
        val dates = ScheduleSearch.find(entries, emptyMap(), "", d("2026-10-09")).map { it.first }
        assertEquals(listOf(d("2026-10-10"), d("2026-10-13"),
            d("2026-10-08"), d("2026-10-06"), d("2024-01-01")), dates)
        assertEquals(2, ScheduleSearch.find(entries, emptyMap(), "", d("2026-10-09"), 2).size)
    }

    @Test fun lookupMatchesAbbreviationNameAndDateAndReflectsCodeChanges() {
        val entries = mapOf(d("2026-10-10") to "XY", d("2026-10-12") to "D")
        val names = mapOf("XY" to "Dodatna smjena", "D" to "Dnevna smjena")
        assertEquals("XY", ScheduleSearch.find(entries, names, "xy", d("2026-10-09")).single().second)
        assertEquals("XY", ScheduleSearch.find(entries, names, "dodatna", d("2026-10-09")).single().second)
        assertEquals(d("2026-10-12"), ScheduleSearch.find(entries, names, "2026-10-12", d("2026-10-09")).single().first)
        assertTrue(ScheduleSearch.find(entries, names, " NEMA ", d("2026-10-09")).isEmpty())
        val changed = entries + (d("2026-10-10") to "D")
        assertTrue(ScheduleSearch.find(changed, names, "XY", d("2026-10-09")).isEmpty())
    }
}
