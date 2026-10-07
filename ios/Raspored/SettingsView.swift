import SwiftUI

struct SettingsView: View {
    @EnvironmentObject private var settings: UISettingsStoreIOS
    @State private var info: SettingsInfo?
    @State private var showSupport = false

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
                            shapeChoices
                            colorChoices
                            intSegmentedRow("Prozirnost", subtitle: "Postavite prozirnost isticanja", values: [25, 50, 75, 100], selected: $settings.todayOpacity)
                        }
                    }

                    group("Radno okruženje", icon: "briefcase.fill") {
                        sectorRow
                        Text("Raspored nije vezan uz određenu ustanovu ili djelatnost. Smjene i vremena možete prilagoditi svom radnom mjestu.")
                            .font(.caption2)
                            .foregroundStyle(RColors.muted)
                    }

                    group("Jezik i vrijeme", icon: "globe") {
                        languageRow
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
                        interactiveRow("Podržite nas!", "Dobrovoljna podrška bez otključavanja funkcija") {
                            showSupport = true
                        }
                        interactiveRow("Pravila privatnosti", "Saznajte kako štitimo vaše podatke") {
                            info = SettingsInfo(
                                title: "Privatnost",
                                message: "Raspored ne koristi oglasne trackere i ne zahtijeva korisnički račun. Podaci rasporeda ne koriste se za oglašavanje niti se prodaju trećim stranama."
                            )
                        }
                    }

                }
                .padding(18)
            }
        }
        .alert(item: $info) { item in
            Alert(
                title: Text(item.title),
                message: Text(item.message),
                dismissButton: .default(Text("U redu"))
            )
        }
        .sheet(isPresented: $showSupport) {
            SupportView()
        }
    }

    private var sectorRow: some View {
        Menu {
            ForEach(
                ["Univerzalno", "Državna služba", "Javna služba", "Privatni sektor", "Ostalo"],
                id: \.self
            ) { option in
                Button {
                    settings.workSector = option
                } label: {
                    if settings.workSector == option {
                        Label(option, systemImage: "checkmark")
                    } else {
                        Text(option)
                    }
                }
            }
        } label: {
            HStack {
                VStack(alignment: .leading) {
                    Text("Sektor").fontWeight(.bold).foregroundStyle(RColors.text)
                    Text(settings.workSector).font(.caption2).foregroundStyle(RColors.muted)
                }
                Spacer()
                Image(systemName: "chevron.down").foregroundStyle(RColors.muted)
            }
            .contentShape(Rectangle())
            .padding(.vertical, 6)
        }
        .buttonStyle(.plain)
    }

    private var languageRow: some View {
        Menu {
            ForEach(["Automatski (Hrvatski)", "Hrvatski"], id: \.self) { option in
                Button {
                    settings.language = option
                } label: {
                    if settings.language == option {
                        Label(option, systemImage: "checkmark")
                    } else {
                        Text(option)
                    }
                }
            }
        } label: {
            HStack {
                VStack(alignment: .leading) {
                    Text("Jezik").fontWeight(.bold).foregroundStyle(RColors.text)
                    Text(settings.language).font(.caption2).foregroundStyle(RColors.muted)
                }
                Spacer()
                Image(systemName: "chevron.down").foregroundStyle(RColors.muted)
            }
            .contentShape(Rectangle())
            .padding(.vertical, 6)
        }
        .buttonStyle(.plain)
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
        .shadow(color: .black.opacity(0.25), radius: 9, y: 3)
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
                            .shadow(color: selected.wrappedValue == value ? RColors.accent.opacity(0.30) : .clear, radius: 6, y: 2)
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
                            .shadow(color: selected.wrappedValue == value ? RColors.accent.opacity(0.30) : .clear, radius: 6, y: 2)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .padding(.vertical, 4)
    }

    private var shapeChoices: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Oblik").fontWeight(.bold).foregroundStyle(RColors.text)
            Text("Odaberite oblik isticanja").font(.caption2).foregroundStyle(RColors.muted)
            HStack(spacing: 7) {
                shapeButton("Zaobljeni kvadrat", symbol: "rounded")
                shapeButton("Krug", symbol: "circle")
                shapeButton("Kvadrat", symbol: "square")
                shapeButton("Pill", symbol: "pill")
            }
        }
        .padding(.vertical, 4)
    }

    private func shapeButton(_ value: String, symbol: String) -> some View {
        let active = settings.todayShape == value
        return Button { settings.todayShape = value } label: {
            ZStack {
                RoundedRectangle(cornerRadius: 11)
                    .fill(active ? RColors.accent.opacity(0.20) : RColors.card2)
                RoundedRectangle(cornerRadius: 11)
                    .stroke(active ? RColors.accent : RColors.stroke.opacity(0.55), lineWidth: active ? 1.5 : 1)
                shapeSymbol(symbol).foregroundStyle(RColors.text)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 46)
            .shadow(color: active ? RColors.accent.opacity(0.30) : .clear, radius: 7, y: 2)
        }
        .buttonStyle(.plain)
    }

    @ViewBuilder
    private func shapeSymbol(_ symbol: String) -> some View {
        switch symbol {
        case "circle":
            Circle().stroke(lineWidth: 1.7).frame(width: 24, height: 24)
        case "square":
            RoundedRectangle(cornerRadius: 2).stroke(lineWidth: 1.7).frame(width: 25, height: 25)
        case "pill":
            Capsule().stroke(lineWidth: 1.7).frame(width: 34, height: 21)
        default:
            RoundedRectangle(cornerRadius: 7).stroke(lineWidth: 1.7).frame(width: 25, height: 25)
        }
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

    private func interactiveRow(_ title: String, _ value: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack {
                VStack(alignment: .leading) {
                    Text(title).fontWeight(.bold).foregroundStyle(RColors.text)
                    Text(value).font(.caption2).foregroundStyle(RColors.muted)
                }
                Spacer()
                Image(systemName: "chevron.right").foregroundStyle(RColors.muted)
            }
            .contentShape(Rectangle())
            .padding(.vertical, 6)
        }
        .buttonStyle(.plain)
    }
}

private struct SettingsInfo: Identifiable {
    let id = UUID()
    let title: String
    let message: String
}
