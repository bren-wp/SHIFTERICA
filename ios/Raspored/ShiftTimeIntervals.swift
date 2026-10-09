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
