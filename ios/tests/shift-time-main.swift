import Foundation

@main
enum ShiftTimeRegressionChecks {
    static func main() {
        func check(_ condition: @autoclosure () -> Bool, _ description: String) {
            guard condition() else {
                fputs("Shift-hour regression failed: " + description + "\n", stderr)
                exit(1)
            }
        }
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "Europe/Zagreb")!

        func night(_ month: Int, _ day: Int) -> [Date] {
            let date = calendar.date(
                from: DateComponents(year: 2026, month: month, day: day)
            )!
            return ShiftTimeIntervalsIOS.minuteInstants(
                on: date,
                from: "19:00", to: "07:00",
                calendar: calendar
            )
        }

        let spring = night(3, 28)
        check(spring.count == 11 * 60, "March spring transition: 11 actual hours")
        check(spring.filter {
            let hour = calendar.component(.hour, from: $0)
            return hour >= 22 || hour < 6
        }.count == 7 * 60, "Spring night bonus: seven actual hours")
        check(spring.filter {
            calendar.component(.weekday, from: $0) == 1
        }.count == 6 * 60, "Spring Sunday: six actual hours")

        let autumn = night(10, 24)
        check(autumn.count == 13 * 60, "October fall transition: 13 actual hours")
        check(autumn.filter {
            let hour = calendar.component(.hour, from: $0)
            return hour >= 22 || hour < 6
        }.count == 9 * 60, "Autumn night bonus: nine actual hours")
        check(autumn.filter {
            calendar.component(.weekday, from: $0) == 1
        }.count == 8 * 60, "Autumn Sunday: eight actual hours")

        let ordinary = night(10, 17)
        check(ordinary.count == 12 * 60, "Regular night remains 12 hours")
        check(ShiftTimeIntervalsIOS.qualifiesForSecondShift(
            code: "P", custom: false, start: "14:00", end: "22:00",
            durationMinutes: 480
        ), "Unedited afternoon shift gets afternoon premium")
        check(!ShiftTimeIntervalsIOS.qualifiesForSecondShift(
            code: "P", custom: false, start: "09:00", end: "17:00",
            durationMinutes: 480
        ), "Edited morning P is no longer an afternoon shift")
        check(!ShiftTimeIntervalsIOS.qualifiesForSecondShift(
            code: "P", custom: false, start: "15:00", end: "22:30",
            durationMinutes: 450
        ), "Afternoon eligibility does not extend beyond 22:00")
        check(ShiftTimeIntervalsIOS.qualifiesForSecondShift(
            code: "XY", custom: true, start: "15:00", end: "21:00",
            durationMinutes: 360
        ), "Valid short custom afternoon shift stays eligible")
        check(!ShiftTimeIntervalsIOS.qualifiesForSecondShift(
            code: "N", custom: false, start: "19:00", end: "07:00",
            durationMinutes: 720
        ), "Turnus night must not be counted again as afternoon")

        print("iOS DST and edited-shift premium regression checks passed")
    }
}
