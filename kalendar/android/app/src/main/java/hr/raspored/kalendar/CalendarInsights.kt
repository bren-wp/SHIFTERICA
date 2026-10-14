package hr.raspored.kalendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth

/** Original Raspored's 2+1 insights layout without salary, overtime or accounting. */
@Composable
internal fun CalendarMonthInsights(month: YearMonth, entries: List<CalendarEntry>) {
    val current = remember(month, entries) {
        entries.filter { it.date.startsWith(month.toString()) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalendarInsightCard(
                title = "Zapisa",
                amount = current.size,
                icon = Icons.Rounded.CalendarMonth,
                accent = ParityStyle.accent,
                modifier = Modifier.weight(1f)
            )
            CalendarInsightCard(
                title = "Smjena",
                amount = current.count { it.category == "Smjena" },
                icon = Icons.Rounded.BarChart,
                accent = ParityStyle.tiles[2],
                modifier = Modifier.weight(1f)
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth().height(45.dp),
            color = ParityStyle.card,
            shape = RoundedCornerShape(17.dp),
            border = BorderStroke(1.dp, ParityStyle.accent.copy(alpha = .3f))
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Icon(
                    Icons.Rounded.Event, contentDescription = null,
                    modifier = Modifier.size(19.dp), tint = ParityStyle.accent
                )
                Text(
                    "Aktivnih dana", color = ParityStyle.muted,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    current.mapTo(HashSet()) { it.date }.size.toString(),
                    color = ParityStyle.text, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
private fun CalendarInsightCard(
    title: String,
    amount: Int,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier
) {
    Surface(
        modifier = modifier.height(77.dp),
        color = ParityStyle.card,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = .42f))
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accent,
                modifier = Modifier.size(17.dp))
            Text(title, color = ParityStyle.muted, fontSize = 12.sp)
            Text(
                amount.toString(), color = ParityStyle.text,
                fontSize = 22.sp, fontWeight = FontWeight.Bold
            )
        }
    }
}
