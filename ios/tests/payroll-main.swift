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
            secondShiftMinutes: 0,
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
        print("iOS payroll cents and mandatory-deduction regression checks passed")
    }
}
