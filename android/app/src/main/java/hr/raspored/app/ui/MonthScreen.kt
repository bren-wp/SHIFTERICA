package hr.raspored.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.data.UiSettingsStore
import hr.raspored.app.model.ShiftType
import java.time.YearMonth

@Composable
internal fun MonthScreen(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    schedule: ScheduleStore,
    uiSettings: UiSettingsStore,
    shiftTypes: List<ShiftType>,
    onOpenShifts: () -> Unit
) {
    var selectedCode by remember { mutableStateOf("D") }
    var erasing by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CalendarCard(
            modifier = Modifier.weight(1f, fill = true),
            month = month,
            onMonthChange = onMonthChange,
            schedule = schedule,
            settings = uiSettings,
            shiftTypes = shiftTypes,
            onDayClick = { date ->
                if (editing) {
                    if (erasing) {
                        schedule.set(date, null)
                    } else {
                        schedule.set(date, selectedCode)
                    }
                }
            }
        )

        CompactShiftToolbar(
            shiftTypes = shiftTypes,
            selectedCode = selectedCode,
            erasing = erasing,
            editing = editing,
            onSelect = { code ->
                selectedCode = code
                erasing = false
            },
            onErase = {
                erasing = true
            },
            onEditingChange = { enabled ->
                editing = enabled
                if (!enabled) erasing = false
            },
            onMore = onOpenShifts
        )
    }
}
