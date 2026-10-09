package hr.raspored.app.model

import hr.raspored.app.data.ScheduleStore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class ObservedHospitalPremiumRates(
    val night: Double?,
    val overtime: Double?,
    val saturday: Double?,
    val sunday: Double?,
    val holiday: Double?,
    val secondShift: Double?,
    val turnus: Double?
)

data class WorkTimeSummary(
    val workedMinutes: Int,
    val regularMinutes: Int,
    val fundMinutes: Int,
    val overtimeMinutes: Int,
    val paidAbsenceMinutes: Int,
    val holidayCreditMinutes: Int,
    val creditedMinutes: Int,
    val workedShiftCount: Int,
    val dayMinutes: Int,
    val nightMinutes: Int,
    val saturdayMinutes: Int,
    val sundayMinutes: Int,
    val holidayWorkedMinutes: Int,
    val secondShiftMinutes: Int,
    val turnusMinutes: Int = 0
)

object CroatianWorkTime {
    private const val FULL_DAY_MINUTES = 8 * 60
    private val paidAbsenceCodes = setOf("GO", "BO", "PD")
    /**
     * Anonimizirane kontrolne stope očitane sa stvarnih obračunskih isprava.
     *
     * Ovo nije pravni tarifnik. Svako polje je popunjeno samo kada je upravo
     * ta stopa vidljiva na dostavljenom obračunu za odabrani mjesec.
     * Nepotvrđena vrijednost namjerno je null.
     */
    fun observedHospitalPremiumRates(month: YearMonth): ObservedHospitalPremiumRates =
        when (month) {
            YearMonth.of(2024, 12) -> observedRates(
                night = 0.40, overtime = 0.50, saturday = 0.25,
                sunday = 0.50, holiday = 1.50, secondShift = 0.10
            )
            YearMonth.of(2025, 1) -> observedRates(
                night = 0.50, overtime = 0.50, saturday = 0.25,
                sunday = 0.50, holiday = 1.50, secondShift = 0.10
            )
            YearMonth.of(2025, 2),
            YearMonth.of(2025, 3) -> observedRates(
                night = 0.50, overtime = 0.50, saturday = 0.25,
                sunday = 0.50, secondShift = 0.10
            )
            YearMonth.of(2025, 5),
            YearMonth.of(2025, 8),
            YearMonth.of(2026, 8) -> observedRates(
                night = 0.50, overtime = 0.50, saturday = 0.25,
                sunday = 0.50, holiday = 1.50, secondShift = 0.10, turnus = 0.05
            )
            YearMonth.of(2025, 7),
            YearMonth.of(2025, 10),
            YearMonth.of(2026, 7) -> observedRates(
                night = 0.50, overtime = 0.50, saturday = 0.25,
                sunday = 0.50, secondShift = 0.10, turnus = 0.05
            )
            YearMonth.of(2026, 6) -> observedRates(
                night = 0.50, overtime = 0.50, saturday = 0.25,
                holiday = 1.50, secondShift = 0.10, turnus = 0.05
            )
            else -> observedRates()
        }

    private fun observedRates(
        night: Double? = null,
        overtime: Double? = null,
        saturday: Double? = null,
        sunday: Double? = null,
        holiday: Double? = null,
        secondShift: Double? = null,
        turnus: Double? = null
    ) = ObservedHospitalPremiumRates(
        night = night,
        overtime = overtime,
        saturday = saturday,
        sunday = sunday,
        holiday = holiday,
        secondShift = secondShift,
        turnus = turnus
    )

    private data class MinuteSlice(
        val date: LocalDate,
        val hour: Int,
        val minute: Int,
        val instant: Instant
    ) {
        val isNight: Boolean get() = hour >= 22 || hour < 6
        val isSecondShift: Boolean get() = hour in 14..21
    }

    fun summarize(
        month: YearMonth,
        schedule: ScheduleStore,
        shiftTypes: List<ShiftType>,
        includedCodes: Set<String>? = null,
        fundOverrideMinutes: Int? = null,
        timeZone: ZoneId = ZoneId.systemDefault()
    ): WorkTimeSummary {
        val current = schedule.monthEntries(month).toMutableMap()
        val previousDate = month.atDay(1).minusDays(1)
        schedule.code(previousDate)?.let { current[previousDate] = it }
        return summarize(
            month = month,
            entries = current,
            shiftTypes = shiftTypes,
            includedCodes = includedCodes,
            fundOverrideMinutes = fundOverrideMinutes,
            timeZone = timeZone
        )
    }

