package hr.raspored.kalendar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.kalendar.model.ShiftType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val quickShiftCodes = listOf("N", "D", "J", "P", "GO", "BO")

@Composable
internal fun MonthManageBar(onMore: () -> Unit) {
    Surface(
        onClick = onMore,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .height(46.dp),
        color = RasporedColors.Card,
        contentColor = RasporedColors.Text,
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke.copy(alpha = .88f)),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Rounded.MoreHoriz,
                contentDescription = null,
                tint = RasporedColors.Accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "UPRAVLJANJE SMJENAMA",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayShiftPickerSheet(
    date: LocalDate,
    currentCode: String?,
    shiftTypes: List<ShiftType>,
    onSelect: (String) -> Unit,
    onClear: () -> Unit,
    onOpenShifts: () -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy.", Locale("hr", "HR"))
    val preferred = quickShiftCodes.mapNotNull { code ->
        shiftTypes.firstOrNull { it.code == code }
    }
    val custom = shiftTypes.filter { shift -> shift.code !in quickShiftCodes }
    val ordered = preferred + custom

    val gridRows = ordered.chunked(3)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RasporedColors.Bg2,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = RasporedColors.Muted) }
    ) {
        // A lazy, bounded sheet prevents the lower actions disappearing on
        // compact phones or when many custom shifts have been created.
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "picker-title") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Odaberi smjenu",
                            color = RasporedColors.Text,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            formatter.format(date).replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale("hr", "HR"))
                                else it.toString()
                            },
                            color = RasporedColors.Muted,
                            fontSize = 12.sp
                        )
                    }
                    Surface(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp),
                        color = RasporedColors.Card2,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, RasporedColors.StrokeSoft)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Close, "Zatvori", tint = RasporedColors.Text)
                        }
                    }
                }
            }
            item(key = "picker-current") {
                Text(
                    if (currentCode == null) "Trenutačno nema smjene za ovaj datum."
                    else "Trenutačno: $currentCode",
                    color = if (currentCode == null) RasporedColors.Muted else RasporedColors.Accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(gridRows, key = { row -> row.joinToString("|") { it.code } }) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { shift ->
                        DayShiftChoice(
                            modifier = Modifier.weight(1f),
                            shift = shift,
                            selected = currentCode == shift.code,
                            onClick = { onSelect(shift.code) }
                        )
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            item(key = "picker-actions") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = onClear,
                        modifier = Modifier.weight(1f).height(52.dp),
                        color = RasporedColors.Card2,
                        shape = RoundedCornerShape(17.dp),
                        border = BorderStroke(1.dp, RasporedColors.Danger.copy(alpha = .58f))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Backspace, null, tint = RasporedColors.Danger)
                            Spacer(Modifier.width(7.dp))
                            Text("Obriši", color = RasporedColors.Text, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        onClick = onOpenShifts,
                        modifier = Modifier.weight(1f).height(52.dp),
                        color = RasporedColors.Accent.copy(alpha = .20f),
                        shape = RoundedCornerShape(17.dp),
                        border = BorderStroke(1.2.dp, RasporedColors.Accent)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Sve smjene", color = RasporedColors.Text,
                                fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            item(key = "picker-hint") {
                Text(
                    "Odabirom smjene ona se odmah sprema za odabrani datum.",
                    modifier = Modifier.fillMaxWidth(),
                    color = RasporedColors.Muted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DayShiftChoice(
    modifier: Modifier,
    shift: ShiftType,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(72.dp).semantics {
            contentDescription = "${shift.name}, ${shift.code}"
            this.selected = selected
        },
        color = shift.color,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            if (selected) 2.5.dp else 1.dp,
            if (selected) RasporedColors.Accent else shift.color.copy(alpha = .92f)
        ),
        shadowElevation = if (selected) 9.dp else 4.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                shift.code,
                color = shift.textColor,
                fontSize = if (shift.code.length == 1) 21.sp else 15.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                shift.shortName,
                color = shift.textColor.copy(alpha = .76f),
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}
