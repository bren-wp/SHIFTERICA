import SwiftUI

/// Production UI components shared conceptually with the original Raspored design.
/// This separate calendar intentionally has no payroll, wages or hourly-rate data.
enum CalendarSectionIOS: CaseIterable {
    case month, year, summary
}

struct CalendarParityBrandMark: View {
    var body: some View {
        GeometryReader { geo in
            let unit = min(geo.size.width, geo.size.height)
            ZStack {
                RoundedRectangle(cornerRadius: unit * 0.32)
                    .fill(Palette.card2)
                    .overlay(
                        RoundedRectangle(cornerRadius: unit * 0.32)
                            .stroke(Palette.mint, lineWidth: 1.5)
                    )
                VStack(spacing: unit * 0.065) {
                    RoundedRectangle(cornerRadius: unit * 0.07)
                        .fill(Palette.mint)
                        .frame(width: unit * 0.54, height: unit * 0.15)
                    ForEach(0..<2, id: \.self) { row in
                        HStack(spacing: unit * 0.065) {
                            ForEach(0..<3, id: \.self) { column in
                                RoundedRectangle(cornerRadius: unit * 0.04)
                                    .fill(row == 0 && column == 1
                                          ? Palette.colors[1] : Palette.mint)
                                    .frame(width: unit * 0.13, height: unit * 0.13)
                            }
                        }
                    }
                }
            }
        }
    }
}

struct CalendarParityHeader: View {
    let onSearch: () -> Void
    let onSettings: () -> Void
    let onAdd: () -> Void

    var body: some View {
        HStack(spacing: 8) {
            CalendarParityBrandMark().frame(width: 44, height: 44)
            Text("Raspored")
                .font(.system(size: 27, weight: .black))
                .foregroundStyle(Palette.text)
                .lineLimit(1)
                .minimumScaleFactor(0.7)
                .frame(maxWidth: .infinity, alignment: .leading)
            iconAction("magnifyingglass", label: "Pretraži", action: onSearch)
            iconAction("slider.horizontal.3", label: "Postavke", action: onSettings)
            Button(action: onAdd) {
                Image(systemName: "plus")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundStyle(Palette.background)
                    .frame(width: 48, height: 48)
                    .background(Palette.mint)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .overlay(RoundedRectangle(cornerRadius: 16)
                        .stroke(Palette.text.opacity(0.28), lineWidth: 1))
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Novi zapis")
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 7)
    }

    private func iconAction(_ icon: String, label: String,
                            action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: icon)
                .font(.system(size: 23, weight: .semibold))
                .foregroundStyle(Palette.text)
                .frame(width: 48, height: 48)
                .background(Palette.card2)
                .clipShape(RoundedRectangle(cornerRadius: 17))
                .overlay(RoundedRectangle(cornerRadius: 17)
                    .stroke(Palette.stroke.opacity(0.7), lineWidth: 1))
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }
}

struct CalendarParityBottomNav: View {
    @Binding var section: CalendarSectionIOS

    var body: some View {
        HStack(spacing: 5) {
            tab(.month, title: "Mjesec", icon: "calendar")
            tab(.year, title: "Godina", icon: "calendar.badge.clock")
            tab(.summary, title: "Sažetak", icon: "chart.bar.xaxis")
        }
        .padding(.horizontal, 5)
        .padding(.vertical, 3)
        .background(Palette.panel)
        .clipShape(RoundedRectangle(cornerRadius: 19))
        .overlay(RoundedRectangle(cornerRadius: 19)
            .stroke(Palette.stroke.opacity(0.5), lineWidth: 1))
        .padding(.horizontal, 12)
        .padding(.vertical, 3)
    }

