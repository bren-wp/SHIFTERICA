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

// Profile settings are edited on the Sati tab, not inserted into the home dashboard.
@Composable
internal fun PayrollProfileEditor(
    month: YearMonth,
    accounting: MonthlyAccountingStore
) {
    val monthYears = accounting.serviceYearsForMonth(month)
    val monthlyOverride = accounting.serviceYearsOverride(month)
    val paymentDelay = accounting.paymentDelayMonths(month)
    val paymentDate = month.plusMonths(paymentDelay.toLong())
    val paymentLabel = paymentDate.month.getDisplayName(
        java.time.format.TextStyle.FULL_STANDALONE, java.util.Locale.forLanguageTag("hr-HR")
    ) + " " + paymentDate.year
    Surface(
        color = RasporedColors.Card, shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke)
    ) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("Podaci za obračun plaće", color = RasporedColors.Text,
                fontWeight = FontWeight.Black, fontSize = 19.sp)
            Text(
                "Unesite podatke s obračunske liste. Iznosi se čuvaju samo na uređaju. " +
                    "Neto je procjena dok nisu poznate sve stavke.",
                color = RasporedColors.Muted, fontSize = 11.sp
            )
            PayrollNumberAdjuster(
                "Godine staža (zadano)", accounting.serviceYears, 60,
                onChange = accounting::updateServiceYears
            )
            PayrollNumberAdjuster(
                "Godine staža za odabrani mjesec", monthYears, 60,
                onChange = { accounting.setServiceYearsForMonth(month, it) }
            )
            if (monthlyOverride != null) {
                TextButton(onClick = {
                    accounting.setServiceYearsForMonth(month, null)
                }) { Text("Vrati zadani staž za ovaj mjesec") }
            }
            Text("Mjesec isplate za odabrani obračun",
                color = RasporedColors.Text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(
                "Isplata: $paymentLabel (odmak $paymentDelay mj.). Porez i odbitak " +
                    "računaju se prema mjesecu isplate; osnovica ostaje vezana uz mjesec rada.",
                color = RasporedColors.Muted, fontSize = 11.sp
            )
            PayrollNumberAdjuster(
                "Odmak isplate (mjeseci)", paymentDelay, 12,
                onChange = { accounting.setPaymentDelayMonths(month, it) }
            )
            if (accounting.paymentDelayOverride(month) != null) {
                TextButton(onClick = { accounting.setPaymentDelayMonths(month, null) }) {
                    Text("Vrati isplatu u sljedećem mjesecu")
                }
            }
            PayrollNumberAdjuster(
                "Broj djece za porezni odbitak", accounting.children, 9,
                onChange = accounting::updateChildren
            )
            PayrollNumberAdjuster(
                "Ostali uzdržavani članovi", accounting.dependents, 10,
                onChange = accounting::updateDependents
            )
            Text(
                "Starije mjesece provjerite pojedinačno: 11 godina daje 5,5 %, " +
                    "a 12 godina 6 %. Promjene ne brišu raspored ni potvrđene neto isplate.",
                color = RasporedColors.Muted, fontSize = 11.sp
            )
            if (!accounting.profileConfirmed) {
                TextButton(onClick = { accounting.confirmPayrollProfile() }) {
                    Text("Potvrdi podatke obračuna")
                }
            }
        }
    }
}

@Composable
private fun PayrollNumberAdjuster(
    label: String,
    value: Int,
    maximum: Int,
    onChange: (Int) -> Unit
) {
    Text(label, color = RasporedColors.Text, fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = { onChange((value - 1).coerceAtLeast(0)) },
            enabled = value > 0
        ) { Text("−") }
        Text(value.toString(), Modifier.weight(1f), textAlign = TextAlign.Center,
            color = RasporedColors.Text, fontWeight = FontWeight.Black)
        OutlinedButton(
            onClick = { onChange((value + 1).coerceAtMost(maximum)) },
            enabled = value < maximum
        ) { Text("+") }
    }
}
