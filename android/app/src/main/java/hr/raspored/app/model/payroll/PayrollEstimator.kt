package hr.raspored.app.model.payroll

import hr.raspored.app.model.WorkTimeSummary
import java.time.YearMonth
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min

data class PayrollInput(
    val month: YearMonth,
    val summary: WorkTimeSummary,
    val annualLeaveMinutes: Int,
    val sickLeaveMinutes: Int,
    val otherPaidAbsenceMinutes: Int,
    val hasDayNightTurnusPattern: Boolean,
    val serviceYears: Int = 0,
    val children: Int = 0,
    val dependents: Int = 0,
    val birthYear: Int? = null,
    val annualLeaveAverageHourlyGross: Double? = null
)

data class PayrollEstimate(
    val officialBase: Double,
    val coefficient: Double,
    val hourlyGross: Double,
    val baseGross: Double,
    val premiumGross: Double,
    val grossOne: Double,
    val pensionFirstPillar: Double,
    val pensionSecondPillar: Double,
    val personalAllowance: Double,
    val taxableIncome: Double,
    val incomeTax: Double,
    val netMonthly: Double,
    val youthAnnualReliefFraction: Double,
    val estimatedYouthRefundShare: Double,
    val employerHealthContribution: Double,
    val grossTwo: Double,
    val turnusApplied: Boolean,
    val seniorityGross: Double = 0.0,
    val turnusPremiumGross: Double = 0.0,
    val secondShiftPremiumGross: Double = 0.0,
    val projectedRegularMinutes: Int = 0
)

object PayrollEstimator {
    /** Do not hide genuine midnight carryovers; do not estimate empty schedules. */
    fun hasRecordedActivity(monthHasEntries: Boolean, workedMinutes: Int): Boolean =
        monthHasEntries || workedMinutes > 0

    // Payslips calculate mandatory deductions and tax in euro cents.
    // Round each monetary line, not just the final UI-formatted value.
    private fun cents(value: Double): Double =
        BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toDouble()

