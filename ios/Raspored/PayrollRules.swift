import Foundation

struct PayrollTaxRatesIOS {
    let lowerPercent: Double
    let higherPercent: Double
}

struct PayrollRatesIOS {
    let night: Double
    let overtime: Double
    let saturday: Double
    let sunday: Double
    let holiday: Double
    let secondShift: Double
    let turnus: Double

    static let publicServices = PayrollRatesIOS(
        night: 0.50,
        overtime: 0.50,
        saturday: 0.25,
        sunday: 0.50,
        holiday: 1.50,
        secondShift: 0.10,
        turnus: 0.05
    )
}

struct JobCoefficientPresetIOS: Identifiable {
    let id: String
    let label: String
    let coefficient: Double
}

enum CroatianPayrollRulesIOS {
    static let basicPersonalAllowance = 600.0
    static let monthlyLowerTaxThreshold = 5_000.0
    static let seniorityRatePerYear = 0.005
    static let pensionFirstPillarRate = 0.15
    static let pensionSecondPillarRate = 0.05
    static let employerHealthRate = 0.165
    static let defaultSickPayRate = 0.85

    static let rijeka2026TaxRates = PayrollTaxRatesIOS(
        lowerPercent: 20.0,
        higherPercent: 25.0
    )

    static let coefficientPresets = [
        JobCoefficientPresetIOS(id: "worker-iii", label: "Radnik III. vrste", coefficient: 1.25),
        JobCoefficientPresetIOS(id: "caregiver", label: "Njegovatelj", coefficient: 1.35),
        JobCoefficientPresetIOS(id: "hospital-orderly", label: "Bolničar", coefficient: 1.35),
        JobCoefficientPresetIOS(id: "medical-driver", label: "Vozač sanitetskog prijevoza", coefficient: 1.43),
        JobCoefficientPresetIOS(id: "health-worker-iii-3", label: "Zdravstveni radnik III. vrste — 3", coefficient: 1.55),
        JobCoefficientPresetIOS(id: "health-worker-iii-2", label: "Zdravstveni radnik III. vrste — 2", coefficient: 1.70),
        JobCoefficientPresetIOS(id: "health-worker-iii-1", label: "Zdravstveni radnik III. vrste — 1", coefficient: 1.78),
        JobCoefficientPresetIOS(id: "bachelor-nurse-3", label: "Medicinska sestra/tehničar prvostupnik — 3", coefficient: 1.82),
        JobCoefficientPresetIOS(id: "bachelor-nurse-2", label: "Medicinska sestra/tehničar prvostupnik — 2", coefficient: 1.87),
        JobCoefficientPresetIOS(id: "bachelor-nurse-1", label: "Medicinska sestra/tehničar prvostupnik — 1", coefficient: 1.95)
    ]

    static func publicServiceBase(month: Date) -> Double? {
        let components = Calendar.raspored.dateComponents([.year, .month], from: month)
        guard components.year == 2026, let number = components.month else { return nil }
        switch number {
        case 1...3: return 1004.87
        case 4...7: return 1015.00
        case 8...11: return 1025.00
        case 12: return 1035.00
        default: return nil
        }
    }

    static func childAllowance(_ children: Int) -> Double {
        let increments = [300.0, 420.0, 600.0, 840.0, 1140.0, 1500.0, 1920.0, 2400.0, 2940.0]
        return increments.prefix(max(0, min(children, increments.count))).reduce(0, +)
    }

    static func personalAllowance(
        children: Int,
        dependents: Int = 0,
        disabilityAllowance: Double = 0
    ) -> Double {
        basicPersonalAllowance +
            childAllowance(children) +
            Double(max(0, dependents)) * 300.0 +
            max(0, disabilityAllowance)
    }

    static func youthAnnualReliefFraction(taxYear: Int, birthYear: Int?) -> Double {
        guard let birthYear, birthYear > 0, birthYear <= taxYear else { return 0 }
        let age = taxYear - birthYear
        if age <= 25 { return 1.0 }
        if (26...30).contains(age) { return 0.5 }
        return 0.0
    }
}
