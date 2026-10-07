package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun CustomTimePair(
    title: String,
    start: String,
    end: String,
    onStart: (String) -> Unit,
    onEnd: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, color = RasporedColors.Muted, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = start,
                onValueChange = onStart,
                label = { Text("Početak") },
                placeholder = { Text("07:00") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = end,
                onValueChange = onEnd,
                label = { Text("Završetak") },
                placeholder = { Text("15:00") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun LabeledField(
    label: String,
    value: String,
    onValue: (String) -> Unit
) {
    Surface(
        color = RasporedColors.Card,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 5.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValue,
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
internal fun LabeledFieldWithCounter(
    label: String,
    value: String,
    counter: String,
    enabled: Boolean,
    onValue: (String) -> Unit
) {
    Surface(
        color = RasporedColors.Card,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 5.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValue,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    Text(
                        counter,
                        color = RasporedColors.Muted,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RasporedColors.Accent,
                    unfocusedBorderColor = RasporedColors.Stroke,
                    focusedTextColor = RasporedColors.Text,
                    unfocusedTextColor = RasporedColors.Text,
                    disabledTextColor = RasporedColors.Muted,
                    disabledBorderColor = RasporedColors.StrokeSoft
                ),
                shape = RoundedCornerShape(15.dp)
            )
            if (!enabled) {
                Text(
                    "Skraćenica ostaje ista kako postojeći raspored ne bi izgubio poveznicu sa smjenom.",
                    color = RasporedColors.Muted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}

@Composable
internal fun ColorCard(
    title: String,
    colors: List<Color>,
    selected: Color,
    onPick: (Color) -> Unit
) {
    Surface(
        color = RasporedColors.Card,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 5.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = RasporedColors.Card2,
                    shape = RoundedCornerShape(11.dp),
                    modifier = Modifier.size(42.dp),
                    border = BorderStroke(1.dp, RasporedColors.Accent)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Palette,
                            null,
                            tint = RasporedColors.Accent
                        )
                    }
                }
                colors.forEach { color ->
                    Surface(
                        onClick = { onPick(color) },
                        color = color,
                        shape = RoundedCornerShape(11.dp),
                        modifier = Modifier.size(42.dp),
                        border = BorderStroke(
                            if (color == selected) 2.dp else 1.dp,
                            if (color == selected) {
                                RasporedColors.Accent
                            } else {
                                RasporedColors.StrokeSoft
                            }
                        ),
                        shadowElevation = if (color == selected) 8.dp else 2.dp
                    ) {}
                }
            }
        }
    }
}

@Composable
internal fun FontSizeCard(
    size: Float,
    onChange: (Float) -> Unit
) {
    Surface(
        color = RasporedColors.Card,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 5.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                "Veličina teksta",
                color = RasporedColors.Text,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledIconButton(
                    onClick = { onChange((size - 1).coerceAtLeast(8f)) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = RasporedColors.Card2
                    )
                ) {
                    Icon(Icons.Rounded.Remove, null)
                }
                Slider(
                    value = size,
                    onValueChange = onChange,
                    valueRange = 8f..24f,
                    modifier = Modifier.weight(1f)
                )
                FilledIconButton(
                    onClick = { onChange((size + 1).coerceAtMost(24f)) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = RasporedColors.Card2
                    )
                ) {
                    Icon(Icons.Rounded.Add, null)
                }
                Surface(
                    color = RasporedColors.Card2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        size.toInt().toString(),
                        Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        color = RasporedColors.Text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

