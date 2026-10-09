import Foundation

struct ShiftReminderEventIOS {
    enum Kind: String { case evening, departure }

    let date: Date
    let code: String
    let start: String
    let end: String
    let kind: Kind
    let fireDate: Date

    var id: String {
        "shift:\(DateFormatter.scheduleKey.string(from: date)):\(kind.rawValue)"
    }

    var title: String {
        if kind == .evening { return "Sutra imate smjenu \(code)" }
        return "Smjena \(code) danas u \(start)"
    }

    var message: String {
        if kind == .evening {
            return "Sutra radite \(code) od \(start) do \(end)."
        }
        return "Pripremite se za smjenu \(code) koja počinje u \(start)."
    }
}

/// Mirrors Android: one 20:00 evening notice, then 06:00 D / 18:00 N.
/// No demo shifts and no dependence on a remote service.
enum ShiftReminderPlanIOS {
    static func upcoming(
        _ entries: [String: String],
        now: Date = Date(),
        evening: Bool = true,
        departure: Bool = true,
        shiftTimes: [String: (start: String, end: String)] = [:]
    ) -> [ShiftReminderEventIOS] {
        let calendar = Calendar.raspored
        // Match Android's rolling 60-day horizon, inclusive of today.
        let today = calendar.startOfDay(for: now)
        let limit = calendar.date(byAdding: .day, value: 60, to: today) ?? today
        var result: [ShiftReminderEventIOS] = []
        for (key, code) in entries where code == "D" || code == "N" {
            guard let date = DateFormatter.scheduleKey.date(from: key),
                  date >= today, date <= limit else { continue }
            let defaultStart = code == "D" ? "07:00" : "19:00"
            let defaultEnd = code == "D" ? "19:00" : "07:00"
            let start = shiftTimes[code]?.start ?? defaultStart
            let end = shiftTimes[code]?.end ?? defaultEnd
            if evening, let priorDay = calendar.date(byAdding: .day, value: -1, to: date),
               let fire = calendar.date(bySettingHour: 20, minute: 0, second: 0, of: priorDay),
               fire > now {
                result.append(ShiftReminderEventIOS(
                    date: date, code: code, start: start, end: end,
                    kind: .evening, fireDate: fire
                ))
            }
            let parts = start.split(separator: ":").compactMap { Int($0) }
            let valid = parts.count == 2 && (0...23).contains(parts[0]) &&
                (0...59).contains(parts[1])
            let hour = valid ? parts[0] : (code == "D" ? 7 : 19)
            let minute = valid ? parts[1] : 0
            if departure, let shiftStart = calendar.date(
                bySettingHour: hour, minute: minute, second: 0, of: date
            ) {
                let fire = shiftStart.addingTimeInterval(-3600)
                if fire > now {
                    result.append(ShiftReminderEventIOS(
                        date: date, code: code, start: start, end: end,
                        kind: .departure, fireDate: fire
                    ))
                }
            }
        }
        // iOS keeps a limited number of pending local notifications.
        return Array(result.sorted { $0.fireDate < $1.fireDate }.prefix(60))
    }
}
