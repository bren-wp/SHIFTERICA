package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.data.MonthlyAccountingStore
import hr.raspored.app.model.CroatianWorkTime
import hr.raspored.app.model.ShiftType
import java.time.YearMonth

@Composable
internal fun FundHoursEditor(
    month: YearMonth, schedule: ScheduleStore,
    shiftTypes: List<ShiftType>, accounting: MonthlyAccountingStore
) {
    val computed = CroatianWorkTime.summarize(month, schedule, shiftTypes).fundMinutes / 60
    val overridden = accounting.fundOverrideMinutes(month)?.div(60)
    val shown = overridden ?: computed
    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke)) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Fond sati", color = RasporedColors.Text, fontWeight = FontWeight.Black, fontSize = 19.sp)
            Text("Automatski: $computed h. Ručno promijenite samo ako službeni fond odstupa.",
                color = RasporedColors.Muted, fontSize = 11.sp)
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = {
                    accounting.setFundHours(month, (shown - 1).coerceAtLeast(0))
                }) { Text("−1 h") }
                Text("$shown h", Modifier.weight(1f), textAlign = TextAlign.Center,
                    color = RasporedColors.Text, fontWeight = FontWeight.Black)
                OutlinedButton(onClick = {
                    accounting.setFundHours(month, (shown + 1).coerceAtMost(744))
                }) { Text("+1 h") }
            }
            if (overridden != null) {
                TextButton(onClick = { accounting.setFundHours(month, null) }) {
                    Text("Vrati automatski fond")
                }
            }
        }
    }
}
