package hr.raspored.app.model

import hr.raspored.app.data.ScheduleStore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class WorkTimeSummary(
    val workedMinutes: Int,
    val regularMinutes: Int,
    val fundMinutes: Int,
    val overtimeMinutes: Int,
    val paidAbsenceMinutes: Int,
    val holidayCreditMinutes: Int,
    val creditedMinutes: Int,
    val workedShiftCount: Int
)

object CroatianWorkTime {
    private const val FULL_DAY_MINUTES = 8 * 60
    private val paidAbsenceCodes = setOf("GO", "BO")

    fun summarize(
        month: YearMonth,
        schedule: ScheduleStore,
        shiftTypes: List<ShiftType>
    ): WorkTimeSummary {
        val shiftByCode = shiftTypes.associateBy { it.code }
        val holidayDates = holidays(month.year).keys

        var fund = 0
        var worked = 0
        var paidAbsence = 0
        var holidayCredit = 0
        var workedCount = 0

        for (day in 1..month.lengthOfMonth()) {
            val date = month.atDay(day)
            val fundDay = date.dayOfWeek != DayOfWeek.SATURDAY &&
                date.dayOfWeek != DayOfWeek.SUNDAY

            if (fundDay) fund += FULL_DAY_MINUTES

            val code = schedule.code(date)
            val shift = shiftByCode[code]

            if (code in paidAbsenceCodes) {
                if (fundDay) paidAbsence += FULL_DAY_MINUTES
                continue
            }

            if (shift != null && shift.durationMinutes > 0) {
                worked += shift.durationMinutes
                workedCount++
                continue
            }

            if (code == null && fundDay && date in holidayDates) {
                paidAbsence += FULL_DAY_MINUTES
                holidayCredit += FULL_DAY_MINUTES
            }
        }

        val remainingRegularCapacity = (fund - paidAbsence).coerceAtLeast(0)
        val regularWorked = minOf(worked, remainingRegularCapacity)
        val regular = minOf(fund, paidAbsence + regularWorked)
        val overtime = (worked - remainingRegularCapacity).coerceAtLeast(0)

        return WorkTimeSummary(
            workedMinutes = worked,
            regularMinutes = regular,
            fundMinutes = fund,
            overtimeMinutes = overtime,
            paidAbsenceMinutes = paidAbsence,
            holidayCreditMinutes = holidayCredit,
            creditedMinutes = regular + overtime,
            workedShiftCount = workedCount
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
