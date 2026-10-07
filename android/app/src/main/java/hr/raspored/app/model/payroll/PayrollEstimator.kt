package hr.raspored.app.model.payroll

import java.time.YearMonth
import kotlin.math.max
import kotlin.math.min

data class PayrollInput(
    val month: YearMonth,
    val coefficient: Double,
    val completedYearsService: Int,
    val fundMinutes: Int,
    val regularWorkedMinutes: Int,
    val overtimeMinutes: Int,
    val annualLeaveMinutes: Int,
    val sickLeaveMinutes: Int,
    val otherPaidAbsenceMinutes: Int,
    val holidayCreditMinutes: Int,
    val nightMinutes: Int,
    val saturdayMinutes: Int,
    val sundayMinutes: Int,
    val holidayWorkedMinutes: Int,
    val secondShiftMinutes: Int,
    val children: Int = 0,
    val dependents: Int = 0,
    val disabilityAllowance: Double = 0.0,
    val birthYear: Int? = null,
    val taxRates: TaxRates = CroatianPayrollRules.rijeka2026TaxRates,
    val turnusEnabled: Boolean = false,
    val sickPayRate: Double = CroatianPayrollRules.DEFAULT_SICK_PAY_RATE,
    val annualLeaveFactor: Double = 1.0,
    val rates: PayrollRates = CroatianPayrollRules.publicServiceRates
)

data class PayrollEstimate(
    val officialBase: Double,
    val baseGrossForFullFund: Double,
    val seniorityAddition: Double,
    val hourlyGross: Double,
    val regularWorkGross: Double,
    val annualLeaveGross: Double,
    val sickLeaveGross: Double,
    val otherPaidAbsenceGross: Double,
    val holidayCreditGross: Double,
    val overtimeBaseGross: Double,
    val nightAddition: Double,
    val saturdayAddition: Double,
    val sundayAddition: Double,
    val holidayAddition: Double,
    val secondShiftAddition: Double,
    val turnusAddition: Double,
    val overtimeAddition: Double,
    val grossOne: Double,
    val pensionFirstPillar: Double,
    val pensionSecondPillar: Double,
    val personalAllowance: Double,
    val taxableIncome: Double,
    val monthlyIncomeTax: Double,
    val netBeforeAnnualYouthRelief: Double,
    val youthAnnualReliefFraction: Double,
    val estimatedYouthRefundShareForMonth: Double,
    val estimatedNetAfterAnnualYouthRelief: Double,
    val employerHealthContribution: Double,
    val grossTwo: Double
)

