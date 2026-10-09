package hr.raspored.app.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
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
        assertEquals(12 * 60, summary.holidayWorkedMinutes)
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
    fun manualFundOverrideChangesOvertimeWithoutChangingWorkedShiftCount() {
        val month = YearMonth.of(2026, 10)
        val entries = (1..16).associate { day -> month.atDay(day) to "D" }

        val automatically = CroatianWorkTime.summarize(
            month = month, entries = entries, shiftTypes = ShiftCatalog.all
        )
        val adjusted = CroatianWorkTime.summarize(
            month = month, entries = entries, shiftTypes = ShiftCatalog.all,
            fundOverrideMinutes = 184 * 60
        )

        assertEquals(192 * 60, automatically.workedMinutes)
        assertEquals(192 * 60, adjusted.workedMinutes)
        assertEquals(16 * 60, automatically.overtimeMinutes)
        assertEquals(8 * 60, adjusted.overtimeMinutes)
        assertEquals(automatically.workedShiftCount, adjusted.workedShiftCount)
        assertEquals(184 * 60, adjusted.fundMinutes)
    }

    @Test
    fun paySlipReferenceMonthsMatchProductionFundAndOvertimeModels() {
        val july = YearMonth.of(2026, 7)
        val full = (1..18).associate { day -> july.atDay(day) to "D" }
        val summary = CroatianWorkTime.summarize(
            month = july, entries = full, shiftTypes = ShiftCatalog.all
        )
        assertEquals(184 * 60, summary.fundMinutes)
        assertEquals(216 * 60, summary.workedMinutes)
        assertEquals(32 * 60, summary.overtimeMinutes)
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
    @Test
    fun editedBuiltInDayAndNightTimesAffectWorkedAndWeekendPremiums() {
        val edited = ShiftCatalog.all.map { shift ->
            when (shift.code) {
                "D" -> shift.copy(start = "10:00", end = "18:00")
                "N" -> shift.copy(start = "20:30", end = "08:30")
                else -> shift
            }
        }
        val month = YearMonth.of(2026, 10)
        val entries = mapOf(
            LocalDate.of(2026, 10, 2) to "D",
            LocalDate.of(2026, 10, 31) to "N"
        )
        val october = CroatianWorkTime.summarize(
            month = month, entries = entries, shiftTypes = edited
        )
        val november = CroatianWorkTime.summarize(
            month = YearMonth.of(2026, 11), entries = entries, shiftTypes = edited
        )
        assertEquals((8 * 60) + (3 * 60 + 30), october.workedMinutes)
        assertEquals(2 * 60, october.nightMinutes)
        assertEquals(8 * 60 + 30, november.workedMinutes)
        assertEquals(6 * 60, november.nightMinutes)
        assertEquals(8 * 60 + 30, november.sundayMinutes)
        assertEquals(0, october.secondShiftMinutes)
    }

    @Test
    fun twelveHourTurnusIsNotMistakenForAfternoonShift() {
        val month = YearMonth.of(2026, 10)
        val entries = mapOf(
            LocalDate.of(2026, 10, 2) to "D",
            LocalDate.of(2026, 10, 3) to "N",
            LocalDate.of(2026, 10, 4) to "P"
        )
        val summary = CroatianWorkTime.summarize(
            month, entries, ShiftCatalog.all
        )
        // P 14:00-22:00 is the only second-shift interval.
        assertEquals(8 * 60, summary.secondShiftMinutes)
        assertEquals((12 + 12 + 8) * 60, summary.workedMinutes)
    }

    @Test
    fun shortCustomAfternoonShiftIsEligibleButMorningIsNot() {
        val afternoon = ShiftCatalog.afternoon.copy(
            code = "XY", name = "Druga smjena", start = "15:00", end = "21:00",
            custom = true
        )
        val morning = ShiftCatalog.morning.copy(
            code = "XZ", name = "Prva smjena", start = "10:00", end = "18:00",
            custom = true
        )
        val month = YearMonth.of(2026, 10)
        val entries = mapOf(
            LocalDate.of(2026, 10, 2) to "XY",
            LocalDate.of(2026, 10, 3) to "XZ"
        )
        val summary = CroatianWorkTime.summarize(
            month, entries, ShiftCatalog.all + afternoon + morning
        )
        assertEquals(6 * 60, summary.secondShiftMinutes)
        assertEquals(14 * 60, summary.workedMinutes)
    }

    @Test
    fun daylightSavingSpringNightCountsElevenElapsedHours() {
        val month = YearMonth.of(2026, 3)
        val entries = mapOf(LocalDate.of(2026, 3, 28) to "N")
        val result = CroatianWorkTime.summarize(
            month, entries, ShiftCatalog.all,
            timeZone = ZoneId.of("Europe/Zagreb")
        )
        assertEquals(11 * 60, result.workedMinutes)
        assertEquals(7 * 60, result.nightMinutes)
        assertEquals(6 * 60, result.sundayMinutes)
        assertEquals(5 * 60, result.saturdayMinutes)
    }

    @Test
    fun daylightSavingAutumnNightCountsThirteenElapsedHours() {
        val month = YearMonth.of(2026, 10)
        val entries = mapOf(LocalDate.of(2026, 10, 24) to "N")
        val result = CroatianWorkTime.summarize(
            month, entries, ShiftCatalog.all,
            timeZone = ZoneId.of("Europe/Zagreb")
        )
        assertEquals(13 * 60, result.workedMinutes)
        assertEquals(9 * 60, result.nightMinutes)
        assertEquals(8 * 60, result.sundayMinutes)
        assertEquals(5 * 60, result.saturdayMinutes)
    }

    @Test
    fun changingAfternoonShiftIntoMorningRemovesAfternoonPremium() {
        val entries = mapOf(LocalDate.of(2026, 10, 12) to "P")
        val day = ShiftCatalog.afternoon.copy(start = "09:00", end = "17:00")
        val month = YearMonth.of(2026, 10)
        val edited = CroatianWorkTime.summarize(
            month, entries, ShiftCatalog.all.map {
                if (it.code == "P") day else it
            }
        )
        val original = CroatianWorkTime.summarize(month, entries, ShiftCatalog.all)
        assertEquals(8 * 60, edited.workedMinutes)
        assertEquals(0, edited.secondShiftMinutes)
        assertEquals(8 * 60, original.secondShiftMinutes)
    }

    @Test
    fun endingAfterTwentyTwoDoesNotQualifyWholeShiftAsAfternoon() {
        val entries = mapOf(LocalDate.of(2026, 10, 12) to "P")
        val editedP = ShiftCatalog.afternoon.copy(start = "15:00", end = "22:30")
        val sum = CroatianWorkTime.summarize(
            YearMonth.of(2026, 10), entries,
            ShiftCatalog.all.map { if (it.code == "P") editedP else it }
        )
        assertEquals(0, sum.secondShiftMinutes)
        assertEquals(450, sum.workedMinutes)
    }

    @Test
    fun overlappingCustomShiftIntervalsCountEachMinuteOnce() {
        val shift = ShiftCatalog.afternoon.copy(
            code = "XY", name = "Podijeljena smjena",
            start = "08:00", end = "16:00",
            secondaryStart = "14:00", secondaryEnd = "20:00", custom = true
        )
        val result = CroatianWorkTime.summarize(
            month = YearMonth.of(2026, 10),
            entries = mapOf(LocalDate.of(2026, 10, 5) to "XY"),
            shiftTypes = ShiftCatalog.all + shift,
            timeZone = ZoneId.of("Europe/Zagreb")
        )
        assertEquals(12 * 60, result.workedMinutes)
        assertEquals(12 * 60, result.dayMinutes)
        assertEquals(0, result.overtimeMinutes)
        assertEquals(0, result.nightMinutes)
        assertEquals(1, result.workedShiftCount)
    }

    @Test
    fun overlappingMidnightIntervalsDoNotDoubleNightOrWeekendSupplements() {
        val shift = ShiftCatalog.night.copy(
            code = "XY", name = "Podijeljena nocna",
            start = "20:00", end = "04:00",
            secondaryStart = "22:00", secondaryEnd = "02:00", custom = true
        )
        val result = CroatianWorkTime.summarize(
            month = YearMonth.of(2026, 10),
            entries = mapOf(LocalDate.of(2026, 10, 3) to "XY"),
            shiftTypes = ShiftCatalog.all + shift,
            timeZone = ZoneId.of("Europe/Zagreb")
        )
        assertEquals(8 * 60, result.workedMinutes)
        assertEquals(6 * 60, result.nightMinutes)
        assertEquals(4 * 60, result.saturdayMinutes)
        assertEquals(4 * 60, result.sundayMinutes)
    }

    @Test
    fun overlappingIntervalOnAutumnDstStillPreservesBothRealOccurrencesOfHour() {
        val shift = ShiftCatalog.night.copy(
            code = "XY", name = "Dvostruka nocna",
            start = "19:00", end = "07:00",
            secondaryStart = "21:00", secondaryEnd = "23:00", custom = true
        )
        val result = CroatianWorkTime.summarize(
            month = YearMonth.of(2026, 10),
            entries = mapOf(LocalDate.of(2026, 10, 24) to "XY"),
            shiftTypes = ShiftCatalog.all + shift,
            timeZone = ZoneId.of("Europe/Zagreb")
        )
        assertEquals(13 * 60, result.workedMinutes)
        assertEquals(9 * 60, result.nightMinutes)
        assertEquals(8 * 60, result.sundayMinutes)
    }

    @Test
    fun secondaryAfterMidnightIsNotCountedOnPreviousMorning() {
        val shift = ShiftCatalog.night.copy(
            code = "XY", start = "20:00", end = "06:00",
            secondaryStart = "01:00", secondaryEnd = "04:00", custom = true
        )
        val summary = CroatianWorkTime.summarize(
            YearMonth.of(2026, 10),
            mapOf(LocalDate.of(2026, 10, 5) to "XY"),
            ShiftCatalog.all + shift,
            timeZone = ZoneId.of("Europe/Zagreb")
        )
        assertEquals(10 * 60, summary.workedMinutes)
        assertEquals(8 * 60, summary.nightMinutes)
        assertEquals(0, summary.overtimeMinutes)
    }

    @Test
    fun secondaryAfterMidnightPreservesMonthBoundaryAndDst() {
        val shift = ShiftCatalog.night.copy(
            code = "XY", start = "20:00", end = "06:00",
            secondaryStart = "01:00", secondaryEnd = "04:00", custom = true
        )
        val entry = mapOf(LocalDate.of(2026, 10, 31) to "XY")
        val october = CroatianWorkTime.summarize(
            YearMonth.of(2026, 10), entry, ShiftCatalog.all + shift,
            timeZone = ZoneId.of("Europe/Zagreb")
        )
        val november = CroatianWorkTime.summarize(
            YearMonth.of(2026, 11), entry, ShiftCatalog.all + shift,
            timeZone = ZoneId.of("Europe/Zagreb")
        )
        assertEquals(4 * 60, october.workedMinutes)
        assertEquals(6 * 60, november.workedMinutes)
        assertEquals(6 * 60, november.sundayMinutes)

        for ((date, expected) in listOf(
            LocalDate.of(2026, 3, 28) to 9 * 60,
            LocalDate.of(2026, 10, 24) to 11 * 60
        )) {
            val total = CroatianWorkTime.summarize(
                YearMonth.from(date), mapOf(date to "XY"),
                ShiftCatalog.all + shift, timeZone = ZoneId.of("Europe/Zagreb")
            )
            assertEquals("DST $date", expected, total.workedMinutes)
        }
    }

    @Test
    fun overlappingEntriesOnAdjacentDaysCountRealMinutesOnlyOnceForPayroll() {
        val overnight = ShiftCatalog.night.copy(
            code = "XY", start = "20:00", end = "06:00", custom = true
        )
        val morning = ShiftCatalog.morning.copy(
            code = "XZ", start = "05:00", end = "11:00", custom = true
        )
        val summary = CroatianWorkTime.summarize(
            YearMonth.of(2026, 10),
            mapOf(
                LocalDate.of(2026, 10, 5) to "XY",
                LocalDate.of(2026, 10, 6) to "XZ"
            ),
            ShiftCatalog.all + overnight + morning,
            fundOverrideMinutes = 8 * 60,
            timeZone = ZoneId.of("Europe/Zagreb")
        )
        assertEquals(15 * 60, summary.workedMinutes)
        assertEquals(8 * 60, summary.regularMinutes)
        assertEquals(7 * 60, summary.overtimeMinutes)
        assertEquals(8 * 60, summary.nightMinutes)
        assertEquals(2, summary.workedShiftCount)
    }

}
