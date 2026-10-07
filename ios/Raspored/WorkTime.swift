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
}

enum CroatianWorkTimeIOS {
    private static let fullDayMinutes = 8 * 60
    private static let paidAbsenceCodes: Set<String> = ["GO", "BO"]

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
              let range = calendar.range(of: .day, in: .month, for: month) else {
            return WorkTimeSummaryIOS(
                workedMinutes: 0,
                regularMinutes: 0,
                fundMinutes: 0,
                overtimeMinutes: 0,
                paidAbsenceMinutes: 0,
                holidayCreditMinutes: 0,
                creditedMinutes: 0,
                workedShiftCount: 0
            )
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

        for day in range {
            guard let date = calendar.date(
                from: DateComponents(year: year, month: components.month, day: day)
            ) else { continue }

            let weekday = calendar.component(.weekday, from: date)
            let fundDay = weekday != 1 && weekday != 7

            if fundDay { fund += fullDayMinutes }

            let code = schedule.code(on: date)
            if let code, let includedCodes, !includedCodes.contains(code) {
                continue
            }
            let shift = code.flatMap { shiftByCode[$0] }

            if let code, paidAbsenceCodes.contains(code) {
                if fundDay { paidAbsence += fullDayMinutes }
                continue
            }

            if let shift, shift.durationMinutes > 0 {
                worked += shift.durationMinutes
                workedCount += 1
                continue
            }

            if code == nil,
               fundDay,
               holidayKeys.contains(DateFormatter.scheduleKey.string(from: date)) {
                paidAbsence += fullDayMinutes
                holidayCredit += fullDayMinutes
            }
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
            workedShiftCount: workedCount
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
