package hr.raspored.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hr.raspored.app.data.ScheduleStore
import hr.raspored.app.model.CroatianWorkTime
import hr.raspored.app.model.ShiftType
import hr.raspored.app.model.payroll.CroatianPayrollRules
import hr.raspored.app.model.payroll.PayrollEstimator
import hr.raspored.app.model.payroll.PayrollInput
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.YearMonth
import java.util.Locale
import kotlin.math.max

internal fun payrollEstimateForMonth(
    month: YearMonth,
    schedule: ScheduleStore,
    shiftTypes: List<ShiftType>,
    fundOverrideMinutes: Int? = null,
    serviceYears: Int = 0,
    children: Int = 0,
    dependents: Int = 0,
    annualLeaveHourlyGross: Double = 0.0
): hr.raspored.app.model.payroll.PayrollEstimate? {
    if (schedule.monthEntries(month).isEmpty()) return null

    val summary = CroatianWorkTime.summarize(
        month, schedule, shiftTypes, fundOverrideMinutes = fundOverrideMinutes
    )
    fun absenceMinutes(code: String): Int =
        schedule.monthEntries(month)
            .count { (date, value) ->
                value == code &&
                    date.dayOfWeek != DayOfWeek.SATURDAY &&
                    date.dayOfWeek != DayOfWeek.SUNDAY
            } * 8 * 60

    val annual = absenceMinutes("GO")
    val sick = absenceMinutes("BO")
    val otherPaid = max(
        0,
        summary.paidAbsenceMinutes -
            annual -
            sick -
            summary.holidayCreditMinutes
    )

    return PayrollEstimator.estimate(
        PayrollInput(
            month = month,
            summary = summary,
            annualLeaveMinutes = annual,
            sickLeaveMinutes = sick,
            otherPaidAbsenceMinutes = otherPaid,
            hasDayNightTurnusPattern =
                schedule.count(month, "D") > 0 ||
                    schedule.count(month, "N") > 0,
            serviceYears = serviceYears,
            children = children,
            dependents = dependents,
            annualLeaveAverageHourlyGross = annualLeaveHourlyGross.takeIf { it > 0.0 }
        )
    )
}

