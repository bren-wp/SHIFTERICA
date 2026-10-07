package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.model.ShiftType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun YearScreen(
    year: Int,
    schedule: ScheduleStore,
    shiftTypes: List<ShiftType>,
    onYearChange: (Int) -> Unit,
    onOpenMonth: (YearMonth) -> Unit
) {
    val now = YearMonth.now()
    val gridState = rememberLazyGridState()

    LaunchedEffect(year) {
        val targetIndex = if (year == now.year) now.monthValue - 1 else 0
        gridState.scrollToItem(targetIndex.coerceAtLeast(0))
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = RasporedColors.Card,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, RasporedColors.Stroke.copy(alpha = .95f)),
            shadowElevation = 7.dp
        ) {
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = { onYearChange(year - 1) },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = RasporedColors.Card2),
                    shape = RoundedCornerShape(18.dp)
                ) { Icon(Icons.Rounded.ChevronLeft, "Prethodna godina") }
                Text((year - 1).toString(), color = RasporedColors.Muted, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(year.toString(), color = RasporedColors.Text, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.weight(1f))
                Text((year + 1).toString(), color = RasporedColors.Muted, fontWeight = FontWeight.Bold)
                FilledIconButton(
                    onClick = { onYearChange(year + 1) },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = RasporedColors.Card2),
                    shape = RoundedCornerShape(18.dp)
                ) { Icon(Icons.Rounded.ChevronRight, "Sljedeća godina") }
            }
        }

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 6.dp)
        ) {
            items((1..12).map { YearMonth.of(year, it) }) { month ->
                MiniMonthCard(month, schedule, shiftTypes) { onOpenMonth(month) }
            }
        }

    }
}

@Composable
private fun MiniMonthCard(month: YearMonth, schedule: ScheduleStore, shiftTypes: List<ShiftType>, onClick: () -> Unit) {
    val dates = miniMonthDates(month)
    Surface(
        onClick = onClick,
        color = RasporedColors.Card,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke.copy(alpha = .92f)),
        shadowElevation = 7.dp
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                month.month.getDisplayName(TextStyle.FULL, Locale("hr", "HR")).uppercase(),
                color = RasporedColors.Text,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
            Row(Modifier.fillMaxWidth()) {
                listOf("PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED").forEachIndexed { i, day ->
                    Text(
                        day,
                        Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = if (i >= 5) RasporedColors.Weekend else RasporedColors.Muted,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            for (row in dates.chunked(7)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    row.forEach { date ->
                        if (date == null) Spacer(Modifier.weight(1f).aspectRatio(1f))
                        else {
                            val shift = shiftTypes.firstOrNull { it.code == schedule.code(date) }
                            val weekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
                            val bg = shift?.color ?: if (weekend) RasporedColors.WeekendEmpty else RasporedColors.Empty
                            val miniBrush = Brush.verticalGradient(
                                listOf(
                                    bg,
                                    bg.copy(alpha = if (shift != null) .80f else .90f)
                                )
                            )
                            Surface(
                                modifier = Modifier.weight(1f).aspectRatio(1f),
                                color = Color.Transparent,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    if (shift != null) .8.dp else .5.dp,
                                    if (shift != null) shift.color.copy(alpha = .92f)
                                    else RasporedColors.StrokeSoft
                                ),
                                shadowElevation = if (shift != null) 2.dp else 0.dp
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(miniBrush, RoundedCornerShape(6.dp))
                                ) {
                                    if (shift != null) {
                                        Text(
                                            date.dayOfMonth.toString(),
                                            modifier = Modifier.align(Alignment.TopStart).padding(start = 2.dp, top = 1.dp),
                                            color = shift.textColor.copy(alpha = .78f),
                                            fontSize = 5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            shift.code,
                                            modifier = Modifier.align(Alignment.Center),
                                            color = shift.textColor,
                                            fontSize = if (shift.code.length == 1) 7.sp else 6.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    } else {
                                        Text(
                                            date.dayOfMonth.toString(),
                                            modifier = Modifier.align(Alignment.Center),
                                            color = if (weekend) RasporedColors.Weekend else RasporedColors.Text,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun miniMonthDates(month: YearMonth): List<LocalDate?> {
    val first = month.atDay(1)
    val offset = first.dayOfWeek.value - 1
    val result = MutableList<LocalDate?>(offset) { null }
    repeat(month.lengthOfMonth()) { index -> result += month.atDay(index + 1) }
    while (result.size % 7 != 0) result += null
    return result
}
