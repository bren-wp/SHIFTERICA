import Foundation

// Deterministic platform-neutral scheduling checks for the iOS planner.
// Run on the macOS CI runner without requiring simulator permissions.
func check(_ condition: @autoclosure () -> Bool, _ message: String) {
    guard condition() else {
        fputs("FAIL: \\(message)\n", stderr)
        exit(1)
    }
}

let calendar = Calendar.raspored
func day(_ key: String) -> Date {
    guard let value = DateFormatter.scheduleKey.date(from: key) else {
        fatalError("Invalid fixture date: \\(key)")
    }
    return value
}
func at(_ key: String, _ hour: Int) -> Date {
    guard let value = calendar.date(bySettingHour: hour, minute: 0, second: 0, of: day(key)) else {
        fatalError("Invalid fixture time")
    }
    return value
}

let shifts = ["2026-10-12": "D", "2026-10-13": "N"]
let reminders = ShiftReminderPlanIOS.upcoming(shifts, now: at("2026-10-11", 19))
check(reminders.count == 4, "D and N each require two local reminders")
check(reminders.map(\.kind) == [.evening, .departure, .evening, .departure],
      "Reminder kinds must be in chronological order")
check(reminders.map { calendar.component(.hour, from: $0.fireDate) } == [20, 6, 20, 18],
      "D departure is 06:00, N departure is 18:00, previous evening is 20:00")
check(Set(reminders.map(\.id)).count == 4, "Reminder IDs must be unique")

let todayEveningGone = ShiftReminderPlanIOS.upcoming(
    ["2026-10-12": "N"], now: at("2026-10-12", 17))
check(todayEveningGone.count == 1 && todayEveningGone[0].kind == .departure,
      "Today's N departure is retained when last night's reminder has passed")

check(ShiftReminderPlanIOS.upcoming(
    ["2026-10-12": "GO", "2026-10-13": "BO"],
    now: at("2026-10-11", 19)).isEmpty,
    "Non-D/N shifts must never schedule notifications")
check(ShiftReminderPlanIOS.upcoming(
    shifts, now: at("2026-10-11", 19), evening: false
).count == 2, "Disabling evening reminders must retain departure reminders")
check(ShiftReminderPlanIOS.upcoming(
    shifts, now: at("2026-10-11", 19), departure: false
).count == 2, "Disabling departure reminders must retain evening reminders")

let baseDay = day("2026-10-11")
let edge = calendar.date(byAdding: .day, value: 60, to: baseDay)!
let beyond = calendar.date(byAdding: .day, value: 61, to: baseDay)!
let edgeKey = DateFormatter.scheduleKey.string(from: edge)
let beyondKey = DateFormatter.scheduleKey.string(from: beyond)
let horizon = ShiftReminderPlanIOS.upcoming(
    [edgeKey: "D", beyondKey: "N", "2026-10-10": "D"],
    now: at("2026-10-11", 19))
check(horizon.count == 2 && horizon.allSatisfy { $0.code == "D" },
      "Only today through day 60 may enter the rolling plan")

print("iOS reminder plan checks passed (time, toggles, IDs, 60-day horizon)")
