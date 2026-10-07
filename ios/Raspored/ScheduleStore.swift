import Foundation
import SwiftUI

@MainActor final class ScheduleStoreIOS: ObservableObject {
    @Published private(set) var entries: [String: String] = [:]
    private let defaults = UserDefaults.standard
    private let key = "raspored.premium.schedule"

    init() {
        if let data = defaults.data(forKey: key), let decoded = try? JSONDecoder().decode([String:String].self, from: data) { entries = decoded }
    }

    func code(on date: Date) -> String? { entries[Self.keyFor(date)] }

    func set(_ code: String?, on date: Date) {
        let k = Self.keyFor(date)
        if let code { entries[k] = code } else { entries.removeValue(forKey: k) }
        persist()
    }

    func monthEntries(_ month: Date) -> [(Date,String)] {
        entries.compactMap { key, value in
            guard let date = DateFormatter.scheduleKey.date(from: key), Calendar.raspored.isDate(date, equalTo: month, toGranularity: .month) else { return nil }
            return (date, value)
        }.sorted { $0.0 < $1.0 }
    }

    func count(_ month: Date, code: String) -> Int { monthEntries(month).filter { $0.1 == code }.count }

    private func persist() {
        if let data = try? JSONEncoder().encode(entries) { defaults.set(data, forKey: key) }
    }

    static func keyFor(_ date: Date) -> String { DateFormatter.scheduleKey.string(from: date) }
}
