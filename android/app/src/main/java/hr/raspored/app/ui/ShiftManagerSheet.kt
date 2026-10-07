package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.ShiftLibraryStore
import hr.raspored.app.model.ShiftType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShiftManagerSheet(
    library: ShiftLibraryStore,
    onDismiss: () -> Unit,
    onNewShift: () -> Unit,
    onEditCustom: (ShiftType) -> Unit
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        "Smjene",
                        color = RasporedColors.Text,
                        fontSize = 31.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Ugrađene smjene imaju fiksno vrijeme; boje i vlastite smjene možete prilagoditi",
                        color = RasporedColors.Muted,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.weight(1f))
                FilledIconButton(
                    onClick = onDismiss,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = RasporedColors.Card2
                    ),
                    shape = CircleShape
                ) {
                    Icon(Icons.Rounded.Close, "Zatvori")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onNewShift,
                    modifier = Modifier.weight(1f).height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RasporedColors.Accent.copy(alpha = .24f)
                    ),
                    border = BorderStroke(1.2.dp, RasporedColors.Accent),
                    shape = RoundedCornerShape(19.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 3.dp
                    )
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(library.all, key = { it.code }) { shift ->
                    ShiftManagerRow(
                        shift = shift,
                        onEditBuiltIn = { editingBuiltIn = shift },
                        onEditCustom = { onEditCustom(shift) },
                        onDeleteCustom = { library.delete(shift.code) }
                    )
                }
            }
        }
    }

    editingBuiltIn?.let { shift ->
        BuiltInShiftEditorDialog(
            shift = shift,
            onDismiss = { editingBuiltIn = null },
            onSave = { background, textColor ->
                library.updateBuiltIn(
                    code = shift.code,
                    background = background,
                    textColor = textColor
                )
            },
            onReset = {
                library.resetBuiltIn(shift.code)
                editingBuiltIn = null
            }
        )
    }

    if (importVisible) {
        AlertDialog(
            onDismissRequest = { importVisible = false },
            containerColor = RasporedColors.Bg2,
            title = {
                Text(
                    "Uvezi smjenu",
                    color = RasporedColors.Text,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Zalijepite JSON jedne smjene ili popisa smjena.",
                        color = RasporedColors.Muted
                    )
                    OutlinedTextField(
                        value = importText,
                        onValueChange = {
                            importText = it
                            importError = null
                        },
                        minLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("{\"code\":\"P2\",\"name\":\"Popodnevna 2\"}")
                        }
                    )
                    importError?.let {
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
                        library.importJson(importText)
                            .onSuccess {
                                importVisible = false
                                importText = ""
                            }
                            .onFailure {
                                importError = it.message ?: "Neispravan JSON."
                            }
                    }
                ) {
                    Text("Uvezi")
                }
            },
            dismissButton = {
                TextButton(onClick = { importVisible = false }) {
                    Text("Odustani")
                }
            }
        )
    }
}

@Composable
private fun ShiftManagerRow(
    shift: ShiftType,
    onEditBuiltIn: () -> Unit,
    onEditCustom: () -> Unit,
    onDeleteCustom: () -> Unit
) {
    Surface(
        color = RasporedColors.Card,
        shape = RoundedCornerShape(21.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke.copy(alpha = .95f)),
        shadowElevation = 7.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(62.dp),
                color = Color.Transparent,
                shape = RoundedCornerShape(17.dp),
                border = BorderStroke(1.2.dp, shift.color.copy(alpha = .96f)),
                shadowElevation = 9.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(shift.color, shift.color.copy(alpha = .80f))
                            ),
                            RoundedCornerShape(17.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        shift.code,
                        color = shift.textColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(start = 14.dp)
                    .weight(1f)
            ) {
                Text(
                    shift.name,
                    color = RasporedColors.Text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
                shift.timeText?.let {
                    Text(it, color = RasporedColors.Muted)
                }
                if (shift.custom) {
                    Text(
                        "Vlastita smjena",
                        color = RasporedColors.Accent,
                        fontSize = 10.sp
                    )
                } else {
                    Text(
                        "Dodirnite za prilagodbu",
                        color = RasporedColors.Muted,
                        fontSize = 10.sp
                    )
                }
            }

            if (shift.custom) {
                Row {
                    Surface(
                        onClick = onEditCustom,
                        color = RasporedColors.Card2,
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp),
                        border = BorderStroke(1.dp, RasporedColors.StrokeSoft)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Tune,
                                "Uredi smjenu",
                                tint = RasporedColors.Text
                            )
                        }
                    }
                    IconButton(onClick = onDeleteCustom) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            "Izbriši",
                            tint = RasporedColors.Danger
                        )
                    }
                }
            } else {
                Surface(
                    onClick = onEditBuiltIn,
                    color = RasporedColors.Card2,
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp),
                    border = BorderStroke(1.dp, RasporedColors.StrokeSoft),
                    shadowElevation = 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Tune,
                            "Prilagodi smjenu",
                            tint = RasporedColors.Text
                        )
                    }
                }
            }
        }
    }
}
