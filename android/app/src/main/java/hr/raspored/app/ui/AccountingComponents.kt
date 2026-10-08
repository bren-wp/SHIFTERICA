package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.data.MonthlyAccountingStore
import hr.raspored.app.model.CroatianWorkTime
import hr.raspored.app.model.ShiftType
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

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

@Composable
internal fun AnnualEarningsCard(month: YearMonth, accounting: MonthlyAccountingStore) {
    val saved = accounting.actualNet(month)
    var input by remember(month, saved) {
        mutableStateOf(saved?.let {
            "%.2f".format(Locale.forLanguageTag("hr-HR"), it)
        } ?: "")
    }
    var invalid by remember(month) { mutableStateOf(false) }
    val actual = accounting.actualForYear(month.year)
    val lastThree = accounting.latestThreeActual(month)
    val currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("hr-HR"))

    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke)) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Godišnja zarada", color = RasporedColors.Text,
                fontWeight = FontWeight.Black, fontSize = 21.sp)
            Text("Za usporedbu unesite neto plaću prije osobnih obustava s platne liste, " +
                "ne umanjenu bankovnu isplatu. Procjene se ne zbrajaju kao zarada.",
                color = RasporedColors.Muted, fontSize = 11.sp)
            OutlinedTextField(value = input,
                onValueChange = { input = it.take(20); invalid = false },
                label = { Text("Neto prije obustava za mjesec (€)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, isError = invalid,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val normalized = if (input.contains(',')) {
                        input.replace(".", "").replace(",", ".")
                    } else input
                    val amount = normalized.trim().toDoubleOrNull()
                    if (amount == null || !amount.isFinite() || amount !in 0.0..1_000_000.0) {
                        invalid = true
                    } else accounting.setActualNet(month, amount)
                }) { Text("Spremi neto") }
                if (saved != null) {
                    TextButton(onClick = {
                        accounting.setActualNet(month, null)
                        input = ""
                    }) { Text("Ukloni") }
                }
            }
            if (invalid) Text("Upišite valjan iznos u eurima.",
                color = RasporedColors.Sick, fontSize = 11.sp)
            val total = actual.sumOf { it.second }
            Text("Potvrđeno " + month.year + ": " + currency.format(total) +
                 " (" + actual.size + " mj.)",
                color = RasporedColors.Text, fontWeight = FontWeight.Bold)
            Text(if (lastThree.size == 3) {
                "Prosjek zadnje 3 potvrđene plaće: " + currency.format(lastThree.average())
            } else "Prosjek 3 plaće bit će vidljiv nakon tri potvrđena unosa.",
                color = RasporedColors.Muted, fontSize = 11.sp)
            actual.forEach { (entryMonth, euros) ->
                Row {
                    Text(entryMonth.toString(), Modifier.weight(1f),
                        color = RasporedColors.Muted)
                    Text(currency.format(euros), color = RasporedColors.Text,
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
internal fun PayrollProfileEditor(accounting: MonthlyAccountingStore) {
    var annualHourlyInput by remember(accounting.annualLeaveHourlyGross) {
        mutableStateOf(
            if (accounting.annualLeaveHourlyGross > 0)
                "%.2f".format(Locale.forLanguageTag("hr-HR"), accounting.annualLeaveHourlyGross)
            else ""
        )
    }
    var invalidHourly by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(
        !accounting.profileConfirmed
    ) }
    Surface(color = RasporedColors.Card, shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Postavke obračuna", color = RasporedColors.Text,
                        fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text("Rijeka · koef. 1,25 · staž " + accounting.serviceYears +
                        " god. · djece " + accounting.children,
                        color = RasporedColors.Muted, fontSize = 11.sp)
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Sakrij" else "Uredi")
                }
            }
            if (!accounting.profileConfirmed) {
                Text("Provjerite i potvrdite parametre prije oslanjanja na neto procjenu.",
                    color = Color(0xFFFFC66B), fontSize = 12.sp)
            }
            if (expanded) {
                Text("Upišite podatke s platne liste. Ostaju isključivo na uređaju. " +
                    "Bez ispravnog staža i dječjih olakšica procjena nije pouzdana.",
                    color = RasporedColors.Muted, fontSize = 11.sp)
                ProfileStepper("Godine staža", accounting.serviceYears, 0, 60, accounting::updateServiceYears)
                ProfileStepper("Djeca za poreznu olakšicu", accounting.children, 0, 9, accounting::updateChildren)
                ProfileStepper("Uzdržavani članovi", accounting.dependents, 0, 10, accounting::updateDependents)
                Text("Godišnji odmor · bruto satnica po prosjeku",
                    color = RasporedColors.Text, fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = annualHourlyInput,
                        onValueChange = { annualHourlyInput = it.take(12); invalidHourly = false },
                        label = { Text("€/h, opcionalno") },
                        singleLine = true,
                        isError = invalidHourly,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        val value = annualHourlyInput.trim().replace(",", ".").toDoubleOrNull()
                        if (annualHourlyInput.isBlank()) {
                            accounting.updateAnnualLeaveHourlyGross(0.0)
                        } else if (value == null || !value.isFinite() || value <= 0 || value > 1000) {
                            invalidHourly = true
                        } else {
                            accounting.updateAnnualLeaveHourlyGross(value)
                        }
                    }) { Text("Spremi") }
                }
                Text("Ako ne znate satnicu prema prosjeku, ostavite prazno. " +
                     "Kod GO tada koristimo osnovnu satnicu i jasno označavamo odstupanje.",
                    color = RasporedColors.Muted, fontSize = 10.sp)
                Button(
                    onClick = {
                        accounting.confirmPayrollProfile()
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Potvrdi parametre obračuna") }
            }
        }
    }
}

@Composable
private fun ProfileStepper(
    label: String, value: Int, minimum: Int, maximum: Int, onChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = RasporedColors.Text, fontSize = 12.sp)
        IconButton(enabled = value > minimum, onClick = { onChange(value - 1) }) {
            Text("−", color = RasporedColors.Accent, fontSize = 22.sp)
        }
        Text(value.toString(), color = RasporedColors.Text, fontWeight = FontWeight.Bold)
        IconButton(enabled = value < maximum, onClick = { onChange(value + 1) }) {
            Text("+", color = RasporedColors.Accent, fontSize = 22.sp)
        }
    }
}
