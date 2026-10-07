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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RasporedColors.Bg2,
        dragHandle = { BottomSheetDefaults.DragHandle(color = RasporedColors.Muted) }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Pretraži raspored", color = RasporedColors.Text, fontSize = 29.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.weight(1f))
                    Surface(
                        onClick = onDismiss,
                        modifier = Modifier.size(46.dp),
                        color = RasporedColors.Card2,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, RasporedColors.StrokeSoft),
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Close, "Zatvori", tint = RasporedColors.Text) }
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Rounded.Search, null, tint = RasporedColors.Muted) },
                    placeholder = { Text("Datum ili vrsta smjene") },
                    singleLine = true,
                    shape = RoundedCornerShape(17.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RasporedColors.Accent,
                        unfocusedBorderColor = RasporedColors.Stroke,
                        focusedTextColor = RasporedColors.Text,
                        unfocusedTextColor = RasporedColors.Text
                    )
                )
                Spacer(Modifier.height(6.dp))
            }

            if (results.isEmpty()) {
                item {
                    Surface(
                        color = RasporedColors.Card,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, RasporedColors.Stroke),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            "Nema pronađenih smjena.",
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            color = RasporedColors.Muted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(results.size) { index ->
                    val (date, code) = results[index]
                    val shift = shiftTypes.firstOrNull { it.code == code }
                    Surface(
                        onClick = { onPick(date) },
                        color = RasporedColors.Card,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, RasporedColors.Stroke),
                        shadowElevation = 4.dp
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = shift?.color ?: RasporedColors.Empty,
                                shape = RoundedCornerShape(11.dp),
                                modifier = Modifier.size(46.dp),
                                shadowElevation = 6.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(code, color = shift?.textColor ?: RasporedColors.Text, fontWeight = FontWeight.Black)
                                }
                            }
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(date.toString(), color = RasporedColors.Text, fontWeight = FontWeight.Bold)
                                Text(shift?.name ?: code, color = RasporedColors.Muted, fontSize = 12.sp)
                            }
                            Icon(Icons.Rounded.ChevronRight, null, tint = RasporedColors.Muted)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

