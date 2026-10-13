import Foundation

/// Plain offline calendar event, independent of the original payroll application.
struct CalendarEntry: Codable, Identifiable, Equatable {
    var id: String = UUID().uuidString
    var date: String
    var title: String
    var note: String = ""
    var category: String = "Smjena"
    var start: String = ""
    var end: String = ""
    var color: Int = 0
}

enum CalendarRules {
    static let categories = ["Smjena", "Događaj", "Napomena", "Slobodno", "Ostalo"]

    static func valid(_ entry: CalendarEntry) -> Bool {
        guard !entry.id.isEmpty, entry.id.count <= 128,
              CalendarClock.parse(entry.date) != nil,
              !entry.title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ||
              !entry.note.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty,
              entry.title.count <= 120, entry.note.count <= 10_000,
              categories.contains(entry.category), (0...5).contains(entry.color)
        else { return false }
        if entry.start.isEmpty && entry.end.isEmpty { return true }
        guard !entry.start.isEmpty && !entry.end.isEmpty else { return false }
        return validTime(entry.start) && validTime(entry.end)
    }

    private static func validTime(_ raw: String) -> Bool {
        guard raw.count == 5 else { return false }
        return raw.range(
            of: #"^(?:[01]\d|2[0-3]):[0-5]\d$"#, options: .regularExpression
        ) != nil
    }

    static func forDay(_ items: [CalendarEntry], key: String) -> [CalendarEntry] {
        items.filter { $0.date == key }.sorted { a, b in
            if a.start.isEmpty != b.start.isEmpty { return !a.start.isEmpty }
            if a.start != b.start { return a.start < b.start }
            return a.title < b.title
        }
    }
}

enum CalendarClock {
    static var calendar: Calendar {
        var cal = Calendar(identifier: .gregorian)
        cal.locale = Locale(identifier: "hr_HR")
        cal.firstWeekday = 2
        cal.timeZone = .current
        return cal
    }

    static func key(_ date: Date) -> String {
        let components = calendar.dateComponents([.year, .month, .day], from: date)
        return String(format: "%04d-%02d-%02d",
            components.year ?? 0, components.month ?? 0, components.day ?? 0)
    }

    static func parse(_ raw: String) -> Date? {
        guard raw.range(of: #"^\d{4}-\d{2}-\d{2}$"#,
                        options: .regularExpression) != nil else { return nil }
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.calendar = calendar
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.isLenient = false
        guard let date = formatter.date(from: raw), key(date) == raw else { return nil }
        return date
    }

    static func monthStart(_ date: Date) -> Date {
        calendar.date(from: calendar.dateComponents([.year, .month], from: date)) ?? date
    }

    /// Seven civil dates, Monday through Sunday, across month and year boundaries.
    static func weekCells(_ date: Date) -> [Date] {
        let cal = calendar
        let day = cal.startOfDay(for: date)
        let offset = (cal.component(.weekday, from: day) + 5) % 7
        guard let monday = cal.date(byAdding: .day, value: -offset, to: day)
        else { return [] }
        return (0..<7).compactMap {
            cal.date(byAdding: .day, value: $0, to: monday)
        }
    }

    static func monthCells(_ date: Date) -> [Date] {
        let first = monthStart(date)
        let offset = (calendar.component(.weekday, from: first) + 5) % 7
        guard let gridStart = calendar.date(byAdding: .day, value: -offset, to: first)
        else { return [] }
        return (0..<42).compactMap {
            calendar.date(byAdding: .day, value: $0, to: gridStart)
        }
    }

    static func title(_ date: Date, format: String) -> String {
        let formatter = DateFormatter()
        formatter.calendar = calendar
        formatter.locale = Locale(identifier: "hr_HR")
        formatter.dateFormat = format
        return formatter.string(from: date)
    }
}
