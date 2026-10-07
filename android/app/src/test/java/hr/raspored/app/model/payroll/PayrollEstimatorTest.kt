package hr.raspored.app.model.payroll

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.YearMonth

class PayrollEstimatorTest {
    @Test
    fun official2026BasesFollowGovernmentDecision() {
        assertEquals(1004.87, CroatianPayrollRules.publicServiceBase(YearMonth.of(2026, 1))!!, 0.001)
        assertEquals(1015.00, CroatianPayrollRules.publicServiceBase(YearMonth.of(2026, 4))!!, 0.001)
        assertEquals(1025.00, CroatianPayrollRules.publicServiceBase(YearMonth.of(2026, 8))!!, 0.001)
        assertEquals(1035.00, CroatianPayrollRules.publicServiceBase(YearMonth.of(2026, 12))!!, 0.001)
        assertNull(CroatianPayrollRules.publicServiceBase(YearMonth.of(2027, 1)))
    }

    @Test
    fun childAllowanceUsesStatutoryProgressiveAmounts() {
        assertEquals(600.0, CroatianPayrollRules.personalAllowance(children = 0), 0.001)
        assertEquals(900.0, CroatianPayrollRules.personalAllowance(children = 1), 0.001)
        assertEquals(1320.0, CroatianPayrollRules.personalAllowance(children = 2), 0.001)
        assertEquals(1920.0, CroatianPayrollRules.personalAllowance(children = 3), 0.001)
    }

    @Test
    fun youthReliefMatches2026BirthYearTable() {
        assertEquals(0.5, CroatianPayrollRules.youthAnnualReliefFraction(2026, 1996), 0.001)
        assertEquals(0.5, CroatianPayrollRules.youthAnnualReliefFraction(2026, 2000), 0.001)
        assertEquals(1.0, CroatianPayrollRules.youthAnnualReliefFraction(2026, 2001), 0.001)
        assertEquals(0.0, CroatianPayrollRules.youthAnnualReliefFraction(2026, 1995), 0.001)
    }

    @Test
    fun radnikIIIBaseAndSeniorityAreAppliedBeforeHourlyRate() {
        val estimate = PayrollEstimator.estimate(
            PayrollInput(
                month = YearMonth.of(2026, 8),
                coefficient = 1.25,
                completedYearsService = 12,
                fundMinutes = 168 * 60,
                regularWorkedMinutes = 168 * 60,
                overtimeMinutes = 0,
                annualLeaveMinutes = 0,
                sickLeaveMinutes = 0,
                otherPaidAbsenceMinutes = 0,
                holidayCreditMinutes = 0,
                nightMinutes = 0,
                saturdayMinutes = 0,
                sundayMinutes = 0,
                holidayWorkedMinutes = 0,
                secondShiftMinutes = 0
            )
        )
        assertNotNull(estimate)
        val base = 1025.0 * 1.25
        assertEquals(base * 0.06, estimate!!.seniorityAddition, 0.01)
        assertEquals(base * 1.06, estimate.grossOne, 0.01)
    }

    @Test
    fun turnusExcludesSecondShiftAdditionButOtherAdditionsAccumulate() {
        val input = PayrollInput(
            month = YearMonth.of(2026, 8),
            coefficient = 1.25,
            completedYearsService = 0,
            fundMinutes = 168 * 60,
            regularWorkedMinutes = 12 * 60,
            overtimeMinutes = 0,
            annualLeaveMinutes = 0,
            sickLeaveMinutes = 0,
            otherPaidAbsenceMinutes = 0,
            holidayCreditMinutes = 0,
            nightMinutes = 8 * 60,
            saturdayMinutes = 12 * 60,
            sundayMinutes = 0,
            holidayWorkedMinutes = 0,
            secondShiftMinutes = 5 * 60,
            turnusEnabled = true
        )
        val estimate = PayrollEstimator.estimate(input)!!
        assertEquals(0.0, estimate.secondShiftAddition, 0.001)
        val expectedTurnus = estimate.hourlyGross * 12.0 * 0.05
        assertEquals(expectedTurnus, estimate.turnusAddition, 0.01)
        assertEquals(estimate.hourlyGross * 8.0 * 0.50, estimate.nightAddition, 0.01)
        assertEquals(estimate.hourlyGross * 12.0 * 0.25, estimate.saturdayAddition, 0.01)
    }
}