    fun estimate(input: PayrollInput): PayrollEstimate? {
        val base = CroatianPayrollRules.officialBase(input.month) ?: return null
        val rates = CroatianPayrollRules.premiumRates()
        val fundMinutes = input.summary.fundMinutes
        if (fundMinutes <= 0) return null

        // Observed hospital pay slips consistently pay next month.
        // Income tax follows payment date; wage base still follows worked month.
        val paymentMonth = input.month.plusMonths(1)
        val coefficient = CroatianPayrollRules.DEFAULT_COEFFICIENT
        // Base tariff remains separate from the seniority line. Under
        // TKU art. 59, shift/overtime premiums use tariff increased by seniority.
        val hourly = base * coefficient / (fundMinutes / 60.0)
        val seniorityRate = input.serviceYears.coerceIn(0, 60) * 0.005

        fun amount(minutes: Int, factor: Double = 1.0): Double =
            hourly * (minutes.coerceAtLeast(0) / 60.0) * factor

        fun premiumAmount(minutes: Int, factor: Double): Double =
            amount(minutes, factor) * (1.0 + seniorityRate)

        val holidayCredit = input.summary.holidayCreditMinutes
        val regularBase = amount(input.summary.regularMinutes)
        val overtimeBase = amount(input.summary.overtimeMinutes)
        val annualLeaveBase = input.annualLeaveAverageHourlyGross
            ?.takeIf { it.isFinite() && it > 0.0 && it <= 1000.0 }
            ?.let { it * input.annualLeaveMinutes.coerceAtLeast(0) / 60.0 }
            ?: amount(input.annualLeaveMinutes)
        val sickLeaveBase = amount(
            input.sickLeaveMinutes,
            CroatianPayrollRules.SICK_PAY_DEFAULT_RATE
        )
        val otherPaidBase = amount(input.otherPaidAbsenceMinutes)
        val holidayCreditBase = amount(holidayCredit)
        // Za buduće/nepotpune rasporede ne prikazivati nedostajuće smjene
        // kao neopravdano neplaćene sate; pretpostavlja se puni ugovoreni fond.
        val projectedRegularMinutes = (
            fundMinutes - input.summary.regularMinutes - input.summary.paidAbsenceMinutes
        ).coerceAtLeast(0)
        val projectedRegularBase = amount(projectedRegularMinutes)
        val seniorityGross = (
            regularBase + overtimeBase + holidayCreditBase + projectedRegularBase
        ) * seniorityRate

        val baseGross = cents(
            cents(regularBase) + cents(overtimeBase) + cents(annualLeaveBase) +
                cents(sickLeaveBase) + cents(otherPaidBase) +
                cents(holidayCreditBase) + cents(projectedRegularBase)
        )

        val turnusApplied = input.hasDayNightTurnusPattern

        val nightPremium = premiumAmount(input.summary.nightMinutes, rates.night)
        val overtimePremium = premiumAmount(input.summary.overtimeMinutes, rates.overtime)
        val saturdayPremium = premiumAmount(input.summary.saturdayMinutes, rates.saturday)
        val sundayPremium = premiumAmount(input.summary.sundayMinutes, rates.sunday)
        val holidayPremium = premiumAmount(input.summary.holidayWorkedMinutes, rates.holiday)
        val turnusPremium = if (turnusApplied) {
            premiumAmount(input.summary.turnusMinutes, rates.turnus)
        } else 0.0
        // Obračuni potvrđuju da je druga smjena zaseban dodatak i kad postoji
        // turnus. Ove dvije stavke nisu međusobno isključive.
        val secondShiftPremium = premiumAmount(input.summary.secondShiftMinutes, rates.secondShift)

        val premiumGross = cents(
            cents(seniorityGross) + cents(nightPremium) + cents(overtimePremium) +
                cents(saturdayPremium) + cents(sundayPremium) +
                cents(holidayPremium) + cents(turnusPremium) + cents(secondShiftPremium)
        )
        val grossOne = cents(baseGross + premiumGross)

        val pensionOne = cents(grossOne * CroatianPayrollRules.PENSION_FIRST_PILLAR_RATE)
        val pensionTwo = cents(grossOne * CroatianPayrollRules.PENSION_SECOND_PILLAR_RATE)
        val pensionTotal = cents(pensionOne + pensionTwo)

        val allowance = CroatianPayrollRules.personalAllowance(
            children = input.children,
            dependents = input.dependents,
            taxYear = paymentMonth.year
        )
        val taxable = cents(max(0.0, grossOne - pensionTotal - allowance))
        val threshold = CroatianPayrollRules.higherRateThreshold(paymentMonth.year)
        val lowerBase = min(taxable, threshold)
        val higherBase = max(0.0, taxable - threshold)
        val (lowerRate, higherRate) = CroatianPayrollRules.rijekaTaxRates(paymentMonth)
        val lowerTax = cents(lowerBase * lowerRate)
        val higherTax = cents(higherBase * higherRate)
        val tax = cents(lowerTax + higherTax)
        // Neto plaća PRIJE osobnih obustava. Ovrhe, krediti, administrativne
        // zabrane i druge obustave nisu dio procjene niti se oduzimaju.
        // MIO i porez su zakonska davanja i moraju ostati u formuli.
        val netBeforeWithholdings = cents(max(0.0, grossOne - pensionTotal - tax))

        val youthFraction = CroatianPayrollRules.youthAnnualReliefFraction(
            taxYear = paymentMonth.year,
            birthYear = input.birthYear
        )
        val youthRefundShare = cents(lowerTax * youthFraction)
        val employerHealth = cents(grossOne * CroatianPayrollRules.EMPLOYER_HEALTH_RATE)

        return PayrollEstimate(
            officialBase = base,
            coefficient = coefficient,
            hourlyGross = hourly,
            baseGross = baseGross,
            premiumGross = premiumGross,
            grossOne = grossOne,
            pensionFirstPillar = pensionOne,
            pensionSecondPillar = pensionTwo,
            personalAllowance = allowance,
            taxableIncome = taxable,
            incomeTax = tax,
            netMonthly = netBeforeWithholdings,
            youthAnnualReliefFraction = youthFraction,
            estimatedYouthRefundShare = youthRefundShare,
            employerHealthContribution = employerHealth,
            grossTwo = cents(grossOne + employerHealth),
            turnusApplied = turnusApplied,
            seniorityGross = cents(seniorityGross),
            turnusPremiumGross = cents(turnusPremium),
            secondShiftPremiumGross = cents(secondShiftPremium),
            projectedRegularMinutes = projectedRegularMinutes
        )
    }
}
