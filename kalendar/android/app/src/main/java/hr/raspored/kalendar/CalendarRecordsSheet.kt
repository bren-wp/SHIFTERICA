package hr.raspored.kalendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
    onPreset: (CalendarShiftPreset) -> Boolean
) {
    var query by remember(date, searchAllInitially) { mutableStateOf("") }
    var visibleCount by remember { mutableIntStateOf(80) }
    var saveError by remember { mutableStateOf(false) }
    LaunchedEffect(query, date) { visibleCount = 80 }
    val requester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(searchAllInitially) {
        if (searchAllInitially) {
            requester.requestFocus()
            keyboard?.show()
        }
    }
    val matches = remember(entries, date, query, searchAllInitially) {
        when {
            query.isNotBlank() -> CalendarSearch.find(entries, query)
            searchAllInitially -> entries.sortedWith(
                compareBy<CalendarEntry>({ it.date }, { it.start }, { it.id })
            )
            else -> EntryRules.forDay(entries, date)
        }
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
                    Text(if (searchAllInitially) "Pretraži zapise" else "Zapisi i smjene",
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
                                border = BorderStroke(1.dp, Color.White.copy(alpha = .38f))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(preset.code, color = ParityStyle.bg,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black)
                                    Text(preset.title,
                                        color = ParityStyle.bg,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
            if (saveError) {
                Text("Nije moguće spremiti zapis. Provjeri slobodan prostor.",
                    color = MaterialTheme.colorScheme.error)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${matches.size} zapisa", Modifier.weight(1f),
                    color = ParityStyle.muted)
                Button(onClick = onAdd) { Text("Dodaj +") }
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                if (matches.isEmpty()) {
                    Surface(color = ParityStyle.card,
                        shape = RoundedCornerShape(18.dp)) {
                        Text("Nema zapisa. Možeš dodati smjenu, događaj ili napomenu.",
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