    private func tab(_ destination: CalendarSectionIOS,
                     title: String, icon: String) -> some View {
        let active = section == destination
        return Button {
            withAnimation(.easeInOut(duration: 0.16)) {
                section = destination
            }
        } label: {
            VStack(spacing: 3) {
                Image(systemName: icon)
                    .font(.system(size: 19, weight: active ? .semibold : .regular))
                    .foregroundStyle(active ? Palette.mint : Palette.secondary)
                    .frame(width: 48, height: 27)
                    .background(active ? Palette.mint.opacity(0.14) : .clear,
                                in: RoundedRectangle(cornerRadius: 12))
                Text(title)
                    .font(.system(size: 11, weight: active ? .bold : .medium))
                    .foregroundStyle(active ? Palette.text : Palette.secondary)
            }
            .frame(maxWidth: .infinity, minHeight: 54)
            .background(active ? Palette.mint.opacity(0.07) : .clear,
                        in: RoundedRectangle(cornerRadius: 15))
        }
        .buttonStyle(.plain)
        .accessibilityLabel(title)
        .accessibilityAddTraits(active ? [.isSelected] : [])
        .accessibilityIdentifier("bottom-nav-" + String(describing: destination))
    }
}

struct CalendarParityMonthGrid: View {
    let month: Date
    let selected: Date
    let entries: [CalendarEntry]
    let weekOnly: Bool
    let onSelect: (Date) -> Void

    private let weekdays = ["PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED"]

    var body: some View {
        GeometryReader { geometry in
            let weekCount = max(1, weekOnly ? 1 :
                CalendarClock.monthCells(month).count / 7)
            // Match the original Raspored: date tiles expand with the
            // actual calendar height rather than stopping at a 57 pt cap.
            // Reserve space for weekday captions and inter-row gaps.
            let usable = geometry.size.height - 43 - CGFloat(weekCount) * 5
            // The optional one-week view stays compact rather than stretching
            // a single row into a giant tile on tall phones or tablets.
            let fittedHeight = max(34, usable / CGFloat(weekCount))
            gridContent(tileHeight: weekOnly ? min(112, fittedHeight) : fittedHeight)
        }
    }

    private func gridContent(tileHeight: CGFloat) -> some View {
        let cells = weekOnly
            ? CalendarClock.weekCells(selected)
            : CalendarClock.monthCells(month)
        let entriesByDay = Dictionary(grouping: entries, by: \.date)
        let selectedKey = CalendarClock.key(selected)
        let todayKey = CalendarClock.key(Date())
        return VStack(spacing: 5) {
            HStack(spacing: 0) {
                ForEach(weekdays.indices, id: \.self) { index in
                    Text(weekdays[index])
                        .font(.system(size: 11, weight: .heavy))
                        .foregroundStyle(index >= 5 ? Palette.weekend : Palette.secondary)
                        .frame(maxWidth: .infinity)
                }
            }
            ForEach(0..<(cells.count / 7), id: \.self) { week in
                HStack(spacing: 3) {
                    ForEach(0..<7, id: \.self) { column in
                        let day = cells[week * 7 + column]
                        let key = CalendarClock.key(day)
                        let sameMonth = CalendarClock.calendar.isDate(
                            day, equalTo: month, toGranularity: .month
                        )
                        CalendarParityDayTile(
                            day: day,
                            entries: entriesByDay[key] ?? [],
                            inMonth: weekOnly || sameMonth,
                            selected: key == selectedKey,
                            today: key == todayKey,
                            tileHeight: tileHeight
                        ) {
                            onSelect(day)
                        }
                    }
                }
            }
        }
        .padding(.horizontal, 7)
        .padding(.vertical, 10)
        .background(Palette.panel, in: RoundedRectangle(cornerRadius: 26))
        .overlay(RoundedRectangle(cornerRadius: 26)
            .stroke(Palette.stroke.opacity(0.95), lineWidth: 1))
        .padding(.horizontal, 2)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
    }
}

private struct CalendarParityDayTile: View {
    let day: Date
    let entries: [CalendarEntry]
    let inMonth: Bool
    let selected: Bool
    let today: Bool
    let tileHeight: CGFloat
    let onClick: () -> Void

    private var fill: Color {
        if let first = entries.first {
            return Palette.colors[first.color]
        }
        let weekday = CalendarClock.calendar.component(.weekday, from: day)
        return weekday == 1 || weekday == 7 ? Palette.weekendEmpty : Palette.empty
    }

