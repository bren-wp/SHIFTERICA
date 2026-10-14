package hr.raspored.kalendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private val ink = ParityStyle.bg
private val panel = ParityStyle.card
private val mint = ParityStyle.accent
private val soft = ParityStyle.muted
private val palette = ParityStyle.tiles
private val hr = Locale("hr", "HR")
private val monthTitle = DateTimeFormatter.ofPattern("LLLL yyyy", hr)
private val dayTitle = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy.", hr)

@Composable
fun CalendarScreen(
    store: CalendarStore,
    onExport: () -> Unit,
    onImport: () -> Unit,
    notice: String?
) {
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1200)
        showSplash = false
    }
    if (showSplash) {
        CalendarSplash()
        return
    }
    var month by remember { mutableStateOf(YearMonth.now()) }
    var section by remember { mutableStateOf(CalendarSection.MONTH) }
    var selected by remember { mutableStateOf(LocalDate.now()) }
    var editing by remember { mutableStateOf<CalendarEntry?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var requestSearch by remember { mutableStateOf(false) }
    val searchRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(section, requestSearch) {
        if (section == CalendarSection.MONTH && requestSearch) {
            searchRequester.requestFocus()
            keyboardController?.show()
            requestSearch = false
        }
    }
    var visibleCount by remember { mutableIntStateOf(80) }
    LaunchedEffect(query, selected) { visibleCount = 80 }
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewPrefs = remember(context) {
        context.getSharedPreferences("raspored.universal.view.v1", android.content.Context.MODE_PRIVATE)
    }
    var weekView by remember { mutableStateOf(viewPrefs.getBoolean("week_view", false)) }
    MaterialTheme(colorScheme = darkColorScheme(
        primary = mint, onPrimary = ink,
        background = ink, onBackground = Color.White,
        surface = panel, onSurface = Color.White,
        secondary = Color(0xFF91A7FF), onSecondary = ink
    )) {
        val all = store.entries.toList()
        Column(Modifier.fillMaxSize().background(ParityStyle.gradient).safeDrawingPadding()) {
            ParityHeader(
                onSearch = { section = CalendarSection.MONTH; requestSearch = true },
                onSettings = { showSettings = true },
                onAdd = { editing = null; showEditor = true }
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (section) {
                    CalendarSection.MONTH -> Column(
                        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {
                    month = month.minusMonths(1)
                    selected = month.atDay(1)
                }, modifier = Modifier.semantics {
                    contentDescription = "Prethodni mjesec"
                }) { Icon(Icons.Rounded.ChevronLeft, contentDescription = null) }
                Text(month.atDay(1).format(monthTitle).uppercase(hr),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                TextButton(onClick = {
                    month = month.plusMonths(1)
                    selected = month.atDay(1)
                }, modifier = Modifier.semantics {
                    contentDescription = "Sljedeći mjesec"
                }) { Icon(Icons.Rounded.ChevronRight, contentDescription = null) }
                TextButton(onClick = {
                    selected = LocalDate.now()
                    month = YearMonth.from(selected)
                }) { Text("Danas") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !weekView, onClick = {
                    weekView = false
                    viewPrefs.edit().putBoolean("week_view", false).apply()
                },
                    label = { Text("Mjesec") })
                FilterChip(selected = weekView, onClick = {
                    weekView = true
                    viewPrefs.edit().putBoolean("week_view", true).apply()
                },
                    label = { Text("Tjedan") })
            }
            MonthGrid(
                month = month, selected = selected, entries = all, weekOnly = weekView,
                onSelect = {
                    selected = it
                    month = YearMonth.from(it)
                    query = ""
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    selected = selected.minusDays(1)
                    month = YearMonth.from(selected)
                    query = ""
                }, modifier = Modifier.weight(1f)) { Text("‹ Prethodni dan") }
                OutlinedButton(onClick = {
                    selected = selected.plusDays(1)
                    month = YearMonth.from(selected)
                    query = ""
                }, modifier = Modifier.weight(1f)) { Text("Sljedeći dan ›") }
            }
            OutlinedTextField(
                value = query, onValueChange = { query = it.take(120) },
                modifier = Modifier.fillMaxWidth().focusRequester(searchRequester),
                label = { Text("Pretraži nazive i napomene") },
                trailingIcon = {
                    if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Očisti pretragu")
                    }
                },
                singleLine = true
            )
            val matches = if (query.isBlank()) EntryRules.forDay(all, selected)
            else CalendarSearch.find(all, query)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (query.isBlank()) selected.format(dayTitle) else "Rezultati pretrage",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(matches.size.toString() + " zapisa", color = soft)
                }
                if (query.isBlank()) TextButton(onClick = {
                    editing = null
                    showEditor = true
                }) { Text("Dodaj +") }
            }
            if (matches.isEmpty()) {
                Surface(color = panel, shape = RoundedCornerShape(18.dp)) {
                    Text(
                        if (query.isBlank()) "Nema zapisa za ovaj dan. Dodaj smjenu, događaj ili napomenu."
                        else "Nema rezultata za uneseni pojam.",
                        modifier = Modifier.fillMaxWidth().padding(24.dp), color = soft
                    )
                }
            } else matches.take(visibleCount).forEach { entry ->
                Surface(
                    onClick = {
                        editing = entry
                        selected = LocalDate.parse(entry.date)
                        month = YearMonth.from(selected)
                        showEditor = true
                    }, color = panel, shape = RoundedCornerShape(16.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(5.dp).height(43.dp)
                            .background(palette[entry.color], RoundedCornerShape(5.dp)))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entry.title.ifBlank { "Napomena" },
                                maxLines = 2, overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.SemiBold)
                            Text(listOfNotNull(
                                entry.category,
                                entry.start.takeIf { it.isNotBlank() }?.let {
                                    it + " – " + entry.end
                                },
                                entry.date.takeIf { query.isNotBlank() }
                            ).joinToString(" · "), color = soft,
                                style = MaterialTheme.typography.bodySmall)
                            if (entry.note.isNotBlank()) Text(entry.note,
                                maxLines = 2, overflow = TextOverflow.Ellipsis, color = soft)
                        }
                        Text("Uredi", color = mint, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            if (matches.size > visibleCount) {
                TextButton(
                    onClick = { visibleCount = (visibleCount + 80).coerceAtMost(matches.size) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text("Prikaži još · preostalo " + (matches.size - visibleCount))
                }
            }
            Spacer(Modifier.height(8.dp))
                    }
                    CalendarSection.YEAR -> ParityYear(
                        year = month.year, entries = all,
                        onYearChange = { year -> month = month.withYear(year) },
                        onMonthPick = { chosen ->
                            month = chosen
                            selected = chosen.atDay(1)
                            section = CalendarSection.MONTH
                        }
                    )
                    CalendarSection.SUMMARY -> ParitySummary(
                        month = month, entries = all,
                        onPreviousMonth = { month = month.minusMonths(1) },
                        onNextMonth = { month = month.plusMonths(1) },
                        onExport = onExport, onImport = onImport, notice = notice
                    )
                }
            }
            ParityBottomNav(section, onSection = { section = it })
        }
        if (showSettings) {
            CalendarSettings(
                weekView = weekView,
                onWeekViewChange = {
                    weekView = it
                    viewPrefs.edit().putBoolean("week_view", it).apply()
                },
                onDismiss = { showSettings = false }
            )
        }
        if (showEditor) {
            CalendarEntryEditor(
                current = editing, day = selected,
                onDismiss = { showEditor = false },
                onSave = {
                    if (store.save(it)) {
                        showEditor = false
                        selected = LocalDate.parse(it.date)
                        month = YearMonth.from(selected)
                        query = ""
                        true
                    } else false
                },
                onDuplicate = { entry ->
                    val copy = entry.copy(id = UUID.randomUUID().toString())
                    if (store.save(copy)) {
                        showEditor = false
                        selected = LocalDate.parse(copy.date)
                        month = YearMonth.from(selected)
                        query = ""
                        true
                    } else false
                },
                onDelete = { entry ->
                    if (store.delete(entry.id)) {
                        showEditor = false
                        true
                    } else false
                }
            )
        }
    }
}

