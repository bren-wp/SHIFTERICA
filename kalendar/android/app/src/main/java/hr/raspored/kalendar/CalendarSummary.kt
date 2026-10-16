package hr.raspored.kalendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** Calendar duration in minutes, not paid hours or any salary calculation. */
internal object CalendarScheduledTime {
    fun minutes(entry: CalendarEntry): Int {
        if (entry.start.isBlank() || entry.end.isBlank()) return 0
        return runCatching {
            val start = LocalTime.parse(entry.start).toSecondOfDay() / 60
            val end = LocalTime.parse(entry.end).toSecondOfDay() / 60
            if (start == end) 1440 else (end - start + 1440) % 1440
        }.getOrDefault(0)
    }

    fun display(minutes: Int): String =
        "${minutes / 60} h ${minutes % 60} min"
}

@Composable
internal fun ParitySummary(
    month: YearMonth,
    entries: List<CalendarEntry>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    notice: String?
) {
    var mode by remember { mutableIntStateOf(0) }
    var included by remember {
        mutableStateOf(CalendarShiftPresets.all.map { it.code }.toSet())
    }
    val monthEntries = remember(month, entries) {
        entries.filter { it.date.startsWith(month.toString()) }
    }
    val shifts = monthEntries.filter { it.category == "Smjena" }
    val includedShifts = shifts.filter { entry ->
        val preset = CalendarShiftPresets.all.firstOrNull {
            it.title == entry.title && it.start == entry.start && it.end == entry.end
        }
        preset == null || preset.code in included
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            color = ParityStyle.card, shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, ParityStyle.stroke)
        ) {
            Row(Modifier.fillMaxWidth().padding(4.dp)) {
                listOf("Pregled", "Sati").forEachIndexed { index, label ->
                    Surface(
                        onClick = { mode = index },
                        modifier = Modifier.weight(1f).height(40.dp),
                        color = if (mode == index) ParityStyle.accent.copy(alpha = .3f)
                            else Color.Transparent,
                        shape = RoundedCornerShape(13.dp),
                        border = if (mode == index) BorderStroke(1.dp, ParityStyle.accent)
                            else null
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(label, color = ParityStyle.text,
                                fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        Surface(
            color = ParityStyle.card, shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, ParityStyle.stroke)
        ) {
            Row(Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                SummaryArrow("Prethodni mjesec", onPreviousMonth, false)
                Text(
                    month.month.getDisplayName(TextStyle.FULL, Locale("hr", "HR"))
                        .uppercase(Locale("hr", "HR")) + " " + month.year,
                    Modifier.weight(1f), color = ParityStyle.text,
                    fontWeight = FontWeight.Black, fontSize = 21.sp,
                    textAlign = TextAlign.Center
                )
                SummaryArrow("Sljedeći mjesec", onNextMonth, true)
            }
        }
        if (mode == 0) {
        Surface(
            color = ParityStyle.card, shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, ParityStyle.stroke)
        ) {
            Column(Modifier.fillMaxWidth().padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pregled svih zapisa", color = ParityStyle.text,
                    fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("${monthEntries.size} zapisa · ${monthEntries.map { it.date }.distinct().size} aktivnih dana",
                    color = ParityStyle.muted, fontSize = 12.sp)
                if (monthEntries.isEmpty()) {
                    Text("Ovaj mjesec još nema događaja. Dodaj prvi plan putem znaka +.",
                        color = ParityStyle.muted, fontSize = 12.sp)
                }
                EntryRules.categories.forEach { category ->
                    val matching = monthEntries.filter { it.category == category }
                    if (matching.isNotEmpty()) {
                        Surface(
                            color = ParityStyle.card2, shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ParityStyle.softStroke)
                        ) {
                            Row(Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                Box(Modifier.size(11.dp).background(
                                    ParityStyle.tiles[matching.first().color],
                                    RoundedCornerShape(4.dp)))
                                Text(category, Modifier.weight(1f),
                                    color = ParityStyle.text,
                                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(matching.size.toString(), color = ParityStyle.accent,
                                    fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
        if (shifts.isNotEmpty()) {
            Surface(
                color = ParityStyle.card, shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, ParityStyle.stroke)
            ) {
                Column(Modifier.fillMaxWidth().padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pregled smjena", color = ParityStyle.text,
                        fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Smjena", Modifier.weight(2.2f), color = ParityStyle.muted,
                            fontSize = 11.sp)
                        Text("Broj", Modifier.weight(.6f), color = ParityStyle.muted,
                            fontSize = 11.sp, textAlign = TextAlign.Center)
                        Text("Vrijeme", Modifier.weight(1.1f), color = ParityStyle.muted,
                            fontSize = 11.sp, textAlign = TextAlign.Center)
                        Text("Uključeno", Modifier.weight(1f), color = ParityStyle.muted,
                            fontSize = 10.sp, textAlign = TextAlign.Center)
                    }
                    (CalendarShiftPresets.builtIn + CalendarShiftPresets.additional.filter { other ->
                        shifts.any { it.title == other.title && it.start == other.start &&
                            it.end == other.end }
                    }).forEach { preset ->
                        val matching = shifts.filter {
                            it.title == preset.title &&
                                it.start == preset.start && it.end == preset.end
                        }
                        Surface(
                            color = ParityStyle.card2,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, ParityStyle.softStroke)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(41.dp).background(
                                        ParityStyle.tiles[preset.color],
                                        RoundedCornerShape(11.dp)
                                    ), contentAlignment = Alignment.Center
                                ) {
                                    Text(preset.code, color = ParityStyle.bg,
                                        fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                                Spacer(Modifier.width(7.dp))
                                Column(Modifier.weight(2.2f)) {
                                    Text(preset.title, color = ParityStyle.text,
                                        fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                                        maxLines = 2)
                                    Text(
                                        if (preset.start.isBlank()) "Bez vremena"
                                        else "${preset.start} – ${preset.end}",
                                        color = ParityStyle.muted, fontSize = 9.sp
                                    )
                                }
                                Text(matching.size.toString(), Modifier.weight(.6f),
                                    textAlign = TextAlign.Center, color = ParityStyle.text,
                                    fontSize = 12.sp)
                                Text(
                                    CalendarScheduledTime.display(
                                        matching.sumOf(CalendarScheduledTime::minutes)
                                    ),
                                    Modifier.weight(1.1f), fontSize = 10.sp,
                                    textAlign = TextAlign.Center, color = ParityStyle.text
                                )
                                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    Switch(
                                        checked = preset.code in included,
                                        onCheckedChange = { selected ->
                                            included = if (selected) included + preset.code
                                                else included - preset.code
                                        },
                                        modifier = Modifier.height(39.dp)
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        "Uključeno utječe samo na ovaj pregled, ne briše zapise.",
                        color = ParityStyle.muted, fontSize = 11.sp
                    )
                    Text("Ukupno: ${includedShifts.size} smjena",
                        color = ParityStyle.accent, fontWeight = FontWeight.Bold)
                }
            }
        }
        } else {
            Surface(
                color = ParityStyle.card, shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, ParityStyle.stroke)
            ) {
                Column(Modifier.fillMaxWidth().padding(17.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Sati po rasporedu", fontSize = 22.sp,
                        color = ParityStyle.text, fontWeight = FontWeight.Black)
                    Text(
                        CalendarScheduledTime.display(
                            monthEntries.sumOf(CalendarScheduledTime::minutes)
                        ),
                        color = ParityStyle.accent, fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Ukupno planirano vrijeme događaja, termina i smjena. Prelasci preko ponoći " +
                            "uračunati su; zapisi bez vremena ne ulaze u zbroj.",
                        color = ParityStyle.muted, fontSize = 12.sp
                    )
                    Text(
                        "Zapisa: ${monthEntries.size} · Dana: " +
                            monthEntries.map { it.date }.distinct().size,
                        color = ParityStyle.text
                    )
                }
            }
        }
        Surface(color = ParityStyle.card, shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, ParityStyle.softStroke)) {
            Column(Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Sigurnosne kopije", color = ParityStyle.text,
                    fontWeight = FontWeight.Black, fontSize = 15.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onExport, Modifier.weight(1f)) {
                        Text("Izvezi kopiju")
                    }
                    OutlinedButton(onClick = onImport, Modifier.weight(1f)) {
                        Text("Uvezi kopiju")
                    }
                }
                if (notice != null) Text(notice, color = ParityStyle.muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SummaryArrow(label: String, onClick: () -> Unit, next: Boolean) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        color = ParityStyle.card2, shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ParityStyle.softStroke)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                if (next) Icons.Rounded.ChevronRight else Icons.Rounded.ChevronLeft,
                contentDescription = label, tint = ParityStyle.text
            )
        }
    }
}
