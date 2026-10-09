import Foundation

@MainActor
func payrollEstimateForMonthIOS(
    month: Date,
    schedule: ScheduleStoreIOS,
    shifts: ShiftLibraryIOS,
    fundOverrideMinutes: Int? = nil,
    serviceYears: Int = 0,
    children: Int = 0,
    dependents: Int = 0,
    annualLeaveHourlyGross: Double = 0
) -> PayrollEstimateIOS? {
    let monthEntries = schedule.monthEntries(month)
    let summary = CroatianWorkTimeIOS.summarize(
        month: month,
        schedule: schedule,
        shifts: shifts.all,
        fundOverrideMinutes: fundOverrideMinutes
    )
    // A prior-month overnight shift may contribute real worked minutes
    // although this month's schedule has no directly saved dates.
    guard PayrollEstimatorIOS.hasRecordedActivity(
        monthHasEntries: !monthEntries.isEmpty,
        workedMinutes: summary.workedMinutes
    ) else { return nil }

    func absenceMinutes(_ code: String) -> Int {
        monthEntries.filter { date, value in
            guard value == code else { return false }
            let weekday = Calendar.raspored.component(.weekday, from: date)
            return weekday != 1 && weekday != 7
        }.count * 8 * 60
    }

    let annual = absenceMinutes("GO")
    let sick = absenceMinutes("BO")
    let otherPaid = max(
        0,
        summary.paidAbsenceMinutes -
            annual -
            sick -
            summary.holidayCreditMinutes
    )

    return PayrollEstimatorIOS.estimate(
        PayrollInputIOS(
            month: month,
            summary: summary,
            annualLeaveMinutes: annual,
            sickLeaveMinutes: sick,
            otherPaidAbsenceMinutes: otherPaid,
            hasDayNightTurnusPattern:
                schedule.count(month, code: "D") > 0 &&
                schedule.count(month, code: "N") > 0 &&
                shifts.byCode("D")?.durationMinutes == 720 &&
                shifts.byCode("N")?.durationMinutes == 720,
            serviceYears: serviceYears,
            children: children,
            dependents: dependents,
            annualLeaveAverageHourlyGross: annualLeaveHourlyGross > 0
                ? annualLeaveHourlyGross : nil
        )
    )
}
