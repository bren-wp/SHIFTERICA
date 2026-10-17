package hr.raspored.kalendar.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hr.raspored.kalendar.CalendarEntry
import hr.raspored.kalendar.LegacyArchiveSearch

/** Separate read-only archive: supports every event, including same-day duplicates. */
@Composable
internal fun LegacyArchiveDialog(events: List<CalendarEntry>, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    // Event storage is append/replace; the count changes on each successful import.
    val sorted = remember(events.size) { LegacyArchiveSearch.chronological(events) }
    val visible = remember(sorted, query) { LegacyArchiveSearch.matching(sorted, query) }
    val listState = rememberLazyListState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Prethodni događaji (${events.size})") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Pretraži arhivu") },
                    placeholder = { Text("Datum, naziv, kategorija ili bilješka") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Pronađeno: ${visible.size}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                )
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)
                ) {
                    if (visible.isEmpty()) {
                        item {
                            Text(
                                if (events.isEmpty())
                                    "Nema starih događaja. Možete uvesti raniju JSON kopiju."
                                else "Nema događaja za ovu pretragu.",
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                    items(visible, key = { it.id }) { item ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(item.date + " · " + item.category,
                                style = MaterialTheme.typography.labelSmall)
                            Text(item.title.ifBlank { "Napomena" },
                                fontWeight = FontWeight.SemiBold)
                            if (item.start.isNotBlank()) {
                                Text(item.start + " – " + item.end)
                            }
                            if (item.note.isNotBlank()) {
                                Text(item.note, maxLines = 5)
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Zatvori") }
        }
    )
}
