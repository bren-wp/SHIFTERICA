import Foundation
import SwiftUI

/// Local, user-entered accounting values. No payslip or identity is persisted.
@MainActor
final class MonthlyAccountingStoreIOS: ObservableObject {
    @Published private(set) var fundHours: [String: Int] = [:]
    @Published private(set) var confirmedCents: [String: Int64] = [:]


    @Published var serviceYears = 0
    @Published var children = 0
    @Published var dependents = 0
    @Published var annualLeaveHourlyGross = 0.0

    func saveProfile() {
        defaults.set(serviceYears, forKey: "raspored.profile.serviceYears")
        defaults.set(children, forKey: "raspored.profile.children")
        defaults.set(dependents, forKey: "raspored.profile.dependents")
        defaults.set(annualLeaveHourlyGross, forKey: "raspored.profile.annualLeaveHourlyGross")
    }

    private let defaults = UserDefaults.standard
    private let fundKey = "raspored.accounting.fund.v1"
    private let netKey = "raspored.accounting.net.v1"


    init() {
        serviceYears = min(60, max(0, defaults.integer(forKey: "raspored.profile.serviceYears")))
        children = min(9, max(0, defaults.integer(forKey: "raspored.profile.children")))
        dependents = min(10, max(0, defaults.integer(forKey: "raspored.profile.dependents")))
        annualLeaveHourlyGross = min(1000, max(0,
            defaults.double(forKey: "raspored.profile.annualLeaveHourlyGross")))
        let storedFund = defaults.dictionary(forKey: fundKey) as? [String: Int] ?? [:]
        fundHours = storedFund.filter { (0...744).contains($0.value) }
        let storedNet = defaults.dictionary(forKey: netKey) as? [String: NSNumber] ?? [:]
        confirmedCents = storedNet.reduce(into: [:]) { result, item in
            let value = item.value.int64Value
            if (0...100_000_000).contains(value) { result[item.key] = value }
        }
    }

    private func key(_ month: Date) -> String {
        let parts = Calendar.raspored.dateComponents([.year, .month], from: month)
        return String(format: "%04d-%02d", parts.year ?? 0, parts.month ?? 0)
    }

    func fundOverrideMinutes(_ month: Date) -> Int? {
        fundHours[key(month)].map { $0 * 60 }
    }

    func setFundHours(_ hours: Int?, month: Date) {
        let k = key(month)
        if let hours { fundHours[k] = min(max(hours, 0), 744) }
        else { fundHours.removeValue(forKey: k) }
        defaults.set(fundHours, forKey: fundKey)
    }

    func actualNet(_ month: Date) -> Double? {
        confirmedCents[key(month)].map { Double($0) / 100 }
    }

    func setActualNet(_ euros: Double?, month: Date) {
        let k = key(month)
        if let euros, euros.isFinite, euros >= 0 {
            confirmedCents[k] = Int64((min(euros, 1_000_000) * 100).rounded())
        } else {
            confirmedCents.removeValue(forKey: k)
        }
        defaults.set(confirmedCents, forKey: netKey)
    }

    func actualForYear(_ year: Int) -> [(String, Double)] {
        confirmedCents
            .filter { $0.key.hasPrefix(String(format: "%04d-", year)) }
            .sorted { $0.key < $1.key }
            .map { ($0.key, Double($0.value) / 100) }
    }

    func latestThreeActual(upTo month: Date) -> [Double] {
        let bound = key(month)
        return confirmedCents
            .filter { $0.key <= bound }
            .sorted { $0.key > $1.key }
            .prefix(3)
            .map { Double($0.value) / 100 }
    }
}
