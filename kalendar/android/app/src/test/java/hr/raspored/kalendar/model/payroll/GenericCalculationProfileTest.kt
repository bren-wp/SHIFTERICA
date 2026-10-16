package hr.raspored.kalendar.model.payroll

import hr.raspored.kalendar.model.WorkTimeSummary
import java.time.YearMonth
import org.junit.Assert.*
import org.junit.Test

class GenericCalculationProfileTest {
    private fun input(bod: Double = 0.0, base: Double = 0.0) = PayrollInput(
        month = YearMonth.of(2026, 10),
        summary = WorkTimeSummary(
            workedMinutes = 168 * 60,
            regularMinutes = 168 * 60,
            fundMinutes = 168 * 60,
            overtimeMinutes = 0,
            paidAbsenceMinutes = 0,
            holidayCreditMinutes = 0,
            creditedMinutes = 168 * 60,
            workedShiftCount = 14,
            dayMinutes = 168 * 60,
            nightMinutes = 0,
            saturdayMinutes = 0,
            sundayMinutes = 0,
            holidayWorkedMinutes = 0,
            secondShiftMinutes = 0
        ),
        annualLeaveMinutes = 0,
        sickLeaveMinutes = 0,
        otherPaidAbsenceMinutes = 0,
        hasDayNightTurnusPattern = false,
        coefficient = bod,
        userMonthlyBase = base
    )

    @Test fun noPrivateDefaultsInIndependentCalendar() {
        assertNull(PayrollEstimator.estimate(input()))
        assertNull(PayrollEstimator.estimate(input(bod = 1.6)))
        assertNull(PayrollEstimator.estimate(input(base = 1000.0)))
        assertNull(PayrollEstimator.estimate(input(bod = Double.NaN, base = 1000.0)))
    }

    @Test fun computesAfterExplicitPositiveUserInputs() {
        val result = PayrollEstimator.estimate(input(bod = 1.6, base = 1000.0))
        assertNotNull(result)
        assertEquals(1.6, result!!.coefficient, 0.000001)
        assertEquals(1000.0, result.officialBase, 0.000001)
        assertEquals(1600.0 / 168.0, result.hourlyGross, 0.000001)
        assertTrue(result.netMonthly.isFinite())
    }
}
