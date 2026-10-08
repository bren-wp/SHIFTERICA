package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.data.MonthlyAccountingStore
import hr.raspored.app.model.CroatianWorkTime
import hr.raspored.app.model.ShiftType
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun SummaryScreen(
    month: YearMonth,
    schedule: ScheduleStore,
    accounting: MonthlyAccountingStore,
    shiftTypes: List<ShiftType>,
    onMonthChange: (YearMonth) -> Unit
) {
    var section by remember { mutableStateOf(0) }
    var includedCodes by remember {
        mutableStateOf(setOf("N", "D", "P", "J", "GO", "BO"))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SegmentedThree(
                listOf("Smjene", "Sati", "Primanja"),
                section
            ) { section = it }
        }

        item {
            Surface(
                color = RasporedColors.Card,
                shape = RoundedCornerShape(23.dp),
                border = BorderStroke(1.dp, RasporedColors.Stroke),
                shadowElevation = 5.dp
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryArrow(Icons.Rounded.ChevronLeft) {
                        onMonthChange(month.minusMonths(1))
                    }
                    Text(
                        month.month
                            .getDisplayName(
                                TextStyle.FULL,
                                Locale("hr", "HR")
                            )
                            .uppercase() + " " + month.year,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = RasporedColors.Text,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Black
                    )
                    SummaryArrow(Icons.Rounded.ChevronRight) {
                        onMonthChange(month.plusMonths(1))
                    }
                }
            }
        }

        when (section) {
            0 -> item {
                ShiftOverview(
                    month = month,
                    schedule = schedule,
                    shiftTypes = shiftTypes,
                    includedCodes = includedCodes,
                    onIncludedChange = { code, enabled ->
                        includedCodes = if (enabled) {
                            includedCodes + code
                        } else {
                            includedCodes - code
                        }
                    }
                )
            }
            1 -> {
                item { FundHoursEditor(month, schedule, shiftTypes, accounting) }
                item {
                    Totals(month, schedule, shiftTypes, includedCodes,
                        accounting.fundOverrideMinutes(month))
                }
            }
            else -> {
                item { PayrollProfileEditor(accounting) }
                item {
                    PayrollEstimateCard(
                        month, schedule, shiftTypes,
                        accounting.fundOverrideMinutes(month),
                        accounting.serviceYears, accounting.children, accounting.dependents,
                        accounting.annualLeaveHourlyGross,
                        accounting.profileConfirmed
                    )
                }
                item { AnnualEarningsCard(month, accounting) }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun ShiftOverview(
    month: YearMonth,
    schedule: ScheduleStore,
    shiftTypes: List<ShiftType>,
    includedCodes: Set<String>,
    onIncludedChange: (String, Boolean) -> Unit
) {
    Surface(
        color = RasporedColors.Card,
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 5.dp
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Pregled smjena", color = RasporedColors.Text, fontSize = 23.sp, fontWeight = FontWeight.Black)
            Row(Modifier.fillMaxWidth()) {
                Text("Smjena", Modifier.weight(2.1f), color = RasporedColors.Muted, fontWeight = FontWeight.SemiBold)
                Text("Broj", Modifier.weight(.7f), textAlign = TextAlign.Center, color = RasporedColors.Muted)
                Text("Vrijeme", Modifier.weight(1.2f), textAlign = TextAlign.Center, color = RasporedColors.Muted)
                Text("Uključeno", Modifier.weight(1f), textAlign = TextAlign.Center, color = RasporedColors.Muted)
            }
            val preferred = listOf("N", "D", "P", "J", "GO", "BO")
            val rows = preferred.mapNotNull { code -> shiftTypes.firstOrNull { it.code == code } }
            rows.forEach { shift ->
                SummaryShiftRow(
                    month = month,
                    schedule = schedule,
                    shift = shift,
                    included = shift.code in includedCodes,
                    onIncludedChange = { enabled -> onIncludedChange(shift.code, enabled) }
                )
            }
        }
    }
}

@Composable
private fun SummaryShiftRow(
    month: YearMonth,
    schedule: ScheduleStore,
    shift: ShiftType,
    included: Boolean,
    onIncludedChange: (Boolean) -> Unit
) {
    val count = schedule.count(month, shift.code)
    val minutes = count * if (shift.code == "GO" || shift.code == "BO") 8 * 60 else shift.durationMinutes
    Surface(
        color = RasporedColors.Card2,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, RasporedColors.StrokeSoft)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(Modifier.weight(2.1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(11.dp),
                    modifier = Modifier.size(44.dp),
                    border = BorderStroke(1.dp, shift.color.copy(alpha = .95f)),
                    shadowElevation = 5.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(shift.color, shift.color.copy(alpha = .80f))
                                ),
                                RoundedCornerShape(11.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            shift.code,
                            color = shift.textColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }
                Column {
                    Text(shift.shortName, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
                    shift.timeText?.let { Text(it, color = RasporedColors.Muted, fontSize = 10.sp) }
                }
            }
            Text(count.toString(), Modifier.weight(.7f), textAlign = TextAlign.Center, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
            Text(formatMinutes(minutes), Modifier.weight(1.2f), textAlign = TextAlign.Center, color = RasporedColors.Text, fontWeight = FontWeight.SemiBold)
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Switch(
                    checked = included,
                    onCheckedChange = onIncludedChange,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = RasporedColors.Accent,
                        uncheckedTrackColor = Color(0xFF44576B)
                    )
                )
            }
        }
    }
}

