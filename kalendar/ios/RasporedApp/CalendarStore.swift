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
            guard next.count < CalendarBackup.maxEntries else { return false }
            next.append(item)
        }
        guard next.count <= CalendarBackup.maxEntries else { return false }
        return persist(next)
    }

    /// Merge-only import, with all-record validation before touching preferences.
    func mergeMissing(_ imported: [CalendarEntry]) throws -> Int {
        guard imported.count <= CalendarBackup.maxEntries,
              imported.allSatisfy(CalendarRules.valid),
              Set(imported.map(\.id)).count == imported.count else {
            throw CalendarBackupError.invalidEntries
        }
        let ids = Set(entries.map(\.id))
        let fresh = imported.filter { !ids.contains($0.id) }
        if fresh.isEmpty { return 0 }
        let next = entries + fresh
        guard next.count <= CalendarBackup.maxEntries else {
            throw CalendarBackupError.oversized
        }
        guard persist(next) else { throw CalendarBackupError.invalidEntries }
        return fresh.count
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
