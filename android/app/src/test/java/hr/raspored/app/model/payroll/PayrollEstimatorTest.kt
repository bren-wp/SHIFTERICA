package hr.raspored.app.model.payroll

import hr.raspored.app.model.WorkTimeSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.YearMonth

class PayrollEstimatorTest {
    private val fullFund = WorkTimeSummary(
        workedMinutes = 168 * 60,
        regularMinutes = 168 * 60,
        fundMinutes = 168 * 60,
        overtimeMinutes = 0,
        paidAbsenceMinutes = 0,
        holidayCreditMinutes = 0,
        creditedMinutes = 168 * 60,
        workedShiftCount = 14,
        dayMinutes = 112 * 60,
        nightMinutes = 56 * 60,
        saturdayMinutes = 24 * 60,
        sundayMinutes = 24 * 60,
        holidayWorkedMinutes = 0,
        secondShiftMinutes = 70 * 60
    )

    @Test
    fun payrollUsesLockedHospitalDefaults() {
        val result = PayrollEstimator.estimate(
            PayrollInput(
                month = YearMonth.of(2026, 8),
                summary = fullFund,
                annualLeaveMinutes = 0,
                sickLeaveMinutes = 0,
                otherPaidAbsenceMinutes = 0,
                hasDayNightTurnusPattern = true
            )
        )

        assertNotNull(result)
        assertEquals(1025.0, result!!.officialBase, 0.001)
        assertEquals(1.25, result.coefficient, 0.001)
        assertEquals(600.0, result.personalAllowance, 0.001)
        assertEquals(true, result.turnusApplied)
        assertEquals(0.50, CroatianPayrollRules.premiumRates().night, 0.001)
    }

    @Test
    fun fixedBaseIsUsedForEverySupportedMonthIn2026() {
        (1..12).forEach { month ->
            assertEquals(
                1025.0,
                CroatianPayrollRules.officialBase(YearMonth.of(2026, month))!!,
                0.001
            )
        }
    }

    @Test
    fun childAndYouthRulesRemainAvailableWithoutBeingRequiredByUi() {
        assertEquals(600.0, CroatianPayrollRules.personalAllowance(), 0.001)
        assertEquals(900.0, CroatianPayrollRules.personalAllowance(children = 1), 0.001)
        assertEquals(1320.0, CroatianPayrollRules.personalAllowance(children = 2), 0.001)

        assertEquals(1.0, CroatianPayrollRules.youthAnnualReliefFraction(2026, 2001), 0.001)
        assertEquals(0.5, CroatianPayrollRules.youthAnnualReliefFraction(2026, 1996), 0.001)
        assertEquals(0.0, CroatianPayrollRules.youthAnnualReliefFraction(2026, 1995), 0.001)
    }
}
