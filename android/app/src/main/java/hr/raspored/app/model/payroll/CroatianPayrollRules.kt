package hr.raspored.app.model.payroll

import java.time.YearMonth

data class TaxRates(
    val lowerPercent: Double,
    val higherPercent: Double
)

data class PayrollRates(
    val night: Double = 0.50,
    val overtime: Double = 0.50,
    val saturday: Double = 0.25,
    val sunday: Double = 0.50,
    val holiday: Double = 1.50,
    val secondShift: Double = 0.10,
    val turnus: Double = 0.05
)

data class JobCoefficientPreset(
    val id: String,
    val label: String,
    val coefficient: Double
)

object CroatianPayrollRules {
    const val BASIC_PERSONAL_ALLOWANCE = 600.0
    const val MONTHLY_LOWER_TAX_THRESHOLD = 5_000.0
    const val SENIORITY_RATE_PER_YEAR = 0.005
    const val PENSION_TOTAL_RATE = 0.20
    const val PENSION_FIRST_PILLAR_RATE = 0.15
    const val PENSION_SECOND_PILLAR_RATE = 0.05
    const val EMPLOYER_HEALTH_RATE = 0.165
    const val DEFAULT_SICK_PAY_RATE = 0.85

    val publicServiceRates = PayrollRates()

    val coefficientPresets = listOf(
        JobCoefficientPreset("worker-iii", "Radnik III. vrste", 1.25),
        JobCoefficientPreset("caregiver", "Njegovatelj", 1.35),
        JobCoefficientPreset("hospital-orderly", "Bolničar", 1.35),
        JobCoefficientPreset("medical-driver", "Vozač sanitetskog prijevoza", 1.43),
        JobCoefficientPreset("health-worker-iii-3", "Zdravstveni radnik III. vrste — 3", 1.55),
        JobCoefficientPreset("health-worker-iii-2", "Zdravstveni radnik III. vrste — 2", 1.70),
        JobCoefficientPreset("health-worker-iii-1", "Zdravstveni radnik III. vrste — 1", 1.78),
        JobCoefficientPreset("bachelor-nurse-3", "Medicinska sestra/tehničar prvostupnik — 3", 1.82),
        JobCoefficientPreset("bachelor-nurse-2", "Medicinska sestra/tehničar prvostupnik — 2", 1.87),
        JobCoefficientPreset("bachelor-nurse-1", "Medicinska sestra/tehničar prvostupnik — 1", 1.95)
    )

    fun publicServiceBase(month: YearMonth): Double? {
        if (month.year != 2026) return null
        return when {
            month.monthValue <= 3 -> 1_004.87
            month.monthValue <= 7 -> 1_015.00
            month.monthValue <= 11 -> 1_025.00
            else -> 1_035.00
        }
    }

    fun childAllowance(children: Int): Double {
        val increments = listOf(300.0, 420.0, 600.0, 840.0, 1_140.0, 1_500.0, 1_920.0, 2_400.0, 2_940.0)
        return (0 until children.coerceIn(0, increments.size)).sumOf { increments[it] }
    }

    fun personalAllowance(
        children: Int,
        dependents: Int = 0,
        disabilityAllowance: Double = 0.0
    ): Double = BASIC_PERSONAL_ALLOWANCE +
        childAllowance(children) +
        dependents.coerceAtLeast(0) * 300.0 +
        disabilityAllowance.coerceAtLeast(0.0)

    /**
     * Godišnja porezna olakšica za mlade primjenjuje se u godišnjem obračunu.
     * 1.0 = 100% nižeg razreda poreza na plaću, 0.5 = 50%, 0.0 = bez olakšice.
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

    val rijeka2026TaxRates = TaxRates(lowerPercent = 20.0, higherPercent = 25.0)
}
