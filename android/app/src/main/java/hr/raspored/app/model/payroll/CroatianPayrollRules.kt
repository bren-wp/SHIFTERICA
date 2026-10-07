package hr.raspored.app.model.payroll

import java.time.YearMonth

data class PayrollPremiumRates(
    val night: Double,
    val overtime: Double,
    val saturday: Double,
    val sunday: Double,
    val holiday: Double,
    val secondShift: Double,
    val turnus: Double
)

data class PayrollCoefficientPreset(
    val label: String,
    val value: Double
)

object CroatianPayrollRules {
    const val BASIC_PERSONAL_ALLOWANCE = 600.0
    const val MONTHLY_HIGHER_RATE_THRESHOLD = 5_000.0
    const val PENSION_FIRST_PILLAR_RATE = 0.15
    const val PENSION_SECOND_PILLAR_RATE = 0.05
    const val EMPLOYER_HEALTH_RATE = 0.165
    const val SICK_PAY_DEFAULT_RATE = 0.85

    const val RIJEKA_LOWER_TAX_RATE = 0.20
    const val RIJEKA_HIGHER_TAX_RATE = 0.25
    const val DEFAULT_COEFFICIENT = 1.25
    const val DEFAULT_OFFICIAL_BASE = 1_025.00

    fun officialBase(month: YearMonth): Double? =
        if (month.year == 2026) DEFAULT_OFFICIAL_BASE else null

    fun premiumRates(): PayrollPremiumRates = PayrollPremiumRates(
        night = 0.50,
        overtime = 0.50,
        saturday = 0.25,
        sunday = 0.50,
        holiday = 1.50,
        secondShift = 0.10,
        turnus = 0.05
    )

    fun personalAllowance(children: Int = 0, dependents: Int = 0): Double {
        val childIncrements = listOf(
            300.0, 420.0, 600.0, 840.0, 1_140.0,
            1_500.0, 1_920.0, 2_400.0, 2_940.0
        )
        val childPart = (0 until children.coerceIn(0, childIncrements.size))
            .sumOf { childIncrements[it] }

        return BASIC_PERSONAL_ALLOWANCE +
            childPart +
            dependents.coerceAtLeast(0) * 300.0
    }

    /**
     * Godišnje umanjenje poreza za mlade. Ne umanjuje mjesečni predujam.
     */
    fun youthAnnualReliefFraction(taxYear: Int, birthYear: Int?): Double {
        if (birthYear == null || birthYear <= 0 || birthYear > taxYear) return 0.0
        val age = taxYear - birthYear
        return when {
            age <= 25 -> 1.0
            age in 26..30 -> 0.5
            else -> 0.0
        }
    }
}
