package hr.raspored.kalendar

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CalendarWeeksTest {
    @Test fun mondayFirstWeekCrossesYearBoundary() {
        val thursday = LocalDate.parse("2026-12-31")
        val days = CalendarWeeks.containing(thursday)
        assertEquals(7, days.size)
        assertEquals(LocalDate.parse("2026-12-28"), days.first())
        assertEquals(LocalDate.parse("2027-01-03"), days.last())
        assertEquals(days, CalendarWeeks.containing(LocalDate.parse("2027-01-01")))
    }

    @Test fun mondayAndSundayBelongToSameWeek() {
        val monday = LocalDate.parse("2026-10-12")
        val sunday = LocalDate.parse("2026-10-18")
        assertEquals(monday, CalendarWeeks.containing(sunday).first())
        assertEquals(sunday, CalendarWeeks.containing(monday).last())
    }
}
