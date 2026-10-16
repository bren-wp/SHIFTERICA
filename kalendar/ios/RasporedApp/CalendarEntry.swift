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
        guard let gridStart = calendar.date(byAdding: .day, value: -offset, to: first),
              let monthDays = calendar.range(of: .day, in: .month, for: first)?.count
        else { return [] }
        let cellCount = ((offset + monthDays + 6) / 7) * 7
        return (0..<cellCount).compactMap {
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


/// Same calendar-only shift presets as original Raspored, without payroll models.
struct CalendarShiftPreset: Identifiable {
    let code: String
    let title: String
    let start: String
    let end: String
    let color: Int

    var id: String { code }

    func record(for date: Date) -> CalendarEntry {
        CalendarEntry(
            date: CalendarClock.key(date), title: title, category: "Smjena",
            start: start, end: end, color: color
        )
    }
}


struct CalendarShiftPresetGroup {
    let name: String
    let presets: [CalendarShiftPreset]
}

enum CalendarShiftPresets {
    /// No duplication when a quick preset was already stored for this date.
    static func alreadyScheduled(
        _ records: [CalendarEntry], preset: CalendarShiftPreset, on day: Date
    ) -> Bool {
        let key = CalendarClock.key(day)
        return records.contains { item in
            item.date == key && item.category == "Smjena" &&
            item.title == preset.title && item.start == preset.start &&
            item.end == preset.end
        }
    }

    static let builtIn: [CalendarShiftPreset] = [
        .init(code: "N", title: "Noćna smjena", start: "19:00", end: "07:00", color: 5),
        .init(code: "D", title: "Dnevna smjena", start: "07:00", end: "19:00", color: 1),
        .init(code: "J", title: "Jutarnja smjena", start: "07:00", end: "15:00", color: 0),
        .init(code: "P", title: "Popodnevna smjena", start: "14:00", end: "22:00", color: 3),
        .init(code: "GO", title: "Godišnji odmor", start: "", end: "", color: 4),
        .init(code: "BO", title: "Bolovanje", start: "", end: "", color: 2)
    ]
    /// Broader presets without wages, payroll or changes to existing local records.
    static let additionalGroups: [CalendarShiftPresetGroup] = [
        .init(name: "Rane i rotacijske", presets: [
            .init(code: "R", title: "Rana smjena", start: "05:00", end: "13:00", color: 0),
            .init(code: "S1", title: "Prva smjena", start: "06:00", end: "14:00", color: 1),
            .init(code: "S2", title: "Druga smjena", start: "14:00", end: "22:00", color: 3),
            .init(code: "S3", title: "Treća smjena", start: "22:00", end: "06:00", color: 5),
            .init(code: "V", title: "Večernja smjena", start: "16:00", end: "00:00", color: 3),
            .init(code: "U", title: "Standardna smjena", start: "08:00", end: "16:00", color: 0)
        ]),
        .init(name: "Kraće smjene", presets: [
            .init(code: "4J", title: "Četverosatna jutarnja", start: "08:00", end: "12:00", color: 0),
            .init(code: "4P", title: "Četverosatna popodnevna", start: "12:00", end: "16:00", color: 3),
            .init(code: "6J", title: "Šestosatna jutarnja", start: "06:00", end: "12:00", color: 0),
            .init(code: "6P", title: "Šestosatna popodnevna", start: "12:00", end: "18:00", color: 3),
            .init(code: "6N", title: "Šestosatna noćna", start: "00:00", end: "06:00", color: 5),
            .init(code: "6V", title: "Šestosatna večernja", start: "16:00", end: "22:00", color: 3)
        ]),
        .init(name: "Produžene i 24-satne", presets: [
            .init(code: "10D", title: "Desetosatna dnevna", start: "07:00", end: "17:00", color: 1),
            .init(code: "10N", title: "Desetosatna noćna", start: "19:00", end: "05:00", color: 5),
            .init(code: "12D", title: "Dvanaestosatna dnevna", start: "06:00", end: "18:00", color: 1),
            .init(code: "12N", title: "Dvanaestosatna noćna", start: "18:00", end: "06:00", color: 5),
            .init(code: "24", title: "Dvadesetčetverosatna smjena", start: "08:00", end: "08:00", color: 2)
        ]),
        .init(name: "Dežurstva i posebne", presets: [
            .init(code: "DE", title: "Dežurstvo", start: "08:00", end: "08:00", color: 2),
            .init(code: "ND", title: "Noćno dežurstvo", start: "19:00", end: "07:00", color: 5),
            .init(code: "PR", title: "Pripravnost", start: "", end: "", color: 2),
            .init(code: "IN", title: "Intervencija", start: "", end: "", color: 3),
            .init(code: "TE", title: "Terenski rad", start: "", end: "", color: 0),
            .init(code: "RK", title: "Rad od kuće", start: "", end: "", color: 4),
            .init(code: "DV", title: "Dvokratna smjena", start: "", end: "", color: 1),
            .init(code: "KL", title: "Klizno radno vrijeme", start: "", end: "", color: 0)
        ]),
        .init(name: "Vikendi i odsutnosti", presets: [
            .init(code: "VI", title: "Vikend smjena", start: "08:00", end: "16:00", color: 3),
            .init(code: "BL", title: "Blagdanska smjena", start: "08:00", end: "16:00", color: 3),
            .init(code: "SO", title: "Slobodan dan", start: "", end: "", color: 4),
            .init(code: "DP", title: "Dopust", start: "", end: "", color: 4),
            .init(code: "RD", title: "Roditeljski dopust", start: "", end: "", color: 4),
            .init(code: "ED", title: "Edukacija", start: "", end: "", color: 2)
        ])
    ]

    static let additional: [CalendarShiftPreset] = additionalGroups.flatMap(\.presets)
    static let all: [CalendarShiftPreset] = builtIn + additional

    static func code(for item: CalendarEntry) -> String? {
        guard item.category == "Smjena" else { return nil }
        return all.first {
            $0.title == item.title && $0.start == item.start && $0.end == item.end
        }?.code
    }

    static func matches(_ preset: CalendarShiftPreset, query: String) -> Bool {
        func normalize(_ value: String) -> String {
            value.replacingOccurrences(of: "đ", with: "d")
                .folding(options: [.caseInsensitive, .diacriticInsensitive],
                         locale: Locale(identifier: "hr_HR"))
        }
        let needle = normalize(query.trimmingCharacters(in: .whitespacesAndNewlines))
        return needle.isEmpty || [preset.code, preset.title, preset.start, preset.end]
            .contains { normalize($0).contains(needle) }
    }

}


/// Scheduled duration only. No paid-time, fund-hour or salary rules.
enum CalendarScheduledTime {
    static func minutes(_ item: CalendarEntry) -> Int {
        func time(_ value: String) -> Int? {
            let fields = value.split(separator: ":", omittingEmptySubsequences: false)
            guard fields.count == 2, let h = Int(fields[0]), let m = Int(fields[1]),
                  (0..<24).contains(h), (0..<60).contains(m) else { return nil }
            return h * 60 + m
        }
        guard let start = time(item.start), let end = time(item.end) else { return 0 }
        return start == end ? 1440 : (end - start + 1440) % 1440
    }

    static func display(_ minutes: Int) -> String {
        String(format: "%d h %d min", minutes / 60, minutes % 60)
    }
}
