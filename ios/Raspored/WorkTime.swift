import Foundation

struct ObservedHospitalPremiumRatesIOS {
    let night: Double?
    let overtime: Double?
    let saturday: Double?
    let sunday: Double?
    let holiday: Double?
    let secondShift: Double?
    let turnus: Double?
}

enum CroatianWorkTimeIOS {
    private static let fullDayMinutes = 8 * 60
    private static let paidAbsenceCodes: Set<String> = ["GO", "BO", "PD"]

    /// Anonimizirane kontrolne stope očitane sa stvarnih obračunskih isprava.
    /// Svako polje je popunjeno samo kada je upravo ta stopa vidljiva na
    /// dostavljenom obračunu za odabrani mjesec; nepotvrđeno ostaje nil.
    static func observedHospitalPremiumRates(month: Date) -> ObservedHospitalPremiumRatesIOS {
        let components = Calendar.raspored.dateComponents([.year, .month], from: month)
        guard let year = components.year, let monthNumber = components.month else {
            return observedRates()
        }

        switch year * 100 + monthNumber {
        case 202412:
            return observedRates(
                night: 0.40, overtime: 0.50, saturday: 0.25,
                sunday: 0.50, holiday: 1.50, secondShift: 0.10
            )
        case 202501:
            return observedRates(
                night: 0.50, overtime: 0.50, saturday: 0.25,
                sunday: 0.50, holiday: 1.50, secondShift: 0.10
            )
        case 202502, 202503:
            return observedRates(
                night: 0.50, overtime: 0.50, saturday: 0.25,
                sunday: 0.50, secondShift: 0.10
            )
        case 202505, 202508, 202608:
            return observedRates(
                night: 0.50, overtime: 0.50, saturday: 0.25,
                sunday: 0.50, holiday: 1.50, secondShift: 0.10, turnus: 0.05
            )
        case 202507, 202510, 202607:
            return observedRates(
                night: 0.50, overtime: 0.50, saturday: 0.25,
                sunday: 0.50, secondShift: 0.10, turnus: 0.05
            )
        case 202606:
            return observedRates(
                night: 0.50, overtime: 0.50, saturday: 0.25,
                holiday: 1.50, secondShift: 0.10, turnus: 0.05
            )
        default:
            return observedRates()
        }
    }

    private static func observedRates(
        night: Double? = nil,
        overtime: Double? = nil,
        saturday: Double? = nil,
        sunday: Double? = nil,
        holiday: Double? = nil,
        secondShift: Double? = nil,
        turnus: Double? = nil
    ) -> ObservedHospitalPremiumRatesIOS {
        ObservedHospitalPremiumRatesIOS(
            night: night,
            overtime: overtime,
            saturday: saturday,
            sunday: sunday,
            holiday: holiday,
            secondShift: secondShift,
            turnus: turnus
        )
    }

    @MainActor
    static func summarize(
        month: Date,
        schedule: ScheduleStoreIOS,
        shifts: [ShiftTypeDef],
        includedCodes: Set<String>? = nil,
        fundOverrideMinutes: Int? = nil
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
        var turnusMinutes = 0

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

        var accountedInstants = Set<Date>()
        while candidate < monthEnd {
            if let code = schedule.code(on: candidate),
               includedCodes == nil || includedCodes?.contains(code) == true,
               let shift = shiftByCode[code] {
                let slices = shiftMinuteSlices(date: candidate, shift: shift)
                let secondShiftEligible = isSecondShiftEligible(code: code, shift: shift)

                var contributed = false
                for cursor in slices where calendar.isDate(cursor, equalTo: firstDate, toGranularity: .month) {
                    // Prevent double pay when two saved shifts overlap across dates.
                    guard accountedInstants.insert(cursor).inserted else { continue }
                    contributed = true
                    worked += 1
                    if code == "D" || code == "N" { turnusMinutes += 1 }

                    let hour = calendar.component(.hour, from: cursor)
                    if hour >= 22 || hour < 6 { nightMinutes += 1 }
                    else { dayMinutes += 1 }

                    if secondShiftEligible && hour >= 14 && hour <= 21 {
                        secondShiftMinutes += 1
                    }

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

        let effectiveFund = fundOverrideMinutes.map { min(max($0, 0), 744 * 60) } ?? fund
        let remainingRegularCapacity = max(0, effectiveFund - paidAbsence)
        let regular = min(worked, remainingRegularCapacity)
        let overtime = max(0, worked - remainingRegularCapacity)

        return WorkTimeSummaryIOS(
            workedMinutes: worked,
            regularMinutes: regular,
            fundMinutes: effectiveFund,
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
            secondShiftMinutes: secondShiftMinutes,
            turnusMinutes: turnusMinutes
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

    /// Respect user-edited start and end times for every shift, including D/N/J.
    private static func shiftMinuteSlices(
        date: Date,
        shift: ShiftTypeDef
    ) -> [Date] {
        ShiftTimeIntervalsIOS.combinedMinuteInstants(
            on: date,
            firstStart: shift.start,
            firstEnd: shift.end,
            secondStart: shift.secondaryStart,
            secondEnd: shift.secondaryEnd
        )
    }

    /// A long day/night turnus is not an afternoon shift despite the overlap.
    private static func isSecondShiftEligible(
        code: String,
        shift: ShiftTypeDef
    ) -> Bool {
        ShiftTimeIntervalsIOS.qualifiesForSecondShift(
            code: code,
            custom: shift.custom,
            start: shift.start,
            end: shift.end,
            durationMinutes: shift.durationMinutes
        )
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
