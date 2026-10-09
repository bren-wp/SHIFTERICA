package hr.raspored.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ShiftUsageNavigatorTest {
    private fun d(day: String): LocalDate = LocalDate.parse(day)

    @Test
    fun nearestUpcomingOccurrenceWinsInsteadOfUnrelatedDays() {
        val entries = mapOf(
            d("2026-10-09") to "X",
            d("2026-10-11") to "N",
            d("2026-10-16") to "X",
            d("2026-12-10") to "X"
        )
        assertEquals(d("2026-10-16"),
            ShiftUsageNavigator.closestDate(entries, "X", d("2026-10-10")))
        assertEquals(d("2026-10-11"),
            ShiftUsageNavigator.closestDate(entries, "N", d("2026-10-10")))
    }

    @Test
    fun noFutureOccurrenceUsesLatestPast() {
        val entries = mapOf(
            d("2026-01-01") to "X",
            d("2026-03-11") to "X",
            d("2026-04-01") to "D"
        )
        assertEquals(d("2026-03-11"),
            ShiftUsageNavigator.closestDate(entries, "X", d("2026-10-10")))
        assertNull(ShiftUsageNavigator.closestDate(entries, "Y", d("2026-10-10")))
    }
}
