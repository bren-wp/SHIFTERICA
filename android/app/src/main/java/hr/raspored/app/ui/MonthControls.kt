package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.model.ShiftType

private val quickShiftCodes = listOf("N", "D", "J", "P", "GO", "BO")

@Composable
internal fun CompactShiftToolbar(
    shiftTypes: List<ShiftType>,
    selectedCode: String?,
    erasing: Boolean,
    editing: Boolean,
    onSelect: (String) -> Unit,
    onErase: () -> Unit,
    onEditingChange: (Boolean) -> Unit,
    onMore: () -> Unit
) {
    if (!editing) {
        Surface(
            onClick = { onEditingChange(true) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .height(50.dp),
            color = RasporedColors.Card,
            contentColor = RasporedColors.Text,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, RasporedColors.Accent.copy(alpha = .72f)),
            shadowElevation = 5.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = RasporedColors.Accent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "UREDI RASPORED",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Icon(
                    Icons.Rounded.MoreHoriz,
                    contentDescription = "Upravljanje smjenama",
                    tint = RasporedColors.Muted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        return
    }

    val quickShifts = quickShiftCodes.mapNotNull { code ->
        shiftTypes.firstOrNull { it.code == code }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        color = RasporedColors.Card,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShiftToolButton(
                modifier = Modifier.weight(1f),
                selected = erasing,
                color = RasporedColors.Card2,
                borderColor = if (erasing) RasporedColors.Accent else RasporedColors.StrokeSoft,
                onClick = onErase
            ) {
                Icon(
                    Icons.Rounded.Backspace,
                    contentDescription = "Obriši smjenu",
                    tint = if (erasing) RasporedColors.Accent else RasporedColors.Text,
                    modifier = Modifier.size(19.dp)
                )
            }

            quickShifts.forEach { shift ->
                ShiftToolButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    selected = !erasing && selectedCode == shift.code,
                    color = shift.color,
                    borderColor = if (!erasing && selectedCode == shift.code) {
                        RasporedColors.Accent
                    } else {
                        shift.color.copy(alpha = .85f)
                    },
                    onClick = { onSelect(shift.code) }
                ) {
                    Text(
                        shift.code,
                        color = shift.textColor,
                        fontWeight = FontWeight.Black,
                        fontSize = if (shift.code.length > 1) 10.sp else 15.sp,
                        maxLines = 1
                    )
                }
            }

            Box(
                Modifier
                    .padding(start = 4.dp)
                    .width(1.dp)
                    .height(28.dp)
                    .background(RasporedColors.Stroke)
            )

            ShiftToolButton(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                selected = false,
                color = RasporedColors.Card2,
                borderColor = RasporedColors.Accent.copy(alpha = .72f),
                onClick = { onEditingChange(false) }
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = "Završi uređivanje",
                    tint = RasporedColors.Accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ShiftToolButton(
    modifier: Modifier,
    selected: Boolean,
    color: Color,
    borderColor: Color,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier.height(44.dp),
        onClick = onClick,
        color = color,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        shadowElevation = if (selected) 6.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.Center, content = content)
    }
}
