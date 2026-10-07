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
