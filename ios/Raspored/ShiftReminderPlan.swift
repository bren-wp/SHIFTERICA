import Foundation

struct ShiftReminderEventIOS {
    enum Kind: String { case evening, departure }

    let date: Date
    let code: String
    let kind: Kind
    let fireDate: Date

    var id: String {
        "shift:\(DateFormatter.scheduleKey.string(from: date)):\(kind.rawValue)"
    }

    var title: String {
        if kind == .evening { return "Sutra imate smjenu \(code)" }
        return code == "D" ? "Dnevna smjena danas u 07:00" : "Noćna smjena danas u 19:00"
    }

    var message: String {
        if kind == .evening {
            return code == "D"
                ? "Sutra radite D od 07:00 do 19:00. Podsjetnik u 06:00."
                : "Sutra radite N od 19:00 do 07:00. Podsjetnik u 18:00."
        }
        return code == "D"
            ? "Vrijeme je za pripremu za dnevnu smjenu."
            : "Vrijeme je za pripremu za noćnu smjenu."
    }
}

/// Mirrors Android: one 20:00 evening notice, then 06:00 D / 18:00 N.
/// No demo shifts and no dependence on a remote service.
enum ShiftReminderPlanIOS {
    static func upcoming(
        _ entries: [String: String],
        now: Date = Date(),
        evening: Bool = true,
        departure: Bool = true
    ) -> [ShiftReminderEventIOS] {
        let calendar = Calendar.raspored
        // Match Android's rolling 60-day horizon, inclusive of today.
        let today = calendar.startOfDay(for: now)
        let limit = calendar.date(byAdding: .day, value: 60, to: today) ?? today
        var result: [ShiftReminderEventIOS] = []
        for (key, code) in entries where code == "D" || code == "N" {
            guard let date = DateFormatter.scheduleKey.date(from: key),
                  date >= today, date <= limit else { continue }
            if evening, let priorDay = calendar.date(byAdding: .day, value: -1, to: date),
               let fire = calendar.date(bySettingHour: 20, minute: 0, second: 0, of: priorDay),
               fire > now {
                result.append(ShiftReminderEventIOS(
                    date: date, code: code, kind: .evening, fireDate: fire
                ))
            }
            let hour = code == "D" ? 6 : 18
            if departure, let fire = calendar.date(
                bySettingHour: hour, minute: 0, second: 0, of: date
            ), fire > now {
                result.append(ShiftReminderEventIOS(
                    date: date, code: code, kind: .departure, fireDate: fire
                ))
            }
        }
        // iOS keeps a limited number of pending local notifications.
        return Array(result.sorted { $0.fireDate < $1.fireDate }.prefix(60))
    }
}
