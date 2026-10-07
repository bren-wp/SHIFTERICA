import SwiftUI

struct YearOverviewView: View {
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @EnvironmentObject private var shifts: ShiftLibraryIOS
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
            .overlay(RoundedRectangle(cornerRadius: 22).stroke(RColors.stroke, lineWidth: 1))
            .shadow(color: .black.opacity(0.24), radius: 8, y: 3)

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

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 9) {
                    Text("Vrste smjena").fontWeight(.bold).foregroundStyle(RColors.text)
                    ForEach(shifts.all) { shift in
                        HStack(spacing: 4) {
                            Text(shift.code)
                                .font(.caption.bold())
                                .foregroundStyle(shift.textColor)
                                .padding(.horizontal, 7)
                                .padding(.vertical, 5)
                                .background(shift.color)
                                .clipShape(RoundedRectangle(cornerRadius: 7))
                            Text(shift.shortName).font(.caption2).foregroundStyle(RColors.muted)
                        }
                    }
                }
                .padding(10)
            }
            .background(RColors.card)
            .clipShape(RoundedRectangle(cornerRadius: 20))
            .overlay(RoundedRectangle(cornerRadius: 20).stroke(RColors.stroke, lineWidth: 1))
        }
        .padding(.horizontal, 14)
        .padding(.bottom, 7)
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
                            RoundedRectangle(cornerRadius: 4)
                                .fill(shift?.color ?? (weekend ? RColors.weekendEmpty : RColors.empty))
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
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: .black.opacity(0.20), radius: 6, y: 2)
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
