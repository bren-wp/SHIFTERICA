import SwiftUI

struct PayrollEstimateCardIOS: View {
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @EnvironmentObject private var payroll: PayrollSettingsStoreIOS

    let month: Date
    @State private var showSettings = false

    var body: some View {
        let summary = CroatianWorkTimeIOS.summarize(
            month: month,
            schedule: schedule,
            shifts: shifts.all
        )
        let annual = fundAbsenceMinutes(code: "GO")
        let sick = fundAbsenceMinutes(code: "BO")
        let otherPaid = max(
            0,
            summary.paidAbsenceMinutes - annual - sick - summary.holidayCreditMinutes
        )

        let estimate = payroll.enabled ? PayrollEstimatorIOS.estimate(
            PayrollInputIOS(
                month: month,
                coefficient: payroll.coefficient,
                completedYearsService: payroll.completedYearsService,
                fundMinutes: summary.fundMinutes,
                regularWorkedMinutes: summary.regularMinutes,
                overtimeMinutes: summary.overtimeMinutes,
                annualLeaveMinutes: annual,
                sickLeaveMinutes: sick,
                otherPaidAbsenceMinutes: otherPaid,
                holidayCreditMinutes: summary.holidayCreditMinutes,
                nightMinutes: summary.nightMinutes,
                saturdayMinutes: summary.saturdayMinutes,
                sundayMinutes: summary.sundayMinutes,
                holidayWorkedMinutes: summary.holidayWorkedMinutes,
                secondShiftMinutes: summary.secondShiftMinutes,
                children: payroll.children,
                dependents: payroll.dependents,
                disabilityAllowance: 0,
                birthYear: payroll.birthYear > 0 ? payroll.birthYear : nil,
                taxRates: PayrollTaxRatesIOS(
                    lowerPercent: payroll.lowerTaxPercent,
                    higherPercent: payroll.higherTaxPercent
                ),
                turnusEnabled: payroll.turnusEnabled,
                sickPayRate: CroatianPayrollRulesIOS.defaultSickPayRate,
                annualLeaveFactor: 1.0,
                rates: .publicServices
            )
        ) : nil

        VStack(alignment: .leading, spacing: 9) {
            HStack {
                Image(systemName: "eurosign.circle.fill")
                    .foregroundStyle(RColors.accent)
                Text("Procjena plaće")
                    .font(.system(size: 21, weight: .black))
                    .foregroundStyle(RColors.text)
                Spacer()
                Button {
                    showSettings = true
                } label: {
                    Image(systemName: "slider.horizontal.3")
                        .foregroundStyle(RColors.accent)
                        .frame(width: 38, height: 38)
                        .background(RColors.card2)
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)
            }

            if !payroll.enabled {
                Text("Procjena je isključena. Uključite je u postavkama obračuna.")
                    .foregroundStyle(RColors.muted)
                Button("Postavke obračuna") { showSettings = true }
                    .buttonStyle(.bordered)
                    .tint(RColors.accent)
            } else if let estimate {
                Text(currency(estimate.netBeforeAnnualYouthRelief))
                    .font(.system(size: 30, weight: .black))
                    .foregroundStyle(RColors.text)
                Text("Procijenjeni neto prije godišnjeg poreznog povrata za mlade")
                    .font(.caption2)
                    .foregroundStyle(RColors.muted)

                HStack(spacing: 7) {
                    mini("Bruto 1", currency(estimate.grossOne))
                    mini("Porez", currency(estimate.monthlyIncomeTax))
                    mini("Odbitak", currency(estimate.personalAllowance))
                }
                HStack(spacing: 7) {
                    mini("Bruto 2", currency(estimate.grossTwo))
                    mini("Sat bruto", currency(estimate.hourlyGross))
                    mini("Staž", "\(payroll.completedYearsService) g.")
                }

                if estimate.youthAnnualReliefFraction > 0 {
                    let percent = Int(estimate.youthAnnualReliefFraction * 100)
                    Text("Olakšica za mlade: \(percent)% godišnjeg poreza na dio plaće oporezovan nižom stopom. Procijenjeni udio budućeg povrata iz ovog mjeseca: \(currency(estimate.estimatedYouthRefundShareForMonth)); efektivni neto nakon tog povrata približno \(currency(estimate.estimatedNetAfterAnnualYouthRelief)).")
                        .font(.caption2)
                        .foregroundStyle(RColors.morning)
                }

                Text("Osnovica \(currency(estimate.officialBase)) · koeficijent \(String(format: "%.2f", payroll.coefficient)) · staž +\(String(format: "%.1f", Double(payroll.completedYearsService) * 0.5))%. BO do 42 dana procjenjuje se s 85%; godišnji odmor je konzervativno procijenjen po redovnoj satnici.")
                    .font(.system(size: 9.5))
                    .foregroundStyle(RColors.muted)
                Text("Orijentacijski izračun, nije platna lista. Porezne stope ovise o prebivalištu, a posebna prava i naknade mogu promijeniti konačni iznos.")
                    .font(.system(size: 8.5))
                    .foregroundStyle(RColors.muted)
            } else {
                Text("Za odabranu godinu nema ugrađene službene osnovice. Procjena se zato ne prikazuje umjesto nagađanja.")
                    .foregroundStyle(RColors.muted)
            }
        }
        .padding(13)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .overlay(RoundedRectangle(cornerRadius: 24).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: .black.opacity(0.25), radius: 9, y: 3)
        .sheet(isPresented: $showSettings) {
            PayrollSettingsView()
                .environmentObject(payroll)
        }
    }

    private func fundAbsenceMinutes(code: String) -> Int {
        schedule.monthEntries(month).filter { date, value in
            guard value == code else { return false }
            let weekday = Calendar.raspored.component(.weekday, from: date)
            return weekday != 1 && weekday != 7
        }.count * 8 * 60
    }

    private func mini(_ label: String, _ value: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label).font(.system(size: 8.5)).foregroundStyle(RColors.muted)
            Text(value).font(.system(size: 13, weight: .black)).foregroundStyle(RColors.text)
                .lineLimit(1)
                .minimumScaleFactor(0.7)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(8)
        .background(RColors.card2)
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }

    private func currency(_ value: Double) -> String {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "hr_HR")
        formatter.numberStyle = .currency
        formatter.currencyCode = "EUR"
        return formatter.string(from: NSNumber(value: value)) ?? String(format: "%.2f €", value)
    }
}
