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

    /** Tax rates are selected by payment month, not by months worked. */
    fun rijekaTaxRates(month: YearMonth): Pair<Double, Double> =
        when {
            month.year <= 2024 -> 0.224 to 0.336
            month.year == 2025 -> 0.22 to 0.32
            else -> RIJEKA_LOWER_TAX_RATE to RIJEKA_HIGHER_TAX_RATE
        }

    /** Monthly 2024 high-rate threshold: 50,400/12; from 2025: 60,000/12. */
    fun higherRateThreshold(taxYear: Int): Double =
        if (taxYear <= 2024) 4_200.0 else MONTHLY_HIGHER_RATE_THRESHOLD

    /**
     * Službene osnovice javnih službi:
     * - 2024: NN 29/2024, article 53
     * - 2025: NN 155/2024
     * - 2026: NN 11/2026
     *
     * Iznosi su dodatno provjereni prema dostavljenim obračunskim ispravama.
     */
    fun officialBase(month: YearMonth): Double? =
        when (month.year) {
            2024 -> 947.18
            2025 -> when (month.monthValue) {
                1 -> 947.18
                in 2..8 -> 975.60
                in 9..12 -> 1_004.87
                else -> null
            }
            2026 -> when (month.monthValue) {
                in 1..3 -> 1_004.87
                in 4..7 -> 1_015.00
                in 8..11 -> 1_025.00
                12 -> 1_035.00
                else -> null
            }
            else -> null
        }

    fun premiumRates() = PayrollPremiumRates(
        night = 0.50,
        overtime = 0.50,
        saturday = 0.25,
        sunday = 0.50,
        holiday = 1.50,
        secondShift = 0.10,
        turnus = 0.05
    )

    fun personalAllowance(
        children: Int = 0,
        dependents: Int = 0,
        taxYear: Int = 2026
    ): Double {
        val childIncrements = listOf(
            300.0,
            420.0,
            600.0,
            840.0,
            1_140.0,
            1_500.0,
            1_920.0,
            2_400.0,
            2_940.0
        )
        val childPart = (0 until children.coerceIn(0, childIncrements.size))
            .sumOf { childIncrements[it] }

        // Starting 2025 the monthly base rose from EUR 560 to EUR 600.
        // Multiply all dependency increments by the same statutory factor.
        val factor = if (taxYear <= 2024) 560.0 / 600.0 else 1.0
        return (BASIC_PERSONAL_ALLOWANCE +
            childPart +
            dependents.coerceAtLeast(0) * 300.0) * factor
    }

    /**
     * Godišnje umanjenje poreza za mlade. Ne umanjuje mjesečni predujam.
     */
    fun youthAnnualReliefFraction(taxYear: Int, birthYear: Int?): Double {
        if (birthYear == null || birthYear <= 0 || birthYear > taxYear) {
            return 0.0
        }

        val age = taxYear - birthYear
        return when {
            age <= 25 -> 1.0
            age in 26..30 -> 0.5
            else -> 0.0
        }
    }
}
