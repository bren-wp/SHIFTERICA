import SwiftUI

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
    guard !schedule.monthEntries(month).isEmpty else { return nil }

    let summary = CroatianWorkTimeIOS.summarize(
        month: month,
        schedule: schedule,
        shifts: shifts.all,
        fundOverrideMinutes: fundOverrideMinutes
    )

    func absenceMinutes(_ code: String) -> Int {
        schedule.monthEntries(month).filter { date, value in
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
                schedule.count(month, code: "N") > 0,
            serviceYears: serviceYears,
            children: children,
            dependents: dependents,
            annualLeaveAverageHourlyGross: annualLeaveHourlyGross > 0
                ? annualLeaveHourlyGross : nil
        )
    )
}

struct PayrollEstimateCardIOS: View {
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @EnvironmentObject private var accounting: MonthlyAccountingStoreIOS

    let month: Date
    @State private var showBreakdown = false

    var body: some View {
        let hasScheduleData = !schedule.monthEntries(month).isEmpty
        let estimate = payrollEstimateForMonthIOS(
            month: month,
            schedule: schedule,
            shifts: shifts,
            fundOverrideMinutes: accounting.fundOverrideMinutes(month),
            serviceYears: accounting.serviceYears,
            children: accounting.children,
            dependents: accounting.dependents,
            annualLeaveHourlyGross: accounting.annualLeaveHourlyGross
        )

        VStack(alignment: .leading, spacing: 9) {
            HStack(spacing: 8) {
                Image(systemName: "eurosign.circle.fill")
                    .foregroundStyle(RColors.accent)
                VStack(alignment: .leading, spacing: 2) {
                    Text("Procjena plaće")
                        .font(.system(size: 21, weight: .black))
                        .foregroundStyle(RColors.text)
                    Text("Rijeka · koeficijent 1,25 · EUR")
                    .font(.system(size: 9.5))
                    .foregroundStyle(RColors.muted)
                }
                Spacer()
            }

            if let estimate {
                Text(currency(estimate.netMonthly))
                    .font(.system(size: 30, weight: .black))
                    .foregroundStyle(RColors.text)
                Text("Procjena za puni fond i dosad upisane dodatke")
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


                if estimate.projectedRegularMinutes > 0 {
                    Text("Nepotpun raspored: " +
                         String(estimate.projectedRegularMinutes / 60) +
                         " h pretpostavljeno do punog fonda, bez budućih dodataka.")
                        .font(.system(size: 11))
                        .foregroundStyle(RColors.text)
                        .padding(11)
                        .background(RColors.accent.opacity(0.10))
                        .clipShape(RoundedRectangle(cornerRadius: 13))
                }
                if schedule.count(month, code: "GO") > 0 &&
                   accounting.annualLeaveHourlyGross <= 0 {
                    Text("GO: nije postavljena satnica prema prosjeku. " +
                         "Procjena može odstupati od platne liste.")
                        .font(.system(size: 11))
                        .foregroundStyle(Color(hex: 0xFFC66B))
                }
                if accounting.serviceYears == 0 &&
                   accounting.children == 0 && accounting.dependents == 0 {
                    Text("Provjerite staž i olakšice: početne nule mogu podcijeniti neto.")
                        .font(.system(size: 11))
                        .foregroundStyle(Color(hex: 0xFFC66B))
                }
                Button {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        showBreakdown.toggle()
                    }
                } label: {
                    HStack {
                        Text(showBreakdown ? "Sakrij detalje" : "Prikaži detalje obračuna")
                        Spacer()
                        Image(systemName: showBreakdown ? "chevron.up" : "chevron.down")
                    }
                    .foregroundStyle(RColors.accent)
                    .padding(11)
                    .background(RColors.card2)
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                }
                .buttonStyle(.plain)
                if showBreakdown {
                    VStack(spacing: 9) {
                        breakdown("Dodatak za staž", currency(estimate.seniorityGross))
                        breakdown("Turnus (5%)", currency(estimate.turnusPremiumGross))
                        breakdown("Druga smjena (10%)", currency(estimate.secondShiftPremiumGross))
                        breakdown("Osobni odbitak", currency(estimate.personalAllowance))
                        breakdown("Porez · Rijeka", "20% / 25%")
                        breakdown("Koeficijent", "1,25")
                    }
                }
                Text("Procjena nije službena platna lista. GO plaćen po prosjeku, ostale naknade i odbici mogu promijeniti isplatu.")
                    .font(.system(size: 11))
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

    private func breakdown(_ label: String, _ value: String) -> some View {
        HStack {
            Text(label).foregroundStyle(RColors.muted)
            Spacer()
            Text(value).fontWeight(.bold).foregroundStyle(RColors.text)
        }
        .font(.system(size: 12))
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
