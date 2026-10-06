package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.data.ShiftLibraryStore
import hr.raspored.app.data.UiSettingsStore
import hr.raspored.app.model.ShiftType
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShiftManagerSheet(
    library: ShiftLibraryStore,
    onDismiss: () -> Unit,
    onNewShift: () -> Unit
) {
    var importVisible by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RasporedColors.Bg2,
        dragHandle = { BottomSheetDefaults.DragHandle(color = RasporedColors.Muted) }
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Smjene", color = RasporedColors.Text, fontSize = 31.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.weight(1f))
                FilledIconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = RasporedColors.Card2),
                    shape = CircleShape
                ) { Icon(Icons.Rounded.Close, "Zatvori") }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onNewShift,
                    modifier = Modifier.weight(1f).height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RasporedColors.Accent.copy(alpha = .24f)),
                    border = BorderStroke(1.dp, RasporedColors.Accent),
                    shape = RoundedCornerShape(19.dp)
                ) {
                    Icon(Icons.Rounded.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Nova smjena", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { importVisible = true },
                    modifier = Modifier.weight(1f).height(64.dp),
                    border = BorderStroke(1.dp, RasporedColors.Stroke),
                    shape = RoundedCornerShape(19.dp)
                ) {
                    Icon(Icons.Rounded.Download, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Uvezi smjenu", fontWeight = FontWeight.Bold)
                }
            }

            library.all.forEach { shift ->
                Surface(
                    color = RasporedColors.Card,
                    shape = RoundedCornerShape(21.dp),
                    border = BorderStroke(1.dp, RasporedColors.Stroke)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(62.dp),
                            color = shift.color,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(shift.code, color = shift.textColor, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            }
                        }
                        Column(Modifier.padding(start = 14.dp).weight(1f)) {
                            Text(shift.name, color = RasporedColors.Text, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                            shift.timeText?.let { Text(it, color = RasporedColors.Muted) }
                            if (shift.custom) Text("Vlastita smjena", color = RasporedColors.Accent, fontSize = 10.sp)
                        }
                        if (shift.custom) {
                            IconButton(onClick = { library.delete(shift.code) }) {
                                Icon(Icons.Rounded.DeleteOutline, "Izbriši", tint = RasporedColors.Danger)
                            }
                        } else {
                            Surface(
                                color = RasporedColors.Card2,
                                shape = CircleShape,
                                modifier = Modifier.size(44.dp)
                            ) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.ChevronRight, null, tint = RasporedColors.Text) } }
                        }
                    }
                }
            }
        }
    }

    if (importVisible) {
        AlertDialog(
            onDismissRequest = { importVisible = false },
            title = { Text("Uvezi smjenu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Zalijepite JSON jedne smjene ili popisa smjena.")
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it; importError = null },
                        minLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("{\"code\":\"P\",\"name\":\"Popodnevna smjena\"}") }
                    )
                    importError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    library.importJson(importText)
                        .onSuccess { importVisible = false; importText = "" }
                        .onFailure { importError = it.message ?: "Neispravan JSON." }
                }) { Text("Uvezi") }
            },
            dismissButton = { TextButton(onClick = { importVisible = false }) { Text("Odustani") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewShiftSheet(library: ShiftLibraryStore, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("Nova smjena") }
    var abbr by remember { mutableStateOf("Nova") }
    var size by remember { mutableFloatStateOf(12f) }
    var bg by remember { mutableStateOf(RasporedColors.Night) }
    var fg by remember { mutableStateOf(Color.Black) }
    var tab by remember { mutableIntStateOf(0) }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var secondaryStart by remember { mutableStateOf("") }
    var secondaryEnd by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RasporedColors.Bg2,
        dragHandle = { BottomSheetDefaults.DragHandle(color = RasporedColors.Muted) }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledIconButton(onClick = onDismiss, colors = IconButtonDefaults.filledIconButtonColors(containerColor = RasporedColors.Card2)) {
                        Icon(Icons.Rounded.ChevronLeft, "Natrag")
                    }
                    Column {
                        Text("Nova smjena", color = RasporedColors.Text, fontSize = 30.sp, fontWeight = FontWeight.Black)
                        Text("Kreirajte novu smjenu", color = RasporedColors.Muted)
                    }
                }
            }
            item { LabeledField("Naziv smjene", name) { name = it; error = null } }
            item { SegmentedSettings(listOf("Izgled", "Raspored"), if (tab == 0) "Izgled" else "Raspored", onSelect = { tab = if (it == "Izgled") 0 else 1 }) }

            if (tab == 0) {
                item { LabeledFieldWithCounter("Skraćenica", abbr, abbr.length.toString() + "/4") { abbr = it.take(4); error = null } }
                item { ColorCard("Boja pozadine", listOf(RasporedColors.Night, RasporedColors.Day, RasporedColors.Annual, RasporedColors.Morning, RasporedColors.Sick, Color(0xFFFF5F67), Color(0xFF36485A)), bg) { bg = it } }
                item { ColorCard("Boja teksta", listOf(Color.White, Color(0xFFD8D8D8), Color(0xFFAAAAAA), Color(0xFF777777), Color(0xFF444444), Color.Black), fg) { fg = it } }
                item {
                    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, RasporedColors.Stroke)) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Veličina teksta", color = RasporedColors.Text, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledIconButton(onClick = { size = (size - 1).coerceAtLeast(8f) }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = RasporedColors.Card2)) { Icon(Icons.Rounded.Remove, null) }
                                Slider(value = size, onValueChange = { size = it }, valueRange = 8f..24f, modifier = Modifier.weight(1f))
                                FilledIconButton(onClick = { size = (size + 1).coerceAtMost(24f) }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = RasporedColors.Card2)) { Icon(Icons.Rounded.Add, null) }
                                Surface(color = RasporedColors.Card2, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(start = 8.dp)) {
                                    Text(size.toInt().toString(), Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = RasporedColors.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, RasporedColors.Stroke)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Vrijeme smjene", color = RasporedColors.Text, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text("Ostavite prazno za odsutnost ili oznaku bez obračuna sati.", color = RasporedColors.Muted, fontSize = 11.sp)
                            TimePair("Prvi interval", start, end, { start = it }, { end = it })
                            TimePair("Drugi interval (neobavezno)", secondaryStart, secondaryEnd, { secondaryStart = it }, { secondaryEnd = it })
                        }
                    }
                }
            }

            error?.let { message -> item { Text(message, color = RasporedColors.Danger, fontSize = 12.sp) } }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(18.dp)) { Text("Odustani") }
                    Button(
                        onClick = {
                            library.save(
                                name = name,
                                code = abbr,
                                background = bg,
                                textColor = fg,
                                fontSize = size.toInt(),
                                start = start,
                                end = end,
                                secondaryStart = secondaryStart,
                                secondaryEnd = secondaryEnd
                            ).onSuccess { onDismiss() }
                                .onFailure { error = it.message ?: "Smjena nije spremljena." }
                        },
                        modifier = Modifier.weight(1f).height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RasporedColors.Accent)
                    ) { Text("Spremi", color = Color(0xFF04131F), fontWeight = FontWeight.Black) }
                }
            }
        }
    }
}

