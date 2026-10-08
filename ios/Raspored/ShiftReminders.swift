import Foundation
import UserNotifications

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
        let limit = calendar.date(byAdding: .day, value: 365, to: now) ?? now
        var result: [ShiftReminderEventIOS] = []
        for (key, code) in entries where code == "D" || code == "N" {
            guard let date = DateFormatter.scheduleKey.date(from: key),
                  date <= limit else { continue }
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

actor ShiftReminderSchedulerIOS {
    static let shared = ShiftReminderSchedulerIOS()
    private var generation = 0

    func refresh(
        entries: [String: String],
        enabled: Bool,
        evening: Bool,
        departure: Bool
    ) async {
        generation += 1
        let token = generation
        let center = UNUserNotificationCenter.current()
        let pending = await center.pendingNotificationRequests()
        let oldIds = pending.map(\.identifier).filter { $0.hasPrefix("shift:") }
        if !oldIds.isEmpty {
            center.removePendingNotificationRequests(withIdentifiers: oldIds)
        }
        guard enabled else { return }
        let settings = await center.notificationSettings()
        guard settings.authorizationStatus == .authorized ||
              settings.authorizationStatus == .provisional else { return }

        let events = ShiftReminderPlanIOS.upcoming(
            entries, evening: evening, departure: departure
        )
        for event in events {
            guard token == generation else { return }
            let content = UNMutableNotificationContent()
            content.title = event.title
            content.body = event.message
            content.sound = .default
            content.categoryIdentifier = "SHIFT_REMINDER"
            let parts = Calendar.raspored.dateComponents(
                [.year, .month, .day, .hour, .minute],
                from: event.fireDate
            )
            let trigger = UNCalendarNotificationTrigger(
                dateMatching: parts, repeats: false
            )
            let request = UNNotificationRequest(
                identifier: event.id, content: content, trigger: trigger
            )
            do {
                try await center.add(request)
            } catch {
                // Keep remaining reminders independent of a single rejected item.
                print("Shift reminder scheduling error: \(error.localizedDescription)")
            }
        }
    }
}

final class ShiftReminderForegroundDelegateIOS: NSObject, UNUserNotificationCenterDelegate {
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification
    ) async -> UNNotificationPresentationOptions {
        [.banner, .list, .sound]
    }
}
