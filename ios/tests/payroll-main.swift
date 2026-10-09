import Foundation

@main
enum PayrollRegressionChecks {
    static func main() {
        func check(_ condition: @autoclosure () -> Bool, _ reason: String) {
            guard condition() else {
                fputs("Payroll regression failed: " + reason + "\n", stderr)
                exit(1)
            }
        }
        let summary = WorkTimeSummaryIOS(
            workedMinutes: 168 * 60,
            regularMinutes: 168 * 60,
            fundMinutes: 168 * 60,
            overtimeMinutes: 0,
            paidAbsenceMinutes: 0,
            holidayCreditMinutes: 0,
            creditedMinutes: 168 * 60,
            workedShiftCount: 14,
            dayMinutes: 112 * 60,
            nightMinutes: 56 * 60,
            saturdayMinutes: 24 * 60,
            sundayMinutes: 24 * 60,
            holidayWorkedMinutes: 0,
            secondShiftMinutes: 12 * 60,
            turnusMinutes: 120 * 60
        )
        let august = Calendar.raspored.date(
            from: DateComponents(year: 2026, month: 8, day: 1)
        )!
        let december = Calendar.raspored.date(
            from: DateComponents(year: 2026, month: 12, day: 1)
        )!
        let input = PayrollInputIOS(
            month: august, summary: summary,
            annualLeaveMinutes: 0, sickLeaveMinutes: 0,
            otherPaidAbsenceMinutes: 0, hasDayNightTurnusPattern: true,
            serviceYears: 12, children: 2
        )
        guard let result = PayrollEstimatorIOS.estimate(input) else {
            fputs("Payroll regression failed: estimate missing\n", stderr)
            exit(1)
        }

        check(result.officialBase == 1_025, "August statutory pay base")
        check(result.coefficient == 1.25, "Existing validated coefficient")
        check(result.personalAllowance == 1_320, "Children allowance")
        check(result.turnusApplied, "Turnus supplement applied")
        let tariff = 1_025.0 * 1.25 / 168.0
        check(abs(result.hourlyGross - tariff) < 0.00001, "Base tariff unchanged")
        check(abs(result.turnusPremiumGross - tariff * 120 * 0.05 * 1.06) < 0.011,
              "Turnus supplement uses seniority-increased hourly wage")
        check(abs(result.secondShiftPremiumGross - tariff * 12 * 0.10 * 1.06) < 0.011,
              "Second shift also uses seniority-increased hourly wage")
        check(abs(result.seniorityGross - tariff * 168 * 0.06) < 0.011,
              "Seniority remains a separate wage item")
        let zero = PayrollEstimatorIOS.estimate(PayrollInputIOS(
            month: august, summary: summary,
            annualLeaveMinutes: 0, sickLeaveMinutes: 0,
            otherPaidAbsenceMinutes: 0, hasDayNightTurnusPattern: true,
            serviceYears: 0, children: 2
        ))!
        check(abs(zero.baseGross - result.baseGross) < 0.00001,
              "Base pay does not absorb seniority twice")
        check(abs(zero.turnusPremiumGross - tariff * 120 * 0.05) < 0.011,
              "Zero-seniority premiums remain unchanged")
        check(result.netMonthly > zero.netMonthly,
              "Gross and net reflect seniority-increased supplements")

        for amount in [
            result.baseGross, result.premiumGross, result.grossOne,
            result.pensionFirstPillar, result.pensionSecondPillar,
            result.taxableIncome, result.incomeTax, result.netMonthly,
            result.employerHealthContribution, result.grossTwo
        ] {
            check(abs((amount * 100).rounded() - amount * 100) < 0.0001,
                  "All taxable money values use cents")
        }

        let expectedNet = result.grossOne - result.pensionFirstPillar -
            result.pensionSecondPillar - result.incomeTax
        check(abs(result.netMonthly - expectedNet) < 0.00001,
              "Net salary equals gross less statutory deductions only")
        check(CroatianPayrollRulesIOS.officialBase(month: december) == 1_035,
              "December statutory base")
        let november2024 = Calendar.raspored.date(
            from: DateComponents(year: 2024, month: 11, day: 1)
        )!
        let december2024 = Calendar.raspored.date(
            from: DateComponents(year: 2024, month: 12, day: 1)
        )!
        let december2025 = Calendar.raspored.date(
            from: DateComponents(year: 2025, month: 12, day: 1)
        )!
        func paymentBoundary(_ month: Date) -> PayrollEstimateIOS {
            PayrollEstimatorIOS.estimate(PayrollInputIOS(
                month: month, summary: summary,
                annualLeaveMinutes: 0, sickLeaveMinutes: 0,
                otherPaidAbsenceMinutes: 0, hasDayNightTurnusPattern: true,
                serviceYears: 11, children: 2
            ))!
        }
        let paid2024 = paymentBoundary(november2024)
        let paid2025 = paymentBoundary(december2024)
        let paid2026 = paymentBoundary(december2025)
        check(paid2024.officialBase == 947.18, "2024 negotiated gross base")
        check(paid2025.officialBase == 947.18, "December 2024 gross base")
        check(abs(paid2024.personalAllowance - 1_232) < 0.001,
              "2024 personal allowance 560 + child deductions")
        check(abs(paid2025.personalAllowance - 1_320) < 0.001,
              "January 2025 payment uses 2025 personal allowance")
        check(abs(paid2026.personalAllowance - 1_320) < 0.001,
              "January 2026 payment uses 2026 personal allowance")
        check(abs(paid2025.incomeTax - (paid2025.taxableIncome * 0.22 * 100).rounded() / 100) < 0.001,
              "December 2024 salary paid January 2025 uses 22%")
        check(abs(paid2026.incomeTax - (paid2026.taxableIncome * 0.20 * 100).rounded() / 100) < 0.001,
              "December 2025 salary paid January 2026 uses 20%")
        check(CroatianPayrollRulesIOS.higherRateThreshold(taxYear: 2024) == 4_200,
              "Historical 2024 monthly high-rate threshold")
        let historical = PayrollEstimatorIOS.estimate(PayrollInputIOS(
            month: august, summary: summary,
            annualLeaveMinutes: 0, sickLeaveMinutes: 0,
            otherPaidAbsenceMinutes: 0, hasDayNightTurnusPattern: true,
            serviceYears: 11, children: 2
        ))!
        check(abs(historical.seniorityGross - tariff * 168 * 0.055) < 0.011,
              "Historical 11 years gives five-and-a-half percent")
        check(abs(historical.turnusPremiumGross - tariff * 120 * 0.05 * 1.055) < 0.011,
              "Turnus premium uses historical 5.5% seniority basis")
        check(result.netMonthly > historical.netMonthly,
              "Current 12-year net exceeds historical 11-year net")
        // An overnight shift from the previous month must remain visible in
        // payroll even without a saved shift on the new month's calendar.
        check(PayrollEstimatorIOS.hasRecordedActivity(
            monthHasEntries: false, workedMinutes: 7 * 60
        ), "Carryover from previous month is payroll activity")
        check(!PayrollEstimatorIOS.hasRecordedActivity(
            monthHasEntries: false, workedMinutes: 0
        ), "Untouched month must not show a fictional full wage")
        check(PayrollEstimatorIOS.hasRecordedActivity(
            monthHasEntries: true, workedMinutes: 0
        ), "A recorded paid absence still qualifies for a wage estimate")
        let november = Calendar.raspored.date(
            from: DateComponents(year: 2026, month: 11, day: 1)
        )!
        let carryoverSummary = WorkTimeSummaryIOS(
            workedMinutes: 7 * 60, regularMinutes: 7 * 60,
            fundMinutes: 21 * 8 * 60, overtimeMinutes: 0,
            paidAbsenceMinutes: 0, holidayCreditMinutes: 0,
            creditedMinutes: 7 * 60, workedShiftCount: 1,
            dayMinutes: 1 * 60, nightMinutes: 6 * 60,
            saturdayMinutes: 0, sundayMinutes: 7 * 60,
            holidayWorkedMinutes: 7 * 60, secondShiftMinutes: 0,
            turnusMinutes: 7 * 60
        )
        let carryoverPay = PayrollEstimatorIOS.estimate(PayrollInputIOS(
            month: november, summary: carryoverSummary,
            annualLeaveMinutes: 0, sickLeaveMinutes: 0,
            otherPaidAbsenceMinutes: 0, hasDayNightTurnusPattern: false,
            serviceYears: 12, children: 2
        ))!
        let blankPay = PayrollEstimatorIOS.estimate(PayrollInputIOS(
            month: november,
            summary: WorkTimeSummaryIOS(
                workedMinutes: 0, regularMinutes: 0,
                fundMinutes: 21 * 8 * 60, overtimeMinutes: 0,
                paidAbsenceMinutes: 0, holidayCreditMinutes: 0,
                creditedMinutes: 0, workedShiftCount: 0,
                dayMinutes: 0, nightMinutes: 0, saturdayMinutes: 0,
                sundayMinutes: 0, holidayWorkedMinutes: 0,
                secondShiftMinutes: 0, turnusMinutes: 0
            ),
            annualLeaveMinutes: 0, sickLeaveMinutes: 0,
            otherPaidAbsenceMinutes: 0, hasDayNightTurnusPattern: false,
            serviceYears: 12, children: 2
        ))!
        check(carryoverPay.premiumGross > blankPay.premiumGross,
              "Seven carried hours receive night, Sunday and holiday premiums")
        check(carryoverPay.projectedRegularMinutes == 21 * 8 * 60 - 7 * 60,
              "Carried hours reduce projected regular time exactly once")
        // Payment month selects the tax rules independently of wage month.
        func paid(_ delay: Int) -> PayrollEstimateIOS {
            PayrollEstimatorIOS.estimate(PayrollInputIOS(
                month: december2025, summary: summary,
                annualLeaveMinutes: 0, sickLeaveMinutes: 0,
                otherPaidAbsenceMinutes: 0, hasDayNightTurnusPattern: true,
                serviceYears: 12, children: 0, paymentDelayMonths: delay
            ))!
        }
        let sameMonth = paid(0)
        let nextMonth = paid(1)
        let latePayment = paid(2)
        check(abs(sameMonth.grossOne - nextMonth.grossOne) < 0.001,
              "Payment delay must not alter negotiated gross wages")
        check(abs(nextMonth.grossOne - latePayment.grossOne) < 0.001,
              "Later payments preserve gross")
        check(abs(sameMonth.taxableIncome - nextMonth.taxableIncome) < 0.001,
              "Unchanged allowance keeps taxable base")
        check(abs(sameMonth.incomeTax - (sameMonth.taxableIncome * 0.22 * 100).rounded() / 100) < 0.001,
              "December 2025 payment uses 2025 Rijeka 22 percent")
        check(abs(nextMonth.incomeTax - (nextMonth.taxableIncome * 0.20 * 100).rounded() / 100) < 0.001,
              "January 2026 payment uses 2026 Rijeka 20 percent")
        check(abs(nextMonth.incomeTax - latePayment.incomeTax) < 0.001,
              "January and February 2026 have the same tax rate")
        check(nextMonth.netMonthly > sameMonth.netMonthly,
              "Next-year payment changes net but not gross")
        for (raw, cents) in [
            ("1.234,56", Int64(123_456)),
            ("1234,56", 123_456),
            ("1234.56", 123_456),
            ("1 234,56", 123_456),
            ("0,1", 10),
            ("0", 0),
            ("1000000,00", 100_000_000)
        ] {
            check(PayrollMoneyInputIOS.parseCents(raw) == cents,
                  "Croatian euro parser failed for \(raw)")
        }
        for bad in ["", "-10,50", "1.2,34", "12,345", "1234.567",
                    "1000000,01", "1e3", "1234 €"] {
            check(PayrollMoneyInputIOS.parseCents(bad) == nil,
                  "Invalid euro amount must be rejected: \(bad)")
        }
        print("iOS payroll, historical seniority, carryover, payment-month and euro-input checks passed")
    }
}
