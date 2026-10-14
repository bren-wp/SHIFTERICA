import SwiftUI

struct CalendarParitySummary: View {
    let month: Date
    let entries: [CalendarEntry]
    let onPreviousMonth: () -> Void
    let onNextMonth: () -> Void
    let onExport: () -> Void
    let onImport: () -> Void
    let notice: String?

    @State private var section = 0
    @State private var included = Set(CalendarShiftPresets.builtIn.map(\.code))

    private var monthEntries: [CalendarEntry] {
        let key = String(CalendarClock.key(month).prefix(7))
        return entries.filter { $0.date.hasPrefix(key) }
    }
    private var shiftEntries: [CalendarEntry] {
        monthEntries.filter { $0.category == "Smjena" }
    }
    private var includedShifts: [CalendarEntry] {
        shiftEntries.filter { item in
            guard let preset = CalendarShiftPresets.builtIn.first(where: {
                $0.title == item.title && $0.start == item.start && $0.end == item.end
            }) else { return true }
            return included.contains(preset.code)
        }
    }
    var body: some View {
        ScrollView {
            VStack(spacing: 10) {
                tabSelector
                navigation
                if section == 0 { shiftOverview } else { hourOverview }
                backups
            }
            .padding(.horizontal, 14).padding(.vertical, 8)
        }
        .scrollDismissesKeyboard(.interactively)
    }
    private var tabSelector: some View {
        HStack(spacing: 4) {
            ForEach(0..<2, id: \.self) { index in
                Button {
                    section = index
                } label: {
                    Text(index == 0 ? "Smjene" : "Sati")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(Palette.text)
                        .frame(maxWidth: .infinity, minHeight: 40)
                        .background(section == index ? Palette.mint.opacity(0.3)
                                    : .clear, in: RoundedRectangle(cornerRadius: 13))
                        .overlay(RoundedRectangle(cornerRadius: 13)
                            .stroke(section == index ? Palette.mint : .clear,
                                    lineWidth: 1))
                }
                .buttonStyle(.plain)
            }
        }
        .padding(4)
        .background(Palette.panel, in: RoundedRectangle(cornerRadius: 18))
    }
    private var navigation: some View {
        HStack(spacing: 6) {
            navArrow("chevron.left", label: "Prethodni mjesec", action: onPreviousMonth)
            Text(CalendarClock.title(month, format: "LLLL yyyy").uppercased())
                .font(.system(size: 21, weight: .black))
                .foregroundStyle(Palette.text)
                .frame(maxWidth: .infinity).lineLimit(1).minimumScaleFactor(0.75)
            navArrow("chevron.right", label: "Sljedeći mjesec", action: onNextMonth)
        }
        .padding(8)
        .background(Palette.panel, in: RoundedRectangle(cornerRadius: 22))
        .overlay(RoundedRectangle(cornerRadius: 22)
            .stroke(Palette.stroke, lineWidth: 1))
    }
    private var shiftOverview: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Pregled smjena")
                .font(.system(size: 22, weight: .black))
                .foregroundStyle(Palette.text)
            HStack(spacing: 4) {
                Text("Smjena").frame(maxWidth: .infinity, alignment: .leading)
                Text("Broj").frame(width: 28)
                Text("Vrijeme").frame(width: 66)
                Text("Uključeno").frame(width: 56)
            }
            .font(.system(size: 10)).foregroundStyle(Palette.secondary)
            ForEach(CalendarShiftPresets.builtIn) { preset in
                presetRow(preset)
            }
            Text("Uključeno mijenja samo pregled, ne briše zapise.")
                .font(.caption).foregroundStyle(Palette.secondary)
            Text("Ukupno: " + String(includedShifts.count) + " smjena")
                .font(.headline).foregroundStyle(Palette.mint)
        }
        .padding(13)
        .background(Palette.panel, in: RoundedRectangle(cornerRadius: 24))
        .overlay(RoundedRectangle(cornerRadius: 24)
            .stroke(Palette.stroke, lineWidth: 1))
    }
    private func presetRow(_ preset: CalendarShiftPreset) -> some View {
        let matching = shiftEntries.filter {
            $0.title == preset.title && $0.start == preset.start && $0.end == preset.end
        }
        return HStack(spacing: 5) {
            Text(preset.code)
                .font(.system(size: 14, weight: .black))
                .foregroundStyle(Palette.background)
                .frame(width: 39, height: 42)
                .background(Palette.colors[preset.color],
                            in: RoundedRectangle(cornerRadius: 11))
            VStack(alignment: .leading, spacing: 3) {
                Text(preset.title)
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundStyle(Palette.text).lineLimit(2)
                Text(preset.start.isEmpty ? "Bez vremena"
                     : preset.start + " – " + preset.end)
                    .font(.system(size: 9))
                    .foregroundStyle(Palette.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            Text(String(matching.count))
                .frame(width: 28)
                .font(.system(size: 11)).foregroundStyle(Palette.text)
            Text(CalendarScheduledTime.display(matching.reduce(0) {
                $0 + CalendarScheduledTime.minutes($1)
            }))
            .font(.system(size: 10))
            .foregroundStyle(Palette.text)
            .frame(width: 66).lineLimit(1).minimumScaleFactor(0.6)
            Toggle("", isOn: Binding(
                get: { included.contains(preset.code) },
                set: { enabled in
                    if enabled { included.insert(preset.code) }
                    else { included.remove(preset.code) }
                }
            ))
            .labelsHidden()
            .tint(Palette.mint)
            .frame(width: 55)
        }
        .padding(8)
        .background(Palette.card2, in: RoundedRectangle(cornerRadius: 16))
    }
    private var hourOverview: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Sati po rasporedu")
                .font(.system(size: 22, weight: .black))
                .foregroundStyle(Palette.text)
            Text(CalendarScheduledTime.display(includedShifts.reduce(0) {
                $0 + CalendarScheduledTime.minutes($1)
            }))
            .font(.system(size: 29, weight: .black)).foregroundStyle(Palette.mint)
            Text("Zbroj unesenih vremena smjena, uključujući prijelaze preko ponoći. " +
                 "Zapisi bez vremena nemaju sate. Nema obračuna plaće ni satnica.")
                .font(.caption).foregroundStyle(Palette.secondary)
            Text("Smjena: " + String(includedShifts.count) +
                 " · Dana: " + String(Set(includedShifts.map(\.date)).count))
                .foregroundStyle(Palette.text)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(17)
        .background(Palette.panel, in: RoundedRectangle(cornerRadius: 22))
    }
    private var backups: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Sigurnosne kopije")
                .font(.system(size: 15, weight: .black))
                .foregroundStyle(Palette.text)
            HStack {
                Button("Izvezi kopiju", action: onExport)
                Button("Uvezi kopiju", action: onImport)
            }
            .buttonStyle(.bordered).tint(Palette.mint)
            if let notice {
                Text(notice).font(.caption).foregroundStyle(Palette.secondary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(Palette.panel, in: RoundedRectangle(cornerRadius: 20))
    }
    private func navArrow(_ symbol: String, label: String,
                          action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: symbol)
                .font(.title2.weight(.semibold)).foregroundStyle(Palette.text)
                .frame(width: 44, height: 44)
                .background(Palette.card2, in: RoundedRectangle(cornerRadius: 14))
        }
        .buttonStyle(.plain).accessibilityLabel(label)
    }
}
