import Foundation
import UserNotifications

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
        // A newer refresh may have started while awaiting the system API.
        // Never let an obsolete refresh erase the newer plan.
        guard token == generation else { return }
        let oldIds = pending.map(\.identifier).filter { $0.hasPrefix("shift:") }
        if !oldIds.isEmpty {
            center.removePendingNotificationRequests(withIdentifiers: oldIds)
        }
        guard enabled else { return }
        let settings = await center.notificationSettings()
        guard token == generation else { return }
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
