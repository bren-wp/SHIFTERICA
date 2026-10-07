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
        assertEquals(7 * 60, ShiftCatalog.afternoon.durationMinutes)
    }

    @Test
    fun monthlyFundMatchesHospitalPayrollCalendarForSummer2026() {
        assertEquals(
            176 * 60,
            CroatianWorkTime.summarize(
                month = YearMonth.of(2026, 6),
                entries = emptyMap(),
                shiftTypes = ShiftCatalog.all
            ).fundMinutes
        )
        assertEquals(
            184 * 60,
            CroatianWorkTime.summarize(
                month = YearMonth.of(2026, 7),
                entries = emptyMap(),
                shiftTypes = ShiftCatalog.all
            ).fundMinutes
        )
        assertEquals(
            168 * 60,
            CroatianWorkTime.summarize(
                month = YearMonth.of(2026, 8),
                entries = emptyMap(),
                shiftTypes = ShiftCatalog.all
            ).fundMinutes
        )
    }

    @Test
    fun emptyWeekdayHolidayCreditsEightHoursWithoutReducingFund() {
        val month = YearMonth.of(2026, 6)

        val summary = CroatianWorkTime.summarize(
            month = month,
            entries = emptyMap(),
            shiftTypes = ShiftCatalog.all
        )

        assertEquals(176 * 60, summary.fundMinutes)
        assertEquals(8 * 60, summary.holidayCreditMinutes)
        assertEquals(8 * 60, summary.paidAbsenceMinutes)
        assertEquals(0, summary.regularMinutes)
        assertEquals(8 * 60, summary.creditedMinutes)
    }

    @Test
    fun excludedShiftCodesDoNotContributeToSummary() {
        val month = YearMonth.of(2026, 10)
        val entries = mapOf(
            month.atDay(2) to "D",
            month.atDay(3) to "N",
            month.atDay(5) to "GO"
        )

        val summary = CroatianWorkTime.summarize(
            month = month,
            entries = entries,
            shiftTypes = ShiftCatalog.all,
            includedCodes = setOf("D")
        )

        assertEquals(12 * 60, summary.workedMinutes)
        assertEquals(0, summary.paidAbsenceMinutes)
        assertEquals(12 * 60, summary.creditedMinutes)
    }

    @Test
    fun fridayNightShiftSplitsNightAndSaturdayHoursAtMidnight() {
        val month = YearMonth.of(2026, 10)
        val entries = mapOf(month.atDay(2) to "N")

        val summary = CroatianWorkTime.summarize(
            month = month,
            entries = entries,
            shiftTypes = ShiftCatalog.all
        )

        assertEquals(12 * 60, summary.workedMinutes)
        assertEquals(8 * 60, summary.nightMinutes)
        assertEquals(4 * 60, summary.dayMinutes)
        assertEquals(7 * 60, summary.saturdayMinutes)
        assertEquals(0, summary.sundayMinutes)
    }

    @Test
    fun nightShiftAtMonthBoundaryIsSplitAcrossCalendarMonths() {
        val entries = mapOf(LocalDate.of(2026, 10, 31) to "N")

        val october = CroatianWorkTime.summarize(
            month = YearMonth.of(2026, 10),
            entries = entries,
            shiftTypes = ShiftCatalog.all
        )
        val november = CroatianWorkTime.summarize(
            month = YearMonth.of(2026, 11),
            entries = entries,
            shiftTypes = ShiftCatalog.all
        )

        assertEquals(5 * 60, october.workedMinutes)
        assertEquals(2 * 60, october.nightMinutes)
        assertEquals(7 * 60, november.workedMinutes)
        assertEquals(6 * 60, november.nightMinutes)
        assertEquals(7 * 60, november.sundayMinutes)
    }

    @Test
    fun hoursAboveMonthlyFundBecomeOvertime() {
        val month = YearMonth.of(2026, 10)
        val entries = (1..16).associate { day -> month.atDay(day) to "D" }

        val summary = CroatianWorkTime.summarize(
            month = month,
            entries = entries,
            shiftTypes = ShiftCatalog.all
        )

        assertEquals(192 * 60, summary.workedMinutes)
        assertEquals(176 * 60, summary.fundMinutes)
        assertEquals(176 * 60, summary.regularMinutes)
        assertEquals(16 * 60, summary.overtimeMinutes)
        assertEquals(192 * 60, summary.creditedMinutes)
    }
}
