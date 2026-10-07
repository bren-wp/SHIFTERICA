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
                    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, RasporedColors.Stroke), shadowElevation = 5.dp) {
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
                    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, RasporedColors.Stroke), shadowElevation = 5.dp) {
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
                        colors = ButtonDefaults.buttonColors(containerColor = RasporedColors.Accent),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 9.dp, pressedElevation = 3.dp)
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
    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, RasporedColors.Stroke), shadowElevation = 5.dp) {
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
    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, RasporedColors.Stroke), shadowElevation = 5.dp) {
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
    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, RasporedColors.Stroke), shadowElevation = 5.dp) {
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
                        border = BorderStroke(if (color == selected) 2.dp else 1.dp, if (color == selected) RasporedColors.Accent else RasporedColors.StrokeSoft),
                        shadowElevation = if (color == selected) 8.dp else 2.dp
                    ) {}
                }
            }
        }
    }
}

