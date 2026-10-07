package hr.raspored.app.model

import hr.raspored.app.data.ScheduleStore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

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
    val secondShiftMinutes: Int
)

object CroatianWorkTime {
    private const val FULL_DAY_MINUTES = 8 * 60
    private val paidAbsenceCodes = setOf("GO", "BO", "PD")

    private data class MinuteSlice(
        val date: LocalDate,
        val hour: Int,
        val minute: Int
    ) {
        val isNight: Boolean get() = hour >= 22 || hour < 6
        val isSecondShift: Boolean get() = hour in 14..21
    }

    fun summarize(
        month: YearMonth,
        schedule: ScheduleStore,
        shiftTypes: List<ShiftType>,
        includedCodes: Set<String>? = null
    ): WorkTimeSummary {
        val current = schedule.monthEntries(month).toMutableMap()
        val previousDate = month.atDay(1).minusDays(1)
        schedule.code(previousDate)?.let { current[previousDate] = it }
        return summarize(
            month = month,
            entries = current,
            shiftTypes = shiftTypes,
            includedCodes = includedCodes
        )
    }

    fun summarize(
        month: YearMonth,
        entries: Map<LocalDate, String>,
        shiftTypes: List<ShiftType>,
        includedCodes: Set<String>? = null
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
            val slices = shiftMinuteSlices(startDate, code, shift)
            var contributed = false

            slices.forEach { slice ->
                if (YearMonth.from(slice.date) != month) return@forEach

                contributed = true
                worked++
                if (slice.isNight) nightMinutes++ else dayMinutes++
                if (slice.isSecondShift) secondShiftMinutes++

                when (slice.date.dayOfWeek) {
                    DayOfWeek.SATURDAY -> saturdayMinutes++
                    DayOfWeek.SUNDAY -> sundayMinutes++
                    else -> Unit
                }
                if (holidays(slice.date.year).containsKey(slice.date)) {
                    holidayWorkedMinutes++
                }
            }

            if (contributed) workedCount++
        }

        val remainingRegularCapacity = (fund - paidAbsence).coerceAtLeast(0)
        val regular = minOf(worked, remainingRegularCapacity)
        val overtime = (worked - remainingRegularCapacity).coerceAtLeast(0)

        return WorkTimeSummary(
            workedMinutes = worked,
            regularMinutes = regular,
            fundMinutes = fund,
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
            secondShiftMinutes = secondShiftMinutes
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

    private fun shiftMinuteSlices(
        date: LocalDate,
        code: String,
        shift: ShiftType
    ): Sequence<MinuteSlice> {
        return when (code) {
            "D" -> intervalMinuteSlices(date, "07:00", "19:00")
            "N" -> intervalMinuteSlices(date, "19:00", "07:00")
            "J" -> intervalMinuteSlices(date, "07:00", "15:00")
            else -> sequence {
                yieldAll(intervalMinuteSlices(date, shift.start, shift.end))
                yieldAll(intervalMinuteSlices(date, shift.secondaryStart, shift.secondaryEnd))
            }
        }
    }

    private fun intervalMinuteSlices(
        date: LocalDate,
        startText: String?,
        endText: String?
    ): Sequence<MinuteSlice> {
        if (startText == null || endText == null) return emptySequence()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val startTime = runCatching { LocalTime.parse(startText, formatter) }.getOrNull()
            ?: return emptySequence()
        val endTime = runCatching { LocalTime.parse(endText, formatter) }.getOrNull()
            ?: return emptySequence()

        val start = date.atTime(startTime)
        var end = date.atTime(endTime)
        if (!end.isAfter(start)) end = end.plusDays(1)

        return sequence {
            var cursor = start
            while (cursor.isBefore(end)) {
                yield(MinuteSlice(cursor.toLocalDate(), cursor.hour, cursor.minute))
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