@Composable
private fun Totals(
    month: YearMonth,
    schedule: ScheduleStore,
    shiftTypes: List<ShiftType>,
    includedCodes: Set<String>,
    fundOverrideMinutes: Int?
) {
    val summary = CroatianWorkTime.summarize(
        month = month,
        schedule = schedule,
        shiftTypes = shiftTypes,
        includedCodes = includedCodes,
        fundOverrideMinutes = fundOverrideMinutes
    )

    Surface(
        color = RasporedColors.Card,
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 5.dp
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Obračun sati", color = RasporedColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(
                "Fond sati računa radne dane od ponedjeljka do petka. GO i BO priznaju 8 sati na radni dan, a prazan državni blagdan također se priznaje kao 8 sati.",
                color = RasporedColors.Muted,
                fontSize = 10.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(Modifier.weight(1f), Icons.Rounded.Schedule, "Odrađeni sati", formatMinutes(summary.workedMinutes), RasporedColors.Day)
                StatTile(Modifier.weight(1f), Icons.Rounded.Groups, "Redovni sati", formatMinutes(summary.regularMinutes), RasporedColors.Accent)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(Modifier.weight(1f), Icons.Rounded.Schedule, "Fond sati", formatMinutes(summary.fundMinutes), RasporedColors.Morning)
                StatTile(Modifier.weight(1f), Icons.Rounded.ShowChart, "Prekovremeni sati", formatMinutes(summary.overtimeMinutes), RasporedColors.Sick)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(Modifier.weight(1f), Icons.Rounded.Groups, "Plaćene odsutnosti", formatMinutes(summary.paidAbsenceMinutes), RasporedColors.Night)
                StatTile(Modifier.weight(1f), Icons.Rounded.Schedule, "Ukupno priznato", formatMinutes(summary.creditedMinutes), RasporedColors.Annual)
            }

            Text(
                "Raspodjela stvarno odrađenih sati",
                color = RasporedColors.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 2.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(Modifier.weight(1f), Icons.Rounded.Schedule, "Dnevni sati", formatMinutes(summary.dayMinutes), RasporedColors.Day)
                StatTile(Modifier.weight(1f), Icons.Rounded.Schedule, "Noćni 22–06", formatMinutes(summary.nightMinutes), RasporedColors.Night)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(Modifier.weight(1f), Icons.Rounded.Schedule, "Subota", formatMinutes(summary.saturdayMinutes), RasporedColors.Morning)
                StatTile(Modifier.weight(1f), Icons.Rounded.Schedule, "Nedjelja", formatMinutes(summary.sundayMinutes), RasporedColors.Sick)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(Modifier.weight(1f), Icons.Rounded.Schedule, "Blagdan — rad", formatMinutes(summary.holidayWorkedMinutes), RasporedColors.Annual)
                StatTile(Modifier.weight(1f), Icons.Rounded.Schedule, "Sati 14–22", formatMinutes(summary.secondShiftMinutes), RasporedColors.Accent)
            }
        }
    }
}

@Composable
private fun StatTile(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, tint: Color) {
    Surface(
        modifier = modifier,
        color = RasporedColors.Card2,
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = .42f))
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(icon, null, tint = tint)
            Text(label, color = RasporedColors.Muted, fontSize = 10.sp)
            Text(value, color = RasporedColors.Text, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
    }
}

@Composable
private fun SummaryArrow(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    FilledIconButton(onClick = onClick, colors = IconButtonDefaults.filledIconButtonColors(containerColor = RasporedColors.Card2), shape = RoundedCornerShape(20.dp)) {
        Icon(icon, null, tint = RasporedColors.Text)
    }
}

@Composable
private fun SegmentedThree(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Surface(
        color = RasporedColors.Card,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 5.dp
    ) {
        Row(Modifier.padding(4.dp)) {
            labels.forEachIndexed { index, label ->
                val active = selected == index
                Surface(
                    modifier = Modifier.weight(1f),
                    onClick = { onSelect(index) },
                    color = if (active) RasporedColors.Accent.copy(alpha = .28f) else Color.Transparent,
                    shape = RoundedCornerShape(17.dp),
                    border = if (active) BorderStroke(1.2.dp, RasporedColors.Accent) else null,
                    shadowElevation = if (active) 7.dp else 0.dp
                ) {
                    Text(label, Modifier.padding(vertical = 12.dp), textAlign = TextAlign.Center, color = if (active) RasporedColors.Text else RasporedColors.Muted, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatMinutes(minutes: Int): String =
    (minutes / 60).toString() + " h " + (minutes % 60) + " min"