@Composable
private fun TimePair(
    title: String,
    start: String,
    end: String,
    onStart: (String) -> Unit,
    onEnd: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, color = RasporedColors.Muted, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(start, onStart, label = { Text("Početak") }, placeholder = { Text("08:00") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(end, onEnd, label = { Text("Kraj") }, placeholder = { Text("14:00") }, singleLine = true, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String, onValue: (String) -> Unit) {
    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, RasporedColors.Stroke)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            OutlinedTextField(
                value, onValue,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RasporedColors.Accent,
                    unfocusedBorderColor = RasporedColors.Stroke,
                    focusedTextColor = RasporedColors.Text,
                    unfocusedTextColor = RasporedColors.Text
                ),
                shape = RoundedCornerShape(15.dp)
            )
        }
    }
}

@Composable
private fun LabeledFieldWithCounter(label: String, value: String, counter: String, onValue: (String) -> Unit) {
    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, RasporedColors.Stroke)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValue,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = { Text(counter, color = RasporedColors.Muted, modifier = Modifier.padding(end = 8.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RasporedColors.Accent,
                    unfocusedBorderColor = RasporedColors.Stroke,
                    focusedTextColor = RasporedColors.Text,
                    unfocusedTextColor = RasporedColors.Text
                ),
                shape = RoundedCornerShape(15.dp)
            )
        }
    }
}

