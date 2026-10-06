package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.model.ShiftType
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun SummaryScreen(month: YearMonth, schedule: ScheduleStore, shiftTypes: List<ShiftType>, onMonthChange: (YearMonth) -> Unit) {
    var range by remember { mutableStateOf(0) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SegmentedThree(listOf("Mjesec", "Godina", "Razdoblje"), range) { range = it }
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
                    SummaryArrow(Icons.Rounded.ChevronLeft) { onMonthChange(month.minusMonths(1)) }
                    Text(
                        month.month.getDisplayName(TextStyle.FULL, Locale("hr", "HR")).uppercase() + " " + month.year,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = RasporedColors.Text,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Black
                    )
                    SummaryArrow(Icons.Rounded.ChevronRight) { onMonthChange(month.plusMonths(1)) }
                }
            }
        }
        item { ShiftOverview(month, schedule, shiftTypes) }
        item { Totals(month, schedule, shiftTypes) }
        item {
            Surface(
                color = RasporedColors.Card,
                shape = RoundedCornerShape(19.dp),
                border = BorderStroke(1.dp, RasporedColors.Stroke),
                shadowElevation = 5.dp
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Search, null, tint = RasporedColors.Muted)
                    Spacer(Modifier.width(8.dp))
                    Text("Pretraži smjene…", color = RasporedColors.Muted, fontSize = 16.sp)
                }
            }
        }
        item { SegmentedThree(listOf("Prošle", "Sve", "Nadolazeće"), 1) {} }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun ShiftOverview(month: YearMonth, schedule: ScheduleStore, shiftTypes: List<ShiftType>) {
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
            val preferred = listOf("N", "D", "J", "BO")
            val rows = preferred.mapNotNull { code -> shiftTypes.firstOrNull { it.code == code } }
            rows.forEachIndexed { index, shift -> SummaryShiftRow(month, schedule, shift, enabled = index < 2) }
        }
    }
}

@Composable
private fun SummaryShiftRow(month: YearMonth, schedule: ScheduleStore, shift: ShiftType, enabled: Boolean) {
    val count = schedule.count(month, shift.code)
    val minutes = count * shift.durationMinutes
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
                Surface(color = shift.color, shape = RoundedCornerShape(10.dp), modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text(shift.code, color = shift.textColor, fontWeight = FontWeight.Black, fontSize = 16.sp) }
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
                    checked = enabled,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(checkedTrackColor = RasporedColors.Accent, uncheckedTrackColor = Color(0xFF44576B))
                )
            }
        }
    }
}

@Composable
private fun Totals(month: YearMonth, schedule: ScheduleStore, shiftTypes: List<ShiftType>) {
    val working = shiftTypes.filter { it.durationMinutes > 0 }.associateBy { it.code }
    val monthValues = schedule.monthEntries(month).values
    val count = monthValues.count { working.containsKey(it) }
    val total = monthValues.sumOf { working[it]?.durationMinutes ?: 0 }
    val avg = if (count > 0) total / count else 0
    Surface(
        color = RasporedColors.Card,
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 5.dp
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Ukupno", color = RasporedColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(Modifier.weight(1f), Icons.Rounded.Groups, "Ukupno smjena", count.toString(), RasporedColors.Accent)
                StatTile(Modifier.weight(1f), Icons.Rounded.Schedule, "Ukupno sati", formatMinutes(total), RasporedColors.Day)
                StatTile(Modifier.weight(1f), Icons.Rounded.ShowChart, "Prosjek po smjeni", formatMinutes(avg), RasporedColors.Sick)
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
                    color = if (active) RasporedColors.Accent.copy(alpha = .22f) else Color.Transparent,
                    shape = RoundedCornerShape(17.dp),
                    border = if (active) BorderStroke(1.dp, RasporedColors.Accent) else null
                ) {
                    Text(label, Modifier.padding(vertical = 12.dp), textAlign = TextAlign.Center, color = if (active) RasporedColors.Text else RasporedColors.Muted, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatMinutes(minutes: Int): String =
    (minutes / 60).toString() + " h " + (minutes % 60) + " min"
