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

        let monday = calendar.date(
            from: DateComponents(year: 2026, month: 10, day: 5)
        )!
        let overlapDay = ShiftTimeIntervalsIOS.combinedMinuteInstants(
            on: monday,
            firstStart: "08:00", firstEnd: "16:00",
            secondStart: "14:00", secondEnd: "20:00",
            calendar: calendar
        )
        check(overlapDay.count == 12 * 60,
              "Overlapping day intervals must not add phantom overtime")
        check(Set(overlapDay).count == overlapDay.count,
              "Every real worked minute is counted at most once")
        let saturday = calendar.date(
            from: DateComponents(year: 2026, month: 10, day: 3)
        )!
        let overlapNight = ShiftTimeIntervalsIOS.combinedMinuteInstants(
            on: saturday,
            firstStart: "20:00", firstEnd: "04:00",
            secondStart: "22:00", secondEnd: "02:00",
            calendar: calendar
        )
        check(overlapNight.count == 8 * 60, "Midnight overlap counts eight hours")
        check(overlapNight.filter {
            let hour = calendar.component(.hour, from: $0)
            return hour >= 22 || hour < 6
        }.count == 6 * 60, "Midnight overlap night premium cannot double")
        check(overlapNight.filter {
            calendar.component(.weekday, from: $0) == 1
        }.count == 4 * 60, "Midnight overlap Sunday premium cannot double")
        let fallSaturday = calendar.date(
            from: DateComponents(year: 2026, month: 10, day: 24)
        )!
        let overlapFall = ShiftTimeIntervalsIOS.combinedMinuteInstants(
            on: fallSaturday,
            firstStart: "19:00", firstEnd: "07:00",
            secondStart: "21:00", secondEnd: "23:00",
            calendar: calendar
        )
        check(overlapFall.count == 13 * 60,
              "Autumn repeated clock hour must survive real-instant deduplication")
        check(overlapFall.filter {
            let hour = calendar.component(.hour, from: $0)
            return hour >= 22 || hour < 6
        }.count == 9 * 60, "Autumn repeated night premium stays correct")

        check(ShiftTimeIntervalsIOS.plannedDurationMinutes(
            firstStart: "08:00", firstEnd: "16:00",
            secondStart: "14:00", secondEnd: "20:00"
        ) == 720, "Shift manager shows twelve planned hours, not fourteen")
        check(ShiftTimeIntervalsIOS.plannedDurationMinutes(
            firstStart: "20:00", firstEnd: "04:00",
            secondStart: "22:00", secondEnd: "02:00"
        ) == 480, "Midnight overlap shows eight planned hours")
        check(ShiftTimeIntervalsIOS.plannedDurationMinutes(
            firstStart: "10:00", firstEnd: "14:00",
            secondStart: "16:00", secondEnd: "20:00"
        ) == 480, "Non-overlapping split shifts retain the full sum")
        check(ShiftTimeIntervalsIOS.plannedDurationMinutes(
            firstStart: nil, firstEnd: nil,
            secondStart: nil, secondEnd: nil
        ) == 0, "Time-free shifts still display zero hours")
        check(ShiftTimeIntervalsIOS.plannedDurationMinutes(
            firstStart: "08:00", firstEnd: "08:00",
            secondStart: nil, secondEnd: nil
        ) == 1440, "Equal start and end still mean an all-day interval")

        let overnight = calendar.date(
            from: DateComponents(year: 2026, month: 10, day: 5)
        )!
        let nextDaySecondary = ShiftTimeIntervalsIOS.combinedMinuteInstants(
            on: overnight,
            firstStart: "20:00", firstEnd: "06:00",
            secondStart: "01:00", secondEnd: "04:00",
            calendar: calendar
        )
        check(nextDaySecondary.count == 10 * 60,
              "Secondary 01-04 belongs after midnight, not previous morning")
        check(nextDaySecondary.filter {
            calendar.isDate($0, inSameDayAs: overnight)
        }.count == 4 * 60, "Primary midnight boundary")
        check(ShiftTimeIntervalsIOS.plannedDurationMinutes(
            firstStart: "20:00", firstEnd: "06:00",
            secondStart: "01:00", secondEnd: "04:00"
        ) == 600, "Displayed duration matches actual 10-hour union")
        check(ShiftTimeIntervalsIOS.plannedDurationMinutes(
            firstStart: "20:00", firstEnd: "06:00",
            secondStart: "07:00", secondEnd: "09:00"
        ) == 720, "Post-night morning extension belongs on following day")
        check(ShiftTimeIntervalsIOS.plannedDurationMinutes(
            firstStart: "20:00", firstEnd: "06:00",
            secondStart: "18:00", secondEnd: "19:00"
        ) == 660, "Evening segment before main shift stays on same day")

        let lastOctober = calendar.date(
            from: DateComponents(year: 2026, month: 10, day: 31)
        )!
        let monthSpanning = ShiftTimeIntervalsIOS.combinedMinuteInstants(
            on: lastOctober,
            firstStart: "20:00", firstEnd: "06:00",
            secondStart: "01:00", secondEnd: "04:00",
            calendar: calendar
        )
        check(monthSpanning.count == 600, "No phantom October minutes")
        check(monthSpanning.filter {
            calendar.component(.month, from: $0) == 11
        }.count == 360, "Six actual hours move into November")

        for (month, day, expected) in [(3, 28, 540), (10, 24, 660)] {
            let date = calendar.date(
                from: DateComponents(year: 2026, month: month, day: day)
            )!
            let actual = ShiftTimeIntervalsIOS.combinedMinuteInstants(
                on: date,
                firstStart: "20:00", firstEnd: "06:00",
                secondStart: "01:00", secondEnd: "04:00",
                calendar: calendar
            )
            check(actual.count == expected,
                  "DST adjustment and post-midnight secondary remain consistent")
        }

        print("iOS DST, overnight intervals and shift-premium checks passed")
    }
}
