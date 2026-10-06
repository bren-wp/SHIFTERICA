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
        Surface(
            onClick = onAdd,
            modifier = Modifier.size(56.dp),
            color = RasporedColors.Accent,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF79FFFF)),
            shadowElevation = 10.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Add, contentDescription = "Nova smjena", modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(50.dp),
        color = RasporedColors.Card2,
        contentColor = RasporedColors.Text,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RasporedColors.StrokeSoft),
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun AppMark(modifier: Modifier = Modifier.size(46.dp)) {
    BoxWithConstraints(modifier = modifier) {
        val unit = maxWidth
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = RasporedColors.Card2,
            shape = RoundedCornerShape(unit * .32f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, RasporedColors.Accent),
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(unit * .065f)
                ) {
                    Box(
                        Modifier
                            .size(width = unit * .54f, height = unit * .15f)
                            .clip(RoundedCornerShape(unit * .07f))
                            .background(RasporedColors.Accent)
                    )
                    repeat(2) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(unit * .065f)) {
                            repeat(3) { column ->
                                val tint = if (row == 0 && column == 1) RasporedColors.Day else RasporedColors.Accent
                                Box(
                                    Modifier
                                        .size(unit * .13f)
                                        .clip(RoundedCornerShape(unit * .04f))
                                        .background(tint)
                                )
                            }
                        }
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
                    border = if (active) androidx.compose.foundation.BorderStroke(1.4.dp, RasporedColors.Accent) else null,
                    shadowElevation = if (active) 7.dp else 0.dp
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(vertical = 17.dp),
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
    Box(
        Modifier
            .fillMaxSize()
            .background(RasporedColors.AppGradient)
            .systemBarsPadding()
    ) {
        SplashBackdrop()
        SplashTile("D", RasporedColors.Day, Modifier.align(Alignment.TopStart).offset(x = 42.dp, y = 170.dp).rotate(-12f), 76.dp)
        SplashTile("N", RasporedColors.Night, Modifier.align(Alignment.TopEnd).offset(x = (-38).dp, y = 220.dp).rotate(13f), 72.dp)
        SplashTile("GO", RasporedColors.Annual, Modifier.align(Alignment.CenterStart).offset(x = (-20).dp, y = 120.dp).rotate(-11f), 70.dp)
        SplashTile("J", RasporedColors.Morning, Modifier.align(Alignment.CenterEnd).offset(x = 18.dp, y = 150.dp).rotate(10f), 70.dp)
        SplashTile("BO", RasporedColors.Sick, Modifier.align(Alignment.BottomEnd).offset(x = (-45).dp, y = (-125).dp).rotate(9f), 74.dp)

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AppMark(Modifier.size(126.dp))
            Text("Raspored", color = RasporedColors.Text, fontSize = 40.sp, fontWeight = FontWeight.Black)
            Text("Pametni planer smjena", color = RasporedColors.Muted, fontSize = 17.sp)
            Spacer(Modifier.height(78.dp))
            LinearProgressIndicator(
                progress = { .68f },
                modifier = Modifier.width(250.dp).height(5.dp).clip(RoundedCornerShape(4.dp)),
                color = RasporedColors.Accent,
                trackColor = RasporedColors.Card2
            )
        }
    }
}

@Composable
private fun SplashTile(code: String, color: Color, modifier: Modifier, size: androidx.compose.ui.unit.Dp) {
    Surface(
        modifier = modifier.size(size),
        color = color,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = .52f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(code, color = Color(0xFF06131F), fontSize = if (code.length == 1) 27.sp else 20.sp, fontWeight = FontWeight.Black)
        }
    }
}


@Composable
private fun BoxScope.SplashBackdrop() {
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = 72.dp)
            .rotate(-5f)
            .width(320.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            "LISTOPAD 2026",
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = RasporedColors.Text.copy(alpha = .18f),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        repeat(5) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                repeat(7) { column ->
                    val accent = when {
                        (row + column) % 5 == 0 -> RasporedColors.Day.copy(alpha = .20f)
                        (row + column) % 4 == 0 -> RasporedColors.Night.copy(alpha = .17f)
                        else -> RasporedColors.Card2.copy(alpha = .30f)
                    }
                    Box(
                        Modifier
                            .size(width = 39.dp, height = 42.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(accent)
                    )
                }
            }
        }
    }
}
