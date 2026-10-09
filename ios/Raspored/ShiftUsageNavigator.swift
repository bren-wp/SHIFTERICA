import Foundation

/** Finds the next recorded date for a shift, or the latest past date. */
enum ShiftUsageNavigatorIOS {
    static func closestDate(
        entries: [String: String],
        code: String,
        today: Date
    ) -> Date? {
        let reference = DateFormatter.scheduleKey.string(from: today)
        let matching = entries.compactMap { key, value -> String? in
            guard value == code, DateFormatter.scheduleKey.date(from: key) != nil else {
                return nil
            }
            return key
        }
        guard !matching.isEmpty else { return nil }
        let nearest = matching.filter { $0 >= reference }.min() ?? matching.max()!
        return DateFormatter.scheduleKey.date(from: nearest)
    }
}
