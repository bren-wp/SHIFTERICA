package hr.raspored.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class CroatianWorkTimeTest {
    @Test
    fun builtInShiftDurationsMatchProductionRules() {
        assertEquals(12 * 60, ShiftCatalog.day.durationMinutes)
        assertEquals(12 * 60, ShiftCatalog.night.durationMinutes)
        assertEquals(8 * 60, ShiftCatalog.morning.durationMinutes)
        assertEquals(8 * 60, ShiftCatalog.afternoon.durationMinutes)
    }

    @Test
    fun monthlyFundMatchesAllAnonymizedHospitalValidationMonths() {
        val expected = linkedMapOf(
            YearMonth.of(2024, 12) to 176,
            YearMonth.of(2025, 1) to 184,
            YearMonth.of(2025, 2) to 160,
            YearMonth.of(2025, 3) to 168,
            YearMonth.of(2025, 5) to 176,
            YearMonth.of(2025, 6) to 168,
            YearMonth.of(2025, 7) to 184,
            YearMonth.of(2025, 8) to 168,
            YearMonth.of(2025, 10) to 184,
            YearMonth.of(2026, 6) to 176,
            YearMonth.of(2026, 7) to 184,
            YearMonth.of(2026, 8) to 168
        )

        expected.forEach { (month, fundHours) ->
            assertEquals(
                "$month",
                fundHours * 60,
                CroatianWorkTime.summarize(
                    month = month,
                    entries = emptyMap(),
                    shiftTypes = ShiftCatalog.all
                ).fundMinutes
            )
        }
    }

    @Test
    fun emptyWeekdayHolidayCreditsEightHoursWithoutReducingFund() {
        val month = YearMonth.of(2026, 6)
        // Lipanj 2026. ima dva blagdana na radni dan. Jedan je ovdje
        // evidentiran kao odrađena D smjena, a drugi ostaje prazan i
        // mora dati točno 8 sati naknade.
        val entries = mapOf(LocalDate.of(2026, 6, 22) to "D")

        val summary = CroatianWorkTime.summarize(
            month = month,
            entries = entries,
            shiftTypes = ShiftCatalog.all
        )

        assertEquals(176 * 60, summary.fundMinutes)
        assertEquals(8 * 60, summary.holidayCreditMinutes)
        assertEquals(8 * 60, summary.paidAbsenceMinutes)
        assertEquals(12 * 60, summary.workedMinutes)
        assertEquals(20 * 60, summary.creditedMinutes)
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

    @Test
    fun observedHospitalPremiumRatesFollowOnlyConfirmedPayslipEvidence() {
        val december2024 = CroatianWorkTime.observedHospitalPremiumRates(YearMonth.of(2024, 12))
        assertEquals(0.40, december2024.night!!, 0.0001)
        assertEquals(0.25, december2024.saturday!!, 0.0001)
        assertEquals(0.50, december2024.sunday!!, 0.0001)
        assertEquals(1.50, december2024.holiday!!, 0.0001)
        assertEquals(0.10, december2024.secondShift!!, 0.0001)
        assertEquals(0.50, december2024.overtime!!, 0.0001)
        assertNull(december2024.turnus)

        val january2025 = CroatianWorkTime.observedHospitalPremiumRates(YearMonth.of(2025, 1))
        assertEquals(0.50, january2025.night!!, 0.0001)
        assertNull(january2025.turnus)

        val may2025 = CroatianWorkTime.observedHospitalPremiumRates(YearMonth.of(2025, 5))
        assertEquals(0.50, may2025.night!!, 0.0001)
        assertEquals(0.05, may2025.turnus!!, 0.0001)

        val august2026 = CroatianWorkTime.observedHospitalPremiumRates(YearMonth.of(2026, 8))
        assertEquals(0.50, august2026.night!!, 0.0001)
        assertEquals(0.05, august2026.turnus!!, 0.0001)
    }
}
