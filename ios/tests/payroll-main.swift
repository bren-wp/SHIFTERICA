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
        print("iOS payroll, historical seniority and deduction checks passed")
    }
}
