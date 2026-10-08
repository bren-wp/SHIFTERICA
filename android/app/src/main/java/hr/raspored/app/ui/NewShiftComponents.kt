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
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RasporedColors.Accent,
                    unfocusedBorderColor = RasporedColors.StrokeSoft,
                    focusedTextColor = RasporedColors.Text,
                    unfocusedTextColor = RasporedColors.Text,
                    focusedLabelColor = RasporedColors.Accent,
                    unfocusedLabelColor = RasporedColors.Muted
                )
            )
            OutlinedTextField(
                value = end,
                onValueChange = onEnd,
                label = { Text("Završetak") },
                placeholder = { Text("15:00") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RasporedColors.Accent,
                    unfocusedBorderColor = RasporedColors.StrokeSoft,
                    focusedTextColor = RasporedColors.Text,
                    unfocusedTextColor = RasporedColors.Text,
                    focusedLabelColor = RasporedColors.Accent,
                    unfocusedLabelColor = RasporedColors.Muted
                )
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
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke.copy(alpha = .92f)),
        shadowElevation = 7.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValue,
                modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RasporedColors.Accent,
                    unfocusedBorderColor = RasporedColors.Stroke,
                    focusedTextColor = RasporedColors.Text,
                    unfocusedTextColor = RasporedColors.Text
                ),
                shape = RoundedCornerShape(16.dp)
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
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke.copy(alpha = .92f)),
        shadowElevation = 7.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValue,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
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
                shape = RoundedCornerShape(16.dp)
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
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke.copy(alpha = .92f)),
        shadowElevation = 7.dp
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
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(46.dp),
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(46.dp),
                        border = BorderStroke(
                            if (color == selected) 2.dp else 1.dp,
                            if (color == selected) {
                                RasporedColors.Accent
                            } else {
                                RasporedColors.StrokeSoft
                            }
                        ),
                        shadowElevation = if (color == selected) 10.dp else 2.dp
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
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke.copy(alpha = .92f)),
        shadowElevation = 7.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                "Veličina teksta",
                color = RasporedColors.Text,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = { onChange((size - 1).coerceAtLeast(8f)) },
                    modifier = Modifier.size(52.dp),
                    color = RasporedColors.Card2,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, RasporedColors.StrokeSoft),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Remove, null, tint = RasporedColors.Text)
                    }
                }
                Slider(
                    value = size,
                    onValueChange = onChange,
                    valueRange = 8f..24f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = RasporedColors.Text,
                        activeTrackColor = RasporedColors.Accent,
                        inactiveTrackColor = RasporedColors.Card2
                    )
                )
                Surface(
                    onClick = { onChange((size + 1).coerceAtMost(24f)) },
                    modifier = Modifier.size(52.dp),
                    color = RasporedColors.Card2,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, RasporedColors.StrokeSoft),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Add, null, tint = RasporedColors.Text)
                    }
                }
                Surface(
                    color = RasporedColors.Card2,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, RasporedColors.StrokeSoft)
                ) {
                    Text(
                        size.toInt().toString(),
                        Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        color = RasporedColors.Text,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}



@Composable
internal fun NewShiftTabs(selected: Int, onSelect: (Int) -> Unit) {
    Surface(
        color = RasporedColors.Card,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke.copy(alpha = .92f)),
        shadowElevation = 7.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Izgled", "Raspored").forEachIndexed { index, label ->
                val active = selected == index
                Surface(
                    onClick = { onSelect(index) },
                    modifier = Modifier.weight(1f),
                    color = if (active) RasporedColors.Accent.copy(alpha = .28f) else Color.Transparent,
                    shape = RoundedCornerShape(17.dp),
                    border = if (active) BorderStroke(1.4.dp, RasporedColors.Accent) else null,
                    shadowElevation = if (active) 8.dp else 0.dp
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(vertical = 13.dp),
                        color = if (active) RasporedColors.Text else RasporedColors.Muted,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
