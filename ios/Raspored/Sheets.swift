import SwiftUI

struct ShiftManagerView: View {
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @Environment(\.dismiss) private var dismiss
    let onNew: () -> Void
    @State private var showImport = false
    @State private var importText = ""
    @State private var importError: String?
    @State private var importedCount: Int?

    var body: some View {
        ZStack {
            LinearGradient(colors: [RColors.bg2, RColors.bg, .black], startPoint: .top, endPoint: .bottom).ignoresSafeArea()
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Capsule().fill(RColors.muted.opacity(0.5)).frame(width: 54, height: 5).frame(maxWidth: .infinity)
                    HStack {
                        Text("Smjene").font(.system(size: 31, weight: .black)).foregroundStyle(RColors.text)
                        Spacer()
                        Button { dismiss() } label: {
                            Image(systemName: "xmark").font(.title2.bold()).foregroundStyle(RColors.text)
                                .frame(width: 48, height: 48).background(RColors.card2).clipShape(Circle())
                        }
                        .buttonStyle(.plain)
                    }
                    HStack(spacing: 10) {
                        action("plus", "Nova smjena", active: true) { onNew() }
                        action("square.and.arrow.down", "Uvezi smjenu", active: false) {
                            importError = nil
                            importedCount = nil
                            showImport = true
                        }
                    }
                    ForEach(shifts.all) { shift in shiftRow(shift) }
                }
                .padding(18)
            }
        }
        .sheet(isPresented: $showImport) {
            ZStack {
                LinearGradient(colors: [RColors.bg2, RColors.bg, .black], startPoint: .top, endPoint: .bottom)
                    .ignoresSafeArea()
                VStack(alignment: .leading, spacing: 14) {
                    Capsule().fill(RColors.muted.opacity(0.5)).frame(width: 54, height: 5).frame(maxWidth: .infinity)
                    HStack {
                        Text("Uvezi smjenu")
                            .font(.system(size: 28, weight: .black))
                            .foregroundStyle(RColors.text)
                        Spacer()
                        Button { showImport = false } label: {
                            Image(systemName: "xmark")
                                .font(.title3.bold())
                                .foregroundStyle(RColors.text)
                                .frame(width: 44, height: 44)
                                .background(RColors.card2)
                                .clipShape(Circle())
                        }
                        .buttonStyle(.plain)
                    }
                    Text("Zalijepite JSON jedne smjene ili popisa smjena.")
                        .font(.subheadline)
                        .foregroundStyle(RColors.muted)
                    TextEditor(text: $importText)
                        .scrollContentBackground(.hidden)
                        .foregroundStyle(RColors.text)
                        .font(.system(.body, design: .monospaced))
                        .padding(10)
                        .frame(minHeight: 180)
                        .background(RColors.card2)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .overlay(RoundedRectangle(cornerRadius: 16).stroke(RColors.stroke, lineWidth: 1))
                    if let importError {
                        Text(importError).font(.caption).foregroundStyle(Color(hex: 0xFF6778))
                    }
                    if let importedCount {
                        Text("Uvezeno smjena: \(importedCount)")
                            .font(.caption.bold())
                            .foregroundStyle(RColors.accent)
                    }
                    HStack(spacing: 10) {
                        Button("Odustani") { showImport = false }
                            .frame(maxWidth: .infinity).frame(height: 54)
                            .background(RColors.card2)
                            .foregroundStyle(RColors.text)
                            .clipShape(RoundedRectangle(cornerRadius: 17))
                        Button("Uvezi") {
                            do {
                                importedCount = try shifts.importJSON(importText)
                                importError = nil
                                importText = ""
                            } catch {
                                importedCount = nil
                                importError = error.localizedDescription
                            }
                        }
                        .frame(maxWidth: .infinity).frame(height: 54)
                        .background(RColors.accent)
                        .foregroundStyle(.black)
                        .fontWeight(.black)
                        .clipShape(RoundedRectangle(cornerRadius: 17))
                    }
                }
                .padding(18)
            }
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.hidden)
        }
    }

    private func shiftRow(_ shift: ShiftTypeDef) -> some View {
        HStack(spacing: 14) {
            Text(shift.code)
                .font(.system(size: 21, weight: .black))
                .foregroundStyle(shift.textColor)
                .frame(width: 62, height: 62)
                .background(shift.color)
                .clipShape(RoundedRectangle(cornerRadius: 16))
            VStack(alignment: .leading, spacing: 3) {
                Text(shift.name).font(.system(size: 19, weight: .bold)).foregroundStyle(RColors.text)
                if let time = shift.timeText { Text(time).foregroundStyle(RColors.muted) }
                if shift.custom { Text("Vlastita smjena").font(.caption2).foregroundStyle(RColors.accent) }
            }
            Spacer()
            if shift.custom {
                Button { shifts.delete(shift.code) } label: {
                    Image(systemName: "trash").foregroundStyle(Color(hex: 0xFF6778))
                        .frame(width: 44, height: 44).background(RColors.card2).clipShape(Circle())
                }
                .buttonStyle(.plain)
            } else {
                Image(systemName: "chevron.right").font(.title3.bold()).foregroundStyle(RColors.text)
                    .frame(width: 44, height: 44).background(RColors.card2).clipShape(Circle())
            }
        }
        .padding(14)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 21))
        .overlay(RoundedRectangle(cornerRadius: 21).stroke(RColors.stroke, lineWidth: 1))
    }

    private func action(_ icon: String, _ label: String, active: Bool, perform: @escaping () -> Void) -> some View {
        Button(action: perform) {
            HStack {
                Image(systemName: icon)
                Text(label).font(.headline)
            }
            .foregroundStyle(RColors.text)
            .frame(maxWidth: .infinity)
            .frame(height: 62)
            .background(active ? RColors.accent.opacity(0.22) : RColors.card)
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .overlay(RoundedRectangle(cornerRadius: 18).stroke(active ? RColors.accent : RColors.stroke, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

private struct ColorChoice: Identifiable, Hashable {
    let hex: UInt32
    var id: UInt32 { hex }
    var color: Color { Color(hex: hex) }
}

struct NewShiftView: View {
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @Environment(\.dismiss) private var dismiss

    @State private var name = "Nova smjena"
    @State private var abbr = "Nova"
    @State private var size = 12.0
    @State private var backgroundHex: UInt32 = 0xFFD21F
    @State private var foregroundHex: UInt32 = 0x06131F
    @State private var tab = 0
    @State private var start = ""
    @State private var end = ""
    @State private var secondaryStart = ""
    @State private var secondaryEnd = ""
    @State private var error: String?

    private let backgrounds: [ColorChoice] = ([0xFFD21F, 0x13B7F3, 0x6CEB82, 0x77DED7, 0xD991EE, 0xFF5BAA, 0xFF5F67, 0x36485A] as [UInt32]).map(ColorChoice.init)
    private let foregrounds: [ColorChoice] = ([0xFFFFFF, 0xD8D8D8, 0xAAAAAA, 0x777777, 0x444444, 0x222A33, 0x06131F] as [UInt32]).map(ColorChoice.init)

    var body: some View {
        ZStack {
            LinearGradient(colors: [RColors.bg2, RColors.bg, .black], startPoint: .top, endPoint: .bottom).ignoresSafeArea()
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Button { dismiss() } label: {
                            Image(systemName: "chevron.left").font(.title2.bold()).foregroundStyle(RColors.text)
                                .frame(width: 50, height: 50).background(RColors.card2).clipShape(RoundedRectangle(cornerRadius: 16))
                        }
                        .buttonStyle(.plain)
                        VStack(alignment: .leading) {
                            Text("Nova smjena").font(.system(size: 30, weight: .black)).foregroundStyle(RColors.text)
                            Text("Kreirajte novu smjenu").foregroundStyle(RColors.muted)
                        }
                    }
                    field("Naziv smjene", value: $name)
                    tabs
                    if tab == 0 { appearanceForm } else { scheduleForm }
                    if let error { Text(error).font(.caption).foregroundStyle(Color(hex: 0xFF6778)) }
                    HStack(spacing: 10) {
                        Button("Odustani") { dismiss() }
                            .frame(maxWidth: .infinity).frame(height: 56).background(RColors.card2)
                            .foregroundStyle(RColors.text).clipShape(RoundedRectangle(cornerRadius: 18))
                        Button("Spremi") { save() }
                            .frame(maxWidth: .infinity).frame(height: 56).background(RColors.accent)
                            .foregroundStyle(.black).fontWeight(.black).clipShape(RoundedRectangle(cornerRadius: 18))
                    }
                }
                .padding(18)
            }
        }
    }

    private var tabs: some View {
        HStack(spacing: 4) {
            tabButton("Izgled", index: 0)
            tabButton("Raspored", index: 1)
        }
        .padding(4).background(RColors.card).clipShape(RoundedRectangle(cornerRadius: 19))
        .overlay(RoundedRectangle(cornerRadius: 19).stroke(RColors.stroke, lineWidth: 1))
    }

    private func tabButton(_ title: String, index: Int) -> some View {
        Button { tab = index } label: {
            Text(title).fontWeight(.bold).foregroundStyle(index == tab ? RColors.text : RColors.muted)
                .frame(maxWidth: .infinity).padding(13)
                .background(index == tab ? RColors.accent.opacity(0.22) : .clear)
                .clipShape(RoundedRectangle(cornerRadius: 15))
                .overlay(RoundedRectangle(cornerRadius: 15).stroke(index == tab ? RColors.accent : .clear, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }

    private var appearanceForm: some View {
        Group {
            VStack(alignment: .leading, spacing: 7) {
                Text("Skraćenica").fontWeight(.bold).foregroundStyle(RColors.text)
                HStack {
                    TextField("", text: $abbr).foregroundStyle(RColors.text).onChange(of: abbr) { _, newValue in
                        abbr = String(newValue.prefix(4))
                    }
                    Spacer()
                    Text(String(abbr.count) + "/4").foregroundStyle(RColors.muted)
                }
                .padding(14).background(RColors.card2).clipShape(RoundedRectangle(cornerRadius: 15))
                .overlay(RoundedRectangle(cornerRadius: 15).stroke(RColors.stroke, lineWidth: 1))
            }
            .padding(14).background(RColors.card).clipShape(RoundedRectangle(cornerRadius: 20))

            colorCard("Boja pozadine", colors: backgrounds, selected: $backgroundHex)
            colorCard("Boja teksta", colors: foregrounds, selected: $foregroundHex)

            VStack(alignment: .leading) {
                Text("Veličina teksta").fontWeight(.bold).foregroundStyle(RColors.text)
                HStack {
                    Button { size = max(8, size - 1) } label: { Image(systemName: "minus") }
                    Slider(value: $size, in: 8...24).tint(RColors.accent)
                    Button { size = min(24, size + 1) } label: { Image(systemName: "plus") }
                    Text(String(Int(size))).font(.headline).foregroundStyle(RColors.text)
                        .frame(width: 48, height: 42).background(RColors.card2).clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            .padding(14).background(RColors.card).clipShape(RoundedRectangle(cornerRadius: 20))
        }
    }

    private var scheduleForm: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Vrijeme smjene").font(.headline).foregroundStyle(RColors.text)
            Text("Ostavite prazno za odsutnost ili oznaku bez obračuna sati.").font(.caption).foregroundStyle(RColors.muted)
            timePair("Prvi interval", start: $start, end: $end)
            timePair("Drugi interval (neobavezno)", start: $secondaryStart, end: $secondaryEnd)
        }
        .padding(14).background(RColors.card).clipShape(RoundedRectangle(cornerRadius: 20))
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(RColors.stroke, lineWidth: 1))
    }

    private func timePair(_ title: String, start: Binding<String>, end: Binding<String>) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title).font(.caption).foregroundStyle(RColors.muted)
            HStack(spacing: 8) {
                TextField("08:00", text: start).textFieldStyle(.plain).padding(12).background(RColors.card2).clipShape(RoundedRectangle(cornerRadius: 12))
                TextField("14:00", text: end).textFieldStyle(.plain).padding(12).background(RColors.card2).clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .foregroundStyle(RColors.text)
        }
    }

    private func field(_ label: String, value: Binding<String>) -> some View {
        VStack(alignment: .leading, spacing: 7) {
            Text(label).fontWeight(.bold).foregroundStyle(RColors.text)
            TextField("", text: value).textFieldStyle(.plain).foregroundStyle(RColors.text)
                .padding(14).background(RColors.card2).clipShape(RoundedRectangle(cornerRadius: 15))
                .overlay(RoundedRectangle(cornerRadius: 15).stroke(RColors.stroke, lineWidth: 1))
        }
        .padding(14).background(RColors.card).clipShape(RoundedRectangle(cornerRadius: 20))
    }

    private func colorCard(_ title: String, colors: [ColorChoice], selected: Binding<UInt32>) -> some View {
        VStack(alignment: .leading, spacing: 9) {
            Text(title).fontWeight(.bold).foregroundStyle(RColors.text)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    Image(systemName: "paintpalette.fill").foregroundStyle(RColors.accent)
                        .frame(width: 42, height: 42).background(RColors.card2).clipShape(RoundedRectangle(cornerRadius: 10))
                        .overlay(RoundedRectangle(cornerRadius: 10).stroke(RColors.accent, lineWidth: 1))
                    ForEach(colors) { choice in
                        Button { selected.wrappedValue = choice.hex } label: {
                            RoundedRectangle(cornerRadius: 10).fill(choice.color).frame(width: 42, height: 42)
                                .overlay(RoundedRectangle(cornerRadius: 10).stroke(selected.wrappedValue == choice.hex ? RColors.accent : RColors.stroke.opacity(0.45), lineWidth: selected.wrappedValue == choice.hex ? 2 : 1))
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
        .padding(14).background(RColors.card).clipShape(RoundedRectangle(cornerRadius: 20))
    }

    private func save() {
        do {
            _ = try shifts.save(
                name: name, code: abbr,
                backgroundHex: backgroundHex, foregroundHex: foregroundHex,
                fontSize: Int(size), start: start, end: end,
                secondaryStart: secondaryStart, secondaryEnd: secondaryEnd
            )
            dismiss()
        } catch {
            self.error = error.localizedDescription
        }
    }
}

struct SearchView: View {
    @EnvironmentObject private var schedule: ScheduleStoreIOS
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @Environment(\.dismiss) private var dismiss
    let onPick: (Date) -> Void
    @State private var query = ""

    var body: some View {
        NavigationStack {
            List {
                ForEach(results, id: \.0) { date, code in
                    Button { onPick(date) } label: {
                        HStack {
                            let shift = shifts.byCode(code)
                            Text(code).fontWeight(.black).foregroundStyle(shift?.textColor ?? RColors.text)
                                .frame(width: 42, height: 42).background(shift?.color ?? RColors.empty).clipShape(RoundedRectangle(cornerRadius: 9))
                            VStack(alignment: .leading) {
                                Text(DateFormatter.scheduleKey.string(from: date))
                                Text(shift?.name ?? code).font(.caption).foregroundStyle(.secondary)
                            }
                        }
                    }
                }
            }
            .searchable(text: $query, prompt: "Datum ili vrsta smjene")
            .navigationTitle("Pretraži raspored")
            .toolbar { ToolbarItem(placement: .topBarTrailing) { Button("Gotovo") { dismiss() } } }
        }
    }

    private var results: [(Date, String)] {
        schedule.entries.compactMap { key, value in
            guard let date = DateFormatter.scheduleKey.date(from: key) else { return nil }
            let name = shifts.byCode(value)?.name ?? value
            if query.isEmpty || key.localizedCaseInsensitiveContains(query) || name.localizedCaseInsensitiveContains(query) { return (date, value) }
            return nil
        }
        .sorted { $0.0 < $1.0 }
        .prefix(50)
        .map { $0 }
    }
}

struct SettingsView: View {
    @EnvironmentObject private var settings: UISettingsStoreIOS
    @Environment(\.dismiss) private var dismiss

    private let highlightColors: [Color] = [RColors.night, RColors.day, RColors.annual, RColors.morning, Color(hex: 0xB16CE4), Color(hex: 0xFF5BAA), Color(hex: 0xFF853A)]

    var body: some View {
        ZStack {
            LinearGradient(colors: [RColors.bg2, RColors.bg, .black], startPoint: .top, endPoint: .bottom).ignoresSafeArea()
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Capsule().fill(RColors.muted.opacity(0.5)).frame(width: 54, height: 5).frame(maxWidth: .infinity)
                    Text("Postavke").font(.system(size: 31, weight: .black)).foregroundStyle(RColors.text)
                    Text("Prilagodite Raspored svojim potrebama").foregroundStyle(RColors.muted)

                    group("Vizualno", icon: "paintpalette.fill") {
                        segmentedRow("Tamni način rada", subtitle: "Odaberite izgled aplikacije", values: ["Automatski", "Uključen", "Isključen"], selected: $settings.themeMode)
                        toggle("Prikaz praznih dana", "Prikaži dane izvan odabranog mjeseca", $settings.showOutsideDays)
                        segmentedRow("Veličina brojeva dana u mjesecu", subtitle: "Odaberite veličinu brojeva u kalendaru", values: ["XS", "S", "M", "L", "XL"], selected: $settings.dayNumberSize)
                        toggle("Istakni vikende", "Oboji subotu i nedjelju drugačijom bojom", $settings.highlightWeekends)
                        toggle("Ikone alarma", "Prikaži ikonu za dane s alarmima", $settings.showAlarmIcons)
                        toggle("Ikone bilješki", "Prikaži ikonu za dane s bilješkama", $settings.showNoteIcons)
                        toggle("Istakni današnji dan", "Prilagodite izgled današnjeg datuma", $settings.highlightToday)
                        if settings.highlightToday {
                            segmentedRow("Oblik", subtitle: "Odaberite oblik isticanja", values: ["Zaobljeni kvadrat", "Krug", "Kvadrat", "Pill"], selected: $settings.todayShape)
                            colorChoices
                            intSegmentedRow("Prozirnost", subtitle: "Postavite prozirnost isticanja", values: [25, 50, 75, 100], selected: $settings.todayOpacity)
                        }
                    }

                    group("Jezik i vrijeme", icon: "globe") {
                        staticRow("Jezik", settings.language)
                        segmentedRow("Prvi dan u tjednu", subtitle: "Odaberite koji dan počinje tjedan", values: ["PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED"], selected: $settings.firstWeekday)
                        segmentedRow("Format vremena", subtitle: "Odaberite prikaz vremena", values: ["Automatski", "24 h", "AM/PM"], selected: $settings.timeFormat)
                        segmentedRow("Format datuma", subtitle: "Odaberite format datuma", values: ["Automatski", "dd.MM.gggg", "MM/dd/gggg", "gggg/MM/dd"], selected: $settings.dateFormat)
                    }

                    group("Bilješke", icon: "note.text") {
                        toggle("Prikaži bilješke u dnevnoj ćeliji", "Prikaži tekst bilješki unutar ćelija kalendara", $settings.showNotesInCell)
                        segmentedRow("Veličina teksta bilješke", subtitle: "Odaberite veličinu teksta u dnevnim ćelijama", values: ["XS", "S", "M", "L", "XL"], selected: $settings.noteTextSize)
                        intSegmentedRow("Prozirnost pozadine", subtitle: "Postavite prozirnost pozadine bilješki", values: [25, 50, 75, 100], selected: $settings.noteBackgroundOpacity)
                    }

                    group("Podrška i privatnost", icon: "shield.fill") {
                        staticRow("Podržite nas!", "Pomozite nam da Raspored bude još bolji")
                        staticRow("Pravila privatnosti", "Saznajte kako štitimo vaše podatke")
                    }

                }
                .padding(18)
            }
        }
    }

    private func group<Content: View>(_ title: String, icon: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 7) {
            HStack {
                Image(systemName: icon).foregroundStyle(RColors.accent)
                Text(title).font(.system(size: 20, weight: .black)).foregroundStyle(RColors.text)
            }
            content()
        }
        .padding(14).background(RColors.card).clipShape(RoundedRectangle(cornerRadius: 22))
        .overlay(RoundedRectangle(cornerRadius: 22).stroke(RColors.stroke, lineWidth: 1))
    }

    private func toggle(_ title: String, _ subtitle: String, _ value: Binding<Bool>) -> some View {
        HStack {
            VStack(alignment: .leading) {
                Text(title).fontWeight(.bold).foregroundStyle(RColors.text)
                Text(subtitle).font(.caption2).foregroundStyle(RColors.muted)
            }
            Spacer()
            Toggle("", isOn: value).labelsHidden().tint(RColors.accent)
        }
        .padding(.vertical, 5)
    }

    private func segmentedRow(_ title: String, subtitle: String, values: [String], selected: Binding<String>) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title).fontWeight(.bold).foregroundStyle(RColors.text)
            Text(subtitle).font(.caption2).foregroundStyle(RColors.muted)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 5) {
                    ForEach(values, id: \.self) { value in
                        Button { selected.wrappedValue = value } label: {
                            Text(value).font(.caption.bold()).foregroundStyle(selected.wrappedValue == value ? RColors.text : RColors.muted)
                                .padding(.horizontal, 12).padding(.vertical, 9)
                                .background(selected.wrappedValue == value ? RColors.accent.opacity(0.22) : RColors.card2)
                                .clipShape(RoundedRectangle(cornerRadius: 11))
                                .overlay(RoundedRectangle(cornerRadius: 11).stroke(selected.wrappedValue == value ? RColors.accent : RColors.stroke.opacity(0.5), lineWidth: 1))
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
        .padding(.vertical, 4)
    }

    private func intSegmentedRow(_ title: String, subtitle: String, values: [Int], selected: Binding<Int>) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title).fontWeight(.bold).foregroundStyle(RColors.text)
            Text(subtitle).font(.caption2).foregroundStyle(RColors.muted)
            HStack(spacing: 5) {
                ForEach(values, id: \.self) { value in
                    Button { selected.wrappedValue = value } label: {
                        Text(String(value) + "%").font(.caption.bold()).foregroundStyle(selected.wrappedValue == value ? RColors.text : RColors.muted)
                            .frame(maxWidth: .infinity).padding(.vertical, 9)
                            .background(selected.wrappedValue == value ? RColors.accent.opacity(0.22) : RColors.card2)
                            .clipShape(RoundedRectangle(cornerRadius: 11))
                            .overlay(RoundedRectangle(cornerRadius: 11).stroke(selected.wrappedValue == value ? RColors.accent : RColors.stroke.opacity(0.5), lineWidth: 1))
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .padding(.vertical, 4)
    }

    private var colorChoices: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Boja").fontWeight(.bold).foregroundStyle(RColors.text)
            HStack(spacing: 7) {
                ForEach(Array(highlightColors.enumerated()), id: \.offset) { index, color in
                    Button { settings.todayColorIndex = index } label: {
                        RoundedRectangle(cornerRadius: 9).fill(color).frame(width: 37, height: 37)
                            .overlay(RoundedRectangle(cornerRadius: 9).stroke(settings.todayColorIndex == index ? RColors.accent : RColors.stroke.opacity(0.45), lineWidth: settings.todayColorIndex == index ? 2 : 1))
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .padding(.vertical, 4)
    }

    private func staticRow(_ title: String, _ value: String) -> some View {
        HStack {
            VStack(alignment: .leading) {
                Text(title).fontWeight(.bold).foregroundStyle(RColors.text)
                Text(value).font(.caption2).foregroundStyle(RColors.muted)
            }
            Spacer()
            Image(systemName: "chevron.right").foregroundStyle(RColors.muted)
        }
        .padding(.vertical, 6)
    }
}
