package hr.raspored.kalendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** Identical shell dimensions and brand tokens to Raspored; no payroll model is referenced. */
internal object ParityStyle {
    val bg = Color(0xFF090F1B)
    val bg2 = Color(0xFF102139)
    val card = Color(0xF015243B)
    val card2 = Color(0xFF1B304C)
    val stroke = Color(0xFF345273)
    val softStroke = Color(0x803E5D7A)
    val text = Color(0xFFF4F7FD)
    val muted = Color(0xFFB8C7DB)
    val accent = Color(0xFF7CEBD6)
    val weekend = Color(0xFFFFA0B5)
    val empty = Color(0xFF192B42)
    val weekendEmpty = Color(0xFF312438)
    val gradient = Brush.verticalGradient(listOf(bg, bg2, Color(0xFF070C16)))
    val tiles = listOf(
        Color(0xFF87DDD5), Color(0xFF78C6FF), Color(0xFFD3B3FF),
        Color(0xFFFFB47D), Color(0xFF82DFAD), Color(0xFFF8CC7B)
    )
}

internal enum class CalendarSection { MONTH, YEAR, SUMMARY }

@Composable
internal fun ParityHeader(
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(46.dp), color = ParityStyle.card2,
            shape = RoundedCornerShape(15.dp),
            border = BorderStroke(1.5.dp, ParityStyle.accent)
        ) {
            Column(
                Modifier.fillMaxSize().padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(Modifier.fillMaxWidth().height(6.dp)
                    .background(ParityStyle.accent, RoundedCornerShape(3.dp)))
                repeat(2) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(3) { column ->
                            Box(Modifier.weight(1f).height(6.dp)
                                .background(
                                    if (row == 0 && column == 1) Color(0xFF78C6FF)
                                    else ParityStyle.accent,
                                    RoundedCornerShape(2.dp)
                                ))
                        }
                    }
                }
            }
        }
        Text(
            "Raspored", modifier = Modifier.weight(1f),
            color = ParityStyle.text, fontWeight = FontWeight.Black,
            fontSize = 27.sp, maxLines = 1
        )
        ParityHeaderAction(Icons.Rounded.Search, "Pretraži", onSearch)
        ParityHeaderAction(Icons.Rounded.Tune, "Postavke", onSettings)
        Surface(
            onClick = onAdd, modifier = Modifier.size(48.dp),
            color = ParityStyle.accent, shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ParityStyle.text.copy(alpha = .28f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Add, "Novi zapis",
                    tint = ParityStyle.bg, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
private fun ParityHeaderAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick, modifier = Modifier.size(48.dp),
        color = ParityStyle.card2, shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, ParityStyle.softStroke)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = ParityStyle.text, modifier = Modifier.size(25.dp))
        }
    }
}

@Composable
internal fun ParityBottomNav(section: CalendarSection, onSection: (CalendarSection) -> Unit) {
    val destinations = listOf(
        Triple(CalendarSection.MONTH, "Mjesec", Icons.Rounded.CalendarMonth),
        Triple(CalendarSection.YEAR, "Godina", Icons.Rounded.DateRange),
        Triple(CalendarSection.SUMMARY, "Sažetak", Icons.Rounded.BarChart)
    )
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp),
        color = ParityStyle.card, shape = RoundedCornerShape(19.dp),
        border = BorderStroke(1.dp, ParityStyle.softStroke)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { (destination, title, icon) ->
                val active = section == destination
                Surface(
                    modifier = Modifier.weight(1f).height(54.dp)
                        .testTag("bottom-nav-" + destination.name.lowercase())
                        .semantics { selected = active },
                    onClick = { onSection(destination) },
                    color = if (active) ParityStyle.accent.copy(alpha = .07f)
                        else Color.Transparent,
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        Box(
                            Modifier.size(48.dp, 27.dp).background(
                                if (active) ParityStyle.accent.copy(alpha = .14f)
                                else Color.Transparent, RoundedCornerShape(12.dp)
                            ), contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp),
                                tint = if (active) ParityStyle.accent else ParityStyle.muted)
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(title, fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) ParityStyle.text else ParityStyle.muted)
                    }
                }
            }
        }
    }
}

