package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Output
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.data.UiSettingsStore
import hr.raspored.app.model.ShiftType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun ShiftLegend(shiftTypes: List<ShiftType>, onOpenShifts: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        color = RasporedColors.Card,
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 5.dp
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Vrste smjena", color = RasporedColors.Text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onOpenShifts, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Rounded.ChevronRight, null, tint = RasporedColors.Muted)
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(shiftTypes, key = { it.code }) { shift -> LegendChip(shift) }
            }
        }
    }
}

@Composable
private fun LegendChip(shift: ShiftType) {
    Surface(
        color = RasporedColors.Card2,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, RasporedColors.StrokeSoft)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Surface(color = shift.color, shape = RoundedCornerShape(10.dp)) {
                Text(
                    shift.code,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    color = shift.textColor,
                    fontWeight = FontWeight.Black
                )
            }
            Text(shift.shortName, color = RasporedColors.Text, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
internal fun QuickToolbar(shiftTypes: List<ShiftType>, onEdit: () -> Unit, onShift: (String) -> Unit, onMore: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
        color = RasporedColors.Card,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolIcon(Icons.Rounded.Backspace, "Gumica", onEdit)
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                items(shiftTypes, key = { it.code }) { shift ->
                    Surface(
                        modifier = Modifier.size(48.dp),
                        onClick = { onShift(shift.code) },
                        color = shift.color,
                        shape = RoundedCornerShape(13.dp),
                        border = BorderStroke(1.dp, shift.color.copy(alpha = .85f)),
                        shadowElevation = 5.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(shift.code, color = shift.textColor, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }
            Box(Modifier.width(1.dp).height(38.dp).background(RasporedColors.Stroke))
            ToolIcon(Icons.Rounded.MoreHoriz, "Više", onMore)
        }
    }
}

@Composable
private fun ToolIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(48.dp),
        onClick = onClick,
        color = RasporedColors.Card2,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, RasporedColors.StrokeSoft)
    ) { Box(contentAlignment = Alignment.Center) { Icon(icon, label, tint = RasporedColors.Text) } }
}

@Composable
internal fun EditingDock(
    shiftTypes: List<ShiftType>,
    selectedCode: String?,
    erasing: Boolean,
    onSelect: (String) -> Unit,
    onErase: () -> Unit,
    onExit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RasporedColors.Card,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 8.dp
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.size(width = 56.dp, height = 5.dp).clip(RoundedCornerShape(3.dp)).background(RasporedColors.Muted.copy(.5f)))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Način uređivanja", color = RasporedColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text("Dodirnite dan kako biste primijenili smjenu", color = RasporedColors.Muted, fontSize = 12.sp)
                }
                OutlinedButton(onClick = onExit, border = BorderStroke(1.dp, RasporedColors.Accent), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Rounded.Output, null, tint = RasporedColors.Accent)
                    Spacer(Modifier.width(6.dp))
                    Text("Izađi iz uređivanja", color = RasporedColors.Accent, fontSize = 12.sp)
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    EditToolChip(
                        label = "Gumica",
                        code = "",
                        color = RasporedColors.Card2,
                        selected = erasing,
                        icon = { Icon(Icons.Rounded.Backspace, null, tint = RasporedColors.Text) },
                        onClick = onErase
                    )
                }
                items(shiftTypes, key = { it.code }) { shift ->
                    EditToolChip(
                        label = shift.shortName,
                        code = shift.code,
                        color = shift.color,
                        selected = selectedCode == shift.code,
                        textColor = shift.textColor,
                        icon = null,
                        onClick = { onSelect(shift.code) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditToolChip(
    label: String,
    code: String,
    color: Color,
    selected: Boolean,
    textColor: Color = RasporedColors.Text,
    icon: (@Composable (() -> Unit))?,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Surface(
            modifier = Modifier.size(64.dp),
            onClick = onClick,
            color = color,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) RasporedColors.Accent else RasporedColors.StrokeSoft),
            shadowElevation = if (selected) 8.dp else 3.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (icon != null) icon() else Text(code, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
        Text(label, color = RasporedColors.Text, fontSize = 11.sp)
    }
}

