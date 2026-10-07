package hr.raspored.app.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class CroatianWorkTimeTest {
    @Test
    fun builtInShiftDurationsMatchProductionRules() {
        assertEquals(12 * 60, ShiftCatalog.day.durationMinutes)
        assertEquals(12 * 60, ShiftCatalog.night.durationMinutes)
        assertEquals(8 * 60, ShiftCatalog.morning.durationMinutes)
    }

    @Test
    fun emptyWeekdayHolidayCreditsEightHours() {
        val month = YearMonth.of(2026, 5)

        val summary = CroatianWorkTime.summarize(
            month = month,
            entries = emptyMap(),
            shiftTypes = ShiftCatalog.all
        )

        assertEquals(8 * 60, summary.holidayCreditMinutes)
        assertEquals(8 * 60, summary.paidAbsenceMinutes)
        assertEquals(0, summary.regularMinutes)
        assertEquals(8 * 60, summary.creditedMinutes)
    }

    @Test
    fun dayAndNightShiftsAboveFundBecomeOvertime() {
        val month = YearMonth.of(2026, 10)
        val entries = linkedMapOf<LocalDate, String>()
        val days = listOf(
            2, 3, 6, 7, 10, 11, 14, 15, 18,
            19, 22, 23, 26, 27, 28, 30, 31
        )

        days.forEachIndexed { index, day ->
            entries[month.atDay(day)] = if (index % 2 == 0) "D" else "N"
        }

        val summary = CroatianWorkTime.summarize(
            month = month,
            entries = entries,
            shiftTypes = ShiftCatalog.all
        )

        assertEquals(204 * 60, summary.workedMinutes)
        assertEquals(176 * 60, summary.fundMinutes)
        assertEquals(176 * 60, summary.regularMinutes)
        assertEquals(28 * 60, summary.overtimeMinutes)
        assertEquals(204 * 60, summary.creditedMinutes)
    }
}
