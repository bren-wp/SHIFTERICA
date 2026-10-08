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
        let payroll = payrollEstimateForMonthIOS(
            month: month,
            schedule: schedule,
            shifts: shifts,
            fundOverrideMinutes: accounting.fundOverrideMinutes(month)
        )

        HStack(spacing: 6) {
            tile(
                "Fond sati",
                compactHours(summary.fundMinutes),
                "clock.fill",
                RColors.morning
            )
            tile(
                "Prekovremeni",
                compactHours(summary.overtimeMinutes),
                "chart.bar.fill",
                RColors.sick
            )
            tile(
                "Plaća (procj.)",
                payroll.map { currency($0.netMonthly) } ?? "—",
                "eurosign.circle.fill",
                RColors.accent
            )
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
                .font(.system(size: 9))
                .foregroundStyle(RColors.muted)
                .lineLimit(1)
            Text(value)
                .font(.system(size: 14, weight: .black))
                .foregroundStyle(RColors.text)
                .lineLimit(1)
                .minimumScaleFactor(0.72)
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
        .shadow(color: .black.opacity(0.18), radius: 4, y: 2)
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
