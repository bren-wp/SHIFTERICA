package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.data.MonthlyAccountingStore
import hr.raspored.app.model.CroatianWorkTime
import hr.raspored.app.model.ShiftType
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

@Composable
internal fun MonthInsights(
    month: YearMonth,
    schedule: ScheduleStore,
    accounting: MonthlyAccountingStore,
    shiftTypes: List<ShiftType>
) {
    // Avoid expensive minute-by-minute recategorization on unrelated recompositions.
    val entries = schedule.monthEntries(month)
    val carryOver = schedule.code(month.atDay(1).minusDays(1))
    val fundOverride = accounting.fundOverrideMinutes(month)
    val years = accounting.serviceYearsForMonth(month)
    val children = accounting.children
    val dependents = accounting.dependents
    val goRate = accounting.annualLeaveHourlyGross
    val paymentDelay = accounting.paymentDelayMonths(month)
    val confirmedNet = accounting.actualNet(month)
    val summary = remember(month, entries, carryOver, shiftTypes, fundOverride) {
        CroatianWorkTime.summarize(
            month, schedule, shiftTypes, fundOverrideMinutes = fundOverride
        )
    }
    val payroll = remember(
        month, entries, carryOver, shiftTypes, fundOverride, years,
        children, dependents, goRate, paymentDelay
    ) {
        payrollEstimateForMonth(
            month, schedule, shiftTypes, fundOverride,
            years, children, dependents, goRate, paymentDelay
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MonthInsightTile(
                modifier = Modifier.weight(1f),
                label = "Fond sati",
                value = compactHours(summary.fundMinutes),
                tint = RasporedColors.Morning,
                icon = Icons.Rounded.Schedule
            )
            MonthInsightTile(
                modifier = Modifier.weight(1f),
                label = "Prekovremeni",
                value = compactHours(summary.overtimeMinutes),
                tint = RasporedColors.Sick,
                icon = Icons.Rounded.ShowChart
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = RasporedColors.Card,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, RasporedColors.Accent.copy(alpha = .28f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Icon(
                    Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = RasporedColors.Accent,
                    modifier = Modifier.size(22.dp)
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        if (confirmedNet != null) "Potvrđeni neto" else "Procjena neta",
                        color = RasporedColors.Muted, fontSize = 11.sp,
                        maxLines = 1
                    )
                    Text(
                        if (confirmedNet != null) "Iz obračunske liste" else "Informativni iznos",
                        color = RasporedColors.Muted, fontSize = 9.sp,
                        maxLines = 1
                    )
                }
                Text(
                    (confirmedNet ?: payroll?.netMonthly)?.let(::compactMoney) ?: "—",
                    color = RasporedColors.Text,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MonthInsightTile(
    modifier: Modifier,
    label: String,
    value: String,
    tint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = modifier,
        color = RasporedColors.Card,
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = .34f)),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(icon, null, tint = tint)
            Text(
                label,
                color = RasporedColors.Muted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                value,
                color = RasporedColors.Text,
                fontSize = if (label.startsWith("Neto")) 12.sp else 15.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun compactHours(minutes: Int): String {
    val hours = minutes / 60
    val remainder = minutes % 60
    return if (remainder == 0) "$hours h" else "$hours h $remainder m"
}

private fun compactMoney(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("hr-HR"))
        .format(value)
