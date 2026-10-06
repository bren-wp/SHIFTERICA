package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Output
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
internal fun MonthScreen(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    schedule: ScheduleStore,
    uiSettings: UiSettingsStore,
    shiftTypes: List<ShiftType>,
    onOpenShifts: () -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var toolCode by remember { mutableStateOf<String?>(null) }
    var erasing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CalendarCard(
            modifier = Modifier.weight(1f, fill = true),
            month = month,
            onMonthChange = onMonthChange,
            schedule = schedule,
            settings = uiSettings,
            shiftTypes = shiftTypes,
            onDayClick = { date ->
                when {
                    editing && erasing -> schedule.set(date, null)
                    editing && toolCode != null -> schedule.set(date, toolCode)
                    else -> {
                        editing = true
                        toolCode = schedule.code(date) ?: shiftTypes.firstOrNull()?.code ?: "D"
                        erasing = false
                    }
                }
            }
        )

        if (editing) {
            EditingDock(
                shiftTypes = shiftTypes,
                selectedCode = toolCode,
                erasing = erasing,
                onSelect = { code -> toolCode = code; erasing = false },
                onErase = { erasing = true; toolCode = null },
                onExit = { editing = false; erasing = false; toolCode = null }
            )
        } else {
            ShiftLegend(shiftTypes = shiftTypes, onOpenShifts = onOpenShifts)
            QuickToolbar(
                shiftTypes = shiftTypes,
                onEdit = { editing = true; erasing = true },
                onShift = { code -> editing = true; toolCode = code; erasing = false },
                onMore = onOpenShifts
            )
        }
    }
}

