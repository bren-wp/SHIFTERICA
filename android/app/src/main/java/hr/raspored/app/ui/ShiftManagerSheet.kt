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
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.model.ShiftDeletionPolicy
import hr.raspored.app.model.ShiftType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShiftManagerSheet(
    library: ShiftLibraryStore,
    schedule: ScheduleStore,
    onDismiss: () -> Unit,
    onNewShift: () -> Unit,
    onEditCustom: (ShiftType) -> Unit
) {
    var editingBuiltIn by remember { mutableStateOf<ShiftType?>(null) }
    var pendingDeletion by remember { mutableStateOf<ShiftType?>(null) }

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
                        "Dodirnite smjenu za promjenu boje i vremena. Zadane vrijednosti možete vratiti.",
                        color = RasporedColors.Muted,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.weight(1f))
                Surface(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp),
                    color = RasporedColors.Card2,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, RasporedColors.StrokeSoft),
                    shadowElevation = 5.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Close,
                            "Zatvori",
                            tint = RasporedColors.Text
                        )
                    }
                }
            }

            Button(
                onClick = onNewShift,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RasporedColors.Accent.copy(alpha = .28f)),
                border = BorderStroke(1.dp, RasporedColors.Accent),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Rounded.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Nova smjena", fontWeight = FontWeight.Bold)
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
                        assignedCount = if (shift.custom)
                            ShiftDeletionPolicy.assignedDates(schedule.entries.values, shift.code)
                        else 0,
                        onEditBuiltIn = { editingBuiltIn = shift },
                        onEditCustom = { onEditCustom(shift) },
                        onDeleteCustom = { pendingDeletion = shift }
                    )
                }
            }
        }
    }

    pendingDeletion?.let { shift ->
        val assignedCount = ShiftDeletionPolicy.assignedDates(schedule.entries.values, shift.code)
        AlertDialog(
            onDismissRequest = { pendingDeletion = null },
            containerColor = RasporedColors.Bg2,
            title = {
                Text(
                    if (assignedCount > 0) "Smjena je u uporabi" else "Izbrisati smjenu?",
                    color = RasporedColors.Text,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (assignedCount > 0)
                        "Smjena ${shift.code} upisana je na $assignedCount datuma. " +
                            "Najprije promijenite ili uklonite tu smjenu s tih datuma. " +
                            "Postojeći raspored ostaje sačuvan."
                    else "Trajno ukloniti vlastitu smjenu ${shift.code}? " +
                        "Ova radnja ne mijenja upisane datume.",
                    color = RasporedColors.Muted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (assignedCount == 0 &&
                        ShiftDeletionPolicy.canDelete(schedule.snapshot().values, shift.code)
                    ) {
                        library.delete(shift.code)
                    }
                    pendingDeletion = null
                }) {
                    Text(
                        if (assignedCount > 0) "Razumijem" else "Izbriši smjenu",
                        color = if (assignedCount > 0) RasporedColors.Accent else RasporedColors.Danger
                    )
                }
            },
            dismissButton = {
                if (assignedCount == 0) {
                    TextButton(onClick = { pendingDeletion = null }) {
                        Text("Odustani", color = RasporedColors.Text)
                    }
                }
            }
        )
    }

    editingBuiltIn?.let { shift ->
        BuiltInShiftEditorDialog(
            shift = shift,
            onDismiss = { editingBuiltIn = null },
            onSave = { background, textColor, start, end ->
                library.updateBuiltIn(
                    code = shift.code,
                    background = background,
                    textColor = textColor,
                    start = start,
                    end = end
                )
            },
            onReset = {
                library.resetBuiltIn(shift.code)
                editingBuiltIn = null
            }
        )
    }


}

@Composable
private fun ShiftManagerRow(
    shift: ShiftType,
    assignedCount: Int,
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
                        if (assignedCount > 0) "U rasporedu: $assignedCount datuma" else "Vlastita smjena · nije upisana",
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
