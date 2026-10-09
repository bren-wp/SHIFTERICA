package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.model.CroatianWorkTime
import hr.raspored.app.model.ShiftType
import hr.raspored.app.model.payroll.CroatianPayrollRules
import hr.raspored.app.model.payroll.PayrollEstimator
import hr.raspored.app.model.payroll.PayrollInput
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.YearMonth
import java.util.Locale
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
    if (schedule.monthEntries(month).isEmpty()) return null

    val summary = CroatianWorkTime.summarize(
        month, schedule, shiftTypes, fundOverrideMinutes = fundOverrideMinutes
    )
    fun absenceMinutes(code: String): Int =
        schedule.monthEntries(month)
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
            hasDayNightTurnusPattern =
                schedule.count(month, "D") > 0 ||
                    schedule.count(month, "N") > 0,
            serviceYears = serviceYears,
            children = children,
            dependents = dependents,
            annualLeaveAverageHourlyGross = annualLeaveHourlyGross.takeIf { it > 0.0 }
        )
    )
}
