package hr.raspored.app.ui

import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.model.CroatianWorkTime
import hr.raspored.app.model.ShiftType
import hr.raspored.app.model.payroll.PayrollEstimator
import hr.raspored.app.model.payroll.PayrollInput
import java.time.DayOfWeek
import java.time.YearMonth
import kotlin.math.max

internal fun payrollEstimateForMonth(
    month: YearMonth,
    schedule: ScheduleStore,
    shiftTypes: List<ShiftType>,
    fundOverrideMinutes: Int? = null,
    serviceYears: Int = 0,
    children: Int = 0,
    dependents: Int = 0,
    annualLeaveHourlyGross: Double = 0.0
): hr.raspored.app.model.payroll.PayrollEstimate? {
    val monthEntries = schedule.monthEntries(month)
    val summary = CroatianWorkTime.summarize(
        month, schedule, shiftTypes, fundOverrideMinutes = fundOverrideMinutes
    )
    // A night shift starting on the LAST day of the previous month can
    // contribute worked hours here even when this month has no saved entries.
    // A truly untouched month still must not project an unearned full salary.
    if (!PayrollEstimator.hasRecordedActivity(monthEntries.isNotEmpty(), summary.workedMinutes)) {
        return null
    }
    fun absenceMinutes(code: String): Int =
        monthEntries
            .count { (date, value) ->
                value == code &&
                    date.dayOfWeek != DayOfWeek.SATURDAY &&
                    date.dayOfWeek != DayOfWeek.SUNDAY
            } * 8 * 60

    val annual = absenceMinutes("GO")
    val sick = absenceMinutes("BO")
    val otherPaid = max(
        0,
        summary.paidAbsenceMinutes -
            annual -
            sick -
            summary.holidayCreditMinutes
    )

    return PayrollEstimator.estimate(
        PayrollInput(
            month = month,
            summary = summary,
            annualLeaveMinutes = annual,
            sickLeaveMinutes = sick,
            otherPaidAbsenceMinutes = otherPaid,
            // A single D or N (or edited non-12-hour shift) is not proof
            // of a 12–24–12–48 rotation. Require both 12-hour definitions
            // before estimating the turnus supplement.
            hasDayNightTurnusPattern =
                schedule.count(month, "D") > 0 &&
                    schedule.count(month, "N") > 0 &&
                    shiftTypes.firstOrNull { it.code == "D" }?.durationMinutes == 720 &&
                    shiftTypes.firstOrNull { it.code == "N" }?.durationMinutes == 720,
            serviceYears = serviceYears,
            children = children,
            dependents = dependents,
            annualLeaveAverageHourlyGross = annualLeaveHourlyGross.takeIf { it > 0.0 }
        )
    )
}
