import Foundation

/// Shift-time arithmetic shared by the iOS payroll model and CI regression tests.
enum ShiftTimeIntervalsIOS {
    static func minuteInstants(
        on date: Date,
        from startText: String?,
        to endText: String?,
        calendar: Calendar = .raspored
    ) -> [Date] {
        guard
            let startTime = parse(startText),
            let endTime = parse(endText),
            let start = calendar.date(
                bySettingHour: startTime.hour,
                minute: startTime.minute,
                second: 0,
                of: date
            ),
            var end = calendar.date(
                bySettingHour: endTime.hour,
                minute: endTime.minute,
                second: 0,
                of: date
            )
        else { return [] }

        if end <= start {
            guard let nextDayEnd = calendar.date(byAdding: .day, value: 1, to: end) else {
                return []
            }
            end = nextDayEnd
        }
        // Advance by absolute elapsed time. A skipped/repeated local hour
        // at the DST transition is counted 0/2 times, respectively.
        var result: [Date] = []
        var instant = start
        while instant < end {
            result.append(instant)
            instant = instant.addingTimeInterval(60)
        }
        return result
    }

    /// Merge both intervals by real elapsed instants. Any overlapping
    /// minutes must count only once for hours, overtime and pay supplements.
    /// Distinct Date values preserve the repeated autumn clock hour.
    static func combinedMinuteInstants(
        on date: Date,
        firstStart: String?,
        firstEnd: String?,
        secondStart: String?,
        secondEnd: String?,
        calendar: Calendar = .raspored
    ) -> [Date] {
        let first = minuteInstants(
            on: date, from: firstStart, to: firstEnd, calendar: calendar
        )
        let second = minuteInstants(
            on: date, from: secondStart, to: secondEnd, calendar: calendar
        )
        return Array(Set(first + second)).sorted()
    }

    /// Planned (wall-clock) duration shown in the shift manager. Actual
    /// worked minutes may differ on the DST transition date.
    static func plannedDurationMinutes(
        firstStart: String?,
        firstEnd: String?,
        secondStart: String?,
        secondEnd: String?
    ) -> Int {
        func bounds(_ from: String?, _ to: String?) -> (Int, Int)? {
            guard let start = parse(from), let end = parse(to) else { return nil }
            let a = start.hour * 60 + start.minute
            let rawEnd = end.hour * 60 + end.minute
            return (a, rawEnd > a ? rawEnd : rawEnd + 1440)
        }
        let first = bounds(firstStart, firstEnd)
        let second = bounds(secondStart, secondEnd)
        switch (first, second) {
        case (nil, nil): return 0
        case let (.some(a), nil): return a.1 - a.0
        case let (nil, .some(b)): return b.1 - b.0
        case let (.some(a), .some(b)):
            let overlap = max(0, min(a.1, b.1) - max(a.0, b.0))
            return (a.1 - a.0) + (b.1 - b.0) - overlap
        }
    }

    static func qualifiesForSecondShift(
        code: String,
        custom: Bool,
        start: String?,
        end: String?,
        durationMinutes: Int
    ) -> Bool {
        if code == "D" || code == "N" || code == "J" { return false }
        if code != "P" && !custom { return false }
        guard let start = parse(start), let end = parse(end) else { return false }
        let from = start.hour * 60 + start.minute
        let to = end.hour * 60 + end.minute
        return (14 * 60..<(18 * 60)).contains(from) &&
            to > from && to <= 22 * 60 &&
            (1...480).contains(durationMinutes)
    }

    private static func parse(_ value: String?) -> (hour: Int, minute: Int)? {
        guard let value else { return nil }
        let pieces = value.split(separator: ":").compactMap { Int($0) }
        guard pieces.count == 2,
            (0...23).contains(pieces[0]),
            (0...59).contains(pieces[1]) else { return nil }
        return (pieces[0], pieces[1])
    }
}
