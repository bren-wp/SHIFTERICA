package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.MonthlyAccountingStore
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.model.ShiftType
import hr.raspored.app.model.payroll.PayrollMoneyInput
import java.time.YearMonth
import java.util.Locale

private val hrLocale: Locale = Locale.forLanguageTag("hr-HR")

private fun decimalInput(value: Double): String =
    String.format(hrLocale, "%.2f", value)

/** User-confirmed payments never overwrite the separate statutory estimate. */
@Composable
internal fun ConfirmedNetEditor(
    month: YearMonth,
    accounting: MonthlyAccountingStore,
    schedule: ScheduleStore,
    shiftTypes: List<ShiftType>
) {
    val focusManager = LocalFocusManager.current
    val actual = accounting.actualNet(month)
    val annualRate = accounting.annualLeaveHourlyGross
    val entries = schedule.monthEntries(month)
    val carryOver = schedule.code(month.atDay(1).minusDays(1))
    val fund = accounting.fundOverrideMinutes(month)
    val serviceYears = accounting.serviceYearsForMonth(month)
    val children = accounting.children
    val dependents = accounting.dependents
    val paymentDelay = accounting.paymentDelayMonths(month)
    val estimate = remember(
        month, entries, carryOver, shiftTypes, fund, serviceYears,
        children, dependents, paymentDelay, annualRate
    ) {
        payrollEstimateForMonth(
            month, schedule, shiftTypes, fund,
            serviceYears, children, dependents, annualRate, paymentDelay
        )?.netMonthly
    }
    var netInput by remember(month, actual) {
        mutableStateOf(actual?.let(::decimalInput) ?: "")
    }
    var netError by remember(month) { mutableStateOf(false) }
    var annualInput by remember(annualRate) { mutableStateOf(
        annualRate.takeIf { it > 0 }?.let(::decimalInput) ?: ""
    ) }
    var annualError by remember { mutableStateOf(false) }

    Surface(
        color = RasporedColors.Card,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke)
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                "Usporedba s isplatnom listom",
                color = RasporedColors.Text,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "Upišite samo iznos, bez fotografije platne liste i osobnih podataka. " +
                    "Vrijednost se sprema lokalno na vaš uređaj.",
                color = RasporedColors.Muted, fontSize = 11.sp
            )
            Text(
                "Procijenjeni neto: " + (estimate?.let {
                    String.format(hrLocale, "%,.2f €", it)
                } ?: "—"),
                color = RasporedColors.Muted, fontSize = 13.sp
            )
            if (actual != null) {
                Text(
                    "Potvrđeni neto: " + String.format(hrLocale, "%,.2f €", actual),
                    color = RasporedColors.Accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (estimate != null) {
                    Text(
                        "Razlika (potvrđeni − procjena): " +
                            String.format(hrLocale, "%+,.2f €", actual - estimate),
                        color = RasporedColors.Text, fontSize = 12.sp
                    )
                }
            }
            OutlinedTextField(
                value = netInput,
                onValueChange = { netInput = it; netError = false },
                label = { Text("Stvarno isplaćeni neto (€)") },
                placeholder = { Text("npr. 1.234,56") },
                isError = netError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )
            if (netError) {
                Text(
                    "Neispravan iznos. Koristite npr. 1234,56 (najviše 1.000.000 €).",
                    color = RasporedColors.Danger, fontSize = 11.sp
                )
            }
            Button(onClick = {
                val cents = PayrollMoneyInput.parseCents(netInput)
                if (cents == null) {
                    netError = true
                } else {
                    accounting.setActualNet(month, cents / 100.0)
                    netInput = decimalInput(cents / 100.0)
                    netError = false
                }
            }) { Text("Spremi potvrđeni neto") }
            if (actual != null) {
                TextButton(onClick = {
                    accounting.setActualNet(month, null)
                    netInput = ""
                    netError = false
                }) { Text("Ukloni potvrđeni neto za ovaj mjesec") }
            }

            Text(
                "Prosječna bruto satnica godišnjeg odmora",
                color = RasporedColors.Text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                "Opcionalno: unesite prosječnu bruto satnicu s obračuna. " +
                    "Bez unosa aplikacija koristi procjenu prema osnovici.",
                color = RasporedColors.Muted, fontSize = 11.sp
            )
            OutlinedTextField(
                value = annualInput,
                onValueChange = { annualInput = it; annualError = false },
                label = { Text("Bruto satnica GO (€/h)") },
                placeholder = { Text("npr. 9,85") },
                isError = annualError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )
            if (annualError) {
                Text(
                    "Unesite iznos veći od 0 i do 1.000 € po satu.",
                    color = RasporedColors.Danger, fontSize = 11.sp
                )
            }
            Button(onClick = {
                val cents = PayrollMoneyInput.parseCents(annualInput)
                if (cents == null || cents == 0L || cents > 100_000L) {
                    annualError = true
                } else {
                    accounting.updateAnnualLeaveHourlyGross(cents / 100.0)
                    annualInput = decimalInput(cents / 100.0)
                    annualError = false
                }
            }) { Text("Spremi satnicu za GO") }
            if (annualRate > 0.0) {
                TextButton(onClick = {
                    accounting.updateAnnualLeaveHourlyGross(0.0)
                    annualInput = ""
                    annualError = false
                }) { Text("Vrati zadanu procjenu GO") }
            }
        }
    }
}