    var body: some View {
        Button(action: onClick) {
            ZStack {
                RoundedRectangle(cornerRadius: 13)
                    .fill(LinearGradient(
                        colors: [
                            fill.opacity(inMonth ? 1 : 0.4),
                            fill.opacity(inMonth ? 0.82 : 0.26)
                        ], startPoint: .top, endPoint: .bottom
                    ))
                Text(String(CalendarClock.calendar.component(.day, from: day)))
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(inMonth
                        ? (entries.isEmpty ? Palette.text : Palette.background)
                        : Palette.secondary.opacity(0.55))
                    .frame(maxWidth: .infinity, maxHeight: .infinity,
                           alignment: .topLeading)
                    .padding(5)
                if let first = entries.first {
                    Text(CalendarParityLabels.short(first))
                        .font(.system(size: 20, weight: .black))
                        .foregroundStyle(Palette.background)
                        .lineLimit(1)
                        .minimumScaleFactor(0.65)
                }
                if selected {
                    Circle()
                        .fill(Palette.mint)
                        .frame(width: 5, height: 5)
                        .frame(maxWidth: .infinity, maxHeight: .infinity,
                               alignment: .bottom)
                        .padding(.bottom, 4)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: tileHeight)
            .overlay(RoundedRectangle(cornerRadius: 13)
                .stroke(today ? Palette.mint
                        : (entries.isEmpty ? Palette.stroke.opacity(0.5) : fill),
                        lineWidth: today ? 2 : 1))
        }
        .buttonStyle(.plain)
        .accessibilityLabel(CalendarClock.title(day, format: "d. MMMM yyyy.") +
                            ", " + String(entries.count) + " zapisa")
    }
}

private enum CalendarParityLabels {
    static func short(_ item: CalendarEntry) -> String {
        if let code = CalendarShiftPresets.code(for: item) { return code }
        let title = item.title.trimmingCharacters(in: .whitespacesAndNewlines)
        let lowered = title.lowercased(with: Locale(identifier: "hr_HR"))
        if lowered.hasPrefix("noć") || lowered.hasPrefix("noc") { return "N" }
        if lowered.hasPrefix("dnev") { return "D" }
        if lowered.hasPrefix("jut") { return "J" }
        if lowered.hasPrefix("pop") { return "P" }
        if lowered.hasPrefix("godiš") || lowered.hasPrefix("godis") { return "GO" }
        if lowered.hasPrefix("bolov") { return "BO" }
        return title.isEmpty ? "•" : String(title.prefix(2)).uppercased()
    }
}

struct CalendarParityYear: View {
    let year: Int
    let entries: [CalendarEntry]
    let onYearChange: (Int) -> Void
    let onMonthSelect: (Date) -> Void
    private let columns = [GridItem(.flexible(), spacing: 9),
                           GridItem(.flexible(), spacing: 9)]

    var body: some View {
        // Index once for the entire year: the previous version rebuilt the
        // full events dictionary 12 times, once inside each mini-month.
        let byDate = Dictionary(grouping: entries, by: \.date)
        return VStack(spacing: 8) {
            HStack {
                navArrow("chevron.left", label: "Prethodna godina") {
                    onYearChange(year - 1)
                }
                Text(String(year - 1))
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(Palette.secondary)
                Text(String(year))
                    .font(.system(size: 27, weight: .black))
                    .foregroundStyle(Palette.text)
                    .frame(maxWidth: .infinity)
                Text(String(year + 1))
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(Palette.secondary)
                navArrow("chevron.right", label: "Sljedeća godina") {
                    onYearChange(year + 1)
                }
            }
            .padding(8)
            .background(Palette.panel, in: RoundedRectangle(cornerRadius: 22))
            .overlay(RoundedRectangle(cornerRadius: 22)
                .stroke(Palette.stroke, lineWidth: 1))
            ScrollView {
                LazyVGrid(columns: columns, spacing: 9) {
                    ForEach(1...12, id: \.self) { month in
                        if let date = CalendarClock.calendar.date(
                            from: DateComponents(year: year, month: month, day: 1)
                        ) {
                            CalendarParityMiniMonth(month: date, indexed: byDate) {
                                onMonthSelect(date)
                            }
                        }
                    }
                }
            }
            shiftLegend
        }
        .padding(.horizontal, 6)
        .padding(.vertical, 6)
    }

