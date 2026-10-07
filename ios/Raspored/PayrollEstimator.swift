import Foundation

struct PayrollInputIOS {
    let month: Date
    let coefficient: Double
    let completedYearsService: Int
    let fundMinutes: Int
    let regularWorkedMinutes: Int
    let overtimeMinutes: Int
    let annualLeaveMinutes: Int
    let sickLeaveMinutes: Int
    let otherPaidAbsenceMinutes: Int
    let holidayCreditMinutes: Int
    let nightMinutes: Int
    let saturdayMinutes: Int
    let sundayMinutes: Int
    let holidayWorkedMinutes: Int
    let secondShiftMinutes: Int
    let children: Int
    let dependents: Int
    let disabilityAllowance: Double
    let birthYear: Int?
    let taxRates: PayrollTaxRatesIOS
    let turnusEnabled: Bool
    let sickPayRate: Double
    let annualLeaveFactor: Double
    let rates: PayrollRatesIOS
}

struct PayrollEstimateIOS {
    let officialBase: Double
    let baseGrossForFullFund: Double
    let seniorityAddition: Double
    let hourlyGross: Double
    let regularWorkGross: Double
    let annualLeaveGross: Double
    let sickLeaveGross: Double
    let otherPaidAbsenceGross: Double
    let holidayCreditGross: Double
    let overtimeBaseGross: Double
    let nightAddition: Double
    let saturdayAddition: Double
    let sundayAddition: Double
    let holidayAddition: Double
    let secondShiftAddition: Double
    let turnusAddition: Double
    let overtimeAddition: Double
    let grossOne: Double
    let pensionFirstPillar: Double
    let pensionSecondPillar: Double
    let personalAllowance: Double
    let taxableIncome: Double
    let monthlyIncomeTax: Double
    let netBeforeAnnualYouthRelief: Double
    let youthAnnualReliefFraction: Double
    let estimatedYouthRefundShareForMonth: Double
    let estimatedNetAfterAnnualYouthRelief: Double
    let employerHealthContribution: Double
    let grossTwo: Double
}

enum PayrollEstimatorIOS {
    static func estimate(_ input: PayrollInputIOS) -> PayrollEstimateIOS? {
        guard
            let officialBase = CroatianPayrollRulesIOS.publicServiceBase(month: input.month),
            input.fundMinutes > 0
        else { return nil }

        let coefficient = min(max(input.coefficient, 0.1), 10)
        let years = min(max(input.completedYearsService, 0), 60)
        let baseGross = officialBase * coefficient
        let seniority = baseGross * CroatianPayrollRulesIOS.seniorityRatePerYear * Double(years)
        let fullFundGross = baseGross + seniority
        let hourly = fullFundGross / (Double(input.fundMinutes) / 60.0)

        func pay(_ minutes: Int, _ multiplier: Double = 1.0) -> Double {
            hourly * (Double(max(0, minutes)) / 60.0) * multiplier
        }

        let regularWorkGross = pay(input.regularWorkedMinutes)
        let annualLeaveGross = pay(input.annualLeaveMinutes, max(0, input.annualLeaveFactor))
        let sickLeaveGross = pay(input.sickLeaveMinutes, min(max(input.sickPayRate, 0), 1))
        let otherPaidAbsenceGross = pay(input.otherPaidAbsenceMinutes)
        let holidayCreditGross = pay(input.holidayCreditMinutes)
        let overtimeBaseGross = pay(input.overtimeMinutes)
        let nightAddition = pay(input.nightMinutes, input.rates.night)
        let saturdayAddition = pay(input.saturdayMinutes, input.rates.saturday)
        let sundayAddition = pay(input.sundayMinutes, input.rates.sunday)
        let holidayAddition = pay(input.holidayWorkedMinutes, input.rates.holiday)
        let overtimeAddition = pay(input.overtimeMinutes, input.rates.overtime)

        let actualWorkedMinutes = max(0, input.regularWorkedMinutes) + max(0, input.overtimeMinutes)
        let turnusAddition = input.turnusEnabled ? pay(actualWorkedMinutes, input.rates.turnus) : 0
        let secondShiftAddition = input.turnusEnabled ? 0 : pay(input.secondShiftMinutes, input.rates.secondShift)

        let grossOne = regularWorkGross + annualLeaveGross + sickLeaveGross +
            otherPaidAbsenceGross + holidayCreditGross + overtimeBaseGross +
            nightAddition + saturdayAddition + sundayAddition + holidayAddition +
            overtimeAddition + secondShiftAddition + turnusAddition

        let pensionFirst = grossOne * CroatianPayrollRulesIOS.pensionFirstPillarRate
        let pensionSecond = grossOne * CroatianPayrollRulesIOS.pensionSecondPillarRate
        let pensionTotal = pensionFirst + pensionSecond

        let allowance = CroatianPayrollRulesIOS.personalAllowance(
            children: input.children,
            dependents: input.dependents,
            disabilityAllowance: input.disabilityAllowance
        )
        let taxable = max(0, grossOne - pensionTotal - allowance)
        let lowerTaxBase = min(taxable, CroatianPayrollRulesIOS.monthlyLowerTaxThreshold)
        let higherTaxBase = max(0, taxable - CroatianPayrollRulesIOS.monthlyLowerTaxThreshold)
        let lowerTax = lowerTaxBase * min(max(input.taxRates.lowerPercent, 0), 100) / 100
        let higherTax = higherTaxBase * min(max(input.taxRates.higherPercent, 0), 100) / 100
        let tax = lowerTax + higherTax
        let net = max(0, grossOne - pensionTotal - tax)

        let year = Calendar.raspored.component(.year, from: input.month)
        let youthFraction = CroatianPayrollRulesIOS.youthAnnualReliefFraction(
            taxYear: year,
            birthYear: input.birthYear
        )
        let youthRefundShare = lowerTax * youthFraction
        let employerHealth = grossOne * CroatianPayrollRulesIOS.employerHealthRate

        return PayrollEstimateIOS(
            officialBase: officialBase,
            baseGrossForFullFund: fullFundGross,
            seniorityAddition: seniority,
            hourlyGross: hourly,
            regularWorkGross: regularWorkGross,
            annualLeaveGross: annualLeaveGross,
            sickLeaveGross: sickLeaveGross,
            otherPaidAbsenceGross: otherPaidAbsenceGross,
            holidayCreditGross: holidayCreditGross,
            overtimeBaseGross: overtimeBaseGross,
            nightAddition: nightAddition,
            saturdayAddition: saturdayAddition,
            sundayAddition: sundayAddition,
            holidayAddition: holidayAddition,
            secondShiftAddition: secondShiftAddition,
            turnusAddition: turnusAddition,
            overtimeAddition: overtimeAddition,
            grossOne: grossOne,
            pensionFirstPillar: pensionFirst,
            pensionSecondPillar: pensionSecond,
            personalAllowance: allowance,
            taxableIncome: taxable,
            monthlyIncomeTax: tax,
            netBeforeAnnualYouthRelief: net,
            youthAnnualReliefFraction: youthFraction,
            estimatedYouthRefundShareForMonth: youthRefundShare,
            estimatedNetAfterAnnualYouthRelief: net + youthRefundShare,
            employerHealthContribution: employerHealth,
            grossTwo: grossOne + employerHealth
        )
    }
}
