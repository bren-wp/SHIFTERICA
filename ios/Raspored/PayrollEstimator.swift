import Foundation

struct PayrollInputIOS {
    let month: Date
    let summary: WorkTimeSummaryIOS
    let annualLeaveMinutes: Int
    let sickLeaveMinutes: Int
    let otherPaidAbsenceMinutes: Int
    let hasDayNightTurnusPattern: Bool
    var serviceYears: Int = 0
    var children: Int = 0
    var dependents: Int = 0
    var birthYear: Int? = nil
    var annualLeaveAverageHourlyGross: Double? = nil
    var paymentDelayMonths: Int = 1
}

struct PayrollEstimateIOS {
    let officialBase: Double
    let coefficient: Double
    let hourlyGross: Double
    let baseGross: Double
    let premiumGross: Double
    let grossOne: Double
    let pensionFirstPillar: Double
    let pensionSecondPillar: Double
    let personalAllowance: Double
    let taxableIncome: Double
    let incomeTax: Double
    let netMonthly: Double
    let youthAnnualReliefFraction: Double
    let estimatedYouthRefundShare: Double
    let employerHealthContribution: Double
    let grossTwo: Double
    let turnusApplied: Bool
    let seniorityGross: Double
    let turnusPremiumGross: Double
    let secondShiftPremiumGross: Double
    let projectedRegularMinutes: Int
}

enum PayrollEstimatorIOS {
    /// Show carryover wages even without a new entry in the selected month.
    /// Avoid projecting a full salary for an untouched month.
    static func hasRecordedActivity(monthHasEntries: Bool, workedMinutes: Int) -> Bool {
        monthHasEntries || workedMinutes > 0
    }

