package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.data.ShiftLibraryStore
import hr.raspored.app.data.UiSettingsStore
import hr.raspored.app.model.ShiftType
import java.time.LocalDate

@Composable
internal fun SettingsGroup(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, RasporedColors.Stroke), shadowElevation = 6.dp) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = RasporedColors.Accent)
                Text(title, color = RasporedColors.Text, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            content()
        }
    }
}

@Composable
internal fun SettingsToggle(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RasporedColors.Card2.copy(alpha = .72f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, RasporedColors.StrokeSoft)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
                Text(subtitle, color = RasporedColors.Muted, fontSize = 11.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(checkedTrackColor = RasporedColors.Accent)
            )
        }
    }
}

@Composable
internal fun SettingsSegmented(
    title: String,
    subtitle: String,
    values: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    compact: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RasporedColors.Card2.copy(alpha = .62f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, RasporedColors.StrokeSoft)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
            Text(subtitle, color = RasporedColors.Muted, fontSize = 11.sp)
            SegmentedSettings(values, selected, onSelect, compact)
        }
    }
}

@Composable
internal fun SegmentedSettings(values: List<String>, selected: String, onSelect: (String) -> Unit, compact: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(if (compact) rememberScrollState() else rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        values.forEach { value ->
            val active = value == selected
            Surface(
                onClick = { onSelect(value) },
                color = if (active) RasporedColors.Accent.copy(alpha = .28f) else RasporedColors.Bg2.copy(alpha = .72f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (active) RasporedColors.Accent else RasporedColors.StrokeSoft),
                shadowElevation = if (active) 8.dp else 0.dp
            ) {
                Text(
                    value,
                    modifier = Modifier.padding(horizontal = if (compact) 11.dp else 16.dp, vertical = 10.dp),
                    color = if (active) RasporedColors.Text else RasporedColors.Muted,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    fontSize = if (compact) 11.sp else 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
internal fun SettingsShapeSelector(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        "Zaobljeni kvadrat" to RoundedCornerShape(8.dp),
        "Krug" to CircleShape,
        "Kvadrat" to RoundedCornerShape(2.dp),
        "Pill" to RoundedCornerShape(50)
    )
    Column(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("Oblik", color = RasporedColors.Text, fontWeight = FontWeight.Bold)
        Text("Odaberite oblik isticanja", color = RasporedColors.Muted, fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            options.forEach { (label, shape) ->
                val active = selected == label
                Surface(
                    onClick = { onSelect(label) },
                    modifier = Modifier.size(width = 66.dp, height = 46.dp),
                    color = if (active) RasporedColors.Accent.copy(alpha = .20f) else RasporedColors.Card2,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (active) RasporedColors.Accent else RasporedColors.StrokeSoft),
                shadowElevation = if (active) 6.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = if (label == "Pill") Modifier.size(width = 34.dp, height = 20.dp) else Modifier.size(24.dp),
                            color = Color.Transparent,
                            shape = shape,
                            border = BorderStroke(1.5.dp, RasporedColors.Text)
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
internal fun SettingsStatic(title: String, value: String, onClick: (() -> Unit)? = null) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RasporedColors.Card2.copy(alpha = .72f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, RasporedColors.StrokeSoft)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
                Text(value, color = RasporedColors.Muted, fontSize = 11.sp)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = RasporedColors.Muted)
        }
    }
}

@Composable
internal fun SettingsMenu(
    title: String,
    value: String,
    values: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = RasporedColors.Card2.copy(alpha = .72f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, RasporedColors.StrokeSoft)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
                    Text(value, color = RasporedColors.Muted, fontSize = 11.sp)
                }
                Icon(Icons.Rounded.ExpandMore, null, tint = RasporedColors.Muted)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = RasporedColors.Card2
        ) {
            values.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = RasporedColors.Text) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    trailingIcon = if (value == option) {
                        { Icon(Icons.Rounded.Check, null, tint = RasporedColors.Accent) }
                    } else null
                )
            }
        }
    }
}
