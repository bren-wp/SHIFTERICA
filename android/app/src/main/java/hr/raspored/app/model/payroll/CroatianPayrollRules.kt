package hr.raspored.app.model.payroll

import java.time.YearMonth

enum class PayrollSector(val label: String) {
    HOSPITAL("Bolnica / javno zdravstvo"),
    PUBLIC_SERVICE("Javna služba"),
    STATE_SERVICE("Državna služba"),
    PRIVATE("Privatni sektor"),
    OTHER("Ostalo");

    companion object {
        fun fromLabel(label: String): PayrollSector =
            entries.firstOrNull { it.label == label } ?: HOSPITAL
    }
}

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

    val coefficientPresets = listOf(
        PayrollCoefficientPreset("Radnik III. vrste", 1.25),
        PayrollCoefficientPreset("Vozač sanitetskog prijevoza", 1.43),
        PayrollCoefficientPreset("Zdravstveni radnik III. vrste — 3", 1.55),
        PayrollCoefficientPreset("Zdravstveni radnik / sanitetski prijevoz", 1.64),
        PayrollCoefficientPreset("Zdravstveni radnik III. vrste — 2", 1.70),
        PayrollCoefficientPreset("Zdravstveni radnik III. vrste — 1", 1.78),
        PayrollCoefficientPreset("Prvostupnik — 3", 1.82),
        PayrollCoefficientPreset("Prvostupnik — 2", 1.87),
        PayrollCoefficientPreset("Prvostupnik — 1", 1.95)
    )

    fun officialBase(month: YearMonth, sector: PayrollSector): Double? {
        if (sector == PayrollSector.PRIVATE || sector == PayrollSector.OTHER) return null
        if (month.year != 2026) return null

        return when (month.monthValue) {
            in 1..3 -> 1_004.87
            in 4..7 -> 1_015.00
            in 8..11 -> 1_025.00
            12 -> 1_035.00
            else -> null
        }
    }

    fun premiumRates(sector: PayrollSector): PayrollPremiumRates? =
        when (sector) {
            PayrollSector.HOSPITAL -> PayrollPremiumRates(
                night = 0.50,
                overtime = 0.50,
                saturday = 0.25,
                sunday = 0.50,
                holiday = 1.50,
                secondShift = 0.10,
                turnus = 0.05
            )
            PayrollSector.PUBLIC_SERVICE -> PayrollPremiumRates(
                night = 0.40,
                overtime = 0.50,
                saturday = 0.25,
                sunday = 0.50,
                holiday = 1.50,
                secondShift = 0.10,
                turnus = 0.05
            )
            PayrollSector.STATE_SERVICE -> PayrollPremiumRates(
                night = 0.50,
                overtime = 0.50,
                saturday = 0.25,
                sunday = 0.50,
                holiday = 1.50,
                secondShift = 0.10,
                turnus = 0.05
            )
            PayrollSector.PRIVATE,
            PayrollSector.OTHER -> null
        }

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
