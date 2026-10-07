import SwiftUI

struct PayrollEstimateCardIOS: View {
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @EnvironmentObject private var shifts: ShiftLibraryIOS

    let month: Date

    var body: some View {
        let summary = CroatianWorkTimeIOS.summarize(
            month: month,
            schedule: schedule,
            shifts: shifts.all
        )
        let annual = absenceMinutes(code: "GO")
        let sick = absenceMinutes(code: "BO")
        let otherPaid = max(
            0,
            summary.paidAbsenceMinutes -
                annual -
                sick -
                summary.holidayCreditMinutes
        )
        let sector = CroatianPayrollRulesIOS.defaultSector
        let hasScheduleData = !schedule.monthEntries(month).isEmpty

        let estimate = hasScheduleData ? PayrollEstimatorIOS.estimate(
            PayrollInputIOS(
                month: month,
                sector: sector,
                coefficient: CroatianPayrollRulesIOS.defaultCoefficient,
                summary: summary,
                annualLeaveMinutes: annual,
                sickLeaveMinutes: sick,
                otherPaidAbsenceMinutes: otherPaid,
                hasDayNightTurnusPattern:
                    schedule.count(month, code: "D") > 0 &&
                    schedule.count(month, code: "N") > 0
            )
        ) : nil

        VStack(alignment: .leading, spacing: 9) {
            HStack(spacing: 8) {
                Image(systemName: "eurosign.circle.fill")
                    .foregroundStyle(RColors.accent)
                VStack(alignment: .leading, spacing: 2) {
                    Text("Procjena plaće")
                        .font(.system(size: 21, weight: .black))
                        .foregroundStyle(RColors.text)
                    Text("Automatski iz mjesečnog rasporeda")
                    .font(.system(size: 9.5))
                    .foregroundStyle(RColors.muted)
                }
                Spacer()
            }

            if let estimate {
                Text(currency(estimate.netMonthly))
                    .font(.system(size: 30, weight: .black))
                    .foregroundStyle(RColors.text)
                Text("Procijenjeni mjesečni neto")
                    .font(.caption2)
                    .foregroundStyle(RColors.muted)

                HStack(spacing: 7) {
                    mini("Bruto 1", currency(estimate.grossOne), RColors.day)
                    mini("Dodaci", currency(estimate.premiumGross), RColors.night)
                    mini("Porez", currency(estimate.incomeTax), RColors.sick)
                }
                HStack(spacing: 7) {
                    mini(
                        "MIO",
                        currency(
                            estimate.pensionFirstPillar +
                                estimate.pensionSecondPillar
                        ),
                        RColors.morning
                    )
                    mini("Bruto 2", currency(estimate.grossTwo), RColors.annual)
                    mini("Sat bruto", currency(estimate.hourlyGross), RColors.accent)
                }


                Text(
                    "Sati, noć, subote, nedjelje, blagdani i prekovremeni preuzimaju se iz kalendara bez ručnog upisa. Procjena trenutno koristi osnovni osobni odbitak; dodatne osobne olakšice mogu samo povećati stvarni neto."
                )
                .font(.system(size: 8.5))
                .foregroundStyle(RColors.muted)

                Text("Orijentacijski izračun, nije službena platna lista.")
                    .font(.system(size: 8.5, weight: .bold))
                    .foregroundStyle(Color(hex: 0xFFC66B))
            } else {
                Text(
                    !hasScheduleData
                    ? "Dodajte smjene u kalendar za odabrani mjesec. Procjena plaće tada će se izračunati automatski."
                    : "Za odabranu godinu nema ugrađene službene osnovice. Procjena se zato ne prikazuje umjesto nagađanja."
                )
                .font(.caption)
                .foregroundStyle(RColors.muted)
            }
        }
        .padding(13)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .overlay(
            RoundedRectangle(cornerRadius: 24)
                .stroke(RColors.stroke, lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.25), radius: 9, y: 3)
    }

    private func absenceMinutes(code: String) -> Int {
        schedule.monthEntries(month).filter { date, value in
            guard value == code else { return false }
            let weekday = Calendar.raspored.component(.weekday, from: date)
            return weekday != 1 && weekday != 7
        }.count * 8 * 60
    }

    private func mini(
        _ label: String,
        _ value: String,
        _ tint: Color
    ) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.system(size: 8.5))
                .foregroundStyle(RColors.muted)
            Text(value)
                .font(.system(size: 12.5, weight: .black))
                .foregroundStyle(RColors.text)
                .lineLimit(1)
                .minimumScaleFactor(0.68)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(8)
        .background(RColors.card2)
        .clipShape(RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(tint.opacity(0.35), lineWidth: 1)
        )
    }

    private func currency(_ value: Double) -> String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "hr_HR")
        formatter.numberStyle = .currency
        formatter.currencyCode = "EUR"
        return formatter.string(from: NSNumber(value: value)) ??
            String(format: "%.2f €", value)
    }
}
