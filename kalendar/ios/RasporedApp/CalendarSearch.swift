import Foundation

/// Same Croatian accent-insensitive lookup as on Android.
enum CalendarSearch {
    private static let locale = Locale(identifier: "hr_HR")

    static func searchable(_ text: String) -> String {
        text.folding(options: [.diacriticInsensitive, .caseInsensitive], locale: locale)
            .lowercased(with: locale)
            .replacingOccurrences(of: "đ", with: "d")
    }

    /// Identical day/global/category filtering semantics as Android.
    static func displayed(
        _ entries: [CalendarEntry], day: String, query: String,
        searchAll: Bool, category: String = "Sve"
    ) -> [CalendarEntry] {
        let base: [CalendarEntry]
        if !query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            base = find(entries, query: query)
        } else if searchAll {
            base = entries.sorted {
                if $0.date != $1.date { return $0.date < $1.date }
                if $0.start != $1.start { return $0.start < $1.start }
                return $0.id < $1.id
            }
        } else {
            base = CalendarRules.forDay(entries, key: day)
        }
        return category == "Sve" ? base : base.filter { $0.category == category }
    }

    static func find(_ entries: [CalendarEntry], query: String) -> [CalendarEntry] {
        let term = searchable(query.trimmingCharacters(in: .whitespacesAndNewlines))
        guard !term.isEmpty else { return [] }
        return entries.filter { entry in
            [entry.date, entry.title, entry.note, entry.category,
             entry.start, entry.end].contains { searchable($0).contains(term) }
        }.sorted {
            if $0.date != $1.date { return $0.date < $1.date }
            if $0.start != $1.start { return $0.start < $1.start }
            return $0.id < $1.id
        }
    }
}
