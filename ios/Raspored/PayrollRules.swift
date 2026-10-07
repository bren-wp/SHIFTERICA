import Foundation

struct PayrollPremiumRatesIOS {
    let night: Double
    let overtime: Double
    let saturday: Double
    let sunday: Double
    let holiday: Double
    let secondShift: Double
    let turnus: Double
}

enum CroatianPayrollRulesIOS {
    static let basicPersonalAllowance = 600.0
    static let monthlyHigherRateThreshold = 5_000.0
    static let pensionFirstPillarRate = 0.15
    static let pensionSecondPillarRate = 0.05
    static let employerHealthRate = 0.165
    static let sickPayDefaultRate = 0.85

    static let rijekaLowerTaxRate = 0.20
    static let rijekaHigherTaxRate = 0.25
    static let defaultCoefficient = 1.25
    static let defaultOfficialBase = 1_025.00


    static func officialBase(month: Date) -> Double? {
        let year = Calendar.raspored.component(.year, from: month)
        return year == 2026 ? defaultOfficialBase : nil
    }

    static func premiumRates() -> PayrollPremiumRatesIOS {
        PayrollPremiumRatesIOS(
            night: 0.50,
            overtime: 0.50,
            saturday: 0.25,
            sunday: 0.50,
            holiday: 1.50,
            secondShift: 0.10,
            turnus: 0.05
        )
    }

    static func personalAllowance(children: Int = 0, dependents: Int = 0) -> Double {
        let childIncrements = [
            300.0, 420.0, 600.0, 840.0, 1140.0,
            1500.0, 1920.0, 2400.0, 2940.0
        ]
        let safeChildren = min(max(children, 0), childIncrements.count)
        let childPart = childIncrements.prefix(safeChildren).reduce(0, +)
        return basicPersonalAllowance + childPart + Double(max(0, dependents)) * 300.0
    }

    static func youthAnnualReliefFraction(taxYear: Int, birthYear: Int?) -> Double {
        guard let birthYear, birthYear > 0, birthYear <= taxYear else { return 0 }
        let age = taxYear - birthYear
        if age <= 25 { return 1.0 }
        if (26...30).contains(age) { return 0.5 }
        return 0.0
    }
}
