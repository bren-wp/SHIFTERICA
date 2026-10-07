package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
        end: String?,
        secondaryStart: String?,
        secondaryEnd: String?
    ) -> Result<ShiftType>,
    onReset: () -> Unit
) {
    var background by remember(shift.code, shift.color) { mutableStateOf(shift.color) }
    var textColor by remember(shift.code, shift.textColor) { mutableStateOf(shift.textColor) }
    var start by remember(shift.code, shift.start) { mutableStateOf(shift.start.orEmpty()) }
    var end by remember(shift.code, shift.end) { mutableStateOf(shift.end.orEmpty()) }
    var secondaryStart by remember(shift.code, shift.secondaryStart) {
        mutableStateOf(shift.secondaryStart.orEmpty())
    }
    var secondaryEnd by remember(shift.code, shift.secondaryEnd) {
        mutableStateOf(shift.secondaryEnd.orEmpty())
    }
    var error by remember { mutableStateOf<String?>(null) }

    val absence = shift.code == "GO" || shift.code == "BO"
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
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                            "Radno vrijeme",
                            color = RasporedColors.Text,
                            fontWeight = FontWeight.Black
                        )

                        if (absence) {
                            Text(
                                "GO i BO su plaćene odsutnosti i u obračunu priznaju 8 sati na radni dan.",
                                color = RasporedColors.Muted,
                                fontSize = 12.sp
                            )
                        } else {
                            TimePair(
                                label = "Prvi interval",
                                start = start,
                                end = end,
                                onStart = {
                                    start = it
                                    error = null
                                },
                                onEnd = {
                                    end = it
                                    error = null
                                }
                            )
                            TimePair(
                                label = "Drugi interval (neobavezno)",
                                start = secondaryStart,
                                end = secondaryEnd,
                                onStart = {
                                    secondaryStart = it
                                    error = null
                                },
                                onEnd = {
                                    secondaryEnd = it
                                    error = null
                                }
                            )
                            Text(
                                "Koristite format HH:mm. Smjene preko ponoći podržane su automatski.",
                                color = RasporedColors.Muted,
                                fontSize = 11.sp
                            )
                        }
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
                    onSave(
                        background,
                        textColor,
                        start.ifBlank { null },
                        end.ifBlank { null },
                        secondaryStart.ifBlank { null },
                        secondaryEnd.ifBlank { null }
                    )
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
private fun TimePair(
    label: String,
    start: String,
    end: String,
    onStart: (String) -> Unit,
    onEnd: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            color = RasporedColors.Muted,
            fontSize = 11.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = start,
                onValueChange = onStart,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Početak") },
                placeholder = { Text("07:00") }
            )
            OutlinedTextField(
                value = end,
                onValueChange = onEnd,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Završetak") },
                placeholder = { Text("15:00") }
            )
        }
    }
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
                        color = if (active) RasporedColors.Accent else RasporedColors.StrokeSoft
                    ),
                    shadowElevation = if (active) 7.dp else 1.dp
                ) {}
            }
        }
    }
}