@Composable
private fun CalendarCard(
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
        modifier = modifier.fillMaxWidth().padding(horizontal = 14.dp),
        color = RasporedColors.Card,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MonthArrow(Icons.Rounded.ChevronLeft) { onMonthChange(month.minusMonths(1)) }
                Text(
                    month.month.getDisplayName(TextStyle.FULL, Locale("hr", "HR")).uppercase() + " " + month.year,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = RasporedColors.Text,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                MonthArrow(Icons.Rounded.ChevronRight) { onMonthChange(month.plusMonths(1)) }
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
                        fontSize = 12.sp
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                userScrollEnabled = false
            ) {
                items(cells) { date ->
                    if (date == null) {
                        Spacer(Modifier.aspectRatio(.87f))
                    } else {
                        CalendarCell(
                            date = date,
                            inside = YearMonth.from(date) == month,
                            shift = shiftTypes.firstOrNull { it.code == schedule.code(date) },
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
private fun MonthArrow(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(46.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = RasporedColors.Card2,
            contentColor = RasporedColors.Text
        ),
        shape = CircleShape
    ) { Icon(icon, null) }
}

@Composable
private fun CalendarCell(
    date: LocalDate,
    inside: Boolean,
    shift: ShiftType?,
    settings: UiSettingsStore,
    onClick: () -> Unit
) {
    val weekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    val base = when {
        shift != null -> shift.color
        weekend && settings.highlightWeekends -> RasporedColors.WeekendEmpty
        else -> RasporedColors.Empty
    }
    val foreground = if (shift != null) shift.textColor else if (weekend) Color(0xFFF7A2AF) else RasporedColors.Text
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
    val todayColor = todayColors[settings.todayColorIndex.coerceIn(todayColors.indices)]
    val shape = todayShape(settings.todayShape)
    val dayNumberSize = when (settings.dayNumberSize) {
        "XS" -> 8.sp
        "S" -> 10.sp
        "L" -> 13.sp
        "XL" -> 15.sp
        else -> 11.sp
    }

    Surface(
        modifier = Modifier.aspectRatio(.87f).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        color = base.copy(alpha = if (inside) 1f else .42f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (today) 2.dp else 1.dp,
            color = when {
                today -> todayColor.copy(alpha = settings.todayOpacity / 100f)
                shift != null -> shift.color.copy(alpha = .8f)
                else -> RasporedColors.StrokeSoft
            }
        )
    ) {
        Box(Modifier.fillMaxSize().padding(5.dp)) {
            if (today) {
                Box(
                    Modifier
                        .matchParentSize()
                        .padding(2.dp)
                        .clip(shape)
                        .background(todayColor.copy(alpha = (settings.todayOpacity / 100f) * .16f))
                )
            }
            Text(
                date.dayOfMonth.toString(),
                color = foreground.copy(alpha = if (inside) 1f else .68f),
                fontSize = dayNumberSize,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.TopStart)
            )
            if (shift != null) {
                Text(
                    shift.code,
                    color = shift.textColor.copy(alpha = if (inside) 1f else .6f),
                    fontSize = if (shift.code.length == 1) (shift.fontSize + 10).sp else (shift.fontSize + 3).sp,
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

@Composable
private fun ShiftLegend(shiftTypes: List<ShiftType>, onOpenShifts: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        color = RasporedColors.Card,
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Vrste smjena", color = RasporedColors.Text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onOpenShifts, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Rounded.ChevronRight, null, tint = RasporedColors.Muted)
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(shiftTypes, key = { it.code }) { shift -> LegendChip(shift) }
            }
        }
    }
}

@Composable
private fun LegendChip(shift: ShiftType) {
    Surface(
        color = RasporedColors.Card2,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, RasporedColors.StrokeSoft)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Surface(color = shift.color, shape = RoundedCornerShape(10.dp)) {
                Text(
                    shift.code,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    color = shift.textColor,
                    fontWeight = FontWeight.Black
                )
            }
            Text(shift.shortName, color = RasporedColors.Text, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun QuickToolbar(shiftTypes: List<ShiftType>, onEdit: () -> Unit, onShift: (String) -> Unit, onMore: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
        color = RasporedColors.Card,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolIcon(Icons.Rounded.Backspace, "Gumica", onEdit)
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                items(shiftTypes, key = { it.code }) { shift ->
                    Surface(
                        modifier = Modifier.size(48.dp),
                        onClick = { onShift(shift.code) },
                        color = shift.color,
                        shape = RoundedCornerShape(13.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(shift.code, color = shift.textColor, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
            Box(Modifier.width(1.dp).height(38.dp).background(RasporedColors.Stroke))
            ToolIcon(Icons.Rounded.MoreHoriz, "Više", onMore)
        }
    }
}

@Composable
private fun ToolIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(48.dp),
        onClick = onClick,
        color = RasporedColors.Card2,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, RasporedColors.StrokeSoft)
    ) { Box(contentAlignment = Alignment.Center) { Icon(icon, label, tint = RasporedColors.Text) } }
}

@Composable
private fun EditingDock(
    shiftTypes: List<ShiftType>,
    selectedCode: String?,
    erasing: Boolean,
    onSelect: (String) -> Unit,
    onErase: () -> Unit,
    onExit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RasporedColors.Card,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.size(width = 56.dp, height = 5.dp).clip(RoundedCornerShape(3.dp)).background(RasporedColors.Muted.copy(.5f)))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Način uređivanja", color = RasporedColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text("Dodirnite dan kako biste primijenili smjenu", color = RasporedColors.Muted, fontSize = 12.sp)
                }
                OutlinedButton(onClick = onExit, border = BorderStroke(1.dp, RasporedColors.Accent), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Rounded.Output, null, tint = RasporedColors.Accent)
                    Spacer(Modifier.width(6.dp))
                    Text("Izađi iz uređivanja", color = RasporedColors.Accent, fontSize = 12.sp)
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    EditToolChip(
                        label = "Gumica",
                        code = "",
                        color = RasporedColors.Card2,
                        selected = erasing,
                        icon = { Icon(Icons.Rounded.Backspace, null, tint = RasporedColors.Text) },
                        onClick = onErase
                    )
                }
                items(shiftTypes, key = { it.code }) { shift ->
                    EditToolChip(
                        label = shift.shortName,
                        code = shift.code,
                        color = shift.color,
                        selected = selectedCode == shift.code,
                        textColor = shift.textColor,
                        icon = null,
                        onClick = { onSelect(shift.code) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditToolChip(
    label: String,
    code: String,
    color: Color,
    selected: Boolean,
    textColor: Color = RasporedColors.Text,
    icon: (@Composable (() -> Unit))?,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Surface(
            modifier = Modifier.size(64.dp),
            onClick = onClick,
            color = color,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) RasporedColors.Accent else RasporedColors.StrokeSoft)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (icon != null) icon() else Text(code, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
        Text(label, color = RasporedColors.Text, fontSize = 11.sp)
    }
}

private fun monthGrid(month: YearMonth, showOutside: Boolean, firstDayValue: Int): List<LocalDate?> {
    val first = month.atDay(1)
    val offset = (first.dayOfWeek.value - firstDayValue + 7) % 7
    val count = ((offset + month.lengthOfMonth() + 6) / 7) * 7
    val start = first.minusDays(offset.toLong())
    return (0 until count).map { index ->
        val date = start.plusDays(index.toLong())
        if (showOutside || YearMonth.from(date) == month) date else null
    }
}
