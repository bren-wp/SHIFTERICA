import Foundation

/// Builds a Monday-/user-start-aligned month grid without shifting dates when
/// days outside the active month are hidden. A nil still occupies a cell.
enum CalendarGridDates {
    static func build(
        month: Date,
        calendar: Calendar,
        firstWeekday: Int,
        showOutsideDays: Bool
    ) -> [Date?] {
        guard
            let interval = calendar.dateInterval(of: .month, for: month),
            let dayCount = calendar.range(of: .day, in: .month, for: month)?.count
        else { return [] }

        let first = interval.start
        let offset = (calendar.component(.weekday, from: first) - firstWeekday + 7) % 7
        guard let start = calendar.date(byAdding: .day, value: -offset, to: first) else {
            return []
        }

        let cellCount = ((offset + dayCount + 6) / 7) * 7
        return (0..<cellCount).map { index -> Date? in
            guard let date = calendar.date(byAdding: .day, value: index, to: start)
            else { return nil }
            if showOutsideDays || calendar.isDate(date, equalTo: month, toGranularity: .month) {
                return date
            }
            return nil
        }
    }
}
