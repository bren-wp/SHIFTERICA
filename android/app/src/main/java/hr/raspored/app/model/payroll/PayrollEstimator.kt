package hr.raspored.app.model.payroll

import hr.raspored.app.model.WorkTimeSummary
import java.time.YearMonth
import kotlin.math.max
import kotlin.math.min

data class PayrollInput(
    val month: YearMonth,
    val sector: PayrollSector,
    val coefficient: Double,
    val summary: WorkTimeSummary,
    val annualLeaveMinutes: Int,
    val sickLeaveMinutes: Int,
    val otherPaidAbsenceMinutes: Int,
    val hasDayNightTurnusPattern: Boolean,
    val serviceYears: Int = 0,
    val children: Int = 0,
    val dependents: Int = 0,
    val birthYear: Int? = null
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
    val turnusApplied: Boolean
)

object PayrollEstimator {
    fun estimate(input: PayrollInput): PayrollEstimate? {
        val base = CroatianPayrollRules.officialBase(input.month, input.sector) ?: return null
        val rates = CroatianPayrollRules.premiumRates(input.sector) ?: return null
        val fundMinutes = input.summary.fundMinutes
        if (fundMinutes <= 0) return null

        val coefficient = input.coefficient.coerceIn(0.5, 8.0)
        val serviceFactor = 1.0 + input.serviceYears.coerceIn(0, 60) * 0.005
        val fullFundGross = base * coefficient * serviceFactor
        val hourly = fullFundGross / (fundMinutes / 60.0)

        fun amount(minutes: Int, factor: Double = 1.0): Double =
            hourly * (minutes.coerceAtLeast(0) / 60.0) * factor

        val holidayCredit = input.summary.holidayCreditMinutes
        val regularBase = amount(input.summary.regularMinutes)
        val overtimeBase = amount(input.summary.overtimeMinutes)
        val annualLeaveBase = amount(input.annualLeaveMinutes)
        val sickLeaveBase = amount(
            input.sickLeaveMinutes,
            CroatianPayrollRules.SICK_PAY_DEFAULT_RATE
        )
        val otherPaidBase = amount(input.otherPaidAbsenceMinutes)
        val holidayCreditBase = amount(holidayCredit)

        val baseGross = regularBase + overtimeBase + annualLeaveBase +
            sickLeaveBase + otherPaidBase + holidayCreditBase

        val turnusApplied = input.sector == PayrollSector.HOSPITAL &&
            input.hasDayNightTurnusPattern

        val nightPremium = amount(input.summary.nightMinutes, rates.night)
        val overtimePremium = amount(input.summary.overtimeMinutes, rates.overtime)
        val saturdayPremium = amount(input.summary.saturdayMinutes, rates.saturday)
        val sundayPremium = amount(input.summary.sundayMinutes, rates.sunday)
        val holidayPremium = amount(input.summary.holidayWorkedMinutes, rates.holiday)
        val turnusPremium = if (turnusApplied) {
            amount(input.summary.workedMinutes, rates.turnus)
        } else 0.0
        val secondShiftPremium = if (turnusApplied) {
            0.0
        } else {
            amount(input.summary.secondShiftMinutes, rates.secondShift)
        }

        val premiumGross = nightPremium + overtimePremium + saturdayPremium +
            sundayPremium + holidayPremium + turnusPremium + secondShiftPremium
        val grossOne = baseGross + premiumGross

        val pensionOne = grossOne * CroatianPayrollRules.PENSION_FIRST_PILLAR_RATE
        val pensionTwo = grossOne * CroatianPayrollRules.PENSION_SECOND_PILLAR_RATE
        val pensionTotal = pensionOne + pensionTwo

        val allowance = CroatianPayrollRules.personalAllowance(
            children = input.children,
            dependents = input.dependents
        )
        val taxable = max(0.0, grossOne - pensionTotal - allowance)
        val lowerBase = min(taxable, CroatianPayrollRules.MONTHLY_HIGHER_RATE_THRESHOLD)
        val higherBase = max(0.0, taxable - CroatianPayrollRules.MONTHLY_HIGHER_RATE_THRESHOLD)
        val lowerTax = lowerBase * CroatianPayrollRules.RIJEKA_LOWER_TAX_RATE
        val higherTax = higherBase * CroatianPayrollRules.RIJEKA_HIGHER_TAX_RATE
        val tax = lowerTax + higherTax
        val net = max(0.0, grossOne - pensionTotal - tax)

        val youthFraction = CroatianPayrollRules.youthAnnualReliefFraction(
            taxYear = input.month.year,
            birthYear = input.birthYear
        )
        val youthRefundShare = lowerTax * youthFraction
        val employerHealth = grossOne * CroatianPayrollRules.EMPLOYER_HEALTH_RATE

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
            netMonthly = net,
            youthAnnualReliefFraction = youthFraction,
            estimatedYouthRefundShare = youthRefundShare,
            employerHealthContribution = employerHealth,
            grossTwo = grossOne + employerHealth,
            turnusApplied = turnusApplied
        )
    }
}