@Composable
private fun CalendarEntryEditor(
    current: CalendarEntry?, day: LocalDate,
    onDismiss: () -> Unit,
    onSave: (CalendarEntry) -> Boolean,
    onDuplicate: (CalendarEntry) -> Boolean,
    onDelete: (CalendarEntry) -> Boolean
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var entryDate by remember(current?.id, day) { mutableStateOf(day) }
    var title by remember(current?.id, day) { mutableStateOf(current?.title ?: "") }
    var note by remember(current?.id, day) { mutableStateOf(current?.note ?: "") }
    var start by remember(current?.id, day) { mutableStateOf(current?.start ?: "") }
    var end by remember(current?.id, day) { mutableStateOf(current?.end ?: "") }
    var category by remember(current?.id, day) {
        mutableStateOf(current?.category ?: "Smjena")
    }
    var color by remember(current?.id, day) { mutableIntStateOf(current?.color ?: 0) }
    var error by remember(current?.id, day) { mutableStateOf(false) }
    var confirmDelete by remember(current?.id, day) { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(
        usePlatformDefaultWidth = false
    )) {
        Surface(Modifier.fillMaxWidth(.94f).fillMaxHeight(.9f),
            shape = RoundedCornerShape(24.dp), color = panel) {
            Column(Modifier.imePadding().verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (current == null) "Novi zapis" else "Uredi zapis",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = {
                    android.app.DatePickerDialog(
                        context,
                        { _, year, month, chosen ->
                            entryDate = LocalDate.of(year, month + 1, chosen)
                        },
                        entryDate.year, entryDate.monthValue - 1, entryDate.dayOfMonth
                    ).show()
                }) {
                    Text("Datum: " + entryDate.format(dayTitle))
                }
                OutlinedTextField(title, { title = it.take(120); error = false },
                    label = { Text("Naziv (ili ostavi samo napomenu)") },
                    modifier = Modifier.fillMaxWidth())
                Text("Vrsta", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EntryRules.categories.forEach { kind ->
                        FilterChip(selected = kind == category,
                            onClick = { category = kind }, label = { Text(kind) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(start, { start = it.take(5); error = false },
                        modifier = Modifier.weight(1f), singleLine = true,
                        label = { Text("Od HH:mm") })
                    OutlinedTextField(end, { end = it.take(5); error = false },
                        modifier = Modifier.weight(1f), singleLine = true,
                        label = { Text("Do HH:mm") })
                }
                Text("Vrijeme je neobavezno. Smjena smije prelaziti ponoć.",
                    color = soft, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(note, { note = it.take(10_000); error = false },
                    minLines = 3, maxLines = 7, label = { Text("Napomena") },
                    modifier = Modifier.fillMaxWidth())
                Text("Boja", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    palette.forEachIndexed { index, shade ->
                        Surface(onClick = { color = index },
                            modifier = Modifier.size(36.dp).semantics {
                                contentDescription = "Boja " + (index + 1)
                            }, shape = CircleShape, color = shade,
                            border = if (color == index) BorderStroke(3.dp, Color.White)
                                else null) { }
                    }
                }
                if (error) Text("Provjeri naziv, napomenu i vrijeme (HH:mm).",
                    color = MaterialTheme.colorScheme.error)
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    TextButton(onClick = onDismiss) { Text("Odustani") }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        val entry = (current ?: CalendarEntry(date = day.toString(), title = ""))
                            .copy(date = entryDate.toString(), title = title.trim(), note = note.trim(),
                                category = category, start = start.trim(), end = end.trim(),
                                color = color)
                        error = !EntryRules.valid(entry) || !onSave(entry)
                    }) { Text("Spremi") }
                }
                if (current != null) {
                    OutlinedButton(onClick = {
                        val duplicate = current.copy(
                            date = entryDate.toString(), title = title.trim(),
                            note = note.trim(), category = category,
                            start = start.trim(), end = end.trim(), color = color
                        )
                        error = !EntryRules.valid(duplicate) || !onDuplicate(duplicate)
                    }) { Text("Dupliciraj zapis") }
                    if (!confirmDelete) {
                        OutlinedButton(onClick = { confirmDelete = true }) {
                            Text("Izbriši zapis")
                        }
                    } else {
                        Text("Trajno izbrisati ovaj zapis?", color = soft)
                        Button(onClick = { error = !onDelete(current) }) {
                            Text("Potvrdi brisanje")
                        }
                        TextButton(onClick = { confirmDelete = false }) { Text("Zadrži") }
                    }
                }
            }
        }
    }
}