/** Mirrors the original compact calendar's year navigation, without any salary data. */
@Composable
internal fun ParityYear(
    year: Int,
    entries: List<CalendarEntry>,
    onYearChange: (Int) -> Unit,
    onMonthPick: (YearMonth) -> Unit
) {
    val indexed = remember(entries) { entries.groupBy { it.date } }
    Column(Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 6.dp)) {
        Surface(
            color = ParityStyle.card, shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, ParityStyle.stroke)
        ) {
            Row(Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                ParityYearArrow(Icons.Rounded.ChevronLeft, "Prethodna godina") {
                    onYearChange(year - 1)
                }
                Text(year.toString(), Modifier.weight(1f), color = ParityStyle.text,
                    fontWeight = FontWeight.Black, fontSize = 23.sp,
                    textAlign = TextAlign.Center)
                ParityYearArrow(Icons.Rounded.ChevronRight, "Sljedeća godina") {
                    onYearChange(year + 1)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            (1..12).chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    pair.forEach { month ->
                        ParityMiniMonth(YearMonth.of(year, month), indexed,
                            modifier = Modifier.weight(1f)) {
                            onMonthPick(YearMonth.of(year, month))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParityYearArrow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.size(45.dp),
        color = ParityStyle.card2, shape = CircleShape,
        border = BorderStroke(1.dp, ParityStyle.softStroke)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = ParityStyle.text)
        }
    }
}

@Composable
private fun ParityMiniMonth(
    month: YearMonth,
    indexed: Map<String, List<CalendarEntry>>,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val cells = remember(month) { CalendarWeeks.monthCells(month) }
    Surface(
        onClick = onClick, modifier = modifier,
        color = ParityStyle.card, shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ParityStyle.stroke)
    ) {
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                month.month.getDisplayName(TextStyle.FULL, Locale("hr", "HR")).uppercase(),
                fontSize = 15.sp, color = ParityStyle.text, fontWeight = FontWeight.Black
            )
            Row {
                listOf("PON","UTO","SRI","ČET","PET","SUB","NED").forEachIndexed { i, day ->
                    Text(day, Modifier.weight(1f), fontSize = 6.sp,
                        textAlign = TextAlign.Center,
                        color = if (i >= 5) ParityStyle.weekend else ParityStyle.muted)
                }
            }
            cells.chunked(7).forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    week.forEach { date ->
                        val own = YearMonth.from(date) == month
                        val entry = indexed[date.toString()]?.firstOrNull()
                        Box(
                            Modifier.weight(1f).aspectRatio(1f)
                                .background(
                                    if (!own) Color.Transparent
                                    else entry?.let { ParityStyle.tiles[it.color] }
                                        ?: if (date.dayOfWeek.value >= 6) ParityStyle.weekendEmpty
                                        else ParityStyle.empty,
                                    RoundedCornerShape(4.dp)
                                ), contentAlignment = Alignment.Center
                        ) {
                            if (own) Text(
                                entry?.title?.take(2)?.uppercase()?.takeIf { it.isNotBlank() }
                                    ?: date.dayOfMonth.toString(),
                                fontSize = 7.sp, maxLines = 1,
                                color = if (entry != null) ParityStyle.bg else ParityStyle.text,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
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
    val monthEntries = remember(month, entries) {
        entries.filter { it.date.startsWith(month.toString()) }
    }
    val distinctDays = monthEntries.map { it.date }.toSet().size
    val shiftCount = monthEntries.count { it.category == "Smjena" }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Surface(color = ParityStyle.card, shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, ParityStyle.stroke)) {
            Row(Modifier.fillMaxWidth().padding(9.dp),
                verticalAlignment = Alignment.CenterVertically) {
                ParityYearArrow(Icons.Rounded.ChevronLeft, "Prethodni mjesec", onPreviousMonth)
                Text("SAŽETAK · " + month.month.getDisplayName(
                    TextStyle.FULL, Locale("hr", "HR")
                ).uppercase() + " " + month.year,
                    modifier = Modifier.weight(1f), fontSize = 16.sp,
                    fontWeight = FontWeight.Black, textAlign = TextAlign.Center,
                    color = ParityStyle.text)
                ParityYearArrow(Icons.Rounded.ChevronRight, "Sljedeći mjesec", onNextMonth)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ParityStat("Zapisa", monthEntries.size.toString(), Modifier.weight(1f))
            ParityStat("Smjena", shiftCount.toString(), Modifier.weight(1f))
            ParityStat("Dana", distinctDays.toString(), Modifier.weight(1f))
        }
        Surface(color = ParityStyle.card, shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, ParityStyle.softStroke)) {
            Column(Modifier.fillMaxWidth().padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("PREGLED VRSTA ZAPISA", fontWeight = FontWeight.Black,
                    color = ParityStyle.text, fontSize = 14.sp)
                EntryRules.categories.forEachIndexed { idx, category ->
                    val count = monthEntries.count { it.category == category }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).background(
                            ParityStyle.tiles[idx], CircleShape))
                        Spacer(Modifier.width(9.dp))
                        Text(category, Modifier.weight(1f), color = ParityStyle.text)
                        Text(count.toString(), color = ParityStyle.muted)
                    }
                }
            }
        }
        Surface(color = ParityStyle.card, shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, ParityStyle.softStroke)) {
            Column(Modifier.fillMaxWidth().padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("SIGURNOSNE KOPIJE", fontWeight = FontWeight.Black,
                    color = ParityStyle.text, fontSize = 14.sp)
                Text("Podaci ostaju na uređaju. Kopiju spremate i uvozite sami.",
                    color = ParityStyle.muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onExport, Modifier.weight(1f)) {
                        Text("Izvezi kopiju")
                    }
                    OutlinedButton(onClick = onImport, Modifier.weight(1f)) {
                        Text("Uvezi kopiju")
                    }
                }
                if (notice != null) Text(notice, color = ParityStyle.muted,
                    fontSize = 12.sp)
            }
        }
        Text("Bez prikaza plaća, satnica i obračuna.",
            color = ParityStyle.muted, fontSize = 11.sp)
    }
}

@Composable
private fun ParityStat(label: String, value: String, modifier: Modifier) {
    Surface(modifier = modifier, color = ParityStyle.card,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ParityStyle.softStroke)) {
        Column(Modifier.padding(11.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black,
                color = ParityStyle.accent)
            Text(label, fontSize = 11.sp, color = ParityStyle.muted)
        }
    }
}
