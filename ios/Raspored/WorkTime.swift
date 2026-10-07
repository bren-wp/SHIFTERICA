import Foundation

struct WorkTimeSummaryIOS {
    let workedMinutes: Int
    let regularMinutes: Int
    let fundMinutes: Int
    let overtimeMinutes: Int
    let paidAbsenceMinutes: Int
    let holidayCreditMinutes: Int
    let creditedMinutes: Int
    let workedShiftCount: Int
    let dayMinutes: Int
    let nightMinutes: Int
    let saturdayMinutes: Int
    let sundayMinutes: Int
    let holidayWorkedMinutes: Int
    let secondShiftMinutes: Int
}

enum CroatianWorkTimeIOS {
    private static let fullDayMinutes = 8 * 60
    private static let paidAbsenceCodes: Set<String> = ["GO", "BO", "PD"]

    @MainActor
    static func summarize(
        month: Date,
        schedule: ScheduleStoreIOS,
        shifts: [ShiftTypeDef],
        includedCodes: Set<String>? = nil
    ) -> WorkTimeSummaryIOS {
        let calendar = Calendar.raspored
        let components = calendar.dateComponents([.year, .month], from: month)

        guard let year = components.year,
              let monthNumber = components.month,
              let range = calendar.range(of: .day, in: .month, for: month),
              let firstDate = calendar.date(
                from: DateComponents(year: year, month: monthNumber, day: 1)
              ) else {
            return zeroSummary
        }

        let shiftByCode = Dictionary(uniqueKeysWithValues: shifts.map { ($0.code, $0) })
        let holidayKeys = Set(
            holidays(year: year).keys.map { DateFormatter.scheduleKey.string(from: $0) }
        )

        var fund = 0
        var worked = 0
        var paidAbsence = 0
        var holidayCredit = 0
        var workedCount = 0
        var dayMinutes = 0
        var nightMinutes = 0
        var saturdayMinutes = 0
        var sundayMinutes = 0
        var holidayWorkedMinutes = 0
        var secondShiftMinutes = 0

        for day in range {
            guard let date = calendar.date(
                from: DateComponents(year: year, month: monthNumber, day: day)
            ) else { continue }

            let weekday = calendar.component(.weekday, from: date)
            let fundDay = weekday != 1 && weekday != 7
            if fundDay { fund += fullDayMinutes }

            let code = schedule.code(on: date)
            if let code, let includedCodes, !includedCodes.contains(code) {
                continue
            }

            if let code, paidAbsenceCodes.contains(code) {
                if fundDay { paidAbsence += fullDayMinutes }
                continue
            }

            if code == nil,
               fundDay,
               holidayKeys.contains(DateFormatter.scheduleKey.string(from: date)) {
                paidAbsence += fullDayMinutes
                holidayCredit += fullDayMinutes
            }
        }

        var candidate = calendar.date(byAdding: .day, value: -1, to: firstDate) ?? firstDate
        guard let monthEnd = calendar.date(byAdding: .month, value: 1, to: firstDate) else {
            return zeroSummary
        }

        while candidate < monthEnd {
            if let code = schedule.code(on: candidate),
               includedCodes == nil || includedCodes?.contains(code) == true,
               let shift = shiftByCode[code] {
                let slices = shiftMinuteSlices(
                    date: candidate,
                    code: code,
                    fallbackDurationMinutes: shift.durationMinutes
                )

                var contributed = false
                for cursor in slices where calendar.isDate(cursor, equalTo: firstDate, toGranularity: .month) {
                    contributed = true
                    worked += 1

                    let hour = calendar.component(.hour, from: cursor)
                    if hour >= 22 || hour < 6 { nightMinutes += 1 }
                    else { dayMinutes += 1 }

                    if hour >= 14 && hour <= 21 { secondShiftMinutes += 1 }

                    let weekday = calendar.component(.weekday, from: cursor)
                    if weekday == 7 { saturdayMinutes += 1 }
                    if weekday == 1 { sundayMinutes += 1 }

                    let key = DateFormatter.scheduleKey.string(from: cursor)
                    if holidayKeys.contains(key) { holidayWorkedMinutes += 1 }
                }

                if contributed { workedCount += 1 }
            }

            candidate = calendar.date(byAdding: .day, value: 1, to: candidate) ?? monthEnd
        }

        let remainingRegularCapacity = max(0, fund - paidAbsence)
        let regular = min(worked, remainingRegularCapacity)
        let overtime = max(0, worked - remainingRegularCapacity)

        return WorkTimeSummaryIOS(
            workedMinutes: worked,
            regularMinutes: regular,
            fundMinutes: fund,
            overtimeMinutes: overtime,
            paidAbsenceMinutes: paidAbsence,
            holidayCreditMinutes: holidayCredit,
            creditedMinutes: regular + overtime + paidAbsence,
            workedShiftCount: workedCount,
            dayMinutes: dayMinutes,
            nightMinutes: nightMinutes,
            saturdayMinutes: saturdayMinutes,
            sundayMinutes: sundayMinutes,
            holidayWorkedMinutes: holidayWorkedMinutes,
            secondShiftMinutes: secondShiftMinutes
        )
    }