@Composable
internal fun PayrollEstimateCard(
    month: YearMonth,
    schedule: ScheduleStore,
    shiftTypes: List<ShiftType>,
    fundOverrideMinutes: Int? = null,
    serviceYears: Int = 0,
    children: Int = 0,
    dependents: Int = 0,
    annualLeaveHourlyGross: Double = 0.0,
    profileConfirmed: Boolean = false
) {
    val hasScheduleData = schedule.monthEntries(month).isNotEmpty()
    var showBreakdown by remember(month) { mutableStateOf(false) }
    val estimate = payrollEstimateForMonth(
        month, schedule, shiftTypes, fundOverrideMinutes, serviceYears, children, dependents,
        annualLeaveHourlyGross
    )

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
            Row {
                Icon(
                    Icons.Rounded.AccountBalanceWallet,
                    null,
                    tint = RasporedColors.Accent
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Procjena plaće",
                        color = RasporedColors.Text,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Rijeka · koeficijent 1,25 · EUR",
                        color = RasporedColors.Muted,
                        fontSize = 10.sp
                    )
                }
            }

            if (estimate == null) {
                Text(
                    if (!hasScheduleData) {
                        "Dodajte smjene u kalendar za odabrani mjesec. Procjena plaće tada će se izračunati automatski."
                    } else {
                        "Za odabranu godinu nema ugrađene službene osnovice. Procjena se zato ne prikazuje umjesto nagađanja."
                    },
                    color = RasporedColors.Muted,
                    fontSize = 11.sp
                )
                return@Column
            }

            Text(
                money(estimate.netMonthly),
                color = RasporedColors.Text,
                fontSize = 31.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "Procijenjeni neto za puni fond uz dosad upisane dodatke",
                color = RasporedColors.Muted,
                fontSize = 10.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PayrollMiniTile(
                    "Bruto 1",
                    money(estimate.grossOne),
                    RasporedColors.Day,
                    Modifier.weight(1f)
                )
                PayrollMiniTile(
                    "Dodaci",
                    money(estimate.premiumGross),
                    RasporedColors.Night,
                    Modifier.weight(1f)
                )
                PayrollMiniTile(
                    "Porez",
                    money(estimate.incomeTax),
                    RasporedColors.Sick,
                    Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PayrollMiniTile(
                    "MIO",
                    money(estimate.pensionFirstPillar + estimate.pensionSecondPillar),
                    RasporedColors.Morning,
                    Modifier.weight(1f)
                )
                PayrollMiniTile(
                    "Bruto 2",
                    money(estimate.grossTwo),
                    RasporedColors.Annual,
                    Modifier.weight(1f)
                )
                PayrollMiniTile(
                    "Sat bruto",
                    money(estimate.hourlyGross),
                    RasporedColors.Accent,
                    Modifier.weight(1f)
                )
            }

            if (estimate.projectedRegularMinutes > 0) {
                Surface(color = RasporedColors.Accent.copy(alpha = 0.09f),
                    shape = RoundedCornerShape(13.dp),
                    border = BorderStroke(1.dp, RasporedColors.Accent.copy(alpha = .30f))) {
                    Text(
                        "Nepotpun raspored: " + estimate.projectedRegularMinutes / 60 +
                            " h redovnog rada pretpostavljeno je do punog fonda, bez budućih dodataka. " +
                            "Ovaj iznos nije konačna plaća.",
                        modifier = Modifier.padding(11.dp),
                        color = RasporedColors.Text, fontSize = 11.sp
                    )
                }
            }
            if (schedule.count(month, "GO") > 0 && annualLeaveHourlyGross <= 0) {
                Text("Za godišnji odmor nije unesena bruto satnica po prosjeku. " +
                    "Procjena može odstupati od platne liste.",
                    color = Color(0xFFFFC66B), fontSize = 11.sp)
            }
            if (!profileConfirmed) {
                Text(
                    "Parametri obračuna nisu potvrđeni. Otvorite Postavke obračuna " +
                        "iznad ove kartice i potvrdite staž, djecu i ostale olakšice.",
                    color = Color(0xFFFFC66B), fontSize = 12.sp
                )
            }
            OutlinedButton(onClick = { showBreakdown = !showBreakdown },
                modifier = Modifier.fillMaxWidth()) {
                Text(if (showBreakdown) "Sakrij detalje obračuna" else "Prikaži detalje obračuna")
            }
            if (showBreakdown) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    BreakdownLine("Dodatak na staž (" + serviceYears * 0.5 + "%)",
                        money(estimate.seniorityGross))
                    BreakdownLine("Rad u turnusu (5%)", money(estimate.turnusPremiumGross))
                    BreakdownLine("Druga smjena (10%)", money(estimate.secondShiftPremiumGross))
                    BreakdownLine("Osobni odbitak", money(estimate.personalAllowance))
                    BreakdownLine("Stopa poreza · Rijeka",
                        if (month.year >= 2026) "20% / 25%" else "22% / 32%")
                    BreakdownLine("Koeficijent", "1,25")
                }
            }
            Text(
                "Izračun koristi fond, smjene, 6 vrsta dodataka i lokalno spremljene porezne postavke. " +
                    "GO plaćen po prosjeku, posebne naknade, putni troškovi i drugi odbici mogu promijeniti isplatu.",
                color = RasporedColors.Muted, fontSize = 11.sp
            )
            Text(
                "Orijentacijski izračun, nije službena platna lista.",
                color = Color(0xFFFFC66B),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PayrollMiniTile(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        color = RasporedColors.Card2,
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, tint.copy(alpha = .35f))
    ) {
        Column(Modifier.padding(9.dp)) {
            Text(label, color = RasporedColors.Muted, fontSize = 9.sp)
            Text(
                value,
                color = RasporedColors.Text,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                maxLines = 1
            )
        }
    }
}

private fun money(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("hr-HR")).format(value)

@Composable
private fun BreakdownLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = RasporedColors.Muted, fontSize = 12.sp)
        Text(value, color = RasporedColors.Text, fontSize = 12.sp,
            fontWeight = FontWeight.Bold)
    }
}
