import Foundation

@MainActor
final class PayrollSettingsStoreIOS: ObservableObject {
    private let defaults = UserDefaults.standard

    @Published var enabled: Bool { didSet { defaults.set(enabled, forKey: "payroll.enabled") } }
    @Published var coefficientPresetId: String {
        didSet { defaults.set(coefficientPresetId, forKey: "payroll.coefficientPresetId") }
    }
    @Published var coefficient: Double { didSet { defaults.set(coefficient, forKey: "payroll.coefficient") } }
    @Published var completedYearsService: Int {
        didSet { defaults.set(completedYearsService, forKey: "payroll.completedYearsService") }
    }
    @Published var children: Int { didSet { defaults.set(children, forKey: "payroll.children") } }
    @Published var dependents: Int { didSet { defaults.set(dependents, forKey: "payroll.dependents") } }
    @Published var birthYear: Int { didSet { defaults.set(birthYear, forKey: "payroll.birthYear") } }
    @Published var turnusEnabled: Bool {
        didSet { defaults.set(turnusEnabled, forKey: "payroll.turnusEnabled") }
    }
    @Published var lowerTaxPercent: Double {
        didSet { defaults.set(lowerTaxPercent, forKey: "payroll.lowerTaxPercent") }
    }
    @Published var higherTaxPercent: Double {
        didSet { defaults.set(higherTaxPercent, forKey: "payroll.higherTaxPercent") }
    }

    init() {
        let d = UserDefaults.standard
        enabled = d.object(forKey: "payroll.enabled") as? Bool ?? true
        coefficientPresetId = d.string(forKey: "payroll.coefficientPresetId") ?? "worker-iii"
        coefficient = d.object(forKey: "payroll.coefficient") as? Double ?? 1.25
        completedYearsService = d.object(forKey: "payroll.completedYearsService") as? Int ?? 0
        children = d.object(forKey: "payroll.children") as? Int ?? 0
        dependents = d.object(forKey: "payroll.dependents") as? Int ?? 0
        birthYear = d.object(forKey: "payroll.birthYear") as? Int ?? 0
        turnusEnabled = d.object(forKey: "payroll.turnusEnabled") as? Bool ?? false
        lowerTaxPercent = d.object(forKey: "payroll.lowerTaxPercent") as? Double ?? 20.0
        higherTaxPercent = d.object(forKey: "payroll.higherTaxPercent") as? Double ?? 25.0
    }

    var coefficientLabel: String {
        CroatianPayrollRulesIOS.coefficientPresets
            .first(where: { $0.id == coefficientPresetId })?.label ?? "Ručni koeficijent"
    }

    func selectPreset(_ id: String) {
        guard let preset = CroatianPayrollRulesIOS.coefficientPresets.first(where: { $0.id == id }) else {
            return
        }
        coefficientPresetId = preset.id
        coefficient = preset.coefficient
    }

    func useManualCoefficient(_ value: Double) {
        coefficientPresetId = "manual"
        coefficient = min(max(value, 0.1), 10)
    }
}