    static func holidays(year: Int) -> [Date: String] {
        let calendar = Calendar.raspored

        func date(_ month: Int, _ day: Int) -> Date {
            calendar.date(
                from: DateComponents(year: year, month: month, day: day)
            )!
        }

        let easter = easterSunday(year: year)

        return [
            date(1, 1): "Nova godina",
            date(1, 6): "Bogojavljenje / Sveta tri kralja",
            easter: "Uskrs",
            calendar.date(byAdding: .day, value: 1, to: easter)!: "Uskrsni ponedjeljak",
            calendar.date(byAdding: .day, value: 60, to: easter)!: "Tijelovo",
            date(5, 1): "Praznik rada",
            date(5, 30): "Dan državnosti",
            date(6, 22): "Dan antifašističke borbe",
            date(8, 5): "Dan pobjede i domovinske zahvalnosti i Dan hrvatskih branitelja",
            date(8, 15): "Velika Gospa",
            date(11, 1): "Svi sveti",
            date(11, 18): "Dan sjećanja na žrtve Domovinskog rata i na žrtvu Vukovara i Škabrnje",
            date(12, 25): "Božić",
            date(12, 26): "Sveti Stjepan"
        ]
    }

    static func holidayName(for date: Date) -> String? {
        let year = Calendar.raspored.component(.year, from: date)
        let key = DateFormatter.scheduleKey.string(from: date)

        return holidays(year: year).first {
            DateFormatter.scheduleKey.string(from: $0.key) == key
        }?.value
    }

    private static func shiftMinuteSlices(
        date: Date,
        code: String,
        fallbackDurationMinutes: Int
    ) -> [Date] {
        let calendar = Calendar.raspored
        let startHour: Int
        let duration: Int

        switch code {
        case "D":
            startHour = 7
            duration = 12 * 60
        case "N":
            startHour = 19
            duration = 12 * 60
        case "J":
            startHour = 7
            duration = 8 * 60
        default:
            guard fallbackDurationMinutes > 0 else { return [] }
            startHour = 0
            duration = fallbackDurationMinutes
        }

        guard let start = calendar.date(
            bySettingHour: startHour,
            minute: 0,
            second: 0,
            of: date
        ) else { return [] }

        return (0..<duration).compactMap {
            calendar.date(byAdding: .minute, value: $0, to: start)
        }
    }

    private static var zeroSummary: WorkTimeSummaryIOS {
        WorkTimeSummaryIOS(
            workedMinutes: 0,
            regularMinutes: 0,
            fundMinutes: 0,
            overtimeMinutes: 0,
            paidAbsenceMinutes: 0,
            holidayCreditMinutes: 0,
            creditedMinutes: 0,
            workedShiftCount: 0,
            dayMinutes: 0,
            nightMinutes: 0,
            saturdayMinutes: 0,
            sundayMinutes: 0,
            holidayWorkedMinutes: 0,
            secondShiftMinutes: 0
        )
    }

    private static func easterSunday(year: Int) -> Date {
        let a = year % 19
        let b = year / 100
        let c = year % 100
        let d = b / 4
        let e = b % 4
        let f = (b + 8) / 25
        let g = (b - f + 1) / 3
        let h = (19 * a + b - d - g + 15) % 30
        let i = c / 4
        let k = c % 4
        let l = (32 + 2 * e + 2 * i - h - k) % 7
        let m = (a + 11 * h + 22 * l) / 451
        let month = (h + l - 7 * m + 114) / 31
        let day = (h + l - 7 * m + 114) % 31 + 1

        return Calendar.raspored.date(
            from: DateComponents(year: year, month: month, day: day)
        )!
    }
}
