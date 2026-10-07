package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.data.UiSettingsStore
import hr.raspored.app.model.ShiftType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun CalendarCard(
    modifier: Modifier,
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    schedule: ScheduleStore,
    settings: UiSettingsStore,
    shiftTypes: List<ShiftType>,
    onDayClick: (LocalDate) -> Unit
) {
    val baseWeekdays = listOf("PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED")
    val startIndex = baseWeekdays.indexOf(settings.firstWeekday).takeIf { it >= 0 } ?: 0
    val weekdays = baseWeekdays.drop(startIndex) + baseWeekdays.take(startIndex)
    val firstDayValue = when (settings.firstWeekday) {
        "UTO" -> DayOfWeek.TUESDAY.value
        "SRI" -> DayOfWeek.WEDNESDAY.value
        "ČET" -> DayOfWeek.THURSDAY.value
        "PET" -> DayOfWeek.FRIDAY.value
        "SUB" -> DayOfWeek.SATURDAY.value
        "NED" -> DayOfWeek.SUNDAY.value
        else -> DayOfWeek.MONDAY.value
    }
    val cells = remember(month, settings.showOutsideDays, firstDayValue) {
        monthGrid(month, settings.showOutsideDays, firstDayValue)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        color = RasporedColors.Card,
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MonthArrow(Icons.Rounded.ChevronLeft) {
                    onMonthChange(month.minusMonths(1))
                }
                Text(
                    month.month
                        .getDisplayName(TextStyle.FULL, Locale("hr", "HR"))
                        .uppercase() + " " + month.year,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = RasporedColors.Text,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                MonthArrow(Icons.Rounded.ChevronRight) {
                    onMonthChange(month.plusMonths(1))
                }
            }

            Row(Modifier.fillMaxWidth()) {
                weekdays.forEach { text ->
                    val weekend = text == "SUB" || text == "NED"
                    Text(
                        text,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = if (weekend) RasporedColors.Weekend else RasporedColors.Muted,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                userScrollEnabled = false
            ) {
                items(cells) { date ->
                    if (date == null) {
                        Spacer(Modifier.aspectRatio(.80f))
                    } else {
                        CalendarCell(
                            date = date,
                            inside = YearMonth.from(date) == month,
                            shift = shiftTypes.firstOrNull {
                                it.code == schedule.code(date)
                            },
                            settings = settings,
                            onClick = { onDayClick(date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        color = RasporedColors.Card2,
        contentColor = RasporedColors.Text,
        shape = CircleShape,
        border = BorderStroke(1.dp, RasporedColors.StrokeSoft),
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun CalendarCell(
    date: LocalDate,
    inside: Boolean,
    shift: ShiftType?,
    settings: UiSettingsStore,
    onClick: () -> Unit
) {
    val weekend =
        date.dayOfWeek == DayOfWeek.SATURDAY ||
            date.dayOfWeek == DayOfWeek.SUNDAY
    val base = when {
        shift != null -> shift.color
        weekend && settings.highlightWeekends -> RasporedColors.WeekendEmpty
        else -> RasporedColors.Empty
    }
    val foreground = when {
        shift != null -> shift.textColor
        weekend -> Color(0xFFF7A2AF)
        else -> RasporedColors.Text
    }
    val today = date == LocalDate.now() && settings.highlightToday
    val todayColors = listOf(
        RasporedColors.Night,
        RasporedColors.Day,
        RasporedColors.Annual,
        RasporedColors.Morning,
        Color(0xFFB16CE4),
        Color(0xFFFF5BAA),
        Color(0xFFFF853A)
    )
    val todayColor = todayColors[
        settings.todayColorIndex.coerceIn(todayColors.indices)
    ]
    val shape = todayShape(settings.todayShape)
    val dayNumberSize = when (settings.dayNumberSize) {
        "XS" -> 9.sp
        "S" -> 10.sp
        "L" -> 14.sp
        "XL" -> 16.sp
        else -> 12.sp
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.aspectRatio(.80f),
        color = base.copy(alpha = if (inside) 1f else .38f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (today) 2.dp else 1.dp,
            color = when {
                today -> todayColor.copy(alpha = settings.todayOpacity / 100f)
                shift != null -> shift.color.copy(alpha = .85f)
                else -> RasporedColors.StrokeSoft
            }
        ),
        shadowElevation = if (shift != null && inside) 5.dp else 0.dp
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            if (today) {
                Box(
                    Modifier
                        .matchParentSize()
                        .padding(2.dp)
                        .clip(shape)
                        .background(
                            todayColor.copy(
                                alpha = (settings.todayOpacity / 100f) * .16f
                            )
                        )
                )
            }

            Text(
                date.dayOfMonth.toString(),
                color = foreground.copy(alpha = if (inside) 1f else .62f),
                fontSize = dayNumberSize,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.TopStart)
            )

            if (shift != null) {
                Text(
                    shift.code,
                    color = shift.textColor.copy(alpha = if (inside) 1f else .58f),
                    fontSize = if (shift.code.length == 1) {
                        (shift.fontSize + 11).sp
                    } else {
                        (shift.fontSize + 4).sp
                    },
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

private fun todayShape(value: String): Shape = when (value) {
    "Krug" -> CircleShape
    "Kvadrat" -> RoundedCornerShape(4.dp)
    "Pill" -> RoundedCornerShape(50)
    else -> RoundedCornerShape(10.dp)
}

private fun monthGrid(
    month: YearMonth,
    showOutside: Boolean,
    firstDayValue: Int
): List<LocalDate?> {
    val first = month.atDay(1)
    val offset = (first.dayOfWeek.value - firstDayValue + 7) % 7
    val count = ((offset + month.lengthOfMonth() + 6) / 7) * 7
    val start = first.minusDays(offset.toLong())

    return (0 until count).map { index ->
        val date = start.plusDays(index.toLong())
        if (showOutside || YearMonth.from(date) == month) date else null
    }
}
