import Foundation
import UserNotifications

actor ShiftReminderSchedulerIOS {
    static let shared = ShiftReminderSchedulerIOS()

    private struct Request {
        let entries: [String: String]
        let enabled: Bool
        let evening: Bool
        let departure: Bool
        let shiftTimes: [String: (start: String, end: String)]
    }

    // Actor methods are re-entrant at await points. Serialize system writes,
    // keeping only the newest requested plan while a refresh is running.
    private var nextRequest: Request?
    private var isApplying = false

    func refresh(
        entries: [String: String],
        enabled: Bool,
        evening: Bool,
        departure: Bool,
        shiftTimes: [String: (start: String, end: String)] = [:]
    ) async {
        nextRequest = Request(
            entries: entries, enabled: enabled, evening: evening,
            departure: departure, shiftTimes: shiftTimes
        )
        guard !isApplying else { return }
        isApplying = true
        while let request = nextRequest {
            nextRequest = nil
            await apply(request)
        }
        isApplying = false
    }

    private func apply(_ request: Request) async {
        let center = UNUserNotificationCenter.current()
        let pending = await center.pendingNotificationRequests()
        guard nextRequest == nil else { return }
        let oldIds = pending.map(\.identifier).filter { $0.hasPrefix("shift:") }
        if !oldIds.isEmpty {
            center.removePendingNotificationRequests(withIdentifiers: oldIds)
        }
        guard request.enabled else { return }
        let settings = await center.notificationSettings()
        guard nextRequest == nil else { return }
        guard settings.authorizationStatus == .authorized ||
              settings.authorizationStatus == .provisional else { return }

        let events = ShiftReminderPlanIOS.upcoming(
            request.entries, evening: request.evening,
            departure: request.departure, shiftTimes: request.shiftTimes
        )
        for event in events {
            // A newer request will cancel this plan once current add completes.
            guard nextRequest == nil else { return }
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
            let notification = UNNotificationRequest(
                identifier: event.id, content: content, trigger: trigger
            )
            do {
                try await center.add(notification)
            } catch {
                // One rejected notification must not block the remaining dates.
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
