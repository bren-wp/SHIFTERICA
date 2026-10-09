package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.model.ShiftType

@Composable
internal fun BuiltInShiftEditorDialog(
    shift: ShiftType,
    onDismiss: () -> Unit,
    onSave: (
        background: Color,
        textColor: Color,
        start: String?,
        end: String?
    ) -> Result<ShiftType>,
    onReset: () -> Unit
) {
    var background by remember(shift.code, shift.color) {
        mutableStateOf(shift.color)
    }
    var textColor by remember(shift.code, shift.textColor) {
        mutableStateOf(shift.textColor)
    }
    var error by remember { mutableStateOf<String?>(null) }
    var startTime by remember(shift.code, shift.start) { mutableStateOf(shift.start.orEmpty()) }
    var endTime by remember(shift.code, shift.end) { mutableStateOf(shift.end.orEmpty()) }

    val backgroundChoices = listOf(
        RasporedColors.Night,
        RasporedColors.Day,
        Color(0xFFFF8A3D),
        RasporedColors.Annual,
        RasporedColors.Morning,
        RasporedColors.Sick,
        Color(0xFFFF5BAA),
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
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Prilagodi " + shift.code,
                    color = RasporedColors.Text,
                    fontWeight = FontWeight.Black
                )
                Text(
                    shift.name,
                    color = RasporedColors.Muted,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 390.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    shift.timeText ?: "Plaćena odsutnost · 8 h na radni dan",
                    color = RasporedColors.Muted,
                    fontSize = 12.sp
                )

                if (shift.start != null && shift.end != null) {
                    Text("Vrijeme smjene · 24-satni format", color = RasporedColors.Text, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startTime, onValueChange = { startTime = it.take(5); error = null },
                            label = { Text("Početak") }, singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endTime, onValueChange = { endTime = it.take(5); error = null },
                            label = { Text("Kraj") }, singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text("Zadano: ${hr.raspored.app.model.ShiftCatalog.byCode(shift.code)?.timeText ?: ""}. Po potrebi vratite zadano.",
                        color = RasporedColors.Muted, fontSize = 11.sp)
                }

                Surface(
                    color = RasporedColors.Card,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, RasporedColors.Stroke)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Izgled",
                            color = RasporedColors.Text,
                            fontWeight = FontWeight.Black
                        )
                        ColorChoiceRow(
                            title = "Boja kockice",
                            colors = backgroundChoices,
                            selected = background,
                            onSelect = { background = it }
                        )
                        ColorChoiceRow(
                            title = "Boja teksta",
                            colors = textChoices,
                            selected = textColor,
                            onSelect = { textColor = it }
                        )
                    }
                }

                error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(background, textColor,
                        startTime.takeIf { shift.start != null },
                        endTime.takeIf { shift.end != null })
                        .onSuccess { onDismiss() }
                        .onFailure {
                            error = it.message ?: "Promjene nije moguće spremiti."
                        }
                }
            ) {
                Text("Spremi")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onReset) {
                    Text("Vrati zadano")
                }
                TextButton(onClick = onDismiss) {
                    Text("Odustani")
                }
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
        Text(
            title,
            color = RasporedColors.Text,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
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
                        width = if (active) 2.dp else 1.dp,
                        color = if (active) {
                            RasporedColors.Accent
                        } else {
                            RasporedColors.StrokeSoft
                        }
                    ),
                    shadowElevation = if (active) 7.dp else 1.dp
                ) {}
            }
        }
    }
}
