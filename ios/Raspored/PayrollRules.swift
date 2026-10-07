import Foundation

enum PayrollSectorIOS: String, CaseIterable, Identifiable {
    case hospital = "Bolnica / javno zdravstvo"
    case publicService = "Javna služba"
    case stateService = "Državna služba"
    case privateSector = "Privatni sektor"
    case other = "Ostalo"

    var id: String { rawValue }

    static func from(_ label: String) -> PayrollSectorIOS {
        PayrollSectorIOS(rawValue: label) ?? .hospital
    }
}

struct PayrollPremiumRatesIOS {
    let night: Double
    let overtime: Double
    let saturday: Double
    let sunday: Double
    let holiday: Double
    let secondShift: Double
    let turnus: Double
}

struct PayrollCoefficientPresetIOS: Identifiable {
    let id: String
    let label: String
    let value: Double
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

    static let coefficientPresets = [
        PayrollCoefficientPresetIOS(id: "1.25", label: "Radnik III. vrste", value: 1.25),
        PayrollCoefficientPresetIOS(id: "1.43", label: "Vozač sanitetskog prijevoza", value: 1.43),
        PayrollCoefficientPresetIOS(id: "1.55", label: "Zdravstveni radnik III. vrste — 3", value: 1.55),
        PayrollCoefficientPresetIOS(id: "1.64", label: "Zdravstveni radnik / sanitetski prijevoz", value: 1.64),
        PayrollCoefficientPresetIOS(id: "1.70", label: "Zdravstveni radnik III. vrste — 2", value: 1.70),
        PayrollCoefficientPresetIOS(id: "1.78", label: "Zdravstveni radnik III. vrste — 1", value: 1.78),
        PayrollCoefficientPresetIOS(id: "1.82", label: "Prvostupnik — 3", value: 1.82),
        PayrollCoefficientPresetIOS(id: "1.87", label: "Prvostupnik — 2", value: 1.87),
        PayrollCoefficientPresetIOS(id: "1.95", label: "Prvostupnik — 1", value: 1.95)
    ]

    static func officialBase(month: Date, sector: PayrollSectorIOS) -> Double? {
        guard sector != .privateSector, sector != .other else { return nil }
        let components = Calendar.raspored.dateComponents([.year, .month], from: month)
        guard components.year == 2026, let monthNumber = components.month else { return nil }

        switch monthNumber {
        case 1...3: return 1_004.87
        case 4...7: return 1_015.00
        case 8...11: return 1_025.00
        case 12: return 1_035.00
        default: return nil
        }
    }

    static func premiumRates(sector: PayrollSectorIOS) -> PayrollPremiumRatesIOS? {
        switch sector {
        case .hospital:
            return PayrollPremiumRatesIOS(
                night: 0.50,
                overtime: 0.50,
                saturday: 0.25,
                sunday: 0.50,
                holiday: 1.50,
                secondShift: 0.10,
                turnus: 0.05
            )
        case .publicService:
            return PayrollPremiumRatesIOS(
                night: 0.40,
                overtime: 0.50,
                saturday: 0.25,
                sunday: 0.50,
                holiday: 1.50,
                secondShift: 0.10,
                turnus: 0.05
            )
        case .stateService:
            return PayrollPremiumRatesIOS(
                night: 0.50,
                overtime: 0.50,
                saturday: 0.25,
                sunday: 0.50,
                holiday: 1.50,
                secondShift: 0.10,
                turnus: 0.05
            )
        case .privateSector, .other:
            return nil
        }
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
