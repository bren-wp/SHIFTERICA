import SwiftUI

struct YearOverviewView: View {
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @State var year: Int
    let onMonth: (Date) -> Void

    private let columns = [GridItem(.flexible(), spacing: 8), GridItem(.flexible(), spacing: 8)]

    var body: some View {
        VStack(spacing: 9) {
            HStack {
                nav("chevron.left") { year -= 1 }
                Text(String(year - 1)).foregroundStyle(RColors.muted).fontWeight(.bold)
                Spacer()
                Text(String(year)).font(.system(size: 28, weight: .black)).foregroundStyle(RColors.text)
                Spacer()
                Text(String(year + 1)).foregroundStyle(RColors.muted).fontWeight(.bold)
                nav("chevron.right") { year += 1 }
            }
            .padding(8)
            .background(RColors.card)
            .clipShape(RoundedRectangle(cornerRadius: 22))
            .overlay(RoundedRectangle(cornerRadius: 22).stroke(RColors.stroke.opacity(0.95), lineWidth: 1))
            .shadow(color: .black.opacity(0.26), radius: 9, y: 3)

            ScrollViewReader { proxy in
                ScrollView {
                    LazyVGrid(columns: columns, spacing: 8) {
                        ForEach(1...12, id: \.self) { monthNumber in
                            if let date = Calendar.raspored.date(
                                from: DateComponents(year: year, month: monthNumber, day: 1)
                            ) {
                                MiniMonthView(month: date)
                                    .id(monthNumber)
                                    .onTapGesture { onMonth(date) }
                            }
                        }
                    }
                }
                .onAppear {
                    scrollToRelevantMonth(proxy)
                }
                .onChange(of: year) { _, _ in
                    withAnimation { scrollToRelevantMonth(proxy) }
                }
            }

        }
        .padding(.horizontal, 8)
        .padding(.bottom, 6)
    }

    private func scrollToRelevantMonth(_ proxy: ScrollViewProxy) {
        let calendar = Calendar.raspored
        let currentYear = calendar.component(.year, from: Date())
        let currentMonth = calendar.component(.month, from: Date())
        proxy.scrollTo(year == currentYear ? currentMonth : 1, anchor: .top)
    }

    private func nav(_ symbol: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: symbol)
                .frame(width: 42, height: 42)
                .background(RColors.card2)
                .clipShape(Circle())
                .foregroundStyle(RColors.text)
        }
        .buttonStyle(.plain)
    }
}

private struct MiniMonthView: View {
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    let month: Date
    private let columns = Array(repeating: GridItem(.flexible(), spacing: 2), count: 7)

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(DateFormatter.monthOnly.string(from: month).uppercased())
                .font(.system(size: 15, weight: .black))
                .foregroundStyle(RColors.text)
            LazyVGrid(columns: columns, spacing: 2) {
                ForEach(Array(["PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED"].enumerated()), id: \.offset) { index, day in
                    Text(day).font(.system(size: 6, weight: .bold)).foregroundStyle(index >= 5 ? RColors.weekend : RColors.muted)
                }
                ForEach(Array(dates.enumerated()), id: \.offset) { _, date in
                    if let date {
                        let shift = shifts.byCode(schedule.code(on: date))
                        let weekday = Calendar.raspored.component(.weekday, from: date)
                        let weekend = weekday == 1 || weekday == 7
                        ZStack {
                            let tileColor = shift?.color ?? (weekend ? RColors.weekendEmpty : RColors.empty)
                            RoundedRectangle(cornerRadius: 5)
                                .fill(
                                    LinearGradient(
                                        colors: [
                                            tileColor,
                                            tileColor.opacity(shift == nil ? 0.90 : 0.80)
                                        ],
                                        startPoint: .top,
                                        endPoint: .bottom
                                    )
                                )
                                .overlay(
                                    RoundedRectangle(cornerRadius: 5)
                                        .stroke(
                                            shift?.color.opacity(0.92) ??
                                                RColors.stroke.opacity(0.45),
                                            lineWidth: shift == nil ? 0.5 : 0.8
                                        )
                                )
                                .shadow(
                                    color: (shift?.color ?? .clear).opacity(0.20),
                                    radius: shift == nil ? 0 : 2,
                                    y: 1
                                )
                            if let shift {
                                Text(String(Calendar.raspored.component(.day, from: date)))
                                    .font(.system(size: 4.5, weight: .bold))
                                    .foregroundStyle(shift.textColor.opacity(0.78))
                                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                                    .padding(1.5)
                                Text(shift.code)
                                    .font(.system(size: shift.code.count == 1 ? 7 : 6, weight: .black))
                                    .foregroundStyle(shift.textColor)
                            } else {
                                Text(String(Calendar.raspored.component(.day, from: date)))
                                    .font(.system(size: 6, weight: .bold))
                                    .foregroundStyle(weekend ? RColors.weekend : RColors.text)
                            }
                        }
                        .aspectRatio(1, contentMode: .fit)
                    } else {
                        Color.clear.aspectRatio(1, contentMode: .fit)
                    }
                }
            }
        }
        .padding(9)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(RColors.stroke.opacity(0.92), lineWidth: 1))
        .shadow(color: .black.opacity(0.23), radius: 7, y: 3)
    }

    private var dates: [Date?] {
        let calendar = Calendar.raspored
        guard let range = calendar.range(of: .day, in: .month, for: month),
              let first = calendar.date(from: calendar.dateComponents([.year, .month], from: month)) else { return [] }
        let offset = (calendar.component(.weekday, from: first) - 2 + 7) % 7
        var array = Array<Date?>(repeating: nil, count: offset)
        for day in range { array.append(calendar.date(byAdding: .day, value: day - 1, to: first)) }
        while array.count % 7 != 0 { array.append(nil) }
        return array
    }
}
