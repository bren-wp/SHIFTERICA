import Foundation

@main
enum MonthGridRegression {
    static func main() {
        func check(_ condition: @autoclosure () -> Bool, _ message: String) {
            guard condition() else {
                fputs("FAIL: " + message + "\n", stderr)
                exit(1)
            }
        }

        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "Europe/Zagreb")!
        calendar.locale = Locale(identifier: "hr_HR")
        let formatter = DateFormatter()
        formatter.calendar = calendar
        formatter.timeZone = calendar.timeZone
        formatter.locale = calendar.locale
        formatter.dateFormat = "yyyy-MM-dd"
        func date(_ key: String) -> Date { formatter.date(from: key)! }
        func key(_ value: Date?) -> String? { value.map(formatter.string(from:)) }

        let october = date("2026-10-01")
        let hidden = CalendarGridDates.build(
            month: october, calendar: calendar, firstWeekday: 2,
            showOutsideDays: false
        )
        check(hidden.count == 35, "October 2026 must occupy exactly five full weeks")
        check(hidden.prefix(3).allSatisfy { $0 == nil },
              "Hidden September days must remain empty Monday–Wednesday slots")
        check(key(hidden[3]) == "2026-10-01", "October 1 must fall on Thursday")
        check(key(hidden[33]) == "2026-10-31", "October 31 must remain on Saturday")
        check(hidden[34] == nil, "Hidden November 1 must retain the Sunday slot")

        let shown = CalendarGridDates.build(
            month: october, calendar: calendar, firstWeekday: 2,
            showOutsideDays: true
        )
        check(shown.count == hidden.count, "Toggling adjacent days cannot resize grid")
        check(key(shown[0]) == "2026-09-28", "First visible Monday is September 28")
        check(key(shown[34]) == "2026-11-01", "Last visible Sunday is November 1")

        let sunday = CalendarGridDates.build(
            month: october, calendar: calendar, firstWeekday: 1,
            showOutsideDays: false
        )
        check(sunday.count == 35 && sunday.prefix(4).allSatisfy { $0 == nil },
              "Sunday-first calendar keeps four empty cells before Thursday")
        check(key(sunday[4]) == "2026-10-01", "Sunday-first weekday alignment")

        let february = CalendarGridDates.build(
            month: date("2027-02-01"), calendar: calendar, firstWeekday: 2,
            showOutsideDays: false
        )
        check(february.count == 28 && key(february[0]) == "2027-02-01",
              "Four-week February uses precisely 28 cells")
        let august = CalendarGridDates.build(
            month: date("2026-08-01"), calendar: calendar, firstWeekday: 2,
            showOutsideDays: false
        )
        check(august.count == 42 && key(august[5]) == "2026-08-01",
              "Six-week August preserves Saturday column")
        let december = CalendarGridDates.build(
            month: date("2026-12-01"), calendar: calendar, firstWeekday: 2,
            showOutsideDays: true
        )
        check(key(december[0]) == "2026-11-30",
              "December grid crosses the month boundary without skipping dates")
        print("iOS calendar grid cell alignment regressions passed")
    }
}
