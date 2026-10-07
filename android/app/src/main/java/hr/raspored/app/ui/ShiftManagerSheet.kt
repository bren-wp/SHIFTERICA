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
internal fun ShiftManagerSheet(
    library: ShiftLibraryStore,
    onDismiss: () -> Unit,
    onNewShift: () -> Unit
) {
    var importVisible by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }
    var editingBuiltIn by remember { mutableStateOf<ShiftType?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RasporedColors.Bg2,
        dragHandle = { BottomSheetDefaults.DragHandle(color = RasporedColors.Muted) }
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 18.dp),
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
                    border = BorderStroke(1.2.dp, RasporedColors.Accent),
                    shape = RoundedCornerShape(19.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 3.dp)
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
                    border = BorderStroke(1.dp, RasporedColors.Stroke),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(62.dp),
                            color = shift.color,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, shift.color.copy(alpha = .9f)),
                            shadowElevation = 8.dp
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
                                onClick = { editingBuiltIn = shift },
                                color = RasporedColors.Card2,
                                shape = CircleShape,
                                modifier = Modifier.size(44.dp),
                                border = BorderStroke(1.dp, RasporedColors.StrokeSoft),
                                shadowElevation = 3.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Palette, "Promijeni boje", tint = RasporedColors.Text)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingBuiltIn?.let { shift ->
        BuiltInShiftColorDialog(
            shift = shift,
            onDismiss = { editingBuiltIn = null },
            onSave = { background, textColor ->
                library.updateBuiltInColors(shift.code, background, textColor)
                editingBuiltIn = null
            },
            onReset = {
                library.resetBuiltInColors(shift.code)
                editingBuiltIn = null
            }
        )
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



@Composable
private fun BuiltInShiftColorDialog(
    shift: ShiftType,
    onDismiss: () -> Unit,
    onSave: (Color, Color) -> Unit,
    onReset: () -> Unit
) {
    var background by remember(shift.code, shift.color) { mutableStateOf(shift.color) }
    var textColor by remember(shift.code, shift.textColor) { mutableStateOf(shift.textColor) }

    val backgroundChoices = listOf(
        RasporedColors.Night,
        RasporedColors.Day,
        RasporedColors.Annual,
        RasporedColors.Morning,
        RasporedColors.Sick,
        Color(0xFFFF5BAA),
        Color(0xFFFF5F67),
        Color(0xFF36485A)
    )
    val textChoices = listOf(
        Color.White,
        Color(0xFFD8D8D8),
        Color(0xFFAAAAAA),
        Color(0xFF777777),
        Color(0xFF444444),
        Color(0xFF06131F),
        Color.Black
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RasporedColors.Bg2,
        title = { Text("Boje — ${shift.name}", color = RasporedColors.Text, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ColorChoiceRow("Boja kockice", backgroundChoices, background) { background = it }
                ColorChoiceRow("Boja teksta", textChoices, textColor) { textColor = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(background, textColor) }) { Text("Spremi") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onReset) { Text("Vrati zadano") }
                TextButton(onClick = onDismiss) { Text("Odustani") }
            }
        }
    )
}

@Composable
private fun ColorChoiceRow(
    title: String,
    colors: List<Color>,
    selected: Color,
    onSelect: (Color) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.forEach { color ->
                val active = color == selected
                Surface(
                    onClick = { onSelect(color) },
                    modifier = Modifier.size(44.dp),
                    color = color,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        if (active) 2.dp else 1.dp,
                        if (active) RasporedColors.Accent else RasporedColors.StrokeSoft
                    ),
                    shadowElevation = if (active) 7.dp else 1.dp
                ) {}
            }
        }
    }
}