    private var shiftLegend: some View {
        let choices = CalendarShiftPresets.builtIn.filter {
            ["N", "D", "GO"].contains($0.code)
        }
        return HStack(spacing: 6) {
            Text("Vrste smjena")
                .font(.system(size: 13, weight: .bold))
                .foregroundStyle(Palette.text)
                .lineLimit(1)
                .minimumScaleFactor(0.7)
            Spacer(minLength: 2)
            ForEach(choices) { item in
                HStack(spacing: 3) {
                    Text(item.code)
                        .font(.system(size: 11, weight: .black))
                        .foregroundStyle(Palette.background)
                        .frame(width: 30, height: 32)
                        .background(Palette.colors[item.color],
                                    in: RoundedRectangle(cornerRadius: 9))
                    Text(item.title.components(separatedBy: " ").first ?? "")
                        .font(.system(size: 10, weight: .semibold))
                        .foregroundStyle(Palette.text)
                        .lineLimit(1)
                        .minimumScaleFactor(0.7)
                }
            }
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 7)
        .background(Palette.panel, in: RoundedRectangle(cornerRadius: 17))
        .overlay(RoundedRectangle(cornerRadius: 17)
            .stroke(Palette.stroke.opacity(0.7), lineWidth: 1))
    }

    private func navArrow(_ name: String, label: String,
                          action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: name)
                .font(.title2.weight(.semibold))
                .foregroundStyle(Palette.text)
                .frame(width: 45, height: 45)
                .background(Palette.card2, in: Circle())
        }
        .accessibilityLabel(label)
    }
}

private struct CalendarParityMiniMonth: View {
    let month: Date
    let indexed: [String: [CalendarEntry]]
    let onClick: () -> Void

    var body: some View {
        let first = CalendarClock.monthStart(month)
        let cells = CalendarClock.monthCells(first)
        return Button(action: onClick) {
            VStack(alignment: .leading, spacing: 5) {
                Text(CalendarClock.title(first, format: "LLLL").uppercased())
                    .font(.system(size: 15, weight: .black))
                    .foregroundStyle(Palette.text)
                HStack(spacing: 2) {
                    ForEach(0..<7, id: \.self) { day in
                        Text(["PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED"][day])
                            .font(.system(size: 6, weight: .bold))
                            .foregroundStyle(day >= 5 ? Palette.weekend : Palette.secondary)
                            .frame(maxWidth: .infinity)
                    }
                }
                ForEach(0..<(cells.count / 7), id: \.self) { week in
                    HStack(spacing: 2) {
                        ForEach(0..<7, id: \.self) { column in
                            let date = cells[week * 7 + column]
                            let own = CalendarClock.calendar.isDate(
                                date, equalTo: first, toGranularity: .month
                            )
                            let entry = indexed[CalendarClock.key(date)]?.first
                            Text(own ? (entry.map(CalendarParityLabels.short)
                                        ?? String(CalendarClock.calendar.component(
                                            .day, from: date))) : "")
                                .font(.system(size: 6, weight: .bold))
                                .lineLimit(1)
                                .minimumScaleFactor(0.5)
                                .foregroundStyle(entry == nil ? Palette.text : Palette.background)
                                .frame(maxWidth: .infinity)
                                .aspectRatio(1, contentMode: .fit)
                                .background(own ? (entry.map { Palette.colors[$0.color] }
                                    ?? Palette.empty) : .clear,
                                    in: RoundedRectangle(cornerRadius: 4))
                        }
                    }
                }
            }
            .padding(9)
            .background(Palette.panel, in: RoundedRectangle(cornerRadius: 18))
            .overlay(RoundedRectangle(cornerRadius: 18)
                .stroke(Palette.stroke.opacity(0.92), lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}
