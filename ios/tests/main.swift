import Foundation

// Deterministic platform-neutral scheduling checks for the iOS planner.
// Run on the macOS CI runner without requiring simulator permissions.
func check(_ condition: @autoclosure () -> Bool, _ message: String) {
    guard condition() else {
        fputs("FAIL: \(message)\n", stderr)
        exit(1)
    }
}

let calendar = Calendar.raspored
func day(_ key: String) -> Date {
    guard let value = DateFormatter.scheduleKey.date(from: key) else {
        fatalError("Invalid fixture date: \(key)")
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

let shiftedNight = ShiftReminderPlanIOS.upcoming(
    ["2026-10-13": "N"], now: at("2026-10-12", 19),
    shiftTimes: ["N": (start: "21:30", end: "09:30")]
)
check(shiftedNight.count == 2, "Edited N must retain both notifications")
check(calendar.component(.hour, from: shiftedNight.last!.fireDate) == 20 &&
      calendar.component(.minute, from: shiftedNight.last!.fireDate) == 30,
      "Edited N start at 21:30 must move departure reminder to 20:30")

let invalidStart = ShiftReminderPlanIOS.upcoming(
    ["2026-10-13": "D"], now: at("2026-10-12", 19),
    shiftTimes: ["D": (start: "invalid", end: "19:00")]
)
check(calendar.component(.hour, from: invalidStart.last!.fireDate) == 6,
      "Invalid custom time must keep the safe default 06:00")

// Backup v1 regression checks: hours optional for old color-only exports.
for code in ["N", "D", "P", "J", "GO", "BO"] {
    check(ShiftBackupTimeRulesIOS.valid(code: code, start: nil, end: nil),
          "Old built-in color-only backup must remain valid for \(code)")
}
check(ShiftBackupTimeRulesIOS.valid(code: "N", start: "20:30", end: "08:30"),
      "Night hours crossing midnight must survive backup")
check(ShiftBackupTimeRulesIOS.valid(code: "D", start: "07:15", end: "19:00"),
      "Minute-precision work hours must survive backup")
for (code, start, end) in [
    ("N", "19:00", "19:00"),
    ("D", "25:00", "19:00"),
    ("P", "14:60", "22:00"),
    ("J", "7:00", "15:00"),
    ("GO", "07:00", "15:00")
] {
    check(!ShiftBackupTimeRulesIOS.valid(code: code, start: start, end: end),
          "Invalid imported hours must be rejected for \(code)")
}
check(!ShiftBackupTimeRulesIOS.valid(code: "N", start: "19:00", end: nil),
      "Incomplete time pairs must be rejected")
check(!ShiftBackupTimeRulesIOS.valid(code: "UNKNOWN", start: nil, end: nil),
      "Unknown built-in code must be rejected")

// A saved calendar code must never lose its custom shift definition by accident.
let assignedCodes = ["XY", "D", "XY", "N", "GO", "XY"]
check(ShiftDeletionPolicyIOS.assignedDates(assignedCodes, code: "XY") == 3,
      "Count custom shift occurrences across the full schedule")
check(!ShiftDeletionPolicyIOS.canDelete(assignedCodes, code: "XY"),
      "Used custom shifts must not be deleted")
check(ShiftDeletionPolicyIOS.canDelete(assignedCodes, code: "AB"),
      "Unused custom shifts may be deleted only after confirmation")
check(ShiftDeletionPolicyIOS.canDelete(["D", "GO"], code: "XY"),
      "After removing shift from all dates, deletion may proceed")

// v1 backup must validate all custom shift intervals before saving any imported shift.
check(ShiftBackupTimeRulesIOS.validCustom(
    start: "22:30", end: "06:30", secondaryStart: "09:00", secondaryEnd: "11:00"
), "Overnight plus secondary custom intervals are valid")
check(ShiftBackupTimeRulesIOS.validCustom(
    start: nil, end: nil, secondaryStart: "11:00", secondaryEnd: "13:00"
), "Previously supported secondary-only custom shift stays importable")
for (start, end, secondStart, secondEnd) in [
    ("07:00", nil, nil, nil),
    (nil, "19:00", nil, nil),
    ("07:00", "19:00", "11:00", nil),
    ("07:00", "19:00", nil, "13:00"),
    ("25:00", "19:00", nil, nil),
    ("07:00", "19:00", "09:60", "11:00")
] {
    check(!ShiftBackupTimeRulesIOS.validCustom(
        start: start, end: end,
        secondaryStart: secondStart, secondaryEnd: secondEnd
    ), "Incomplete or invalid imported custom hours must be rejected")
}

// Shift manager navigation must find a real date without changing entries.
let shiftUsage = [
    "2026-10-09": "XY",
    "2026-10-11": "N",
    "2026-10-16": "XY",
    "2026-12-10": "XY"
]
let nearest = ShiftUsageNavigatorIOS.closestDate(
    entries: shiftUsage, code: "XY", today: day("2026-10-10")
)
check(nearest.map { DateFormatter.scheduleKey.string(from: $0) } == "2026-10-16",
      "Custom shift navigation opens nearest future assigned date")
let mostRecent = ShiftUsageNavigatorIOS.closestDate(
    entries: shiftUsage, code: "XY", today: day("2027-01-01")
)
check(mostRecent.map { DateFormatter.scheduleKey.string(from: $0) } == "2026-12-10",
      "Custom shift navigation falls back to latest past date")
check(ShiftUsageNavigatorIOS.closestDate(
    entries: shiftUsage, code: "AB", today: day("2026-10-10")
) == nil, "Never navigate when the shift is absent from calendar")

// Search must show upcoming dates before recent history and support shift abbreviations.
let searchDates = [
    "2024-01-01": "N",
    "2026-10-08": "D",
    "2026-10-13": "N",
    "2026-10-10": "XY",
    "2026-10-06": "N"
]
let searchNames = ["N": "Noćna smjena", "D": "Dnevna smjena", "XY": "Posebna smjena"]
let ranked = ScheduleSearchIOS.find(
    entries: searchDates, namesByCode: searchNames, query: "",
    todayKey: "2026-10-09"
)
check(ranked.map { $0.0 } == [
    "2026-10-10", "2026-10-13", "2026-10-08", "2026-10-06", "2024-01-01"
], "Upcoming shifts first, then recent past dates")
check(ScheduleSearchIOS.find(
    entries: searchDates, namesByCode: searchNames,
    query: "xy", todayKey: "2026-10-09"
).map { $0.0 } == ["2026-10-10"], "Search matches custom code")
check(ScheduleSearchIOS.find(
    entries: searchDates, namesByCode: searchNames,
    query: "posebna", todayKey: "2026-10-09"
).map { $0.0 } == ["2026-10-10"], "Search matches custom shift name")
check(ScheduleSearchIOS.find(
    entries: searchDates, namesByCode: searchNames,
    query: "2026-10-13", todayKey: "2026-10-09"
).map { $0.0 } == ["2026-10-13"], "Search matches exact date")
check(ScheduleSearchIOS.find(
    entries: searchDates.merging(["2026-10-10": "D"]) { _, new in new },
    namesByCode: searchNames, query: "XY", todayKey: "2026-10-09"
).isEmpty, "Search does not retain stale results when date's shift changes")
check(ScheduleSearchIOS.find(
    entries: searchDates, namesByCode: searchNames, query: "",
    todayKey: "2026-10-09", limit: 2
).count == 2, "Search respects result limit")

// Users may search Croatian names without typing accented letters.
let localizedSearch = ["2026-10-10": "N", "2026-10-11": "XY"]
let localizedNames = ["N": "Noćna smjena", "XY": "Đurđica čuvarska"]
check(ScheduleSearchIOS.find(
    entries: localizedSearch, namesByCode: localizedNames,
    query: "  NOCNA  ", todayKey: "2026-10-09"
).map { $0.0 } == ["2026-10-10"], "Unaccented night shift search")
check(ScheduleSearchIOS.find(
    entries: localizedSearch, namesByCode: localizedNames,
    query: "noćna", todayKey: "2026-10-09"
).map { $0.0 } == ["2026-10-10"], "Accented night shift search")
check(ScheduleSearchIOS.find(
    entries: localizedSearch, namesByCode: localizedNames,
    query: "durdica cuvarska", todayKey: "2026-10-09"
).map { $0.0 } == ["2026-10-11"], "Croatian đ/č/ć normalized search")

print("iOS reminders, scheduling and Croatian search checks passed")
