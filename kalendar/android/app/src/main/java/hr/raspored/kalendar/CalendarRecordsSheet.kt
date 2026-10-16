package hr.raspored.kalendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Keep the main calendar full-height, like Raspored. Open details as a bottom sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalendarRecordsSheet(
    date: LocalDate,
    entries: List<CalendarEntry>,
    searchAllInitially: Boolean,
    onDismiss: () -> Unit,
    onEdit: (CalendarEntry) -> Unit,
    onAdd: () -> Unit,
    onSettings: () -> Unit,
    onEventPreset: (CalendarEventPreset) -> Unit,
    onPreset: (CalendarShiftPreset) -> Boolean
) {
    var query by remember(date, searchAllInitially) { mutableStateOf("") }
    var visibleCount by remember { mutableIntStateOf(80) }
    var saveError by remember { mutableStateOf(false) }
    var moreShifts by remember(date) { mutableStateOf(false) }
    var moreEvents by remember(date) { mutableStateOf(false) }
    var category by remember(date, searchAllInitially) { mutableStateOf("Sve") }
    var presetQuery by remember(date) { mutableStateOf("") }
    LaunchedEffect(query, date, searchAllInitially) {
        visibleCount = 80
        category = "Sve"
    }
    val requester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(searchAllInitially) {
        if (searchAllInitially) {
            requester.requestFocus()
            keyboard?.show()
        }
    }
    val candidates = remember(entries, date, query, searchAllInitially) {
        CalendarSearch.displayed(entries, date, query, searchAllInitially)
    }
    val matches = remember(candidates, category) {
        if (category == "Sve") candidates
        else candidates.filter { it.category == category }
    }
    val presentCategories = remember(candidates) {
        val found = candidates.mapTo(HashSet()) { it.category }
        EntryRules.categories.filter { it in found }
    }
    val formatter = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy.", Locale("hr", "HR"))
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ParityStyle.bg2,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = ParityStyle.muted) }
    ) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(.88f).navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (searchAllInitially) "Pretraži zapise" else "Dnevni plan",
                        color = ParityStyle.text, fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.headlineSmall)
                    Text(formatter.format(date), color = ParityStyle.muted,
                        style = MaterialTheme.typography.labelSmall)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Zatvori",
                        tint = ParityStyle.text)
                }
            }
            OutlinedTextField(
                value = query, onValueChange = { query = it.take(120) },
                modifier = Modifier.fillMaxWidth().focusRequester(requester),
                label = { Text("Pretraži nazive i napomene") },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        TextButton(onClick = { query = "" }) { Text("Očisti") }
                    }
                }, singleLine = true
            )
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (listOf("Sve") + presentCategories).forEach { choice ->
                    FilterChip(
                        selected = category == choice,
                        onClick = {
                            category = choice
                            visibleCount = 80
                        },
                        label = { Text(choice, maxLines = 1) }
                    )
                }
            }
            if (saveError) {
                Text("Nije moguće spremiti zapis. Provjeri slobodan prostor.",
                    color = MaterialTheme.colorScheme.error)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${matches.size} zapisa", Modifier.weight(1f),
                    color = ParityStyle.muted)
                TextButton(onClick = onSettings) { Text("Postavke") }
                Button(onClick = onAdd) { Text("Dodaj +") }
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                if (!searchAllInitially && query.isBlank()) {
                    Text("BRZI DOGAĐAJI I OBVEZE", color = ParityStyle.accent,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelMedium)
                    CalendarEventPresets.all.take(
                        if (moreEvents) CalendarEventPresets.all.size else 6
                    ).chunked(3).forEach { group ->
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            group.forEach { preset ->
                                Surface(
                                    onClick = { onEventPreset(preset) },
                                    modifier = Modifier.weight(1f).height(65.dp),
                                    color = ParityStyle.card2,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, ParityStyle.tiles[preset.color])
                                ) {
                                    Column(
                                        Modifier.fillMaxSize().padding(5.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(preset.code, color = ParityStyle.tiles[preset.color],
                                            fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.titleSmall)
                                        Text(preset.title, maxLines = 2, color = ParityStyle.text,
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                    TextButton(onClick = { moreEvents = !moreEvents },
                        modifier = Modifier.fillMaxWidth()) {
                        Text(if (moreEvents) "Manje prijedloga"
                             else "Prikaži svih ${CalendarEventPresets.all.size} prijedloga")
                    }
                    Text("Odabir otvara uređivanje. Zapis nastaje tek nakon spremanja.",
                        color = ParityStyle.muted,
                        style = MaterialTheme.typography.labelSmall)
                    OutlinedButton(
                        onClick = { moreShifts = !moreShifts },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (moreShifts) "Sakrij dodatne smjene"
                             else "Sve vrste smjena · ${CalendarShiftPresets.additional.size} dodatnih")
                    }
                    if (moreShifts) {
            if (!searchAllInitially && query.isBlank()) {
                Text("ODABERI SMJENU", color = ParityStyle.text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.labelMedium)
                CalendarShiftPresets.builtIn.chunked(3).forEach { presets ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.forEach { preset ->
                            val alreadyPresent = CalendarShiftPresets.alreadyScheduled(
                                entries, preset, date
                            )
                            Surface(
                                onClick = {
                                    if (onPreset(preset)) onDismiss()
                                    else saveError = true
                                },
                                modifier = Modifier.weight(1f).height(67.dp),
                                color = ParityStyle.tiles[preset.color],
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    if (alreadyPresent) 2.dp else 1.dp,
                                    if (alreadyPresent) ParityStyle.bg
                                    else Color.White.copy(alpha = .38f)
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(preset.code, color = ParityStyle.bg,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black)
                                    Text(if (alreadyPresent) "✓ " + preset.title
                                         else preset.title,
                                        color = ParityStyle.bg,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
                        OutlinedTextField(
                            value = presetQuery,
                            onValueChange = { presetQuery = it.take(80) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Pretraži vrste smjena") }
                        )
                        Text(
                            "Dvokratne i promjenjive smjene: dodaj zasebne zapise " +
                                "za svaki stvarni vremenski interval.",
                            color = ParityStyle.muted,
                            style = MaterialTheme.typography.bodySmall
                        )
                        CalendarShiftPresets.additionalGroups.forEach { group ->
                            val filtered = group.presets.filter {
                                CalendarShiftPresets.matches(it, presetQuery)
                            }
                            if (filtered.isNotEmpty()) {
                                Text(group.name.uppercase(Locale("hr", "HR")),
                                    color = ParityStyle.accent,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium)
                                filtered.forEach { preset ->
                                    val present = CalendarShiftPresets.alreadyScheduled(
                                        entries, preset, date
                                    )
                                    Surface(
                                        onClick = {
                                            if (onPreset(preset)) onDismiss()
                                            else saveError = true
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = ParityStyle.card2,
                                        shape = RoundedCornerShape(15.dp),
                                        border = BorderStroke(1.dp, ParityStyle.softStroke)
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                Modifier.size(44.dp).background(
                                                    ParityStyle.tiles[preset.color],
                                                    RoundedCornerShape(12.dp)
                                                ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(preset.code, color = ParityStyle.bg,
                                                    fontWeight = FontWeight.Black,
                                                    style = MaterialTheme.typography.titleSmall)
                                            }
                                            Column(Modifier.weight(1f)) {
                                                Text(preset.title, color = ParityStyle.text,
                                                    fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    if (preset.start.isBlank()) "Bez fiksnog vremena"
                                                    else "${preset.start} – ${preset.end}",
                                                    color = ParityStyle.muted,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                            if (present) Text("✓", color = ParityStyle.accent,
                                                style = MaterialTheme.typography.titleLarge)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (matches.isEmpty()) {
                    Surface(color = ParityStyle.card,
                        shape = RoundedCornerShape(18.dp)) {
                        Text("Nema zapisa. Dodaj događaj, obvezu, termin ili smjenu.",
                            Modifier.fillMaxWidth().padding(20.dp),
                            color = ParityStyle.muted)
                    }
                }
                matches.take(visibleCount).forEach { entry ->
                    Surface(
                        onClick = { onEdit(entry) }, color = ParityStyle.card,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ParityStyle.softStroke)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(13.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(5.dp).height(44.dp).background(
                                ParityStyle.tiles[entry.color], RoundedCornerShape(5.dp)
                            ))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(entry.title.ifBlank { "Napomena" }, maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = ParityStyle.text, fontWeight = FontWeight.SemiBold)
                                Text(
                                    listOf(entry.category, entry.date,
                                        if (entry.start.isEmpty()) ""
                                        else entry.start + " – " + entry.end)
                                        .filter { it.isNotBlank() }.joinToString(" · "),
                                    color = ParityStyle.muted,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                if (entry.note.isNotEmpty()) Text(entry.note, maxLines = 2,
                                    overflow = TextOverflow.Ellipsis, color = ParityStyle.muted)
                            }
                        }
                    }
                }
                if (matches.size > visibleCount) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { visibleCount = (visibleCount + 80).coerceAtMost(matches.size) }
                    ) {
                        Text("Prikaži još · preostalo " + (matches.size - visibleCount))
                    }
                }
            }
        }
    }
}
