import SwiftUI

/// Original Raspored month insight card geometry, without payroll or money.
struct CalendarMonthMetrics: View {
    let entries: [CalendarEntry]

    private var shiftCount: Int {
        entries.filter { $0.category == "Smjena" }.count
    }

    private var activeDays: Int {
        Set(entries.map(\.date)).count
    }

    var body: some View {
        VStack(spacing: 5) {
            HStack(spacing: 7) {
                stat("Zapisi", value: entries.count, symbol: "calendar",
                     accent: Palette.mint)
                stat("Smjene", value: shiftCount, symbol: "chart.bar.fill",
                     accent: Palette.colors[2])
            }
            HStack(spacing: 12) {
                Image(systemName: "checkmark.circle.fill")
                    .font(.system(size: 19, weight: .bold))
                    .foregroundStyle(Palette.mint)
                    .frame(width: 27)
                VStack(alignment: .leading, spacing: 2) {
                    Text("Aktivni dani")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(Palette.secondary)
                    Text("Datumi s barem jednim zapisom")
                        .font(.system(size: 11))
                        .foregroundStyle(Palette.secondary)
                }
                Spacer()
                Text(String(activeDays))
                    .font(.system(size: 23, weight: .black))
                    .foregroundStyle(Palette.text)
            }
            .padding(.horizontal, 12)
            .frame(height: 43)
            .background(Palette.panel, in: RoundedRectangle(cornerRadius: 19))
            .overlay(RoundedRectangle(cornerRadius: 19)
                .stroke(Palette.stroke.opacity(0.9), lineWidth: 1))
        }
    }

    private func stat(_ label: String, value: Int,
                      symbol: String, accent: Color) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Image(systemName: symbol)
                .font(.system(size: 17, weight: .semibold))
                .foregroundStyle(accent)
            Text(label)
                .font(.system(size: 12))
                .foregroundStyle(Palette.secondary)
            Text(String(value))
                .font(.system(size: 21, weight: .black))
                .foregroundStyle(Palette.text)
        }
        .padding(.horizontal, 13)
        .frame(maxWidth: .infinity, minHeight: 75, alignment: .leading)
        .background(Palette.panel, in: RoundedRectangle(cornerRadius: 21))
        .overlay(RoundedRectangle(cornerRadius: 21)
            .stroke(Palette.stroke.opacity(0.9), lineWidth: 1))
    }
}