    private static func cents(_ value: Double) -> Double {
        var decimal = Decimal(value)
        var rounded = Decimal()
        NSDecimalRound(&rounded, &decimal, 2, .plain)
        return NSDecimalNumber(decimal: rounded).doubleValue
    }
    static func estimate(_ input: PayrollInputIOS) -> PayrollEstimateIOS? {
        guard
            let base = CroatianPayrollRulesIOS.officialBase(month: input.month),
            input.summary.fundMinutes > 0
        else { return nil }

        // Next-month payment is the default, but late/same-month payment
        // may be configured for each month without changing the gross base.
        let paymentMonth = Calendar.raspored.date(
            byAdding: .month, value: min(12, max(0, input.paymentDelayMonths)),
            to: input.month
        ) ?? input.month
        let paymentTaxYear = Calendar.raspored.component(.year, from: paymentMonth)
        let coefficient = CroatianPayrollRulesIOS.defaultCoefficient
        let years = min(max(input.serviceYears, 0), 60)
        // TKU art. 59: premiums use the hourly base increased by seniority.
        // Seniority remains a separate gross wage line, applied only once.
        let hourly = base * coefficient / (Double(input.summary.fundMinutes) / 60.0)
        let seniorityRate = Double(years) * 0.005

        func amount(_ minutes: Int, factor: Double = 1.0) -> Double {
            hourly * (Double(max(0, minutes)) / 60.0) * factor
        }
        func premiumAmount(_ minutes: Int, factor: Double) -> Double {
            amount(minutes, factor: factor) * (1.0 + seniorityRate)
        }

        let regularBase = amount(input.summary.regularMinutes)
        let overtimeBase = amount(input.summary.overtimeMinutes)
        let annualLeaveBase: Double
        if let average = input.annualLeaveAverageHourlyGross,
           average.isFinite, average > 0, average <= 1000 {
            annualLeaveBase = average * Double(max(0, input.annualLeaveMinutes)) / 60.0
        } else {
            annualLeaveBase = amount(input.annualLeaveMinutes)
        }
        let sickLeaveBase = amount(
            input.sickLeaveMinutes,
            factor: CroatianPayrollRulesIOS.sickPayDefaultRate
        )
        let otherPaidBase = amount(input.otherPaidAbsenceMinutes)
        let holidayCreditBase = amount(input.summary.holidayCreditMinutes)
        let projectedRegularMinutes = max(
            0,
            input.summary.fundMinutes -
                input.summary.regularMinutes - input.summary.paidAbsenceMinutes
        )
        let projectedRegularBase = amount(projectedRegularMinutes)
        let seniorityGross = (
            regularBase + overtimeBase + holidayCreditBase + projectedRegularBase
        ) * seniorityRate
        let baseGross = cents(
            cents(regularBase) + cents(overtimeBase) + cents(annualLeaveBase) +
                cents(sickLeaveBase) + cents(otherPaidBase) +
                cents(holidayCreditBase) + cents(projectedRegularBase)
        )

        let turnusApplied = input.hasDayNightTurnusPattern
        let rates = CroatianPayrollRulesIOS.premiumRates()
        let nightPremium = premiumAmount(input.summary.nightMinutes, factor: rates.night)
        let overtimePremium = premiumAmount(input.summary.overtimeMinutes, factor: rates.overtime)
        let saturdayPremium = premiumAmount(input.summary.saturdayMinutes, factor: rates.saturday)
        let sundayPremium = premiumAmount(input.summary.sundayMinutes, factor: rates.sunday)
        let holidayPremium = premiumAmount(input.summary.holidayWorkedMinutes, factor: rates.holiday)
        let turnusPremium = turnusApplied ?
            premiumAmount(input.summary.turnusMinutes, factor: rates.turnus) : 0
        // Payslips show that second-shift and turnus supplements can coexist.
        let secondShiftPremium = premiumAmount(input.summary.secondShiftMinutes, factor: rates.secondShift)
        let premiumGross = cents(
            cents(seniorityGross) + cents(nightPremium) + cents(overtimePremium) +
                cents(saturdayPremium) + cents(sundayPremium) +
                cents(holidayPremium) + cents(turnusPremium) + cents(secondShiftPremium)
        )
        let grossOne = cents(baseGross + premiumGross)

        let pensionOne = cents(grossOne * CroatianPayrollRulesIOS.pensionFirstPillarRate)
        let pensionTwo = cents(grossOne * CroatianPayrollRulesIOS.pensionSecondPillarRate)
        let pensionTotal = cents(pensionOne + pensionTwo)

        let allowance = CroatianPayrollRulesIOS.personalAllowance(
            children: input.children,
            dependents: input.dependents,
            taxYear: paymentTaxYear
        )
        let taxable = cents(max(0, grossOne - pensionTotal - allowance))
        let threshold = CroatianPayrollRulesIOS.higherRateThreshold(taxYear: paymentTaxYear)
        let lowerBase = min(taxable, threshold)
        let higherBase = max(0, taxable - threshold)
        let taxRates = CroatianPayrollRulesIOS.rijekaTaxRates(month: paymentMonth)
        let lowerTax = cents(lowerBase * taxRates.lower)
        let higherTax = cents(higherBase * taxRates.higher)
        let tax = cents(lowerTax + higherTax)
        // Net salary BEFORE personal withholdings (loans, garnishments and
        // administrative bans). Only mandatory pension contributions and
        // income tax reduce gross salary in this model.
        let netBeforeWithholdings = cents(max(0, grossOne - pensionTotal - tax))

        let taxYear = paymentTaxYear
        let youthFraction = CroatianPayrollRulesIOS.youthAnnualReliefFraction(
            taxYear: taxYear,
            birthYear: input.birthYear
        )
        let youthRefund = cents(lowerTax * youthFraction)
        let employerHealth = cents(grossOne * CroatianPayrollRulesIOS.employerHealthRate)

        return PayrollEstimateIOS(
            officialBase: base,
            coefficient: coefficient,
            hourlyGross: hourly,
            baseGross: baseGross,
            premiumGross: premiumGross,
            grossOne: grossOne,
            pensionFirstPillar: pensionOne,
            pensionSecondPillar: pensionTwo,
            personalAllowance: allowance,
            taxableIncome: taxable,
            incomeTax: tax,
            netMonthly: netBeforeWithholdings,
            youthAnnualReliefFraction: youthFraction,
            estimatedYouthRefundShare: youthRefund,
            employerHealthContribution: employerHealth,
            grossTwo: cents(grossOne + employerHealth),
            turnusApplied: turnusApplied,
            seniorityGross: cents(seniorityGross),
            turnusPremiumGross: cents(turnusPremium),
            secondShiftPremiumGross: cents(secondShiftPremium),
            projectedRegularMinutes: projectedRegularMinutes
        )
    }
}


/// Strictly parses a local euro amount without locale-dependent Double rounding.
/// Whitespace, comma decimals and dot-grouped thousands are supported.
enum PayrollMoneyInputIOS {
    static func parseCents(_ raw: String) -> Int64? {
        let input = raw.trimmingCharacters(in: .whitespacesAndNewlines)
            .replacingOccurrences(of: " ", with: "")
            .replacingOccurrences(of: "\u{00A0}", with: "")
            .replacingOccurrences(of: "\u{202F}", with: "")
        let normalized: String
        if input.range(
            of: #"^[0-9]{1,3}(\.[0-9]{3})+,[0-9]{1,2}$"#,
            options: .regularExpression
        ) != nil {
            normalized = input.replacingOccurrences(of: ".", with: "")
                .replacingOccurrences(of: ",", with: ".")
        } else if input.range(
            of: #"^[0-9]{1,7}([,.][0-9]{1,2})?$"#,
            options: .regularExpression
        ) != nil {
            normalized = input.replacingOccurrences(of: ",", with: ".")
        } else {
            return nil
        }
        let parts = normalized.split(separator: ".", omittingEmptySubsequences: false)
        guard let euros = Int64(parts[0]), euros <= 1_000_000 else { return nil }
        let fractional: Int64
        if parts.count > 1 {
            guard let digits = Int64(parts[1]) else { return nil }
            fractional = parts[1].count == 1 ? digits * 10 : digits
        } else {
            fractional = 0
        }
        let cents = euros * 100 + fractional
        return cents <= 100_000_000 ? cents : nil
    }
}
