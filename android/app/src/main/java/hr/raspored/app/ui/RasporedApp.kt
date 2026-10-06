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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.data.ShiftLibraryStore
import hr.raspored.app.data.UiSettingsStore
import java.time.YearMonth

internal enum class MainSection { MONTH, YEAR, SUMMARY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RasporedApp() {
    val context = LocalContext.current
    val schedule = remember { ScheduleStore(context) }
    val uiSettings = remember { UiSettingsStore(context) }
    val shiftLibrary = remember { ShiftLibraryStore(context) }

    var section by remember { mutableStateOf(MainSection.MONTH) }
    var month by remember { mutableStateOf(YearMonth.of(2026, 10)) }
    var showShifts by remember { mutableStateOf(false) }
    var showNewShift by remember { mutableStateOf(false) }
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
            TopTabs(section = section, month = month, onSection = { section = it })

            when (section) {
                MainSection.MONTH -> MonthScreen(
                    month = month,
                    onMonthChange = { month = it },
                    schedule = schedule,
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
                    shiftTypes = shiftLibrary.all,
                    onMonthChange = { month = it }
                )
            }
        }
    }

    if (showShifts) {
        ShiftManagerSheet(
            library = shiftLibrary,
            onDismiss = { showShifts = false },
            onNewShift = { showShifts = false; showNewShift = true }
        )
    }
    if (showNewShift) {
        NewShiftSheet(library = shiftLibrary, onDismiss = { showNewShift = false })
    }
    if (showSettings) {
        SettingsSheet(store = uiSettings, onDismiss = { showSettings = false })
    }
    if (showSearch) {
        SearchSheet(
            schedule = schedule,
            shiftTypes = shiftLibrary.all,
            onDismiss = { showSearch = false },
            onPick = { date ->
                month = YearMonth.from(date)
                section = MainSection.MONTH
                showSearch = false
            }
        )
    }
}

@Composable
private fun PremiumHeader(
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AppMark()
        Text(
            "Raspored",
            color = RasporedColors.Text,
            fontSize = 31.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(Modifier.weight(1f))
        HeaderIconButton(Icons.Rounded.Search, "Pretraži", onSearch)
        HeaderIconButton(Icons.Rounded.Tune, "Postavke", onSettings)
        FilledIconButton(
            onClick = onAdd,
            modifier = Modifier.size(54.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = RasporedColors.Accent,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(19.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Nova smjena", modifier = Modifier.size(31.dp))
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = RasporedColors.Card2,
            contentColor = RasporedColors.Text
        ),
        shape = RoundedCornerShape(17.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(27.dp))
    }
}

@Composable
private fun AppMark() {
    Surface(
        modifier = Modifier.size(46.dp),
        color = RasporedColors.Card2,
        shape = RoundedCornerShape(15.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, RasporedColors.Accent)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(
                    Modifier
                        .size(width = 25.dp, height = 7.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(RasporedColors.Accent)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) {
                        Box(Modifier.size(6.dp).clip(RoundedCornerShape(2.dp)).background(if (it == 1) RasporedColors.Day else RasporedColors.Accent))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) {
                        Box(Modifier.size(6.dp).clip(RoundedCornerShape(2.dp)).background(RasporedColors.Accent.copy(alpha = .9f)))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopTabs(section: MainSection, month: YearMonth, onSection: (MainSection) -> Unit) {
    val items = listOf(
        MainSection.MONTH to month.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("hr", "HR")).uppercase(),
        MainSection.YEAR to month.year.toString(),
        MainSection.SUMMARY to "SAŽETAK"
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 5.dp),
        color = RasporedColors.Card.copy(alpha = .96f),
        shape = RoundedCornerShape(23.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RasporedColors.StrokeSoft)
    ) {
        Row(Modifier.padding(4.dp)) {
            items.forEach { (item, label) ->
                val active = section == item
                Surface(
                    modifier = Modifier.weight(1f),
                    onClick = { onSection(item) },
                    color = if (active) RasporedColors.Accent.copy(alpha = .22f) else Color.Transparent,
                    shape = RoundedCornerShape(18.dp),
                    border = if (active) androidx.compose.foundation.BorderStroke(1.4.dp, RasporedColors.Accent) else null
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(vertical = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = if (active) Color.White else RasporedColors.Muted,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashView() {
    Box(Modifier.fillMaxSize().background(RasporedColors.AppGradient).systemBarsPadding(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Surface(modifier = Modifier.size(122.dp), color = RasporedColors.Card2, shape = RoundedCornerShape(32.dp), border = androidx.compose.foundation.BorderStroke(2.dp, RasporedColors.Accent)) { Box(contentAlignment = Alignment.Center) { AppMark() } }
            Text("Raspored", color = RasporedColors.Text, fontSize = 40.sp, fontWeight = FontWeight.Black)
            Text("Pametni planer smjena", color = RasporedColors.Muted, fontSize = 17.sp)
            LinearProgressIndicator(progress = { .68f }, modifier = Modifier.width(220.dp), color = RasporedColors.Accent, trackColor = RasporedColors.Card2)
        }
    }
}