@Composable
private fun ColorCard(title: String, colors: List<Color>, selected: Color, onPick: (Color) -> Unit) {
    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, RasporedColors.Stroke)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = RasporedColors.Card2, shape = RoundedCornerShape(11.dp), modifier = Modifier.size(42.dp), border = BorderStroke(1.dp, RasporedColors.Accent)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Palette, null, tint = RasporedColors.Accent) }
                }
                colors.forEach { color ->
                    Surface(
                        onClick = { onPick(color) },
                        color = color,
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier.size(42.dp),
                        border = BorderStroke(if (color == selected) 2.dp else 1.dp, if (color == selected) RasporedColors.Accent else RasporedColors.StrokeSoft)
                    ) {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchSheet(
    schedule: ScheduleStore,
    shiftTypes: List<ShiftType>,
    onDismiss: () -> Unit,
    onPick: (LocalDate) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results = remember(query, schedule.entries.size, shiftTypes.size) {
        schedule.entries.toList()
            .filter { (date, code) ->
                query.isBlank() || date.toString().contains(query, true) ||
                    (shiftTypes.firstOrNull { it.code == code }?.name?.contains(query, true) == true)
            }
            .sortedBy { it.first }
            .take(50)
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = RasporedColors.Bg2) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text("Pretraži raspored", color = RasporedColors.Text, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                query, { query = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                placeholder = { Text("Datum ili vrsta smjene") },
                shape = RoundedCornerShape(17.dp)
            )
            Spacer(Modifier.height(10.dp))
            results.forEach { (date, code) ->
                val shift = shiftTypes.firstOrNull { it.code == code }
                Surface(
                    onClick = { onPick(date) },
                    color = RasporedColors.Card,
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = shift?.color ?: RasporedColors.Empty, shape = RoundedCornerShape(9.dp), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text(code, color = shift?.textColor ?: RasporedColors.Text, fontWeight = FontWeight.Black) }
                        }
                        Column(Modifier.padding(start = 10.dp)) {
                            Text(date.toString(), color = RasporedColors.Text, fontWeight = FontWeight.Bold)
                            Text(shift?.name ?: code, color = RasporedColors.Muted)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsSheet(store: UiSettingsStore, onDismiss: () -> Unit) {
    val todayColors = listOf(
        RasporedColors.Night,
        RasporedColors.Day,
        RasporedColors.Annual,
        RasporedColors.Morning,
        Color(0xFFB16CE4),
        Color(0xFFFF5BAA),
        Color(0xFFFF853A)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RasporedColors.Bg2,
        dragHandle = { BottomSheetDefaults.DragHandle(color = RasporedColors.Muted) }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Postavke", color = RasporedColors.Text, fontSize = 30.sp, fontWeight = FontWeight.Black)
                Text("Prilagodite Raspored svojim potrebama", color = RasporedColors.Muted)
            }
            item {
                SettingsGroup("Vizualno", Icons.Rounded.Palette) {
                    SettingsSegmented("Tamni način rada", "Odaberite izgled aplikacije", listOf("Automatski", "Uključen", "Isključen"), store.themeMode, store::setThemeMode)
                    SettingsToggle("Prikaz praznih dana", "Prikaži dane izvan odabranog mjeseca", store.showOutsideDays, store::setShowOutsideDays)
                    SettingsSegmented("Veličina brojeva dana u mjesecu", "Odaberite veličinu brojeva u kalendaru", listOf("XS", "S", "M", "L", "XL"), store.dayNumberSize, store::setDayNumberSize)
                    SettingsToggle("Istakni vikende", "Oboji subotu i nedjelju drugačijom bojom", store.highlightWeekends, store::setHighlightWeekends)
                    SettingsToggle("Ikone alarma", "Prikaži ikonu za dane s alarmima", store.showAlarmIcons, store::setShowAlarmIcons)
                    SettingsToggle("Ikone bilješki", "Prikaži ikonu za dane s bilješkama", store.showNoteIcons, store::setShowNoteIcons)
                    SettingsToggle("Istakni današnji dan", "Prilagodite izgled današnjeg datuma", store.highlightToday, store::setHighlightToday)
                    if (store.highlightToday) {
                        SettingsSegmented("Oblik", "Odaberite oblik isticanja", listOf("Zaobljeni kvadrat", "Krug", "Kvadrat", "Pill"), store.todayShape, store::setTodayShape, compact = true)
                        Text("Boja", color = RasporedColors.Text, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            todayColors.forEachIndexed { index, color ->
                                Surface(
                                    onClick = { store.setTodayColorIndex(index) },
                                    color = color,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(40.dp),
                                    border = BorderStroke(if (store.todayColorIndex == index) 2.dp else 1.dp, if (store.todayColorIndex == index) RasporedColors.Accent else RasporedColors.StrokeSoft)
                                ) {}
                            }
                        }
                        SettingsSegmented("Prozirnost", "Postavite prozirnost isticanja", listOf("25%", "50%", "75%", "100%"), store.todayOpacity.toString() + "%", { store.setTodayOpacity(it.removeSuffix("%").toInt()) }, compact = true)
                    }
                }
            }
            item {
                SettingsGroup("Jezik i vrijeme", Icons.Rounded.Language) {
                    SettingsStatic("Jezik", store.language)
                    SettingsSegmented("Prvi dan u tjednu", "Odaberite koji dan počinje tjedan", listOf("PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED"), store.firstWeekday, store::setFirstWeekday, compact = true)
                    SettingsSegmented("Format vremena", "Odaberite prikaz vremena", listOf("Automatski", "24 h", "AM/PM"), store.timeFormat, store::setTimeFormat)
                    SettingsSegmented("Format datuma", "Odaberite format datuma", listOf("Automatski", "dd.MM.gggg", "MM/dd/gggg", "gggg/MM/dd"), store.dateFormat, store::setDateFormat, compact = true)
                }
            }
            item {
                SettingsGroup("Bilješke", Icons.Rounded.Notes) {
                    SettingsToggle("Prikaži bilješke u dnevnoj ćeliji", "Prikaži tekst bilješki unutar ćelija kalendara", store.showNotesInCell, store::setShowNotesInCell)
                    SettingsSegmented("Veličina teksta bilješke", "Odaberite veličinu teksta u dnevnim ćelijama", listOf("XS", "S", "M", "L", "XL"), store.noteTextSize, store::setNoteTextSize)
                    SettingsSegmented("Prozirnost pozadine", "Postavite prozirnost pozadine bilješki", listOf("25%", "50%", "75%", "100%"), store.noteBackgroundOpacity.toString() + "%", { store.setNoteBackgroundOpacity(it.removeSuffix("%").toInt()) }, compact = true)
                }
            }
            item {
                SettingsGroup("Podrška i privatnost", Icons.Rounded.Shield) {
                    SettingsStatic("Podržite nas!", "Pomozite nam da Raspored bude još bolji")
                    SettingsStatic("Pravila privatnosti", "Saznajte kako štitimo vaše podatke")
                }
            }
            item {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RasporedColors.Accent),
                    shape = RoundedCornerShape(18.dp)
                ) { Text("Gotovo", color = Color(0xFF04131F), fontWeight = FontWeight.Black) }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, RasporedColors.Stroke)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = RasporedColors.Accent)
                Text(title, color = RasporedColors.Text, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            content()
        }
    }
}

@Composable
private fun SettingsToggle(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
            Text(subtitle, color = RasporedColors.Muted, fontSize = 11.sp)
        }
        Switch(checked, onChange, colors = SwitchDefaults.colors(checkedTrackColor = RasporedColors.Accent))
    }
}

@Composable
private fun SettingsSegmented(
    title: String,
    subtitle: String,
    values: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    compact: Boolean = false
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
        Text(subtitle, color = RasporedColors.Muted, fontSize = 11.sp)
        SegmentedSettings(values, selected, onSelect, compact)
    }
}

@Composable
private fun SegmentedSettings(values: List<String>, selected: String, onSelect: (String) -> Unit, compact: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(if (compact) rememberScrollState() else rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        values.forEach { value ->
            val active = value == selected
            Surface(
                onClick = { onSelect(value) },
                color = if (active) RasporedColors.Accent.copy(alpha = .22f) else RasporedColors.Card2,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (active) RasporedColors.Accent else RasporedColors.StrokeSoft)
            ) {
                Text(
                    value,
                    modifier = Modifier.padding(horizontal = if (compact) 11.dp else 16.dp, vertical = 10.dp),
                    color = if (active) RasporedColors.Text else RasporedColors.Muted,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    fontSize = if (compact) 11.sp else 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SettingsStatic(title: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
            Text(value, color = RasporedColors.Muted, fontSize = 11.sp)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = RasporedColors.Muted)
    }
}
