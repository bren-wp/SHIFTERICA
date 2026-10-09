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
    fun turnusAndSecondShiftSupplementsUseSeniorityInPremiumBase() {
        val summary = fullFund.copy(turnusMinutes = 120 * 60, secondShiftMinutes = 36 * 60)
        val result = PayrollEstimator.estimate(PayrollInput(
            month = YearMonth.of(2026, 8), summary = summary,
            annualLeaveMinutes = 0, sickLeaveMinutes = 0,
            otherPaidAbsenceMinutes = 0, hasDayNightTurnusPattern = true,
            serviceYears = 12, children = 2
        ))!!
        assertEquals(1_025.0 * 1.25 / 168, result.hourlyGross, 0.001)
        assertEquals(result.hourlyGross * 120 * 0.05 * 1.06, result.turnusPremiumGross, 0.011)
        assertEquals(result.hourlyGross * 36 * 0.10 * 1.06, result.secondShiftPremiumGross, 0.011)
        assertEquals(result.hourlyGross * 168 * 0.06, result.seniorityGross, 0.011)
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
        assertEquals(76.88, result.seniorityGross, 0.001)
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
        assertEquals(947.18,
            CroatianPayrollRules.officialBase(YearMonth.of(2024, 12))!!, 0.001)
        assertNull(CroatianPayrollRules.officialBase(YearMonth.of(2023, 12)))
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
    @Test
    fun payrollDeductionsAndNetAreRoundedAtEachPayslipStage() {
        val result = PayrollEstimator.estimate(PayrollInput(
            month = YearMonth.of(2026, 8), summary = fullFund,
            annualLeaveMinutes = 0, sickLeaveMinutes = 0,
            otherPaidAbsenceMinutes = 0, hasDayNightTurnusPattern = true,
            serviceYears = 12, children = 2
        ))!!
        val roundedValues = listOf(
            result.baseGross, result.premiumGross, result.grossOne,
            result.pensionFirstPillar, result.pensionSecondPillar,
            result.taxableIncome, result.incomeTax, result.netMonthly,
            result.employerHealthContribution, result.grossTwo
        )
        roundedValues.forEach { amount ->
            assertEquals(kotlin.math.round(amount * 100.0) / 100.0, amount, 0.00001)
        }
        assertEquals(
            result.grossOne - result.pensionFirstPillar -
                result.pensionSecondPillar - result.incomeTax,
            result.netMonthly, 0.00001
        )
        assertEquals(1_320.0, result.personalAllowance, 0.00001)
    }

    @Test
    fun allWorkOrganizationPremiumsIncreaseWithSeniorityExactlyOnce() {
        // Fully synthetic time summary; no confidential salary or employee data.
        val synthetic = fullFund.copy(
            workedMinutes = 180 * 60, regularMinutes = 168 * 60,
            overtimeMinutes = 12 * 60, creditedMinutes = 180 * 60,
            nightMinutes = 16 * 60, saturdayMinutes = 8 * 60,
            sundayMinutes = 8 * 60, holidayWorkedMinutes = 8 * 60,
            secondShiftMinutes = 12 * 60, turnusMinutes = 24 * 60
        )
        fun calculate(years: Int) = PayrollEstimator.estimate(PayrollInput(
            month = YearMonth.of(2026, 8), summary = synthetic,
            annualLeaveMinutes = 0, sickLeaveMinutes = 0,
            otherPaidAbsenceMinutes = 0, hasDayNightTurnusPattern = true,
            serviceYears = years, children = 2
        ))!!
        val zero = calculate(0)
        val twelve = calculate(12)
        val hourly = 1_025.0 * 1.25 / 168.0
        assertEquals(hourly, twelve.hourlyGross, 0.000001)
        assertEquals(hourly * 24 * 0.05, zero.turnusPremiumGross, 0.011)
        assertEquals(hourly * 24 * 0.05 * 1.06, twelve.turnusPremiumGross, 0.011)
        assertEquals(hourly * 12 * 0.10 * 1.06, twelve.secondShiftPremiumGross, 0.011)
        assertEquals(hourly * 180 * 0.06, twelve.seniorityGross, 0.011)
        assertEquals(zero.baseGross, twelve.baseGross, 0.000001)
        val expectedExtra = hourly * (
            16.0 * 0.50 + 12.0 * 0.50 + 8.0 * 0.25 +
            8.0 * 0.50 + 8.0 * 1.50 + 12.0 * 0.10 + 24.0 * 0.05
        ) * 0.06
        assertEquals(expectedExtra,
            twelve.premiumGross - twelve.seniorityGross - zero.premiumGross,
            0.08 // line-level rounding to cents
        )
        org.junit.Assert.assertTrue(twelve.netMonthly > zero.netMonthly)
        assertEquals(1_320.0, twelve.personalAllowance, 0.001)
    }

    @Test
    fun crossYearPayDateSetsCorrectTaxRatesAndPersonalAllowance() {
        // The wage is earned in December but paid in January.
        // Gross-base month and taxation month MUST be independent.
        fun forMonth(year: Int, month: Int) = PayrollEstimator.estimate(PayrollInput(
            month = YearMonth.of(year, month),
            summary = fullFund,
            annualLeaveMinutes = 0, sickLeaveMinutes = 0,
            otherPaidAbsenceMinutes = 0, hasDayNightTurnusPattern = true,
            serviceYears = 11, children = 2
        ))!!
        val november2024 = forMonth(2024, 11) // December 2024 payment
        val december2024 = forMonth(2024, 12) // January 2025 payment
        val december2025 = forMonth(2025, 12) // January 2026 payment
        assertEquals(947.18, november2024.officialBase, 0.001)
        assertEquals(947.18, december2024.officialBase, 0.001)
        assertEquals(1_232.0, november2024.personalAllowance, 0.001)
        assertEquals(1_320.0, december2024.personalAllowance, 0.001)
        assertEquals(1_320.0, december2025.personalAllowance, 0.001)
        assertEquals(0.224,
            CroatianPayrollRules.rijekaTaxRates(YearMonth.of(2024, 12)).first, 0.0001)
        assertEquals(0.22,
            CroatianPayrollRules.rijekaTaxRates(YearMonth.of(2025, 1)).first, 0.0001)
        assertEquals(0.20,
            CroatianPayrollRules.rijekaTaxRates(YearMonth.of(2026, 1)).first, 0.0001)
        assertEquals(4_200.0, CroatianPayrollRules.higherRateThreshold(2024), 0.001)
        assertEquals(5_000.0, CroatianPayrollRules.higherRateThreshold(2025), 0.001)

        // The December 2025 wage uses January 2026 lower rate (20%).
        assertEquals(
            kotlin.math.round(december2025.taxableIncome * 0.20 * 100) / 100,
            december2025.incomeTax, 0.001
        )
        // December 2024 wage uses January 2025 lower rate (22%).
        assertEquals(
            kotlin.math.round(december2024.taxableIncome * 0.22 * 100) / 100,
            december2024.incomeTax, 0.001
        )
    }

    @Test
    fun historicalElevenAndCurrentTwelveYearsYieldDifferentPremiums() {
        // Only anonymized synthetic summaries: existing monthly records
        // must not inherit current seniority without user confirmation.
        val worked = fullFund.copy(
            workedMinutes = 168 * 60, regularMinutes = 168 * 60,
            overtimeMinutes = 0, turnusMinutes = 120 * 60,
            secondShiftMinutes = 24 * 60
        )
        fun estimate(years: Int) = PayrollEstimator.estimate(PayrollInput(
            month = YearMonth.of(2026, 8), summary = worked,
            annualLeaveMinutes = 0, sickLeaveMinutes = 0,
            otherPaidAbsenceMinutes = 0, hasDayNightTurnusPattern = true,
            serviceYears = years, children = 2
        ))!!
        val historical = estimate(11)
        val current = estimate(12)
        val tariff = 1_025.0 * 1.25 / 168.0
        assertEquals(tariff * 168 * 0.055, historical.seniorityGross, 0.011)
        assertEquals(tariff * 168 * 0.060, current.seniorityGross, 0.011)
        assertEquals(tariff * 120 * 0.05 * 1.055,
            historical.turnusPremiumGross, 0.011)
        assertEquals(tariff * 120 * 0.05 * 1.060,
            current.turnusPremiumGross, 0.011)
        org.junit.Assert.assertTrue(current.netMonthly > historical.netMonthly)
    }

    @Test
    fun overnightCarryoverCountsAsPayrollActivityWithoutCurrentMonthEntry() {
        // Synthetic seven-hour carryover; no real employee data.
        val month = YearMonth.of(2026, 11)
        val carried = fullFund.copy(
            fundMinutes = 21 * 8 * 60,
            workedMinutes = 7 * 60,
            regularMinutes = 7 * 60,
            overtimeMinutes = 0,
            nightMinutes = 6 * 60,
            sundayMinutes = 7 * 60,
            dayMinutes = 1 * 60,
            creditedMinutes = 7 * 60,
            workedShiftCount = 1,
            paidAbsenceMinutes = 0,
            holidayCreditMinutes = 0,
            saturdayMinutes = 0,
            holidayWorkedMinutes = 7 * 60,
            secondShiftMinutes = 0,
            turnusMinutes = 7 * 60
        )
        org.junit.Assert.assertTrue(PayrollEstimator.hasRecordedActivity(
            monthHasEntries = false, workedMinutes = carried.workedMinutes
        ))
        org.junit.Assert.assertFalse(PayrollEstimator.hasRecordedActivity(
            monthHasEntries = false, workedMinutes = 0
        ))
        org.junit.Assert.assertTrue(PayrollEstimator.hasRecordedActivity(
            monthHasEntries = true, workedMinutes = 0
        ))
        val withCarryover = PayrollEstimator.estimate(PayrollInput(
            month = month, summary = carried,
            annualLeaveMinutes = 0, sickLeaveMinutes = 0,
            otherPaidAbsenceMinutes = 0, hasDayNightTurnusPattern = false,
            serviceYears = 12, children = 2
        ))!!
        val withoutCarryover = PayrollEstimator.estimate(PayrollInput(
            month = month, summary = carried.copy(
                workedMinutes = 0, regularMinutes = 0, nightMinutes = 0,
                sundayMinutes = 0, holidayWorkedMinutes = 0,
                dayMinutes = 0, creditedMinutes = 0,
                workedShiftCount = 0, turnusMinutes = 0
            ),
            annualLeaveMinutes = 0, sickLeaveMinutes = 0,
            otherPaidAbsenceMinutes = 0, hasDayNightTurnusPattern = false,
            serviceYears = 12, children = 2
        ))!!
        // Sunday, overnight and All Saints premiums are separate percentages
        // on the SAME seven worked hours; workedMinutes is NOT tripled.
        org.junit.Assert.assertTrue(withCarryover.premiumGross > withoutCarryover.premiumGross)
        assertEquals(7 * 60, carried.holidayWorkedMinutes)
        assertEquals(21 * 8 * 60 - 7 * 60,
            withCarryover.projectedRegularMinutes)
        assertEquals(1_025.0, withCarryover.officialBase, 0.001)
    }

    @Test
    fun delayedAndSameMonthPaymentsSelectTaxYearWithoutChangingGross() {
        // December 2025 wage can be paid in December 2025, January 2026,
        // or February 2026. The base stays 2025; tax follows payment.
        fun forDelay(month: YearMonth, delay: Int) =
            PayrollEstimator.estimate(PayrollInput(
                month = month, summary = fullFund,
                annualLeaveMinutes = 0, sickLeaveMinutes = 0,
                otherPaidAbsenceMinutes = 0, hasDayNightTurnusPattern = true,
                serviceYears = 12, children = 0, paymentDelayMonths = delay
            ))!!
        val month = YearMonth.of(2025, 12)
        val sameMonth = forDelay(month, 0)
        val nextMonth = forDelay(month, 1)
        val twoMonths = forDelay(month, 2)

        assertEquals(sameMonth.officialBase, nextMonth.officialBase, 0.001)
        assertEquals(sameMonth.grossOne, nextMonth.grossOne, 0.001)
        assertEquals(nextMonth.grossOne, twoMonths.grossOne, 0.001)
        assertEquals(sameMonth.taxableIncome, nextMonth.taxableIncome, 0.001)
        assertEquals(sameMonth.taxableIncome, twoMonths.taxableIncome, 0.001)
        assertEquals(sameMonth.taxableIncome * 0.22,
            sameMonth.incomeTax, 0.011)
        assertEquals(nextMonth.taxableIncome * 0.20,
            nextMonth.incomeTax, 0.011)
        assertEquals(nextMonth.incomeTax, twoMonths.incomeTax, 0.001)
        org.junit.Assert.assertTrue(nextMonth.netMonthly > sameMonth.netMonthly)

        // No change to the statutory gross base even on a cross-year delay.
        val november2024 = forDelay(YearMonth.of(2024, 11), 2)
        assertEquals(947.18, november2024.officialBase, 0.001)
        assertEquals(600.0, november2024.personalAllowance, 0.001)
    }

    @Test
    fun euroMoneyParserAcceptsCroatianCentsAndRejectsAmbiguousInput() {
        assertEquals(123_456L, PayrollMoneyInput.parseCents("1.234,56"))
        assertEquals(123_456L, PayrollMoneyInput.parseCents("1234,56"))
        assertEquals(123_456L, PayrollMoneyInput.parseCents("1234.56"))
        assertEquals(123_456L, PayrollMoneyInput.parseCents("1 234,56"))
        assertEquals(10L, PayrollMoneyInput.parseCents("0,1"))
        assertEquals(0L, PayrollMoneyInput.parseCents("0"))
        assertEquals(100_000_000L, PayrollMoneyInput.parseCents("1000000,00"))
        assertNull(PayrollMoneyInput.parseCents(""))
        assertNull(PayrollMoneyInput.parseCents("-10,50"))
        assertNull(PayrollMoneyInput.parseCents("1.2,34"))
        assertNull(PayrollMoneyInput.parseCents("12,345"))
        assertNull(PayrollMoneyInput.parseCents("1234.567"))
        assertNull(PayrollMoneyInput.parseCents("1000000,01"))
        assertNull(PayrollMoneyInput.parseCents("1e3"))
        assertNull(PayrollMoneyInput.parseCents("1234 €"))
    }

}