    fun summarize(
        month: YearMonth,
        entries: Map<LocalDate, String>,
        shiftTypes: List<ShiftType>,
        includedCodes: Set<String>? = null,
        fundOverrideMinutes: Int? = null,
        timeZone: ZoneId = ZoneId.systemDefault()
    ): WorkTimeSummary {
        val shiftByCode = shiftTypes.associateBy { it.code }
        val holidays = holidays(month.year)

        var fund = 0
        var worked = 0
        var paidAbsence = 0
        var holidayCredit = 0
        var workedCount = 0
        var dayMinutes = 0
        var nightMinutes = 0
        var saturdayMinutes = 0
        var sundayMinutes = 0
        var holidayWorkedMinutes = 0
        var secondShiftMinutes = 0
        var turnusMinutes = 0

        for (day in 1..month.lengthOfMonth()) {
            val date = month.atDay(day)
            val fundDay = date.dayOfWeek != DayOfWeek.SATURDAY &&
                date.dayOfWeek != DayOfWeek.SUNDAY
            if (fundDay) fund += FULL_DAY_MINUTES

            val code = entries[date]
            if (code != null && includedCodes != null && code !in includedCodes) {
                continue
            }

            if (code in paidAbsenceCodes) {
                if (fundDay) paidAbsence += FULL_DAY_MINUTES
                continue
            }

            if (code == null && fundDay && holidays.containsKey(date)) {
                paidAbsence += FULL_DAY_MINUTES
                holidayCredit += FULL_DAY_MINUTES
            }
        }

        val candidates = linkedSetOf<LocalDate>()
        candidates += month.atDay(1).minusDays(1)
        for (day in 1..month.lengthOfMonth()) candidates += month.atDay(day)

        candidates.forEach { startDate ->
            val code = entries[startDate] ?: return@forEach
            if (includedCodes != null && code !in includedCodes) return@forEach

            val shift = shiftByCode[code] ?: return@forEach
            val slices = shiftMinuteSlices(startDate, shift, timeZone)
            val secondShiftEligible = isSecondShiftEligible(code, shift)
            var contributed = false

            slices.forEach { slice ->
                if (YearMonth.from(slice.date) != month) return@forEach

                contributed = true
                worked++
                if (code == "D" || code == "N") turnusMinutes++
                if (slice.isNight) nightMinutes++ else dayMinutes++
                if (secondShiftEligible && slice.isSecondShift) secondShiftMinutes++

                when (slice.date.dayOfWeek) {
                    DayOfWeek.SATURDAY -> saturdayMinutes++
                    DayOfWeek.SUNDAY -> sundayMinutes++
                    else -> Unit
                }
                // The slice already belongs to this month; reuse its holiday
                // table instead of recalculating Easter and allocating a map
                // for every single worked minute.
                if (holidays.containsKey(slice.date)) {
                    holidayWorkedMinutes++
                }
            }

            if (contributed) workedCount++
        }

        // Explicit override changes the monthly fund, not the underlying shift history.
        val effectiveFund = fundOverrideMinutes?.coerceIn(0, 744 * 60) ?: fund
        val remainingRegularCapacity = (effectiveFund - paidAbsence).coerceAtLeast(0)
        val regular = minOf(worked, remainingRegularCapacity)
        val overtime = (worked - remainingRegularCapacity).coerceAtLeast(0)

        return WorkTimeSummary(
            workedMinutes = worked,
            regularMinutes = regular,
            fundMinutes = effectiveFund,
            overtimeMinutes = overtime,
            paidAbsenceMinutes = paidAbsence,
            holidayCreditMinutes = holidayCredit,
            creditedMinutes = regular + overtime + paidAbsence,
            workedShiftCount = workedCount,
            dayMinutes = dayMinutes,
            nightMinutes = nightMinutes,
            saturdayMinutes = saturdayMinutes,
            sundayMinutes = sundayMinutes,
            holidayWorkedMinutes = holidayWorkedMinutes,
            secondShiftMinutes = secondShiftMinutes,
            turnusMinutes = turnusMinutes
        )
    }

