import Foundation

/// No mutations or remote services. Sort next occurrences first, then latest history.
enum ScheduleSearchIOS {
    /// Match Croatian shift names even when the keyboard omits č, ć, š, ž or đ.
    private static let searchLocale = Locale(identifier: "hr_HR")

    private static func searchable(_ value: String) -> String {
        value.folding(options: [.diacriticInsensitive, .caseInsensitive], locale: searchLocale)
            .lowercased(with: searchLocale)
            .replacingOccurrences(of: "đ", with: "d")
    }

    static func find(
        entries: [String: String],
        namesByCode: [String: String],
        query: String,
        todayKey: String,
        limit: Int = 50
    ) -> [(String, String)] {
        guard limit > 0 else { return [] }
        let term = searchable(query.trimmingCharacters(in: .whitespacesAndNewlines))
        let matches: [(String, String)] = entries.compactMap { key, code in
            guard let date = DateFormatter.scheduleKey.date(from: key),
                  DateFormatter.scheduleKey.string(from: date) == key else {
                return nil
            }
            let name = namesByCode[code] ?? code
            guard term.isEmpty ||
                key.contains(term) ||
                searchable(code).contains(term) ||
                searchable(name).contains(term)
            else { return nil }
            return (key, code)
        }
        return Array(matches.sorted { lhs, rhs in
            let leftPast = lhs.0 < todayKey
            let rightPast = rhs.0 < todayKey
            if leftPast != rightPast { return !leftPast }
            return leftPast ? lhs.0 > rhs.0 : lhs.0 < rhs.0
        }.prefix(limit))
    }
}
