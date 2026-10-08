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

    /// Rijeka 2025: 22/32%; from 2026: 20/25%.
    static func rijekaTaxRates(month: Date) -> (lower: Double, higher: Double) {
        Calendar.raspored.component(.year, from: month) < 2026
            ? (0.22, 0.32) : (rijekaLowerTaxRate, rijekaHigherTaxRate)
    }

    /// Službene osnovice: NN 155/2024 za 2025. i NN 11/2026 za 2026.
    /// Iznosi su dodatno provjereni prema dostavljenim obračunskim ispravama.
    static func officialBase(month: Date) -> Double? {
        let components = Calendar.raspored.dateComponents(
            [.year, .month],
            from: month
        )
        guard
            let year = components.year,
            let monthNumber = components.month
        else {
            return nil
        }

        switch year {
        case 2025:
            switch monthNumber {
            case 1: return 947.18
            case 2...8: return 975.60
            case 9...12: return 1_004.87
            default: return nil
            }
        case 2026:
            switch monthNumber {
            case 1...3: return 1_004.87
            case 4...7: return 1_015.00
            case 8...11: return 1_025.00
            case 12: return 1_035.00
            default: return nil
            }
        default:
            return nil
        }
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

    static func personalAllowance(
        children: Int = 0,
        dependents: Int = 0
    ) -> Double {
        let childIncrements = [
            300.0,
            420.0,
            600.0,
            840.0,
            1_140.0,
            1_500.0,
            1_920.0,
            2_400.0,
            2_940.0
        ]
        let safeChildren = min(max(children, 0), childIncrements.count)
        let childPart = childIncrements.prefix(safeChildren).reduce(0, +)

        return basicPersonalAllowance +
            childPart +
            Double(max(0, dependents)) * 300.0
    }

    static func youthAnnualReliefFraction(
        taxYear: Int,
        birthYear: Int?
    ) -> Double {
        guard
            let birthYear,
            birthYear > 0,
            birthYear <= taxYear
        else {
            return 0
        }

        let age = taxYear - birthYear
        if age <= 25 { return 1.0 }
        if (26...30).contains(age) { return 0.5 }
        return 0.0
    }
}
