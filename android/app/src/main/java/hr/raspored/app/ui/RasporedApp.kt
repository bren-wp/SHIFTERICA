package hr.raspored.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.data.MonthlyAccountingStore
import hr.raspored.app.data.ShiftLibraryStore
import hr.raspored.app.data.UiSettingsStore
import hr.raspored.app.model.ShiftType
import hr.raspored.app.reminders.ShiftReminders
import java.time.YearMonth
import java.time.LocalDate

internal enum class MainSection { MONTH, YEAR, SUMMARY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RasporedApp() {
    val context = LocalContext.current
    val schedule = remember { ScheduleStore(context) }
    val accounting = remember { MonthlyAccountingStore(context) }
    val uiSettings = remember { UiSettingsStore(context) }
    val shiftLibrary = remember { ShiftLibraryStore(context) }

    // Schedule edits, deletions and preference changes reschedule both types.
    // Also refreshes after app resume/start; OS reboot uses the secure receiver.
    val reminderEntries = schedule.entries.toMap()
    val remindersEnabled = uiSettings.remindersEnabled
    val eveningEnabled = uiSettings.eveningReminderEnabled
    val shiftTimeEnabled = uiSettings.shiftTimeReminderEnabled
    val reminderStartTimes = shiftLibrary.all.mapNotNull { shift ->
        shift.start?.let { shift.code to it }
    }.toMap()
    LaunchedEffect(reminderEntries, remindersEnabled, eveningEnabled, shiftTimeEnabled, reminderStartTimes) {
        ShiftReminders.refresh(
            context, reminderEntries, remindersEnabled, eveningEnabled, shiftTimeEnabled, reminderStartTimes
        )
    }

    var section by remember { mutableStateOf(MainSection.MONTH) }
    var dateToOpen by remember { mutableStateOf<LocalDate?>(null) }
    var month by remember { mutableStateOf(YearMonth.now()) }
    var showShifts by remember { mutableStateOf(false) }
    var showNewShift by remember { mutableStateOf(false) }
    var editingCustomShift by remember { mutableStateOf<ShiftType?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(1200); showSplash = false }

    if (showSplash) {
        SplashView()
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RasporedColors.AppGradient)
            .systemBarsPadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            PremiumHeader(
                onSearch = { showSearch = true },
                onSettings = { showSettings = true },
                onAdd = { showNewShift = true }
            )
            TopTabs(section = section, month = month, onSection = { section = it }, onMonthChange = { month = it })

            when (section) {
                MainSection.MONTH -> MonthScreen(
                    month = month,
                    onMonthChange = { month = it },
                    dateToOpen = dateToOpen,
                    onDateOpened = { dateToOpen = null },
                    schedule = schedule,
                    accounting = accounting,
                    uiSettings = uiSettings,
                    shiftTypes = shiftLibrary.all,
                    onOpenShifts = { showShifts = true }
                )
                MainSection.YEAR -> YearScreen(
                    year = month.year,
                    schedule = schedule,
                    shiftTypes = shiftLibrary.all,
                    onYearChange = { month = month.withYear(it) },
                    onOpenMonth = { month = it; section = MainSection.MONTH }
                )
                MainSection.SUMMARY -> SummaryScreen(
                    month = month,
                    schedule = schedule,
                    accounting = accounting,
                    shiftTypes = shiftLibrary.all,
                    onMonthChange = { month = it }
                )
            }
        }
    }

    if (showShifts) {
        ShiftManagerSheet(
            library = shiftLibrary,
            schedule = schedule,
            onDismiss = { showShifts = false },
            onNewShift = {
                showShifts = false
                editingCustomShift = null
                showNewShift = true
            },
            onEditCustom = { shift ->
                showShifts = false
                editingCustomShift = shift
                showNewShift = true
            },
            onShowAssignedDate = { date ->
                showShifts = false
                month = YearMonth.from(date)
                section = MainSection.MONTH
                dateToOpen = date
            }
        )
    }
    if (showNewShift) {
        NewShiftSheet(
            library = shiftLibrary,
            initialShift = editingCustomShift,
            onDismiss = {
                showNewShift = false
                editingCustomShift = null
            }
        )
    }
    if (showSettings) {
        SettingsSheet(
            store = uiSettings,
            schedule = schedule,
            library = shiftLibrary,
            onDismiss = { showSettings = false }
        )
    }
    if (showSearch) {
        SearchSheet(
            schedule = schedule,
            shiftTypes = shiftLibrary.all,
            onDismiss = { showSearch = false },
            onPick = { date ->
                showSearch = false
                month = YearMonth.from(date)
                section = MainSection.MONTH
                dateToOpen = date
            }
        )
    }
}

