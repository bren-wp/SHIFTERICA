import Foundation
import SwiftUI

@MainActor
final class CalendarStore: ObservableObject {
    @Published private(set) var entries: [CalendarEntry] = []
    private let defaults = UserDefaults.standard
    private let key = "hr.raspored.universal.calendar.v1"

    init() {
        guard let data = defaults.data(forKey: key),
              let stored = try? JSONDecoder().decode([CalendarEntry].self, from: data)
        else { return }
        var seen = Set<String>()
        entries = stored.prefix(100_000).filter {
            CalendarRules.valid($0) && seen.insert($0.id).inserted
        }
    }

    @discardableResult
    func save(_ item: CalendarEntry) -> Bool {
        guard CalendarRules.valid(item) else { return false }
        var next = entries
        if let index = next.firstIndex(where: { $0.id == item.id }) {
            next[index] = item
        } else {
            next.append(item)
        }
        return persist(next)
    }

    @discardableResult
    func delete(_ id: String) -> Bool {
        let next = entries.filter { $0.id != id }
        return next.count < entries.count && persist(next)
    }

    private func persist(_ next: [CalendarEntry]) -> Bool {
        guard let data = try? JSONEncoder().encode(next) else { return false }
        defaults.set(data, forKey: key)
        entries = next
        return true
    }
}
