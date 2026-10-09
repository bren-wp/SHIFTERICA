package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth


@Composable
internal fun PremiumHeader(
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AppMark()
        Text(
            "Raspored",
            color = RasporedColors.Text,
            fontSize = 30.sp,
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
        modifier = Modifier.size(48.dp),
        color = RasporedColors.Card2,
        contentColor = RasporedColors.Text,
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, RasporedColors.StrokeSoft),
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(26.dp))
        }
    }
}

/**
 * Compact, accessible navigation for the three primary destinations.
 * The calendar and summary already include their own month arrows; the
 * navigation intentionally avoids repeating them.
 */
@Composable
internal fun SubtleBottomNavigation(
    section: MainSection,
    onSection: (MainSection) -> Unit
) {
    val destinations = listOf(
        Triple(MainSection.MONTH, "Mjesec", Icons.Rounded.CalendarMonth),
        Triple(MainSection.YEAR, "Godina", Icons.Rounded.DateRange),
        Triple(MainSection.SUMMARY, "Sažetak", Icons.Rounded.BarChart)
    )
    Surface(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        color = RasporedColors.Card,
        shape = RoundedCornerShape(19.dp),
        border = BorderStroke(1.dp, RasporedColors.StrokeSoft),
        shadowElevation = 2.dp
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { (destination, title, symbol) ->
                val active = section == destination
                Surface(
                    onClick = { onSection(destination) },
                    modifier = Modifier.weight(1f).height(54.dp)
                        .testTag("bottom-nav-" + destination.name.lowercase())
                        .semantics { selected = active },
                    color = Color.Transparent,
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(width = 48.dp, height = 27.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = if (active) RasporedColors.Accent.copy(alpha = .14f)
                                    else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ) {}
                            Icon(
                                symbol,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (active) RasporedColors.Accent else RasporedColors.Muted
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            title,
                            color = if (active) RasporedColors.Text else RasporedColors.Muted,
                            fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