    fun holidays(year: Int): Map<LocalDate, String> {
        val easter = easterSunday(year)
        return linkedMapOf(
            LocalDate.of(year, 1, 1) to "Nova godina",
            LocalDate.of(year, 1, 6) to "Bogojavljenje / Sveta tri kralja",
            easter to "Uskrs",
            easter.plusDays(1) to "Uskrsni ponedjeljak",
            easter.plusDays(60) to "Tijelovo",
            LocalDate.of(year, 5, 1) to "Praznik rada",
            LocalDate.of(year, 5, 30) to "Dan državnosti",
            LocalDate.of(year, 6, 22) to "Dan antifašističke borbe",
            LocalDate.of(year, 8, 5) to "Dan pobjede i domovinske zahvalnosti i Dan hrvatskih branitelja",
            LocalDate.of(year, 8, 15) to "Velika Gospa",
            LocalDate.of(year, 11, 1) to "Svi sveti",
            LocalDate.of(year, 11, 18) to "Dan sjećanja na žrtve Domovinskog rata i na žrtvu Vukovara i Škabrnje",
            LocalDate.of(year, 12, 25) to "Božić",
            LocalDate.of(year, 12, 26) to "Sveti Stjepan"
        )
    }

    fun holidayName(date: LocalDate): String? = holidays(date.year)[date]

    /**
     * Use the times currently saved for the shift, even for D/N/J.
     * The previous hard-coded hours caused incorrect night/weekend/holiday
     * supplements as soon as users customized a built-in shift.
     */
    private fun shiftMinuteSlices(
        date: LocalDate,
        shift: ShiftType,
        timeZone: ZoneId
    ): Sequence<MinuteSlice> = sequence {
        yieldAll(intervalMinuteSlices(date, shift.start, shift.end, timeZone))
        yieldAll(intervalMinuteSlices(date, shift.secondaryStart, shift.secondaryEnd, timeZone))
    }.distinctBy { it.instant } // Real moments, not wall-clock hour: preserve DST fall-back.

    /**
     * A twelve-hour turnus is not an afternoon shift just because part of
     * its hours falls between 14:00 and 22:00. Payslip 2nd-shift supplements
     * apply to P and short custom afternoon shifts, not D/N/J by default.
     */
    private fun isSecondShiftEligible(code: String, shift: ShiftType): Boolean {
        if (code == "D" || code == "N" || code == "J") return false
        if (code != "P" && !shift.custom) return false
        val start = shift.start ?: return false
        val end = shift.end ?: return false
        val startTime = runCatching { LocalTime.parse(start) }.getOrNull() ?: return false
        val endTime = runCatching { LocalTime.parse(end) }.getOrNull() ?: return false
        return !startTime.isBefore(LocalTime.of(14, 0)) &&
            startTime.isBefore(LocalTime.of(18, 0)) &&
            endTime > startTime && !endTime.isAfter(LocalTime.of(22, 0)) &&
            shift.durationMinutes in 1..(8 * 60)
    }

    private fun intervalMinuteSlices(
        date: LocalDate,
        startText: String?,
        endText: String?,
        timeZone: ZoneId
    ): Sequence<MinuteSlice> {
        if (startText == null || endText == null) return emptySequence()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val startTime = runCatching { LocalTime.parse(startText, formatter) }.getOrNull()
            ?: return emptySequence()
        val endTime = runCatching { LocalTime.parse(endText, formatter) }.getOrNull()
            ?: return emptySequence()

        // ZonedDateTime advances on the actual timeline. Spring-forward
        // omits an hour and autumn fallback repeats it: this must match iOS.
        val start = date.atTime(startTime).atZone(timeZone)
        var end = date.atTime(endTime).atZone(timeZone)
        if (!end.isAfter(start)) end = end.plusDays(1)

        return sequence {
            var cursor = start
            while (cursor.isBefore(end)) {
                yield(MinuteSlice(cursor.toLocalDate(), cursor.hour, cursor.minute, cursor.toInstant()))
                cursor = cursor.plusMinutes(1)
            }
        }
    }

    private fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate.of(year, month, day)
    }
}