object PayrollEstimator {
    fun estimate(input: PayrollInput): PayrollEstimate? {
        val officialBase = CroatianPayrollRules.publicServiceBase(input.month) ?: return null
        if (input.fundMinutes <= 0) return null

        val coefficient = input.coefficient.coerceIn(0.1, 10.0)
        val years = input.completedYearsService.coerceIn(0, 60)
        val baseGross = officialBase * coefficient
        val seniority = baseGross * CroatianPayrollRules.SENIORITY_RATE_PER_YEAR * years
        val fullFundGross = baseGross + seniority
        val hourly = fullFundGross / (input.fundMinutes / 60.0)

        fun pay(minutes: Int, multiplier: Double = 1.0): Double =
            hourly * (minutes.coerceAtLeast(0) / 60.0) * multiplier

        val regularWorkGross = pay(input.regularWorkedMinutes)
        val annualLeaveGross = pay(input.annualLeaveMinutes, input.annualLeaveFactor.coerceAtLeast(0.0))
        val sickLeaveGross = pay(input.sickLeaveMinutes, input.sickPayRate.coerceIn(0.0, 1.0))
        val otherPaidAbsenceGross = pay(input.otherPaidAbsenceMinutes)
        val holidayCreditGross = pay(input.holidayCreditMinutes)
        val overtimeBaseGross = pay(input.overtimeMinutes)

        val nightAddition = pay(input.nightMinutes, input.rates.night)
        val saturdayAddition = pay(input.saturdayMinutes, input.rates.saturday)
        val sundayAddition = pay(input.sundayMinutes, input.rates.sunday)
        val holidayAddition = pay(input.holidayWorkedMinutes, input.rates.holiday)
        val overtimeAddition = pay(input.overtimeMinutes, input.rates.overtime)

        val actualWorkedMinutes =
            input.regularWorkedMinutes.coerceAtLeast(0) + input.overtimeMinutes.coerceAtLeast(0)
        val turnusAddition = if (input.turnusEnabled) {
            pay(actualWorkedMinutes, input.rates.turnus)
        } else 0.0
        val secondShiftAddition = if (input.turnusEnabled) {
            0.0
        } else {
            pay(input.secondShiftMinutes, input.rates.secondShift)
        }

        val grossOne = regularWorkGross + annualLeaveGross + sickLeaveGross +
            otherPaidAbsenceGross + holidayCreditGross + overtimeBaseGross +
            nightAddition + saturdayAddition + sundayAddition + holidayAddition +
            overtimeAddition + secondShiftAddition + turnusAddition

        val pensionFirst = grossOne * CroatianPayrollRules.PENSION_FIRST_PILLAR_RATE
        val pensionSecond = grossOne * CroatianPayrollRules.PENSION_SECOND_PILLAR_RATE
        val pensionTotal = pensionFirst + pensionSecond

        val allowance = CroatianPayrollRules.personalAllowance(
            children = input.children,
            dependents = input.dependents,
            disabilityAllowance = input.disabilityAllowance
        )
        val taxable = max(0.0, grossOne - pensionTotal - allowance)

        val lowerTaxBase = min(taxable, CroatianPayrollRules.MONTHLY_LOWER_TAX_THRESHOLD)
        val higherTaxBase = max(0.0, taxable - CroatianPayrollRules.MONTHLY_LOWER_TAX_THRESHOLD)
        val lowerTax = lowerTaxBase * input.taxRates.lowerPercent.coerceIn(0.0, 100.0) / 100.0
        val higherTax = higherTaxBase * input.taxRates.higherPercent.coerceIn(0.0, 100.0) / 100.0
        val tax = lowerTax + higherTax

        val net = max(0.0, grossOne - pensionTotal - tax)
        val youthFraction = CroatianPayrollRules.youthAnnualReliefFraction(
            input.month.year,
            input.birthYear
        )
        // Zakonska olakšica za mlade odnosi se na godišnji porez i na dio
        // osnovice koji se oporezuje nižom stopom. Ovdje prikazujemo samo
        // orijentacijsku mjesečnu komponentu budućeg godišnjeg povrata.
        val youthRefundShare = lowerTax * youthFraction
        val employerHealth = grossOne * CroatianPayrollRules.EMPLOYER_HEALTH_RATE

        return PayrollEstimate(
            officialBase = officialBase,
            baseGrossForFullFund = fullFundGross,
            seniorityAddition = seniority,
            hourlyGross = hourly,
            regularWorkGross = regularWorkGross,
            annualLeaveGross = annualLeaveGross,
            sickLeaveGross = sickLeaveGross,
            otherPaidAbsenceGross = otherPaidAbsenceGross,
            holidayCreditGross = holidayCreditGross,
            overtimeBaseGross = overtimeBaseGross,
            nightAddition = nightAddition,
            saturdayAddition = saturdayAddition,
            sundayAddition = sundayAddition,
            holidayAddition = holidayAddition,
            secondShiftAddition = secondShiftAddition,
            turnusAddition = turnusAddition,
            overtimeAddition = overtimeAddition,
            grossOne = grossOne,
            pensionFirstPillar = pensionFirst,
            pensionSecondPillar = pensionSecond,
            personalAllowance = allowance,
            taxableIncome = taxable,
            monthlyIncomeTax = tax,
            netBeforeAnnualYouthRelief = net,
            youthAnnualReliefFraction = youthFraction,
            estimatedYouthRefundShareForMonth = youthRefundShare,
            estimatedNetAfterAnnualYouthRelief = net + youthRefundShare,
            employerHealthContribution = employerHealth,
            grossTwo = grossOne + employerHealth
        )
    }
}
