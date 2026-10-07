package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.PayrollSettingsStore
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.model.CroatianWorkTime
import hr.raspored.app.model.ShiftType
import hr.raspored.app.model.payroll.PayrollEstimator
import hr.raspored.app.model.payroll.PayrollInput
import hr.raspored.app.model.payroll.TaxRates
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.YearMonth
import java.util.Locale
import kotlin.math.max

@Composable
internal fun PayrollEstimateCard(
    month: YearMonth,
    schedule: ScheduleStore,
    shiftTypes: List<ShiftType>,
    settings: PayrollSettingsStore
) {
    var showSettings by remember { mutableStateOf(false) }
    val summary = CroatianWorkTime.summarize(month, schedule, shiftTypes)

    val fundAbsenceMinutes: (String) -> Int = { code ->
        schedule.monthEntries(month)
            .count { (date, value) ->
                value == code &&
                    date.dayOfWeek != DayOfWeek.SATURDAY &&
                    date.dayOfWeek != DayOfWeek.SUNDAY
            } * 8 * 60
    }
    val annual = fundAbsenceMinutes("GO")
    val sick = fundAbsenceMinutes("BO")
    val holidayCredit = summary.holidayCreditMinutes
    val otherPaid = max(
        0,
        summary.paidAbsenceMinutes - annual - sick - holidayCredit
    )

    val estimate = if (settings.enabled) {
        PayrollEstimator.estimate(
            PayrollInput(
                month = month,
                coefficient = settings.coefficient,
                completedYearsService = settings.completedYearsService,
                fundMinutes = summary.fundMinutes,
                regularWorkedMinutes = summary.regularMinutes,
                overtimeMinutes = summary.overtimeMinutes,
                annualLeaveMinutes = annual,
                sickLeaveMinutes = sick,
                otherPaidAbsenceMinutes = otherPaid,
                holidayCreditMinutes = holidayCredit,
                nightMinutes = summary.nightMinutes,
                saturdayMinutes = summary.saturdayMinutes,
                sundayMinutes = summary.sundayMinutes,
                holidayWorkedMinutes = summary.holidayWorkedMinutes,
                secondShiftMinutes = summary.secondShiftMinutes,
                children = settings.children,
                dependents = settings.dependents,
                birthYear = settings.birthYear.takeIf { it > 0 },
                taxRates = TaxRates(settings.lowerTaxPercent, settings.higherTaxPercent),
                turnusEnabled = settings.turnusEnabled
            )
        )
    } else null

    Surface(
        color = RasporedColors.Card,
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.dp, RasporedColors.Stroke),
        shadowElevation = 5.dp
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AccountBalanceWallet, null, tint = RasporedColors.Accent)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Procjena plaće",
                    color = RasporedColors.Text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Rounded.Edit, "Postavke obračuna", tint = RasporedColors.Accent)
                }
            }

            if (!settings.enabled) {
                Text(
                    "Procjena je isključena. Uključite je u postavkama obračuna.",
                    color = RasporedColors.Muted
                )
                OutlinedButton(onClick = { showSettings = true }) {
                    Text("Postavke obračuna")
                }
            } else if (estimate == null) {
                Text(
                    "Za ${month.year}. nema ugrađene službene osnovice. Procjena se zato ne prikazuje umjesto nagađanja.",
                    color = RasporedColors.Muted
                )
            } else {
                Text(
                    money(estimate.netBeforeAnnualYouthRelief),
                    color = RasporedColors.Text,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Procijenjeni neto prije godišnjeg poreznog povrata za mlade",
                    color = RasporedColors.Muted,
                    fontSize = 11.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PayrollMiniTile("Bruto 1", money(estimate.grossOne), Modifier.weight(1f))
                    PayrollMiniTile("Porez", money(estimate.monthlyIncomeTax), Modifier.weight(1f))
                    PayrollMiniTile("Odbitak", money(estimate.personalAllowance), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PayrollMiniTile("Bruto 2", money(estimate.grossTwo), Modifier.weight(1f))
                    PayrollMiniTile("Sat bruto", money(estimate.hourlyGross), Modifier.weight(1f))
                    PayrollMiniTile(
                        "Staž",
                        "${settings.completedYearsService} g.",
                        Modifier.weight(1f)
                    )
                }

                if (estimate.youthAnnualReliefFraction > 0.0) {
                    val percent = (estimate.youthAnnualReliefFraction * 100).toInt()
                    Text(
                        "Olakšica za mlade: $percent% godišnjeg poreza na dio plaće oporezovan nižom stopom. Procijenjeni udio povrata koji proizlazi iz ovog mjeseca: ${money(estimate.estimatedYouthRefundShareForMonth)}; efektivni neto nakon tog budućeg povrata približno ${money(estimate.estimatedNetAfterAnnualYouthRelief)}.",
                        color = RasporedColors.Morning,
                        fontSize = 11.sp
                    )
                }

                Text(
                    "Osnovica ${money(estimate.officialBase)} · koeficijent ${String.format(Locale.US, "%.2f", settings.coefficient)} · staž +${String.format(Locale.US, "%.1f", settings.completedYearsService * 0.5)}%. BO do 42 dana procjenjuje se s 85%; godišnji odmor je konzervativno procijenjen po redovnoj satnici i stvarni obračun može biti povoljniji.",
                    color = RasporedColors.Muted,
                    fontSize = 10.sp
                )
                Text(
                    "Orijentacijski izračun, nije platna lista. Porezne stope ovise o prebivalištu, a posebna prava i naknade mogu promijeniti konačni iznos.",
                    color = RasporedColors.Muted,
                    fontSize = 9.sp
                )
            }
        }
    }

    if (showSettings) {
        PayrollSettingsSheet(settings) { showSettings = false }
    }
}

@Composable
private fun PayrollMiniTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = RasporedColors.Card2,
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, RasporedColors.StrokeSoft)
    ) {
        Column(Modifier.padding(9.dp)) {
            Text(label, color = RasporedColors.Muted, fontSize = 9.sp)
            Text(value, color = RasporedColors.Text, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
    }
}

private fun money(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("hr-HR")).format(value)
