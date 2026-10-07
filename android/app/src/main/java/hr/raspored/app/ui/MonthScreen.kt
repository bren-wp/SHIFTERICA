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

