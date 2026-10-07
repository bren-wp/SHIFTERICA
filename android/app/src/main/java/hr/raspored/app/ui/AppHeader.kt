package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale


@Composable
internal fun PremiumHeader(
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AppMark()
        Text(
            "Raspored",
            color = RasporedColors.Text,
            fontSize = 29.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(Modifier.weight(1f))
        HeaderIconButton(Icons.Rounded.Search, "Pretraži", onSearch)
        HeaderIconButton(Icons.Rounded.Tune, "Postavke", onSettings)
        Surface(
            onClick = onAdd,
            modifier = Modifier.size(52.dp),
            color = RasporedColors.Accent,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.2.dp, Color(0xFF79FFFF)),
            shadowElevation = 10.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Add, contentDescription = "Nova smjena", modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(46.dp),
        color = RasporedColors.Card2,
        contentColor = RasporedColors.Text,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, RasporedColors.StrokeSoft),
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
internal fun TopTabs(section: MainSection, month: YearMonth, onSection: (MainSection) -> Unit) {
    val items = listOf(
        MainSection.MONTH to month.month.getDisplayName(TextStyle.FULL, Locale("hr", "HR")).uppercase(),
        MainSection.YEAR to month.year.toString(),
        MainSection.SUMMARY to "SAŽETAK"
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp),
        color = RasporedColors.Card.copy(alpha = .96f),
        shape = RoundedCornerShape(23.dp),
        border = BorderStroke(1.dp, RasporedColors.StrokeSoft)
    ) {
        Row(Modifier.padding(4.dp)) {
            items.forEach { (item, label) ->
                val active = section == item
                Surface(
                    modifier = Modifier.weight(1f),
                    onClick = { onSection(item) },
                    color = if (active) RasporedColors.Accent.copy(alpha = .22f) else Color.Transparent,
                    shape = RoundedCornerShape(18.dp),
                    border = if (active) BorderStroke(1.4.dp, RasporedColors.Accent) else null,
                    shadowElevation = if (active) 7.dp else 0.dp
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(vertical = 13.dp),
                        textAlign = TextAlign.Center,
                        color = if (active) Color.White else RasporedColors.Muted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

