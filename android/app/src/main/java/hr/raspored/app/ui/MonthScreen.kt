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
import java.time.LocalDate
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
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        MonthInsights(
            month = month,
            schedule = schedule,
            shiftTypes = shiftTypes
        )

        CalendarCard(
            modifier = Modifier.weight(1f, fill = true),
            month = month,
            onMonthChange = onMonthChange,
            schedule = schedule,
            settings = uiSettings,
            shiftTypes = shiftTypes,
            onDayClick = { date -> selectedDate = date }
        )

        MonthManageBar(onMore = onOpenShifts)
    }

    selectedDate?.let { date ->
        DayShiftPickerSheet(
            date = date,
            currentCode = schedule.code(date),
            shiftTypes = shiftTypes,
            onSelect = { code ->
                schedule.set(date, code)
                selectedDate = null
            },
            onClear = {
                schedule.set(date, null)
                selectedDate = null
            },
            onOpenShifts = {
                selectedDate = null
                onOpenShifts()
            },
            onDismiss = { selectedDate = null }
        )
    }
}
