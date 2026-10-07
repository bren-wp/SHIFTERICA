package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
internal fun SettingsSheet(store: UiSettingsStore, onDismiss: () -> Unit) {
    var infoDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showSupport by remember { mutableStateOf(false) }
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
                    SettingsSegmented("Tamni način rada", "Odaberite izgled aplikacije", listOf("Automatski", "Uključen", "Isključen"), store.themeMode, store::updateThemeMode)
                    SettingsToggle("Prikaz praznih dana", "Prikaži dane izvan odabranog mjeseca", store.showOutsideDays, store::updateShowOutsideDays)
                    SettingsSegmented("Veličina brojeva dana u mjesecu", "Odaberite veličinu brojeva u kalendaru", listOf("XS", "S", "M", "L", "XL"), store.dayNumberSize, store::updateDayNumberSize)
                    SettingsToggle("Istakni vikende", "Oboji subotu i nedjelju drugačijom bojom", store.highlightWeekends, store::updateHighlightWeekends)
                    SettingsToggle("Ikone alarma", "Prikaži ikonu za dane s alarmima", store.showAlarmIcons, store::updateShowAlarmIcons)
                    SettingsToggle("Ikone bilješki", "Prikaži ikonu za dane s bilješkama", store.showNoteIcons, store::updateShowNoteIcons)
                    SettingsToggle("Istakni današnji dan", "Prilagodite izgled današnjeg datuma", store.highlightToday, store::updateHighlightToday)
                    if (store.highlightToday) {
                        SettingsShapeSelector(store.todayShape, store::updateTodayShape)
                        Text("Boja", color = RasporedColors.Text, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            todayColors.forEachIndexed { index, color ->
                                Surface(
                                    onClick = { store.updateTodayColorIndex(index) },
                                    color = color,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(40.dp),
                                    border = BorderStroke(if (store.todayColorIndex == index) 2.dp else 1.dp, if (store.todayColorIndex == index) RasporedColors.Accent else RasporedColors.StrokeSoft)
                                ) {}
                            }
                        }
                        SettingsSegmented("Prozirnost", "Postavite prozirnost isticanja", listOf("25%", "50%", "75%", "100%"), store.todayOpacity.toString() + "%", { store.updateTodayOpacity(it.removeSuffix("%").toInt()) }, compact = true)
                    }
                }
            }
            item {
                SettingsGroup("Jezik i vrijeme", Icons.Rounded.Language) {
                    SettingsMenu(
                        title = "Jezik",
                        value = store.language,
                        values = listOf("Automatski (Hrvatski)", "Hrvatski"),
                        onSelect = store::updateLanguage
                    )
                    SettingsSegmented("Prvi dan u tjednu", "Odaberite koji dan počinje tjedan", listOf("PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED"), store.firstWeekday, store::updateFirstWeekday, compact = true)
                    SettingsSegmented("Format vremena", "Odaberite prikaz vremena", listOf("Automatski", "24 h", "AM/PM"), store.timeFormat, store::updateTimeFormat)
                    SettingsSegmented("Format datuma", "Odaberite format datuma", listOf("Automatski", "dd.MM.gggg", "MM/dd/gggg", "gggg/MM/dd"), store.dateFormat, store::updateDateFormat, compact = true)
                }
            }
            item {
                SettingsGroup("Bilješke", Icons.Rounded.Notes) {
                    SettingsToggle("Prikaži bilješke u dnevnoj ćeliji", "Prikaži tekst bilješki unutar ćelija kalendara", store.showNotesInCell, store::updateShowNotesInCell)
                    SettingsSegmented("Veličina teksta bilješke", "Odaberite veličinu teksta u dnevnim ćelijama", listOf("XS", "S", "M", "L", "XL"), store.noteTextSize, store::updateNoteTextSize)
                    SettingsSegmented("Prozirnost pozadine", "Postavite prozirnost pozadine bilješki", listOf("25%", "50%", "75%", "100%"), store.noteBackgroundOpacity.toString() + "%", { store.updateNoteBackgroundOpacity(it.removeSuffix("%").toInt()) }, compact = true)
                }
            }
            item {
                SettingsGroup("Podrška i privatnost", Icons.Rounded.Shield) {
                    SettingsStatic("Podržite nas!", "Dobrovoljna podrška bez otključavanja funkcija") {
                        showSupport = true
                    }
                    SettingsStatic("Pravila privatnosti", "Saznajte kako štitimo vaše podatke") {
                        infoDialog = "Privatnost" to "Raspored ne koristi oglasne trackere i ne zahtijeva korisnički račun. Podaci rasporeda ne koriste se za oglašavanje niti se prodaju trećim stranama."
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }

    if (showSupport) {
        SupportDialog(onDismiss = { showSupport = false })
    }

    infoDialog?.let { (title, message) ->
        AlertDialog(
            onDismissRequest = { infoDialog = null },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { infoDialog = null }) { Text("U redu") } }
        )
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, RasporedColors.Stroke), shadowElevation = 6.dp) {
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
                border = BorderStroke(1.dp, if (active) RasporedColors.Accent else RasporedColors.StrokeSoft),
                shadowElevation = if (active) 6.dp else 0.dp
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
private fun SettingsShapeSelector(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        "Zaobljeni kvadrat" to RoundedCornerShape(8.dp),
        "Krug" to CircleShape,
        "Kvadrat" to RoundedCornerShape(2.dp),
        "Pill" to RoundedCornerShape(50)
    )
    Column(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("Oblik", color = RasporedColors.Text, fontWeight = FontWeight.Bold)
        Text("Odaberite oblik isticanja", color = RasporedColors.Muted, fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            options.forEach { (label, shape) ->
                val active = selected == label
                Surface(
                    onClick = { onSelect(label) },
                    modifier = Modifier.size(width = 66.dp, height = 46.dp),
                    color = if (active) RasporedColors.Accent.copy(alpha = .20f) else RasporedColors.Card2,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (active) RasporedColors.Accent else RasporedColors.StrokeSoft),
                shadowElevation = if (active) 6.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = if (label == "Pill") Modifier.size(width = 34.dp, height = 20.dp) else Modifier.size(24.dp),
                            color = Color.Transparent,
                            shape = shape,
                            border = BorderStroke(1.5.dp, RasporedColors.Text)
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsStatic(title: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
            Text(value, color = RasporedColors.Muted, fontSize = 11.sp)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = RasporedColors.Muted)
    }
}

@Composable
private fun SettingsMenu(
    title: String,
    value: String,
    values: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier.fillMaxWidth().clickable { expanded = true }.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
                Text(value, color = RasporedColors.Muted, fontSize = 11.sp)
            }
            Icon(Icons.Rounded.ExpandMore, null, tint = RasporedColors.Muted)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = RasporedColors.Card2
        ) {
            values.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = RasporedColors.Text) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    trailingIcon = if (value == option) {
                        { Icon(Icons.Rounded.Check, null, tint = RasporedColors.Accent) }
                    } else null
                )
            }
        }
    }
}
