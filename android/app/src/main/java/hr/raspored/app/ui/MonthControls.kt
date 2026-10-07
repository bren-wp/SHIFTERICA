package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.model.ShiftType

@Composable
internal fun CompactShiftToolbar(
    shiftTypes: List<ShiftType>,
    selectedCode: String?,
    erasing: Boolean,
    onSelect: (String) -> Unit,
    onErase: () -> Unit,
    onMore: () -> Unit
) {
    val preferred = listOf("D", "N", "J", "GO", "BO", "P")
    val ordered = preferred.mapNotNull { code ->
        shiftTypes.firstOrNull { it.code == code }
    } + shiftTypes.filter { it.code !in preferred }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        color = RasporedColors.Card,
        shape = RoundedCornerShape(21.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShiftToolButton(
                selected = erasing,
                color = RasporedColors.Card2,
                borderColor = if (erasing) RasporedColors.Accent else RasporedColors.StrokeSoft,
                onClick = onErase
            ) {
                Icon(
                    Icons.Rounded.Backspace,
                    contentDescription = "Obriši smjenu",
                    tint = if (erasing) RasporedColors.Accent else RasporedColors.Text,
                    modifier = Modifier.size(21.dp)
                )
            }

            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(ordered, key = { it.code }) { shift ->
                    ShiftToolButton(
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
                            fontSize = if (shift.code.length > 1) 12.sp else 16.sp
                        )
                    }
                }
            }

            Box(
                Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(RasporedColors.Stroke)
            )

            ShiftToolButton(
                selected = false,
                color = RasporedColors.Card2,
                borderColor = RasporedColors.StrokeSoft,
                onClick = onMore
            ) {
                Icon(
                    Icons.Rounded.MoreHoriz,
                    contentDescription = "Više smjena",
                    tint = RasporedColors.Text,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun ShiftToolButton(
    selected: Boolean,
    color: Color,
    borderColor: Color,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = Modifier.size(45.dp),
        onClick = onClick,
        color = color,
        shape = RoundedCornerShape(13.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        shadowElevation = if (selected) 7.dp else 2.dp
    ) {
        Box(contentAlignment = Alignment.Center, content = content)
    }
}
