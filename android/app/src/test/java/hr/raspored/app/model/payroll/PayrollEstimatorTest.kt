package hr.raspored.app.model.payroll

import hr.raspored.app.model.WorkTimeSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun netSalaryIgnoresAllPersonalWithholdings() {
        // Administrative bans, loans and garnishments affect the transfer,
        // not earned net before personal withholdings.
        val result = PayrollEstimator.estimate(PayrollInput(
            month = YearMonth.of(2026, 10),
            summary = fullFund.copy(turnusMinutes = 120 * 60),
            annualLeaveMinutes = 0,
            sickLeaveMinutes = 0,
            otherPaidAbsenceMinutes = 0,
            hasDayNightTurnusPattern = true,
            serviceYears = 12,
            children = 2
        ))!!
        val mandatoryContributions =
            result.pensionFirstPillar + result.pensionSecondPillar
        val expectedNetBeforeWithholdings =
            result.grossOne - mandatoryContributions - result.incomeTax

        assertEquals(
            expectedNetBeforeWithholdings.coerceAtLeast(0.0),
            result.netMonthly,
            0.000001
        )

        val externalPayslipWithholding = 300.0
        val hypotheticalBankTransfer = result.netMonthly - externalPayslipWithholding
        org.junit.Assert.assertTrue(result.netMonthly > hypotheticalBankTransfer)
    }

    @Test
    fun payrollUsesHospitalDefaultsForAugust2026() {
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
        assertEquals(1_025.0, result!!.officialBase, 0.001)
        assertEquals(1.25, result.coefficient, 0.001)
        assertEquals(600.0, result.personalAllowance, 0.001)
        assertEquals(true, result.turnusApplied)
        assertEquals(0.50, CroatianPayrollRules.premiumRates().night, 0.001)
    }


    @Test
    fun turnusAndSecondShiftSupplementsAreBothPaidAndSeniorityDoesNotInflateTariff() {
        val summary = fullFund.copy(turnusMinutes = 120 * 60, secondShiftMinutes = 36 * 60)
        val result = PayrollEstimator.estimate(PayrollInput(
            month = YearMonth.of(2026, 8), summary = summary,
            annualLeaveMinutes = 0, sickLeaveMinutes = 0,
            otherPaidAbsenceMinutes = 0, hasDayNightTurnusPattern = true,
            serviceYears = 12, children = 2
        ))!!
        assertEquals(1_025.0 * 1.25 / 168, result.hourlyGross, 0.001)
        assertEquals(result.hourlyGross * 120 * 0.05, result.turnusPremiumGross, 0.001)
        assertEquals(result.hourlyGross * 36 * 0.10, result.secondShiftPremiumGross, 0.001)
        assertEquals(result.hourlyGross * 168 * 0.06, result.seniorityGross, 0.001)
        assertEquals(1_320.0, result.personalAllowance, 0.001)
    }

    @Test
    fun shiftPremiumCompositionProducesReasonableTurnusNetAndGross() {
        // Synthetic regression fixture. No actual payslip amounts or identity stored.
        val summary = fullFund.copy(
            fundMinutes = 184 * 60, regularMinutes = 184 * 60,
            workedMinutes = 216 * 60, creditedMinutes = 216 * 60,
            overtimeMinutes = 32 * 60, nightMinutes = 68 * 60,
            saturdayMinutes = 25 * 60, sundayMinutes = 25 * 60,
            secondShiftMinutes = 36 * 60, turnusMinutes = 120 * 60
        )
        val result = PayrollEstimator.estimate(PayrollInput(
            month = YearMonth.of(2026, 7), summary = summary,
            annualLeaveMinutes = 0, sickLeaveMinutes = 0,
            otherPaidAbsenceMinutes = 0, hasDayNightTurnusPattern = true,
            serviceYears = 12, children = 2
        ))!!
        org.junit.Assert.assertTrue(result.grossOne in 2_000.0..2_250.0)
        org.junit.Assert.assertTrue(result.netMonthly in 1_500.0..1_750.0)
        assertEquals(0, result.projectedRegularMinutes)
    }

    @Test
    fun incompleteFutureMonthShowsFullTimeBaselineAsProjectionNotZeroWage() {
        val summary = fullFund.copy(
            workedMinutes = 48 * 60, regularMinutes = 48 * 60,
            creditedMinutes = 48 * 60, overtimeMinutes = 0,
            nightMinutes = 0, saturdayMinutes = 0, sundayMinutes = 0,
            secondShiftMinutes = 0, turnusMinutes = 0
        )
        val result = PayrollEstimator.estimate(PayrollInput(
            month = YearMonth.of(2026, 10), summary = summary,
            annualLeaveMinutes = 0, sickLeaveMinutes = 0,
            otherPaidAbsenceMinutes = 0, hasDayNightTurnusPattern = false,
            serviceYears = 12, children = 2
        ))!!
        assertEquals(120 * 60, result.projectedRegularMinutes)
        assertEquals(1_025.0 * 1.25, result.baseGross, 0.001)
        assertEquals(1_025.0 * 1.25 * 0.06, result.seniorityGross, 0.001)
    }


    @Test
    fun annualLeaveAverageFromPayslipReplacesOnlyLeaveBase() {
        val summary = fullFund.copy(
            workedMinutes = 0, regularMinutes = 0, overtimeMinutes = 0,
            paidAbsenceMinutes = 96 * 60, creditedMinutes = 96 * 60,
            nightMinutes = 0, saturdayMinutes = 0, sundayMinutes = 0,
            secondShiftMinutes = 0
        )
        val observed = PayrollEstimator.estimate(PayrollInput(
            month = YearMonth.of(2026, 6), summary = summary,
            annualLeaveMinutes = 96 * 60, sickLeaveMinutes = 0,
            otherPaidAbsenceMinutes = 0, hasDayNightTurnusPattern = false,
            serviceYears = 12, children = 2,
            annualLeaveAverageHourlyGross = 11.58
        ))!!
        assertEquals(11.58 * 96, observed.baseGross -
            (72.0 * 1_015.0 * 1.25 / 168.0), 0.001)
        assertEquals(72 * 60, observed.projectedRegularMinutes)
    }

    @Test
    fun rijekaHistoricalTaxRatesDifferFor2025And2026() {
        assertEquals(0.22, CroatianPayrollRules.rijekaTaxRates(YearMonth.of(2025, 8)).first, 0.001)
        assertEquals(0.20, CroatianPayrollRules.rijekaTaxRates(YearMonth.of(2026, 8)).first, 0.001)
    }

    @Test
    fun officialBasesFollowPublished2025And2026Steps() {
        assertEquals(
            947.18,
            CroatianPayrollRules.officialBase(YearMonth.of(2025, 1))!!,
            0.001
        )
        assertEquals(
            975.60,
            CroatianPayrollRules.officialBase(YearMonth.of(2025, 2))!!,
            0.001
        )
        assertEquals(
            975.60,
            CroatianPayrollRules.officialBase(YearMonth.of(2025, 8))!!,
            0.001
        )
        assertEquals(
            1_004.87,
            CroatianPayrollRules.officialBase(YearMonth.of(2025, 9))!!,
            0.001
        )
        assertEquals(
            1_004.87,
            CroatianPayrollRules.officialBase(YearMonth.of(2026, 3))!!,
            0.001
        )
        assertEquals(
            1_015.00,
            CroatianPayrollRules.officialBase(YearMonth.of(2026, 4))!!,
            0.001
        )
        assertEquals(
            1_015.00,
            CroatianPayrollRules.officialBase(YearMonth.of(2026, 7))!!,
            0.001
        )
        assertEquals(
            1_025.00,
            CroatianPayrollRules.officialBase(YearMonth.of(2026, 8))!!,
            0.001
        )
        assertEquals(
            1_025.00,
            CroatianPayrollRules.officialBase(YearMonth.of(2026, 11))!!,
            0.001
        )
        assertEquals(
            1_035.00,
            CroatianPayrollRules.officialBase(YearMonth.of(2026, 12))!!,
            0.001
        )
        assertNull(CroatianPayrollRules.officialBase(YearMonth.of(2024, 12)))
    }

    @Test
    fun childAndYouthRulesRemainAvailableWithoutBeingRequiredByUi() {
        assertEquals(600.0, CroatianPayrollRules.personalAllowance(), 0.001)
        assertEquals(
            900.0,
            CroatianPayrollRules.personalAllowance(children = 1),
            0.001
        )
        assertEquals(
            1_320.0,
            CroatianPayrollRules.personalAllowance(children = 2),
            0.001
        )

        assertEquals(
            1.0,
            CroatianPayrollRules.youthAnnualReliefFraction(2026, 2001),
            0.001
        )
        assertEquals(
            0.5,
            CroatianPayrollRules.youthAnnualReliefFraction(2026, 1996),
            0.001
        )
        assertEquals(
            0.0,
            CroatianPayrollRules.youthAnnualReliefFraction(2026, 1995),
            0.001
        )
    }
}
