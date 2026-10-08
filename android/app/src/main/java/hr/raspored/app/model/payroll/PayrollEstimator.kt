package hr.raspored.app.model.payroll

import hr.raspored.app.model.WorkTimeSummary
import java.time.YearMonth
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
    val turnusApplied: Boolean,
    val seniorityGross: Double = 0.0,
    val turnusPremiumGross: Double = 0.0,
    val secondShiftPremiumGross: Double = 0.0,
    val projectedRegularMinutes: Int = 0
)

object PayrollEstimator {
    fun estimate(input: PayrollInput): PayrollEstimate? {
        val base = CroatianPayrollRules.officialBase(input.month) ?: return null
        val rates = CroatianPayrollRules.premiumRates()
        val fundMinutes = input.summary.fundMinutes
        if (fundMinutes <= 0) return null

        val coefficient = CroatianPayrollRules.DEFAULT_COEFFICIENT
        // Tarifni sat je osnovica × koeficijent / fond. Staž se obračunava
        // kao zaseban dodatak; ne smije povećavati satnicu svih dodataka.
        val hourly = base * coefficient / (fundMinutes / 60.0)

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
        // Za buduće/nepotpune rasporede ne prikazivati nedostajuće smjene
        // kao neopravdano neplaćene sate; pretpostavlja se puni ugovoreni fond.
        val projectedRegularMinutes = (
            fundMinutes - input.summary.regularMinutes - input.summary.paidAbsenceMinutes
        ).coerceAtLeast(0)
        val projectedRegularBase = amount(projectedRegularMinutes)
        val seniorityGross = (
            regularBase + overtimeBase + holidayCreditBase + projectedRegularBase
        ) * input.serviceYears.coerceIn(0, 60) * 0.005

        val baseGross = regularBase + overtimeBase + annualLeaveBase +
            sickLeaveBase + otherPaidBase + holidayCreditBase + projectedRegularBase

        val turnusApplied = input.hasDayNightTurnusPattern

        val nightPremium = amount(input.summary.nightMinutes, rates.night)
        val overtimePremium = amount(input.summary.overtimeMinutes, rates.overtime)
        val saturdayPremium = amount(input.summary.saturdayMinutes, rates.saturday)
        val sundayPremium = amount(input.summary.sundayMinutes, rates.sunday)
        val holidayPremium = amount(input.summary.holidayWorkedMinutes, rates.holiday)
        val turnusPremium = if (turnusApplied) {
            amount(input.summary.turnusMinutes, rates.turnus)
        } else 0.0
        // Obračuni potvrđuju da je druga smjena zaseban dodatak i kad postoji
        // turnus. Ove dvije stavke nisu međusobno isključive.
        val secondShiftPremium = amount(input.summary.secondShiftMinutes, rates.secondShift)

        val premiumGross = seniorityGross + nightPremium + overtimePremium + saturdayPremium +
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
        val (lowerRate, higherRate) = CroatianPayrollRules.rijekaTaxRates(input.month)
        val lowerTax = lowerBase * lowerRate
        val higherTax = higherBase * higherRate
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
            turnusApplied = turnusApplied,
            seniorityGross = seniorityGross,
            turnusPremiumGross = turnusPremium,
            secondShiftPremiumGross = secondShiftPremium,
            projectedRegularMinutes = projectedRegularMinutes
        )
    }
}
