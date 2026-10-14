package hr.raspored.kalendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

/** Same 7-column premium calendar tiles as the original Raspored, with notes as entries. */
@Composable
internal fun MonthGrid(
    modifier: Modifier = Modifier,
    month: YearMonth, selected: LocalDate,
    entries: List<CalendarEntry>, weekOnly: Boolean, onSelect: (LocalDate) -> Unit
) {
    val dates = remember(month, selected, weekOnly) {
        if (weekOnly) CalendarWeeks.containing(selected) else CalendarWeeks.monthCells(month)
    }
    val byDate = remember(entries) { entries.groupBy { it.date } }
    val today = LocalDate.now()
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 2.dp),
        color = ParityStyle.card, shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, ParityStyle.stroke)
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 7.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row {
                listOf("PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED").forEachIndexed { i, day ->
                    Text(
                        day, modifier = Modifier.weight(1f), fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
                        color = if (i >= 5) ParityStyle.weekend else ParityStyle.muted
                    )
                }
            }
            dates.chunked(7).forEach { week ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    week.forEach { date ->
                        val own = weekOnly || YearMonth.from(date) == month
                        val dayItems = byDate[date.toString()].orEmpty()
                        val item = dayItems.firstOrNull()
                        val tileColor = when {
                            item != null -> ParityStyle.tiles[item.color]
                            date.dayOfWeek.value >= 6 -> ParityStyle.weekendEmpty
                            else -> ParityStyle.empty
                        }
                        val outline = when {
                            date == today -> ParityStyle.accent
                            item != null -> tileColor
                            else -> ParityStyle.softStroke
                        }
                        Surface(
                            onClick = { onSelect(date) },
                            modifier = Modifier.weight(1f).fillMaxHeight().semantics {
                                contentDescription = date.toString() + ", " +
                                    dayItems.size + " zapisa"
                            },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(13.dp),
                            border = BorderStroke(
                                if (date == today) 2.dp else 1.dp, outline
                            ),
                            shadowElevation = if (own && item != null) 4.dp else 1.dp
                        ) {
                            Box(
                                Modifier.fillMaxSize().background(
                                    Brush.verticalGradient(
                                        listOf(
                                            tileColor.copy(alpha = if (own) 1f else .4f),
                                            tileColor.copy(alpha = if (own) .82f else .26f)
                                        )
                                    ), RoundedCornerShape(13.dp)
                                ).padding(5.dp)
                            ) {
                                Text(
                                    date.dayOfMonth.toString(),
                                    Modifier.align(Alignment.TopStart),
                                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                    color = if (own) {
                                        if (item == null) ParityStyle.text else ParityStyle.bg
                                    } else ParityStyle.muted.copy(alpha = .5f)
                                )
                                if (item != null) Text(
                                    calendarTileCode(item),
                                    modifier = Modifier.align(Alignment.Center),
                                    fontSize = 20.sp, fontWeight = FontWeight.Black,
                                    color = ParityStyle.bg, maxLines = 1
                                )
                                if (date == selected) Box(
                                    Modifier.align(Alignment.BottomCenter).size(5.dp)
                                        .background(ParityStyle.accent, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun calendarTileCode(item: CalendarEntry): String {
    val title = item.title.trim()
    val lowered = title.lowercase(Locale("hr", "HR"))
    return when {
        lowered.startsWith("noć") || lowered.startsWith("noc") -> "N"
        lowered.startsWith("dnev") -> "D"
        lowered.startsWith("jut") -> "J"
        lowered.startsWith("pop") -> "P"
        lowered.startsWith("godiš") || lowered.startsWith("godis") -> "GO"
        lowered.startsWith("bolov") -> "BO"
        title.isNotBlank() -> title.take(2).uppercase(Locale("hr", "HR"))
        else -> "•"
    }
}
