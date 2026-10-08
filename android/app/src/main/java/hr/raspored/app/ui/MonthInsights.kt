package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val summary = CroatianWorkTime.summarize(
        month, schedule, shiftTypes,
        fundOverrideMinutes = accounting.fundOverrideMinutes(month)
    )
    val payroll = payrollEstimateForMonth(
        month, schedule, shiftTypes, accounting.fundOverrideMinutes(month),
        accounting.serviceYears, accounting.children, accounting.dependents
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
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
        MonthInsightTile(
            modifier = Modifier.weight(1f),
            label = "Plaća (procj.)",
            value = payroll?.netMonthly?.let(::compactMoney) ?: "—",
            tint = RasporedColors.Accent,
            icon = Icons.Rounded.AccountBalanceWallet
        )
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
                fontSize = 9.sp,
                maxLines = 1
            )
            Text(
                value,
                color = RasporedColors.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
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
