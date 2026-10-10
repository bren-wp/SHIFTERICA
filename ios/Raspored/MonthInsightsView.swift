import SwiftUI

struct MonthInsightsView: View {
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @EnvironmentObject private var accounting: MonthlyAccountingStoreIOS

    let month: Date

    var body: some View {
        let summary = CroatianWorkTimeIOS.summarize(
            month: month,
            schedule: schedule,
            shifts: shifts.all,
            fundOverrideMinutes: accounting.fundOverrideMinutes(month)
        )
        let confirmedNet = accounting.actualNet(month)
        let payroll = payrollEstimateForMonthIOS(
            month: month,
            schedule: schedule,
            shifts: shifts,
            fundOverrideMinutes: accounting.fundOverrideMinutes(month),
            serviceYears: accounting.serviceYearsForMonth(month),
            children: accounting.children,
            dependents: accounting.dependents,
            annualLeaveHourlyGross: accounting.annualLeaveHourlyGrossForMonth(month),
            paymentDelayMonths: accounting.paymentDelayMonths(month)
        )

        VStack(spacing: 5) {
            HStack(spacing: 6) {
                tile("Fond sati", compactHours(summary.fundMinutes),
                     "clock.fill", RColors.morning)
                tile("Prekovremeni", compactHours(summary.overtimeMinutes),
                     "chart.bar.fill", RColors.sick)
            }
            HStack(spacing: 9) {
                Image(systemName: "eurosign.circle.fill")
                    .font(.system(size: 21))
                    .foregroundStyle(RColors.accent)
                    .accessibilityHidden(true)
                VStack(alignment: .leading, spacing: 2) {
                    Text(confirmedNet == nil ? "Procjena neta" : "Potvrđeni neto")
                        .font(.system(size: 11, weight: .medium))
                        .foregroundStyle(RColors.muted)
                    Text(confirmedNet == nil ? "Informativni iznos" : "Iz obračunske liste")
                        .font(.system(size: 9))
                        .foregroundStyle(RColors.muted)
                }
                Spacer(minLength: 3)
                Text((confirmedNet ?? payroll?.netMonthly).map(currency) ?? "—")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundStyle(RColors.text)
                    .lineLimit(1)
                    .minimumScaleFactor(0.65)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(RColors.card, in: RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(RColors.accent.opacity(0.28), lineWidth: 1)
            )
            .accessibilityElement(children: .combine)
        }
        .padding(.horizontal, 3)
    }

    private func tile(
        _ label: String,
        _ value: String,
        _ icon: String,
        _ tint: Color
    ) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Image(systemName: icon)
                .foregroundStyle(tint)
            Text(label)
                .font(.system(size: 10))
                .foregroundStyle(RColors.muted)
                .lineLimit(1)
            Text(value)
                .font(.system(size: 15, weight: .bold))
                .foregroundStyle(RColors.text)
                .lineLimit(1)
                .minimumScaleFactor(0.68)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 10)
        .padding(.vertical, 8)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 17))
        .overlay(
            RoundedRectangle(cornerRadius: 17)
                .stroke(tint.opacity(0.34), lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.12), radius: 3, y: 1)
        .accessibilityElement(children: .combine)
    }

    private func compactHours(_ minutes: Int) -> String {
        let hours = minutes / 60
        let remainder = minutes % 60
        return remainder == 0
            ? "\(hours) h"
            : "\(hours) h \(remainder) m"
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
