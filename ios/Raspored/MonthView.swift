import SwiftUI

struct MonthView: View {
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @EnvironmentObject private var settings: UISettingsStoreIOS
    @Binding var month: Date
    let onOpenShifts: () -> Void

    @State private var editing = false
    @State private var selectedCode: String?
    @State private var erasing = false

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 5), count: 7)

    var body: some View {
        VStack(spacing: 10) {
            calendarCard
            if editing { editingDock } else { legend; quickToolbar }
        }
        .padding(.horizontal, 14)
        .padding(.bottom, 8)
    }

    private var calendarCard: some View {
        VStack(spacing: 10) {
            HStack {
                circleButton("chevron.left") { changeMonth(-1) }
                Spacer()
                Text(DateFormatter.monthTitle.string(from: month).uppercased())
                    .font(.system(size: 24, weight: .black))
                    .foregroundStyle(RColors.text)
                Spacer()
                circleButton("chevron.right") { changeMonth(1) }
            }

            LazyVGrid(columns: columns, spacing: 5) {
                ForEach(Array(weekdayLabels.enumerated()), id: \.offset) { _, label in
                    Text(label)
                        .font(.system(size: 11, weight: .bold))
                        .foregroundStyle(label == "SUB" || label == "NED" ? RColors.weekend : RColors.muted)
                }

                ForEach(Array(gridDates.enumerated()), id: \.offset) { _, date in
                    if let date { dayCell(date) }
                    else { Color.clear.aspectRatio(0.87, contentMode: .fit) }
                }
            }
        }
        .padding(14)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 27))
        .overlay(RoundedRectangle(cornerRadius: 27).stroke(RColors.stroke, lineWidth: 1))
        .frame(maxHeight: .infinity)
    }

    private func circleButton(_ icon: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: icon)
                .font(.system(size: 22, weight: .bold))
                .foregroundStyle(RColors.text)
                .frame(width: 50, height: 50)
                .background(RColors.card2)
                .clipShape(Circle())
                .overlay(Circle().stroke(RColors.stroke.opacity(0.7), lineWidth: 1))
                .shadow(color: .black.opacity(0.28), radius: 6, y: 3)
        }
        .buttonStyle(.plain)
    }

    private func dayCell(_ date: Date) -> some View {
        let inside = Calendar.raspored.isDate(date, equalTo: month, toGranularity: .month)
        let code = schedule.code(on: date)
        let shift = shifts.byCode(code)
        let weekday = Calendar.raspored.component(.weekday, from: date)
        let weekend = weekday == 1 || weekday == 7
        let background = shift?.color ?? (weekend && settings.highlightWeekends ? RColors.weekendEmpty : RColors.empty)
        let daySize = dayNumberFontSize(settings.dayNumberSize)
        let isToday = settings.highlightToday && Calendar.raspored.isDateInToday(date)

        return Button {
            if editing {
                if erasing { schedule.set(nil, on: date) }
                else if let selectedCode { schedule.set(selectedCode, on: date) }
            } else {
                editing = true
                selectedCode = code ?? shifts.all.first?.code ?? "D"
                erasing = false
            }
        } label: {
            ZStack {
                RoundedRectangle(cornerRadius: 11)
                    .fill(background.opacity(inside ? 1 : 0.42))
                    .overlay {
                        RoundedRectangle(cornerRadius: 11)
                            .stroke(shift?.color ?? RColors.stroke.opacity(0.55), lineWidth: 1)
                    }

                if isToday { todayHighlight }

                Text(String(Calendar.raspored.component(.day, from: date)))
                    .font(.system(size: daySize, weight: .semibold))
                    .foregroundStyle((shift == nil && weekend ? RColors.weekend : RColors.text).opacity(inside ? 1 : 0.65))
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                    .padding(5)

                if let shift {
                    Text(shift.code)
                        .font(.system(size: shift.code.count == 1 ? CGFloat(shift.fontSize + 9) : CGFloat(shift.fontSize + 2), weight: .black))
                        .foregroundStyle(shift.textColor.opacity(inside ? 1 : 0.6))
                }
            }
            .aspectRatio(0.94, contentMode: .fit)
                .shadow(color: (shift?.color ?? .clear).opacity(shift == nil || !inside ? 0 : 0.30), radius: 6, y: 3)
        }
        .buttonStyle(.plain)
    }

    @ViewBuilder private var todayHighlight: some View {
        let palette: [Color] = [RColors.night, RColors.day, RColors.annual, RColors.morning, Color(hex: 0xB16CE4), Color(hex: 0xFF5BAA), Color(hex: 0xFF853A)]
        let tint = palette[min(max(settings.todayColorIndex, 0), palette.count - 1)]
        let opacity = Double(settings.todayOpacity) / 100.0
        switch settings.todayShape {
        case "Krug":
            Circle().stroke(tint.opacity(opacity), lineWidth: 2).padding(3)
        case "Kvadrat":
            RoundedRectangle(cornerRadius: 4).stroke(tint.opacity(opacity), lineWidth: 2).padding(2)
        case "Pill":
            Capsule().stroke(tint.opacity(opacity), lineWidth: 2).padding(3)
        default:
            RoundedRectangle(cornerRadius: 9).stroke(tint.opacity(opacity), lineWidth: 2).padding(2)
        }
    }

    private var legend: some View {
        VStack(alignment: .leading, spacing: 9) {
            HStack {
                Text("Vrste smjena").font(.headline).foregroundStyle(RColors.text)
                Spacer()
                Button(action: onOpenShifts) { Image(systemName: "chevron.right").foregroundStyle(RColors.muted) }
            }
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(shifts.all) { shift in
                        VStack(spacing: 5) {
                            Text(shift.code)
                                .font(.system(size: 15, weight: .black))
                                .foregroundStyle(shift.textColor)
                                .frame(width: 44, height: 40)
                                .background(shift.color)
                                .clipShape(RoundedRectangle(cornerRadius: 10))
                            Text(shift.shortName).font(.caption2).foregroundStyle(RColors.text)
                        }
                        .padding(8)
                        .background(RColors.card2)
                        .clipShape(RoundedRectangle(cornerRadius: 15))
                    }
                }
            }
        }
        .padding(13)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .overlay(RoundedRectangle(cornerRadius: 24).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: .black.opacity(0.26), radius: 10, y: 4)
    }

    private var quickToolbar: some View {
        HStack(spacing: 8) {
            toolIcon("eraser") { editing = true; erasing = true; selectedCode = nil }
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 7) {
                    ForEach(shifts.all) { shift in
                        Button {
                            editing = true
                            selectedCode = shift.code
                            erasing = false
                        } label: {
                            Text(shift.code)
                                .font(.system(size: 16, weight: .black))
                                .foregroundStyle(shift.textColor)
                                .frame(width: 46, height: 46)
                                .background(shift.color)
                                .clipShape(RoundedRectangle(cornerRadius: 13))
                                .shadow(color: shift.color.opacity(0.30), radius: 6, y: 3)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            Divider().frame(height: 36).overlay(RColors.stroke)
            toolIcon("ellipsis") { onOpenShifts() }
        }
        .padding(8)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 23))
        .overlay(RoundedRectangle(cornerRadius: 23).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: .black.opacity(0.30), radius: 10, y: 4)
    }

    private func toolIcon(_ symbol: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: symbol)
                .foregroundStyle(RColors.text)
                .frame(width: 46, height: 46)
                .background(RColors.card2)
                .clipShape(RoundedRectangle(cornerRadius: 13))
        }
        .buttonStyle(.plain)
    }

    private var editingDock: some View {
        VStack(spacing: 10) {
            Capsule().fill(RColors.muted.opacity(0.45)).frame(width: 54, height: 5)
            HStack {
                VStack(alignment: .leading) {
                    Text("Način uređivanja").font(.system(size: 22, weight: .black)).foregroundStyle(RColors.text)
                    Text("Dodirnite dan kako biste primijenili smjenu").font(.caption).foregroundStyle(RColors.muted)
                }
                Spacer()
                Button {
                    editing = false; selectedCode = nil; erasing = false
                } label: {
                    Label("Izađi iz uređivanja", systemImage: "rectangle.portrait.and.arrow.forward")
                        .font(.caption.bold())
                        .foregroundStyle(RColors.accent)
                        .padding(11)
                        .overlay(RoundedRectangle(cornerRadius: 13).stroke(RColors.accent, lineWidth: 1))
                }
                .buttonStyle(.plain)
            }
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 10) {
                    editChip(label: "Gumica", code: "", color: RColors.card2, textColor: RColors.text, selected: erasing, icon: "eraser") {
                        erasing = true; selectedCode = nil
                    }
                    ForEach(shifts.all) { shift in
                        editChip(label: shift.shortName, code: shift.code, color: shift.color, textColor: shift.textColor, selected: selectedCode == shift.code, icon: nil) {
                            selectedCode = shift.code; erasing = false
                        }
                    }
                }
            }
        }
        .padding(15)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 26))
        .overlay(RoundedRectangle(cornerRadius: 26).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: .black.opacity(0.32), radius: 12, y: 5)
    }

    private func editChip(label: String, code: String, color: Color, textColor: Color, selected: Bool, icon: String?, action: @escaping () -> Void) -> some View {
        VStack(spacing: 4) {
            Button(action: action) {
                Group {
                    if let icon { Image(systemName: icon).font(.title2).foregroundStyle(RColors.text) }
                    else { Text(code).font(.system(size: 19, weight: .black)).foregroundStyle(textColor) }
                }
                .frame(width: 62, height: 62)
                .background(color)
                .clipShape(RoundedRectangle(cornerRadius: 17))
                .overlay(RoundedRectangle(cornerRadius: 17).stroke(selected ? RColors.accent : RColors.stroke.opacity(0.5), lineWidth: selected ? 2 : 1))
                .shadow(color: selected ? RColors.accent.opacity(0.35) : color.opacity(0.20), radius: selected ? 8 : 4, y: 3)
            }
            .buttonStyle(.plain)
            Text(label).font(.caption2).foregroundStyle(RColors.text)
        }
    }

    private var weekdayLabels: [String] {
        let base = ["PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED"]
        let start = base.firstIndex(of: settings.firstWeekday) ?? 0
        return Array(base[start...]) + Array(base[..<start])
    }

    private var gridDates: [Date?] {
        let calendar = Calendar.raspored
        guard let interval = calendar.dateInterval(of: .month, for: month) else { return [] }
        let first = interval.start
        let weekday = calendar.component(.weekday, from: first)
        let target = calendarWeekday(settings.firstWeekday)
        let offset = (weekday - target + 7) % 7
        let start = calendar.date(byAdding: .day, value: -offset, to: first) ?? first
        let count = calendar.range(of: .day, in: .month, for: month)?.count ?? 30
        let visible = ((offset + count + 6) / 7) * 7
        return (0..<visible).compactMap { index -> Date? in
            guard let date = calendar.date(byAdding: .day, value: index, to: start) else { return nil }
            if settings.showOutsideDays || calendar.isDate(date, equalTo: month, toGranularity: .month) { return date }
            return nil
        }
    }

    private func dayNumberFontSize(_ value: String) -> CGFloat {
        switch value {
        case "XS": return 8
        case "S": return 10
        case "L": return 13
        case "XL": return 15
        default: return 11
        }
    }

    private func calendarWeekday(_ value: String) -> Int {
        switch value {
        case "NED": return 1
        case "UTO": return 3
        case "SRI": return 4
        case "ČET": return 5
        case "PET": return 6
        case "SUB": return 7
        default: return 2
        }
    }

    private func changeMonth(_ value: Int) {
        month = Calendar.raspored.date(byAdding: .month, value: value, to: month) ?? month
    }
}
