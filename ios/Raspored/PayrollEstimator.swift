import Foundation

struct PayrollInputIOS {
    let month: Date
    let sector: PayrollSectorIOS
    let coefficient: Double
    let summary: WorkTimeSummaryIOS
    let annualLeaveMinutes: Int
    let sickLeaveMinutes: Int
    let otherPaidAbsenceMinutes: Int
    let hasDayNightTurnusPattern: Bool
    var serviceYears: Int = 0
    var children: Int = 0
    var dependents: Int = 0
    var birthYear: Int? = nil
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
}

enum PayrollEstimatorIOS {
    static func estimate(_ input: PayrollInputIOS) -> PayrollEstimateIOS? {
        guard
            let base = CroatianPayrollRulesIOS.officialBase(
                month: input.month,
                sector: input.sector
            ),
            let rates = CroatianPayrollRulesIOS.premiumRates(sector: input.sector),
            input.summary.fundMinutes > 0
        else { return nil }

        let coefficient = min(max(input.coefficient, 0.5), 8.0)
        let years = min(max(input.serviceYears, 0), 60)
        let serviceFactor = 1.0 + Double(years) * 0.005
        let fullFundGross = base * coefficient * serviceFactor
        let hourly = fullFundGross / (Double(input.summary.fundMinutes) / 60.0)

        func amount(_ minutes: Int, factor: Double = 1.0) -> Double {
            hourly * (Double(max(0, minutes)) / 60.0) * factor
        }

        let regularBase = amount(input.summary.regularMinutes)
        let overtimeBase = amount(input.summary.overtimeMinutes)
        let annualLeaveBase = amount(input.annualLeaveMinutes)
        let sickLeaveBase = amount(
            input.sickLeaveMinutes,
            factor: CroatianPayrollRulesIOS.sickPayDefaultRate
        )
        let otherPaidBase = amount(input.otherPaidAbsenceMinutes)
        let holidayCreditBase = amount(input.summary.holidayCreditMinutes)

        let baseGross = regularBase + overtimeBase + annualLeaveBase +
            sickLeaveBase + otherPaidBase + holidayCreditBase

        let turnusApplied = input.sector == .hospital && input.hasDayNightTurnusPattern
        let nightPremium = amount(input.summary.nightMinutes, factor: rates.night)
        let overtimePremium = amount(input.summary.overtimeMinutes, factor: rates.overtime)
        let saturdayPremium = amount(input.summary.saturdayMinutes, factor: rates.saturday)
        let sundayPremium = amount(input.summary.sundayMinutes, factor: rates.sunday)
        let holidayPremium = amount(input.summary.holidayWorkedMinutes, factor: rates.holiday)
        let turnusPremium = turnusApplied ?
            amount(input.summary.workedMinutes, factor: rates.turnus) : 0
        let secondShiftPremium = turnusApplied ? 0 :
            amount(input.summary.secondShiftMinutes, factor: rates.secondShift)

        let premiumGross = nightPremium + overtimePremium + saturdayPremium +
            sundayPremium + holidayPremium + turnusPremium + secondShiftPremium
        let grossOne = baseGross + premiumGross

        let pensionOne = grossOne * CroatianPayrollRulesIOS.pensionFirstPillarRate
        let pensionTwo = grossOne * CroatianPayrollRulesIOS.pensionSecondPillarRate
        let pensionTotal = pensionOne + pensionTwo

        let allowance = CroatianPayrollRulesIOS.personalAllowance(
            children: input.children,
            dependents: input.dependents
        )
        let taxable = max(0, grossOne - pensionTotal - allowance)
        let lowerBase = min(taxable, CroatianPayrollRulesIOS.monthlyHigherRateThreshold)
        let higherBase = max(0, taxable - CroatianPayrollRulesIOS.monthlyHigherRateThreshold)
        let lowerTax = lowerBase * CroatianPayrollRulesIOS.rijekaLowerTaxRate
        let higherTax = higherBase * CroatianPayrollRulesIOS.rijekaHigherTaxRate
        let tax = lowerTax + higherTax
        let net = max(0, grossOne - pensionTotal - tax)

        let taxYear = Calendar.raspored.component(.year, from: input.month)
        let youthFraction = CroatianPayrollRulesIOS.youthAnnualReliefFraction(
            taxYear: taxYear,
            birthYear: input.birthYear
        )
        let youthRefund = lowerTax * youthFraction
        let employerHealth = grossOne * CroatianPayrollRulesIOS.employerHealthRate

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
            netMonthly: net,
            youthAnnualReliefFraction: youthFraction,
            estimatedYouthRefundShare: youthRefund,
            employerHealthContribution: employerHealth,
            grossTwo: grossOne + employerHealth,
            turnusApplied: turnusApplied
        )
    }
}
