import Foundation

/// No mutations or remote services. Sort next occurrences first, then latest history.
enum ScheduleSearchIOS {
    static func find(
        entries: [String: String],
        namesByCode: [String: String],
        query: String,
        todayKey: String,
        limit: Int = 50
    ) -> [(String, String)] {
        guard limit > 0 else { return [] }
        let term = query.trimmingCharacters(in: .whitespacesAndNewlines)
        let matches: [(String, String)] = entries.compactMap { key, code in
            guard let date = DateFormatter.scheduleKey.date(from: key),
                  DateFormatter.scheduleKey.string(from: date) == key else {
                return nil
            }
            let name = namesByCode[code] ?? code
            guard term.isEmpty ||
                key.localizedStandardContains(term) ||
                code.localizedStandardContains(term) ||
                name.localizedStandardContains(term)
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
