import SwiftUI

extension MonthView {
    var calendarCard: some View {
        VStack(spacing: 6) {
            HStack {
                circleButton("chevron.left", label: "Prethodni mjesec") { changeMonth(-1) }
                Spacer()
                Text(DateFormatter.monthTitle.string(from: month).uppercased())
                    .font(.system(size: 22, weight: .black))
                    .lineLimit(1)
                    .minimumScaleFactor(0.75)
                    .foregroundStyle(RColors.text)
                Spacer()
                circleButton("chevron.right", label: "Sljedeći mjesec") { changeMonth(1) }
            }

            HStack(spacing: 2) {
                ForEach(Array(weekdayLabels.enumerated()), id: \.offset) { _, label in
                    Text(label)
                        .font(.system(size: 11, weight: .bold))
                        .foregroundStyle(
                            label == "SUB" || label == "NED"
                                ? RColors.weekend
                                : RColors.muted
                        )
                        .frame(maxWidth: .infinity)
                }
            }

            VStack(spacing: 3) {
                ForEach(Array(calendarWeeks.enumerated()), id: \.offset) { _, week in
                    HStack(spacing: 3) {
                        ForEach(Array(week.enumerated()), id: \.offset) { _, date in
                            if let date {
                                dayCell(date)
                                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                            } else {
                                Color.clear
                                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                            }
                        }
                    }
                    .frame(maxHeight: .infinity)
                }
            }
            .frame(maxHeight: .infinity)
        }
        .padding(.horizontal, 7)
        .padding(.vertical, 7)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 26))
        .overlay(
            RoundedRectangle(cornerRadius: 26)
                .stroke(RColors.stroke.opacity(0.95), lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.13), radius: 5, y: 2)
        .frame(maxHeight: .infinity)
    }

    func circleButton(_ icon: String, label: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: icon)
                .font(.system(size: 20, weight: .bold))
                .foregroundStyle(RColors.text)
                .frame(width: 46, height: 46)
                .background(RColors.card2)
                .clipShape(Circle())
                .overlay(
                    Circle()
                        .stroke(RColors.stroke.opacity(0.7), lineWidth: 1)
                )
                .shadow(color: .black.opacity(0.16), radius: 4, y: 2)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }

    func dayCell(_ date: Date) -> some View {
        let inside = Calendar.raspored.isDate(
            date,
            equalTo: month,
            toGranularity: .month
        )
        let code = schedule.code(on: date)
        let shift = shifts.byCode(code)
        let weekday = Calendar.raspored.component(.weekday, from: date)
        let weekend = weekday == 1 || weekday == 7
        let background = shift?.color ??
            (
                weekend && settings.highlightWeekends
                    ? RColors.weekendEmpty
                    : RColors.empty
            )
        let daySize = dayNumberFontSize(settings.dayNumberSize)
        let isToday =
            settings.highlightToday &&
            Calendar.raspored.isDateInToday(date)

        return Button {
            selectedDate = date
        } label: {
            ZStack {
                RoundedRectangle(cornerRadius: 13)
                    .fill(
                        LinearGradient(
                            colors: [
                                background.opacity(inside ? 1 : 0.42),
                                background.opacity(
                                    inside
                                        ? (shift == nil ? 0.90 : 0.80)
                                        : 0.30
                                )
                            ],
                            startPoint: .top,
                            endPoint: .bottom
                        )
                    )
                    .overlay {
                        RoundedRectangle(cornerRadius: 13)
                            .stroke(
                                shift?.color.opacity(0.96) ??
                                    RColors.stroke.opacity(0.55),
                                lineWidth: shift == nil ? 1 : 1.2
                            )
                    }
                    .overlay(alignment: .top) {
                        if shift != nil && inside {
                            Capsule()
                                .fill(Color.white.opacity(0.24))
                                .frame(maxWidth: 36, minHeight: 1, maxHeight: 1)
                                .padding(.top, 1)
                        }
                    }

                if isToday {
                    todayHighlight
                }

                Text(String(Calendar.raspored.component(.day, from: date)))
                    .font(.system(size: daySize, weight: .semibold))
                    .foregroundStyle(
                        (
                            shift == nil && weekend
                                ? RColors.weekend
                                : RColors.text
                        )
                        .opacity(inside ? 1 : 0.62)
                    )
                    .frame(
                        maxWidth: .infinity,
                        maxHeight: .infinity,
                        alignment: .topLeading
                    )
                    .padding(5)

                if let shift {
                    Text(shift.code)
                        .font(
                            .system(
                                size: shift.code.count == 1
                                    ? CGFloat(shift.fontSize + 11)
                                    : CGFloat(shift.fontSize + 4),
                                weight: .black
                            )
                        )
                        .foregroundStyle(
                            shift.textColor.opacity(inside ? 1 : 0.58)
                        )
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .shadow(
                color: (shift?.color ?? .clear)
                    .opacity(shift == nil || !inside ? 0 : 0.30),
                radius: 7,
                y: 3
            )
        }
        .buttonStyle(.plain)
    }

    @ViewBuilder
    private var todayHighlight: some View {
        let palette: [Color] = [
            RColors.night,
            RColors.day,
            RColors.annual,
            RColors.morning,
            Color(hex: 0xB16CE4),
            Color(hex: 0xFF5BAA),
            Color(hex: 0xFF853A)
        ]
        let tint = palette[
            min(max(settings.todayColorIndex, 0), palette.count - 1)
        ]
        let opacity = Double(settings.todayOpacity) / 100.0

        switch settings.todayShape {
        case "Krug":
            Circle()
                .stroke(tint.opacity(opacity), lineWidth: 2)
                .padding(3)
        case "Kvadrat":
            RoundedRectangle(cornerRadius: 4)
                .stroke(tint.opacity(opacity), lineWidth: 2)
                .padding(2)
        case "Pill":
            Capsule()
                .stroke(tint.opacity(opacity), lineWidth: 2)
                .padding(3)
        default:
            RoundedRectangle(cornerRadius: 9)
                .stroke(tint.opacity(opacity), lineWidth: 2)
                .padding(2)
        }
    }

    var calendarWeeks: [[Date?]] {
        stride(from: 0, to: gridDates.count, by: 7).map { start in
            Array(gridDates[start..<min(start + 7, gridDates.count)])
        }
    }

    var weekdayLabels: [String] {
        let base = ["PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED"]
        let start = base.firstIndex(of: settings.firstWeekday) ?? 0
        return Array(base[start...]) + Array(base[..<start])
    }

    var gridDates: [Date?] {
        let calendar = Calendar.raspored
        guard let interval = calendar.dateInterval(of: .month, for: month) else {
            return []
        }

        let first = interval.start
        let weekday = calendar.component(.weekday, from: first)
        let target = calendarWeekday(settings.firstWeekday)
        let offset = (weekday - target + 7) % 7
        let start =
            calendar.date(byAdding: .day, value: -offset, to: first) ?? first
        let count =
            calendar.range(of: .day, in: .month, for: month)?.count ?? 30
        let visible = ((offset + count + 6) / 7) * 7

        return (0..<visible).compactMap { index -> Date? in
            guard
                let date = calendar.date(
                    byAdding: .day,
                    value: index,
                    to: start
                )
            else {
                return nil
            }

            if settings.showOutsideDays ||
                calendar.isDate(
                    date,
                    equalTo: month,
                    toGranularity: .month
                ) {
                return date
            }

            return nil
        }
    }

    func dayNumberFontSize(_ value: String) -> CGFloat {
        switch value {
        case "XS": return 9
        case "S": return 10
        case "L": return 14
        case "XL": return 16
        default: return 12
        }
    }

    func calendarWeekday(_ value: String) -> Int {
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

    func changeMonth(_ value: Int) {
        month =
            Calendar.raspored.date(
                byAdding: .month,
                value: value,
                to: month
            ) ?? month
    }
}
