import SwiftUI

private struct ColorChoice: Identifiable, Hashable {
    let hex: UInt32
    var id: UInt32 { hex }
    var color: Color { Color(hex: hex) }
}

struct NewShiftView: View {
    @EnvironmentObject private var shifts: ShiftLibraryIOS
    @Environment(\.dismiss) private var dismiss

    let initialShift: ShiftTypeDef?

    @State private var name: String
    @State private var abbr: String
    @State private var size: Double
    @State private var backgroundHex: UInt32
    @State private var foregroundHex: UInt32
    @State private var tab = 0
    @State private var start: String
    @State private var end: String
    @State private var secondaryStart: String
    @State private var secondaryEnd: String
    @State private var error: String?

    private let backgrounds: [ColorChoice] = ([0xFFD21F, 0x13B7F3, 0xFF8A3D, 0x6CEB82, 0x77DED7, 0xD991EE, 0xFF5BAA, 0xFF5F67, 0x36485A] as [UInt32]).map(ColorChoice.init)
    private let foregrounds: [ColorChoice] = ([0xFFFFFF, 0xD8D8D8, 0xAAAAAA, 0x777777, 0x444444, 0x222A33, 0x06131F] as [UInt32]).map(ColorChoice.init)

    init(initialShift: ShiftTypeDef? = nil) {
        self.initialShift = initialShift
        _name = State(initialValue: initialShift?.name ?? "Nova smjena")
        _abbr = State(initialValue: initialShift?.code ?? "Nova")
        _size = State(initialValue: Double(initialShift?.fontSize ?? 12))
        _backgroundHex = State(initialValue: initialShift?.backgroundHex ?? 0xFFD21F)
        _foregroundHex = State(initialValue: initialShift?.foregroundHex ?? 0x06131F)
        _start = State(initialValue: initialShift?.start ?? "")
        _end = State(initialValue: initialShift?.end ?? "")
        _secondaryStart = State(initialValue: initialShift?.secondaryStart ?? "")
        _secondaryEnd = State(initialValue: initialShift?.secondaryEnd ?? "")
    }

    var body: some View {
        ZStack {
            LinearGradient(colors: [RColors.bg2, RColors.bg, .black], startPoint: .top, endPoint: .bottom).ignoresSafeArea()
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Button { dismiss() } label: {
                            Image(systemName: "chevron.left").font(.title2.bold()).foregroundStyle(RColors.text)
                                .frame(width: 52, height: 52)
                                .background(RColors.card2)
                                .clipShape(RoundedRectangle(cornerRadius: 17))
                                .overlay(
                                    RoundedRectangle(cornerRadius: 17)
                                        .stroke(RColors.accent.opacity(0.78), lineWidth: 1.2)
                                )
                                .shadow(color: .black.opacity(0.30), radius: 7, y: 3)
                        }
                        .buttonStyle(.plain)
                        VStack(alignment: .leading) {
                            Text(initialShift == nil ? "Nova smjena" : "Uredi smjenu")
                                .font(.system(size: 30, weight: .black))
                                .foregroundStyle(RColors.text)
                            Text(initialShift == nil ? "Kreirajte novu smjenu" : "Prilagodite izgled i radno vrijeme")
                                .foregroundStyle(RColors.muted)
                        }
                    }
                    field("Naziv smjene", value: $name)
                    tabs
                    if tab == 0 { appearanceForm } else { scheduleForm }
                    if let error { Text(error).font(.caption).foregroundStyle(Color(hex: 0xFF6778)) }
                    HStack(spacing: 10) {
                        Button("Odustani") { dismiss() }
                            .frame(maxWidth: .infinity).frame(height: 60)
                            .background(RColors.card2)
                            .foregroundStyle(RColors.text)
                            .fontWeight(.bold)
                            .clipShape(RoundedRectangle(cornerRadius: 19))
                            .overlay(
                                RoundedRectangle(cornerRadius: 19)
                                    .stroke(RColors.stroke.opacity(0.95), lineWidth: 1.2)
                            )
                        Button("Spremi") { save() }
                            .frame(maxWidth: .infinity).frame(height: 60)
                            .background(RColors.accent)
                            .foregroundStyle(.black)
                            .fontWeight(.black)
                            .clipShape(RoundedRectangle(cornerRadius: 19))
                            .overlay(
                                RoundedRectangle(cornerRadius: 19)
                                    .stroke(Color.white.opacity(0.42), lineWidth: 1)
                            )
                            .shadow(color: RColors.accent.opacity(0.42), radius: 11, y: 4)
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
        .padding(4)
        .background(RColors.card)
        .clipShape(RoundedRectangle(cornerRadius: 22))
        .overlay(
            RoundedRectangle(cornerRadius: 22)
                .stroke(RColors.stroke.opacity(0.92), lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.26), radius: 9, y: 3)
    }

    private func tabButton(_ title: String, index: Int) -> some View {
        Button { tab = index } label: {
            Text(title).fontWeight(.bold).foregroundStyle(index == tab ? RColors.text : RColors.muted)
                .frame(maxWidth: .infinity).padding(13)
                .background(index == tab ? RColors.accent.opacity(0.28) : .clear)
                .clipShape(RoundedRectangle(cornerRadius: 17))
                .overlay(
                    RoundedRectangle(cornerRadius: 17)
                        .stroke(index == tab ? RColors.accent : .clear, lineWidth: index == tab ? 1.4 : 1)
                )
                .shadow(
                    color: index == tab ? RColors.accent.opacity(0.34) : .clear,
                    radius: 8,
                    y: 2
                )
        }
        .buttonStyle(.plain)
    }

    private var appearanceForm: some View {
        Group {
            VStack(alignment: .leading, spacing: 7) {
                Text("Skraćenica").fontWeight(.bold).foregroundStyle(RColors.text)
                HStack {
                    TextField("", text: $abbr)
                        .foregroundStyle(initialShift == nil ? RColors.text : RColors.muted)
                        .disabled(initialShift != nil)
                        .onChange(of: abbr) { _, newValue in
                            abbr = String(newValue.prefix(4))
                        }
                    Spacer()
                    Text(String(abbr.count) + "/4").foregroundStyle(RColors.muted)
                }
                .padding(14).background(RColors.card2).clipShape(RoundedRectangle(cornerRadius: 16))
                .overlay(RoundedRectangle(cornerRadius: 15).stroke(RColors.stroke, lineWidth: 1))
                if initialShift != nil {
                    Text("Skraćenica ostaje ista kako postojeći raspored ne bi izgubio poveznicu sa smjenom.")
                        .font(.caption2)
                        .foregroundStyle(RColors.muted)
                }
            }
            .padding(14).background(RColors.card).clipShape(RoundedRectangle(cornerRadius: 22))
            .overlay(RoundedRectangle(cornerRadius: 22).stroke(RColors.stroke.opacity(0.92), lineWidth: 1))
            .shadow(color: .black.opacity(0.25), radius: 9, y: 3)

            colorCard("Boja pozadine", colors: backgrounds, selected: $backgroundHex)
            colorCard("Boja teksta", colors: foregrounds, selected: $foregroundHex)

            VStack(alignment: .leading) {
                Text("Veličina teksta").fontWeight(.bold).foregroundStyle(RColors.text)
                HStack(spacing: 8) {
                    Button { size = max(8, size - 1) } label: {
                        Image(systemName: "minus")
                            .font(.headline.bold())
                            .foregroundStyle(RColors.text)
                            .frame(width: 52, height: 52)
                            .background(RColors.card2)
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                            .overlay(
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(RColors.stroke.opacity(0.72), lineWidth: 1)
                            )
                    }
                    .buttonStyle(.plain)
                    Slider(value: $size, in: 8...24).tint(RColors.accent)
                    Button { size = min(24, size + 1) } label: {
                        Image(systemName: "plus")
                            .font(.headline.bold())
                            .foregroundStyle(RColors.text)
                            .frame(width: 52, height: 52)
                            .background(RColors.card2)
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                            .overlay(
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(RColors.stroke.opacity(0.72), lineWidth: 1)
                            )
                    }
                    .buttonStyle(.plain)
                    Text(String(Int(size)))
                        .font(.headline.weight(.black))
                        .foregroundStyle(RColors.text)
                        .frame(width: 50, height: 48)
                        .background(RColors.card2)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                        .overlay(
                            RoundedRectangle(cornerRadius: 14)
                                .stroke(RColors.stroke.opacity(0.72), lineWidth: 1)
                        )
                }
            }
            .padding(14).background(RColors.card).clipShape(RoundedRectangle(cornerRadius: 22))
            .overlay(RoundedRectangle(cornerRadius: 22).stroke(RColors.stroke.opacity(0.92), lineWidth: 1))
            .shadow(color: .black.opacity(0.25), radius: 9, y: 3)
        }
    }

    private var scheduleForm: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Vrijeme smjene").font(.headline).foregroundStyle(RColors.text)
            Text("Ostavite prazno za odsutnost ili oznaku bez obračuna sati.").font(.caption).foregroundStyle(RColors.muted)
            timePair("Prvi interval", start: $start, end: $end)
            timePair("Drugi interval (neobavezno)", start: $secondaryStart, end: $secondaryEnd)
        }
        .padding(14).background(RColors.card).clipShape(RoundedRectangle(cornerRadius: 22))
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(RColors.stroke, lineWidth: 1))
        .shadow(color: .black.opacity(0.25), radius: 9, y: 3)
    }

    private func timePair(_ title: String, start: Binding<String>, end: Binding<String>) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title).font(.caption).foregroundStyle(RColors.muted)
            HStack(spacing: 8) {
                TextField("08:00", text: start)
                    .textFieldStyle(.plain)
                    .padding(12)
                    .background(RColors.card2)
                    .clipShape(RoundedRectangle(cornerRadius: 13))
                    .overlay(
                        RoundedRectangle(cornerRadius: 13)
                            .stroke(RColors.stroke.opacity(0.72), lineWidth: 1)
                    )
                TextField("14:00", text: end)
                    .textFieldStyle(.plain)
                    .padding(12)
                    .background(RColors.card2)
                    .clipShape(RoundedRectangle(cornerRadius: 13))
                    .overlay(
                        RoundedRectangle(cornerRadius: 13)
                            .stroke(RColors.stroke.opacity(0.72), lineWidth: 1)
                    )
            }
            .foregroundStyle(RColors.text)
        }
    }

    private func field(_ label: String, value: Binding<String>) -> some View {
        VStack(alignment: .leading, spacing: 7) {
            Text(label).fontWeight(.bold).foregroundStyle(RColors.text)
            TextField("", text: value).textFieldStyle(.plain).foregroundStyle(RColors.text)
                .padding(14).background(RColors.card2).clipShape(RoundedRectangle(cornerRadius: 16))
                .overlay(RoundedRectangle(cornerRadius: 15).stroke(RColors.stroke, lineWidth: 1))
        }
        .padding(14).background(RColors.card).clipShape(RoundedRectangle(cornerRadius: 22))
        .overlay(RoundedRectangle(cornerRadius: 22).stroke(RColors.stroke.opacity(0.92), lineWidth: 1))
        .shadow(color: .black.opacity(0.25), radius: 9, y: 3)
    }

    private func colorCard(_ title: String, colors: [ColorChoice], selected: Binding<UInt32>) -> some View {
        VStack(alignment: .leading, spacing: 9) {
            Text(title).fontWeight(.bold).foregroundStyle(RColors.text)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    Image(systemName: "paintpalette.fill").foregroundStyle(RColors.accent)
                        .frame(width: 46, height: 46)
                        .background(RColors.card2)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(RColors.accent, lineWidth: 1.2))
                        .shadow(color: RColors.accent.opacity(0.22), radius: 5, y: 2)
                    ForEach(colors) { choice in
                        Button { selected.wrappedValue = choice.hex } label: {
                            RoundedRectangle(cornerRadius: 12)
                                .fill(choice.color)
                                .frame(width: 46, height: 46)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(
                                            selected.wrappedValue == choice.hex
                                                ? RColors.accent
                                                : RColors.stroke.opacity(0.45),
                                            lineWidth: selected.wrappedValue == choice.hex ? 2 : 1
                                        )
                                )
                                .shadow(
                                    color: selected.wrappedValue == choice.hex
                                        ? RColors.accent.opacity(0.38)
                                        : .clear,
                                    radius: 8,
                                    y: 2
                                )
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
        .padding(14).background(RColors.card).clipShape(RoundedRectangle(cornerRadius: 22))
        .overlay(RoundedRectangle(cornerRadius: 22).stroke(RColors.stroke.opacity(0.92), lineWidth: 1))
        .shadow(color: .black.opacity(0.25), radius: 9, y: 3)
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

